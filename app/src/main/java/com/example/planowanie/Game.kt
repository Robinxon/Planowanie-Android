package com.example.planowanie

import java.io.Serializable

class Game: Serializable {
    lateinit var player1: Player
    lateinit var player2: Player
    lateinit var player3: Player
    lateinit var player4: Player

    var currentRound: Int = 1
    var currentPlayer: Int = 1
    var currentCards: Int = 13
    var toDisabling: Int = -1
    var ended: Boolean = false
    var atuts = IntArray(20) {-1}
}