package pl.robinxon.planowanie

import java.util.*

class Player {
    var points: Int = 0
    var planned: MutableList<Int> = ArrayList<Int>(Collections.nCopies(17, null))
    var taken: MutableList<Int> = ArrayList<Int>(Collections.nCopies(17, null))
}