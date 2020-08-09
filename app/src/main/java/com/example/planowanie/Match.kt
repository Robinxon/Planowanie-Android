package com.example.planowanie

import java.io.Serializable

class Match: Serializable {
    lateinit var game1: Game
    lateinit var game2: Game
    lateinit var game3: Game
    lateinit var game4: Game

    var currentGame: Int = 1

    var player1Name: String = ""
    var player2Name: String = ""
    var player3Name: String = ""
    var player4Name: String = ""

    var settingPlayers: Int = 0
    var settingGames: Int = 0
}