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
package openfoodfacts.github.scrachx.openfood.features.allergensalert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository

class AllergensAdapter(
        private val repository: ProductRepository,
        var allergens: MutableList<AllergenName> = mutableListOf()
) : RecyclerView.Adapter<AllergensAdapter.AllergenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllergenViewHolder {
        val contactView = LayoutInflater.from(parent.context).inflate(R.layout.item_allergens, parent, false)
        return AllergenViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: AllergenViewHolder, position: Int) {
        val allergen = allergens[position]
        holder.nameTextView.text = allergen.name
        holder.messageButton.setOnClickListener {
            allergens.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            repository.setAllergenEnabled(allergen.allergenTag, false)
        }
    }

    override fun getItemCount() = allergens.size

    class AllergenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageButton: Button = itemView.findViewById(R.id.delete_button)
        val nameTextView: TextView = itemView.findViewById(R.id.allergen_name)
    }
}