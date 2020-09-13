package pl.robinxon.planowanie

import java.util.*

class Game {
    var players: MutableList<Player>? = ArrayList<Player>(Collections.nCopies(5, null))
    var currentRound: Int = 1
    var currentPlayer: Int = 1
    var currentCards = mutableListOf(0, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1 )
    var ended: Boolean = false
    var atuts: MutableList<Int>? = ArrayList<Int>(Collections.nCopies(17, null))
}