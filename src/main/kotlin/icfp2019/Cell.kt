package icfp2019

import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.experimental.inv

class Cell {
    companion object {
        val ZERO: Short = 0 // This is NOT representing an empty cell, it means NO FLAGS, avoid a runtime cast

        // Values for cell properties
        val OBSTACLE: Short = 1 // This cell is a wall/obstacle
        val WRAPPED: Short = 2 // This cell has been 'wrapped' by the bot
        val MYSTERY: Short = 4 // This cell contains a 'mystery' object
        val BOOST_EXT: Short = 8 // This cell contains an unclaimed extension boost
        val BOOST_FAST: Short = 16 // This cell contains an unclaimed fast wheels boost
        val BOOST_DRILL: Short = 32 // This cell contains an unclaimed drill boost
        val BOOST_TELEPORT: Short = 64 // This cell contains an unclaimed teleport boost
        val BOOST_CLONE: Short = 128 // This cell contains a clone boost

        fun setFlag(value: Short, flag: Short): Short {
            return value xor flag
        }

        fun setFlags(value: Short, vararg flags: Short): Short {
            return flags.foldRight(value, { flag, acc -> setFlag(acc, flag) })
        }

        fun unsetFlag(value: Short, flag: Short): Short {
            return value and flag.inv()
        }

        fun hasFlag(value: Short, flag: Short): Boolean {
            // an Undefined flag is a special case.
            if (value == ZERO || (value > ZERO && flag == ZERO)) return false

            return value and flag == flag
        }
    }
}
