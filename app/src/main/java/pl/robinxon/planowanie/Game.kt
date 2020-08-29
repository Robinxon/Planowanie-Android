package pl.robinxon.planowanie

import java.io.Serializable

class Game: Serializable {
    var players = arrayOfNulls<Player>(5)
    var currentRound: Int = 1
    var currentPlayer: Int = 1
    var currentCards = intArrayOf(0, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1 )
    var ended: Boolean = false
    var atuts = arrayOfNulls<Int>(17)
}