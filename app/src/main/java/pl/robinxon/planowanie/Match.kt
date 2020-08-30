package pl.robinxon.planowanie

import java.io.Serializable
import java.util.*

class Match: Serializable {
    var games = arrayOfNulls<Game>(5)

    var currentGame: Int = 1

    var playerNames = arrayOfNulls<String>(5)

    var settingPlayers: Int? = null
    var settingGames: Int? = null
    var roundsInGame: Int? = null
    var ended: Boolean = false
    var saved: Boolean = false

    val date: Calendar = Calendar.getInstance()
}