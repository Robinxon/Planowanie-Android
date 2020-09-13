package pl.robinxon.planowanie

import java.util.*

class Match {
    var games: MutableList<Game>? = ArrayList<Game>(Collections.nCopies(5, null))

    var currentGame: Int = 1

    var playerNames: MutableList<String>? = ArrayList<String>(Collections.nCopies(5, null))

    var settingPlayers: Int? = null
    var settingGames: Int? = null
    var roundsInGame: Int? = null
    var ended: Boolean = false
    var saved: Boolean = false

    var date: String? = null
}