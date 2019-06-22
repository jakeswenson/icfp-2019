package icfp2019

sealed class Booster {
    companion object {
        fun fromString(code: Char) = when (code) {
            'B' -> ExtraArm
            'F' -> FastWheels
            'L' -> Drill
            'X' -> CloningLocation
            'C' -> CloneToken
            'R' -> Teleporter
            else -> throw IllegalArgumentException("Unknown booster code: '$code'")
        }
    }

    object ExtraArm : Booster()
    object FastWheels : Booster()
    object Drill : Booster()
    object Teleporter : Booster()

    object CloningLocation : Booster()
    object CloneToken : Booster()
}
