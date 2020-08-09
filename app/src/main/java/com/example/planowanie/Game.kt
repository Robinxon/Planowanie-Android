package com.example.planowanie

import java.io.Serializable

class Game: Serializable {
    lateinit var player1: Player
    lateinit var player2: Player
    lateinit var player3: Player
    lateinit var player4: Player

    var round: Int = 1
}