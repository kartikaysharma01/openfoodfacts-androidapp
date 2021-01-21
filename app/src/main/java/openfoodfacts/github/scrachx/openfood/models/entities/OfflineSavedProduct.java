package openfoodfacts.github.scrachx.openfood.models.entities;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.utils.FileUtilsKt;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.firstNotEmpty;

@Entity
public class OfflineSavedProduct implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    @Index(unique = true)
    private String barcode;
    @Index
    private boolean isDataUploaded;
    private String productDetails;

    @Generated(hash = 17816421)
    public OfflineSavedProduct(Long id, String barcode, boolean isDataUploaded, String productDetails) {
        this.id = id;
        this.barcode = barcode;
        this.isDataUploaded = isDataUploaded;
        this.productDetails = productDetails;
    }

    @Generated(hash = 403273060)
    public OfflineSavedProduct() {
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Map<String, String> getProductDetailsMap() {
        if (this.productDetails == null) {
            return null;
        }
        try (
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(this.productDetails, Base64.DEFAULT));
            ObjectInputStream in = new ObjectInputStream(bis)
        ) {
            return (Map<String, String>) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            Log.e(OfflineSavedProduct.class.getSimpleName(), "getProductDetailsMap", e);
        }
        return null;
    }

    public void setProductDetailsMap(Map<String, String> detailsMap) {
        try (
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos)
        ) {
            out.writeObject(detailsMap);
            out.flush();
            this.productDetails = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            Log.e(OfflineSavedProduct.class.getSimpleName(), "setProductDetailsMap", e);
        }
    }

    @Nullable
    public String getLanguage() {
        return getProductDetailsMap().get(ApiFields.Keys.LANG);
    }

    @Nullable
    public String getName() {
        final Map<String, String> map = getProductDetailsMap();
        final String language = firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return firstNotEmpty(map.get(ApiFields.Keys.lcProductNameKey(language)), map.get(ApiFields.Keys.lcProductNameKey("en")));
    }

    @Nullable
    public String getIngredients() {
        final Map<String, String> map = getProductDetailsMap();
        final String language = firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return firstNotEmpty(map.get(ApiFields.Keys.lcIngredientsKey(language)), map.get(ApiFields.Keys.lcIngredientsKey("en")));
    }

    @Nullable
    public String getImageFront() {
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_FRONT);
    }

    @Nullable
    public String getImageIngredients() {
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_INGREDIENTS);
    }

    @Nullable
    public String getImageNutrition() {
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_NUTRITION);
    }

    @Nullable
    public String getImageFrontLocalUrl() {
        String localUrl = getProductDetailsMap().get(ApiFields.Keys.IMAGE_FRONT);
        if (!TextUtils.isEmpty(localUrl)) {
            return FileUtilsKt.LOCALE_FILE_SCHEME + localUrl;
        }
        return null;
    }

    public String getProductDetails() {
        return this.productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getIsDataUploaded() {
        return this.isDataUploaded;
    }

    public void setIsDataUploaded(boolean isDataUploaded) {
        this.isDataUploaded = isDataUploaded;
    }

    @NonNull
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(id)
            .append(barcode)
            .append(isDataUploaded)
            .append(getProductDetailsMap().toString())
            .toString();
    }
}
