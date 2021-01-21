package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonValue
import java.util.Locale.ROOT

/**
 * Kind of Product Image
 */
enum class ProductImageField {
    // DO NOT CHANGE ENUM NAMES
    FRONT, INGREDIENTS, NUTRITION, PACKAGING, OTHER;


    @JsonValue
    override fun toString() = name.toLowerCase(ROOT)
}