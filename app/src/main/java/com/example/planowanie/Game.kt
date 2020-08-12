package com.example.planowanie

import java.io.Serializable

class Game: Serializable {
    lateinit var player1: Player
    lateinit var player2: Player
    lateinit var player3: Player
    lateinit var player4: Player

    var currentRound: Int = 1
    var currentPlayer: Int = 0
    var currentCards: Int = 13
    var toDisabling: Int = -1
    var atuts = IntArray(20) {-1}
}