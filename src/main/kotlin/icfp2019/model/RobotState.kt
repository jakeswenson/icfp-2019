package icfp2019.model

enum class MovementSpeed {
    Normal,
    Fast
}

data class RobotState(
    val robotId: RobotId,
    val currentPosition: Point,
    val orientation: Orientation = Orientation.Right,
    val remainingFastWheelTime: Int = 0,
    val remainingDrillTime: Int = 0,
    val armRelativePoints: List<Point> = listOf(Point(1, 0), Point(1, 1), Point(1, -1))
) {
    fun optimumManipulatorArmTarget(): Point {
        val cuddlePoint = Point(-1, 0)
        if (!armRelativePoints.contains(cuddlePoint)) {
            return cuddlePoint
        } else {
            // Assume all arm extensions extend the original T
            var balancePoint = 0
            var pointCount = 0
            armRelativePoints.forEach {
                if (it.x == 1) {
                    balancePoint += it.y
                    pointCount++
                }
            }

            var point = Point(1, -(pointCount + 1) / 2)
            if (balancePoint <= 0) {
                point = Point(1, (pointCount + 1) / 2)
            }
            return point
        }
    }

    fun hasActiveDrill(): Boolean {
        return remainingDrillTime > 1
    }

    fun hasActiveFastWheels(): Boolean {
        return remainingFastWheelTime > 1
    }

    fun speed(): MovementSpeed = if (hasActiveFastWheels()) MovementSpeed.Fast else MovementSpeed.Normal

    fun turnClockwise(): List<Point> = armRelativePoints.map { Point(it.y, -it.x) }
    fun turnCounterClockwise(): List<Point> = armRelativePoints.map { Point(-it.y, it.x) }
}
