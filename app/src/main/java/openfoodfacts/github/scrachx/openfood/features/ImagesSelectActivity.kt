/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductImagesListBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.ProductImagesSelectionAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.IMAGE_FILE
import openfoodfacts.github.scrachx.openfood.images.IMG_ID
import openfoodfacts.github.scrachx.openfood.images.ImageNameJsonParser.extractImagesNameSortedByUploadTimeDesc
import openfoodfacts.github.scrachx.openfood.images.PRODUCT_BARCODE
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.utils.MY_PERMISSIONS_REQUEST_STORAGE
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import openfoodfacts.github.scrachx.openfood.utils.isAllGranted
import openfoodfacts.github.scrachx.openfood.utils.isUserSet
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class ImagesSelectActivity : BaseActivity() {
    private var _binding: ActivityProductImagesListBinding? = null
    private val binding get() = _binding!!

    private var adapter: ProductImagesSelectionAdapter? = null
    private val disp = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductImagesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = intent.getStringExtra(TOOLBAR_TITLE)

        binding.closeZoom.setOnClickListener { closeZoom() }
        binding.expandedImage.setOnClickListener { closeZoom() }
        binding.btnAcceptSelection.setOnClickListener { acceptSelection() }
        binding.btnChooseImage.setOnClickListener { chooseImage() }

        // Get intent data
        val code = intent.getStringExtra(PRODUCT_BARCODE) ?: error("Cannot start activity without product barcode.")
        loadProductImages(code)
    }

    private fun loadProductImages(code: String) {
        productsApi.getProductImages(code)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(LOG_TAG, "cannot download images from server", it) }
                .subscribe { node ->
                    val imageNames = extractImagesNameSortedByUploadTimeDesc(node)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)

                    //Check if user is logged in
                    adapter = ProductImagesSelectionAdapter(this, imageNames, code)
                    { setSelectedImage(it) }

                    binding.imagesRecycler.adapter = adapter
                    binding.imagesRecycler.layoutManager = GridLayoutManager(this, 3)
                }.addTo(disp)
    }

    private fun setSelectedImage(selectedPosition: Int) {
        if (selectedPosition >= 0) {
            val finalUrlString = adapter!!.getImageUrl(selectedPosition)
            picassoBuilder(this).load(finalUrlString).resize(400, 400).centerInside().into(binding.expandedImage)
            binding.zoomContainer.visibility = View.VISIBLE
            binding.imagesRecycler.visibility = View.INVISIBLE
        }
        updateButtonAccept()
    }

    private fun closeZoom() {
        binding.zoomContainer.visibility = View.INVISIBLE
        binding.imagesRecycler.visibility = View.VISIBLE
    }

    private fun acceptSelection() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(IMG_ID, adapter!!.getSelectedImageName())

        })
        finish()
    }

    private fun chooseImage() {
        if (ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_STORAGE)
        } else {
            EasyImage.openGallery(this, -1, false)
        }
    }

    private fun updateButtonAccept() {
        val visible = isUserSet() && adapter!!.isSelectionDone()
        binding.btnAcceptSelection.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        binding.txtInfo.visibility = binding.btnAcceptSelection.visibility
    }

    override fun onDestroy() {
        disp.dispose()
        _binding = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PhotoReceiverHandler { newPhotoFile ->
            setResult(RESULT_OK, Intent().apply {
                putExtra(IMAGE_FILE, newPhotoFile)
            })
            finish()
        }.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE && isAllGranted(grantResults)) {
            chooseImage()
        }
    }


    companion object {
        const val TOOLBAR_TITLE = "TOOLBAR_TITLE"
        private val LOG_TAG = ImagesSelectActivity::class.simpleName

        @JvmStatic
        fun start(context: Context, toolbarTitle: String, productCode: String) = context.startActivity(Intent(context, this::class.java).apply {
            putExtra(TOOLBAR_TITLE, toolbarTitle)
            putExtra(PRODUCT_BARCODE, productCode)
        })

        class SelectImageContract(
                private val toolbarTitle: String
        ) : ActivityResultContract<String, Pair<String?, File?>>() {
            override fun createIntent(context: Context, barcode: String) = Intent(context, ImagesSelectActivity::class.java).apply {
                putExtra(TOOLBAR_TITLE, toolbarTitle)
                putExtra(PRODUCT_BARCODE, barcode)
            }

            override fun parseResult(resultCode: Int, intent: Intent?) =
                    if (resultCode != RESULT_OK) null to null
                    else intent?.getStringExtra(IMG_ID) to intent?.getSerializableExtra(IMAGE_FILE) as File?

        }
    }
}