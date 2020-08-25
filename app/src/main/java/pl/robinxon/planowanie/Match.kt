package pl.robinxon.planowanie

import java.io.Serializable
import java.util.*

class Match: Serializable {
    var game1: Game? = null
    var game2: Game? = null
    var game3: Game? = null
    var game4: Game? = null

    var currentGame: Int = 1

    var player1Name: String? = null
    var player2Name: String? = null
    var player3Name: String? = null
    var player4Name: String? = null

    var settingPlayers: Int = 0
    var settingGames: Int = 0

    val date: Calendar = Calendar.getInstance()
}