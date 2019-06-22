package icfp2019

data class Problem(
    val problemId: ProblemId,
    val size: Size,
    val startingPosition: Point,
    val map: Array<Array<Node>>
)
