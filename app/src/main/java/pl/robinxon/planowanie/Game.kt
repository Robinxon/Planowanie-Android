package pl.robinxon.planowanie

import java.io.Serializable

class Game: Serializable {
    var player1: Player? = null
    var player2: Player? = null
    var player3: Player? = null
    var player4: Player? = null

    var currentRound: Int = 1
    var currentPlayer: Int = 1
    var currentCards: Int = 13
    var toDisabling: Int = -1
    var ended: Boolean = false
    var atuts = IntArray(20) {-1}
}