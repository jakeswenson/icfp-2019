package icfp2019.model

sealed class Booster {
    companion object {
        private val mapping = mapOf(
            'B' to ExtraArm,
            'F' to FastWheels,
            'L' to Drill,
            'X' to CloningLocation,
            'C' to CloneToken,
            'R' to Teleporter
        )

        private val inverseMapping = mapping.map { it.value to it.key.toLowerCase() }.toMap()

        val parseChars = mapping.keys.let { it.plus(it.map { it.toLowerCase() }) }.toSet()

        fun fromChar(code: Char): Booster = when (code) {
            in parseChars -> mapping.getValue(code.toUpperCase())
            else -> throw IllegalArgumentException("Unknown booster code: '$code'")
        }

        fun verifyConsistent(): Boolean =
            if (!mapping.all { it.value.toChar() in parseChars }) error("Boosters not consistent")
            else true
    }

    fun canPickup(): Boolean = when (this) {
        CloningLocation -> false
        else -> true
    }

    fun toChar(): Char = inverseMapping.getValue(this)

    object ExtraArm : Booster()
    object FastWheels : Booster()
    object Drill : Booster()
    object Teleporter : Booster()

    object CloningLocation : Booster()
    object CloneToken : Booster()
}
