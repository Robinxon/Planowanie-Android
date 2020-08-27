package pl.robinxon.planowanie

import java.io.Serializable

class Game: Serializable {
    var players = arrayOfNulls<Player>(5)
    var currentRound: Int = 1
    var currentPlayer: Int = 1
    var currentCards: Int = 13
    var toDisabling: Int? = null
    var ended: Boolean = false
    var atuts = arrayOfNulls<Int>(17)
}