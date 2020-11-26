package openfoodfacts.github.scrachx.openfood.utils

import openfoodfacts.github.scrachx.openfood.models.Units
import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber
import java.util.regex.Pattern

object UnitUtils {
    const val UNIT_IU = "IU"
    private const val SALT_PER_SODIUM = 2.54
    private const val KJ_PER_KCAL = 4.184f
    private const val OZ_PER_L = 33.814f

    /**
     * Converts a give quantity's unit to kcal
     *
     * @param value The value to be converted
     * @param originalUnit [Units.ENERGY_KCAL] or [Units.ENERGY_KJ]
     * @return return the converted value
     */
    @JvmStatic
    fun convertToKiloCalories(value: Int, originalUnit: String): Int {
        return when {
            originalUnit.equals(Units.ENERGY_KJ, ignoreCase = true) -> {
                (value / KJ_PER_KCAL).toInt()
            }
            originalUnit.equals(Units.ENERGY_KCAL, ignoreCase = true) -> {
                value
            }
            else -> {
                throw IllegalArgumentException("energyUnit is neither Units.ENERGY_KCAL nor Units.ENERGY_KJ")
            }
        }
    }

    fun convertToGrams(a: Float, unit: String?): Float {
        return convertToGrams(a.toDouble(), unit).toFloat()
    }

    /**
     * Converts a given quantity's unitOfValue to grams.
     *
     * @param value The value to be converted
     * @param unitOfValue represents milligrams, 2 represents micrograms
     * @return return the converted value
     */
    fun convertToGrams(value: Double, unitOfValue: String?): Double {
        return when {
            Units.UNIT_MILLIGRAM.equals(unitOfValue, ignoreCase = true) -> value / 1000
            Units.UNIT_MICROGRAM.equals(unitOfValue, ignoreCase = true) -> value / 1000000
            Units.UNIT_KILOGRAM.equals(unitOfValue, ignoreCase = true) -> value * 1000
            Units.UNIT_LITER.equals(unitOfValue, ignoreCase = true) -> value * 1000
            Units.UNIT_DECILITRE.equals(unitOfValue, ignoreCase = true) -> value * 100
            Units.UNIT_CENTILITRE.equals(unitOfValue, ignoreCase = true) -> value * 10
            Units.UNIT_MILLILITRE.equals(unitOfValue, ignoreCase = true) -> value
            //TODO : what about % DV and IU
            else -> value
        }

    }

    fun convertFromGram(valueInGramOrMl: Float, targetUnit: String?): Float {
        return convertFromGram(valueInGramOrMl.toDouble(), targetUnit).toFloat()
    }

    fun convertFromGram(valueInGramOrMl: Double, targetUnit: String?): Double {
        return when (targetUnit) {
            Units.UNIT_KILOGRAM, Units.UNIT_LITER -> valueInGramOrMl / 1000
            Units.UNIT_MILLIGRAM -> valueInGramOrMl * 1000
            Units.UNIT_MICROGRAM -> valueInGramOrMl * 1000000
            Units.UNIT_DECILITRE -> valueInGramOrMl / 100
            Units.UNIT_CENTILITRE -> valueInGramOrMl / 10
            else -> valueInGramOrMl
        }
    }

    @JvmStatic
    fun saltToSodium(saltValue: Double): Double {
        return saltValue / SALT_PER_SODIUM
    }

    @JvmStatic
    fun sodiumToSalt(sodiumValue: Double): Double {
        return sodiumValue * SALT_PER_SODIUM
    }

    /**
     * Function which returns volume in oz if parameter is in cl, ml, or l
     *
     * @param servingSize value to transform
     * @return volume in oz if servingSize is a volume parameter else return the the parameter unchanged
     */
    @JvmStatic
    fun getServingInOz(servingSize: String): String {
        val regex = Pattern.compile("(\\d+(?:\\.\\d+)?)")
        val matcher = regex.matcher(servingSize)
        return when {
            servingSize.contains("ml", true) -> {
                matcher.find()
                var value = matcher.group(1).toFloat()
                value *= OZ_PER_L / 1000
                "${getRoundNumber(value)} oz"
            }
            servingSize.contains("cl", true) -> {
                matcher.find()
                var value = matcher.group(1).toFloat()
                value *= OZ_PER_L / 100
                "${getRoundNumber(value)} oz"
            }
            servingSize.contains("l", true) -> {
                matcher.find()
                var value = matcher.group(1).toFloat()
                value *= OZ_PER_L
                "${getRoundNumber(value)} oz"

            }
            //TODO: HANDLE OTHER CASES, NOT L NOR OZ NOR ML NOR CL
            else -> servingSize
        }

    }

    /**
     * Function that returns the volume in liters if input parameter is in oz
     *
     * @param servingSize the value to transform: not null
     * @return volume in liter if input parameter is a volume parameter else return the parameter unchanged
     */
    @JvmStatic
    fun getServingInL(servingSize: String): String {
        return when {
            servingSize.contains("oz", true) -> {
                val regex = Pattern.compile("(\\d+(?:\\.\\d+)?)")
                val matcher = regex.matcher(servingSize)
                matcher.find()
                var value = matcher.group(1).toFloat()
                value /= OZ_PER_L
                "$value l"
            }
            else -> servingSize
        }
        // TODO: HANDLE OTHER CASES eg. not in L nor oz
    }
}