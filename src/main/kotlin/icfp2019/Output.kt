package icfp2019

/*
A solution for a task
  prob-NNN.desc
is a sequence of actions encoded as a single-line text file named
  prob-NNN.sol
for the corresponding numberNNN.
The actions are encoded as follows:
  action ::=
      W(move up)
      | S(move down)
      | A(move left)
      | D(move right)
      | Z(do nothing)
      | E(turn manipulators 90°clockwise)
      | Q(turn manipulators 90°counterclockwise)
      | B(dx,dy)(attach a new manipulator with relative coordinates(dx,dy))
      | F(attach fast wheels)
      | L(start using a drill)

  solution ::= rep(action)

  A solution isvalid, if it does not force the worker-wrapper to go through the walls and obstacles
  (unless it uses a drill), respects the rules of using boosters, and, upon finishing,
  leaves all reachablesquares of the map wrapped.
 */
class Output {
    companion object {
        fun encodeActions(actions: Iterable<Action>): String =
            actions.joinToString(separator = "") { it.toSolutionString() }

        fun encodeRobotActions(robotActions: Map<RobotId, List<Action>>): String =
            robotActions.entries.sortedBy { it.key.id }
                .joinToString(separator = "#") { encodeActions(it.value) }
    }
}
