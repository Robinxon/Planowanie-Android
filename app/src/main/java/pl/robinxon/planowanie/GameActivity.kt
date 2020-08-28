package pl.robinxon.planowanie

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.kaushikthedeveloper.doublebackpress.DoubleBackPress
import com.kaushikthedeveloper.doublebackpress.helper.DoubleBackPressAction
import com.kaushikthedeveloper.doublebackpress.helper.FirstBackPressAction
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.system.exitProcess

class GameActivity: AppCompatActivity() {
    //inicjalizacja bazy danych firebase i jej zmiennych
    private val fireDatabase = Firebase.database
    private lateinit var fireMatch: DatabaseReference

    private lateinit var match: Match

    override fun onCreate(savedInstanceState: Bundle?) {
        //inicjalizacja widoku
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //opcje bazy danych
        fireMatch = fireDatabase.getReference("match")

        //dodanie listenerów do zmiennych na serwerze
        fireMatch.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot
                    .getValue<String>()
                Log.d("database_test", "Match is: $value")
                match = decodeJsonToMatch(value!!)
                presetBoard()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("database_test", "Failed to read value Match.", error.toException())
            }
        })

        //ustawienie listenerów do przycisków
        buttonToggle.setOnClickListener { buttonToggle() }
        buttonClear.setOnClickListener { buttonClear() }
        buttonPreviousRound.setOnClickListener { buttonPreviousRound()}
        buttonNextRound.setOnClickListener { buttonNextRound() }
        buttonBackToGame.setOnClickListener { buttonBackToGame() }
        buttonNextGame.setOnClickListener { buttonNextGame() }
        for(i in 0..13) {
            val button: Button = findViewById(resources.getIdentifier("buttonPlan$i","id", packageName))
            button.setOnClickListener {
                buttonPlanClick(it)
            }
        } //przyciski planowania

        //nasłuchiwanie na zakończenie aktywności
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish_activity_game") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish_activity_game"))
    }

    //region Menu wyboru gry
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.game_menu, menu)
        if(match.settingGames == 1) {
            menu.findItem(R.id.gameMenuGame2).isVisible = false
            menu.findItem(R.id.gameMenuGame3).isVisible = false
            menu.findItem(R.id.gameMenuGame4).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.gameMenuGame1 -> {
            if (match.games[1] != null) {
                match.currentGame = 1
                saveToFire()
            } else {
                Toast.makeText(this, getString(R.string.game_not_started), Toast.LENGTH_SHORT)
                    .show()
            }
            true
        }

        R.id.gameMenuGame2 -> {
            if (match.games[2] != null) {
                match.currentGame = 2
                saveToFire()
            } else {
                Toast.makeText(this, getString(R.string.game_not_started), Toast.LENGTH_SHORT)
                    .show()
            }
            true
        }

        R.id.gameMenuGame3 -> {
            if (match.games[3] != null) {
                match.currentGame = 3
                saveToFire()
            } else {
                Toast.makeText(this, getString(R.string.game_not_started), Toast.LENGTH_SHORT)
                    .show()
            }
            true
        }

        R.id.gameMenuGame4 -> {
            if (match.games[4] != null) {
                match.currentGame = 4
                saveToFire()
            } else {
                Toast.makeText(this, getString(R.string.game_not_started), Toast.LENGTH_SHORT)
                    .show()
            }
            true
        }

        R.id.gameSummary -> {
            startActivity(Intent(this, SummaryActivity::class.java))
            true
        }

        R.id.gameMenuExit -> {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
    //endregion

    //region Potwierdzenie wyjścia z aplikacji
    private var firstBackPressAction: FirstBackPressAction = FirstBackPressAction {
        Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
    }

    private var doubleBackPressAction = DoubleBackPressAction {
        finish()
        exitProcess(0)
    }

    private var doubleBackPress = DoubleBackPress()
        .withDoublePressDuration(3000)
        .withFirstBackPressAction(firstBackPressAction)
        .withDoubleBackPressAction(doubleBackPressAction)

    override fun onBackPressed() {
        doubleBackPress.onBackPressed()
    }
    //endregion

    //region Wyłapywanie zmiany layoutu i aktualizacja widoku
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setButtonsVisibility()
    }
    //endregion

    //region Funkcje przycisków
    private fun buttonToggle() {
        setNextPlayer()
        saveToFire()
    }

    private fun buttonClear() {
        match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.taken[match.games[match.currentGame]!!.currentRound] = null
        match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.planned[match.games[match.currentGame]!!.currentRound] = null
        saveToFire()
    }

    private fun buttonPreviousRound() {
        if(match.games[match.currentGame]!!.currentRound > 1) {
            if(!match.games[match.currentGame]!!.ended) {match.games[match.currentGame]!!.currentRound--}
            calculatePoints()
            setAtut()
            setPlayerInRound()
            markActivePlayerAndClearOthers()
            updatePlannedAndMarkGoodOrBad()
            setPlayerPointsAndNames()
            disableButtonToPlan()
        }
    }

    private fun buttonNextRound() {
        if(
            match.games[match.currentGame]!!.players[1]!!.taken[match.games[match.currentGame]!!.currentRound] == null
            || match.games[match.currentGame]!!.players[2]!!.taken[match.games[match.currentGame]!!.currentRound] == null
            || (match.games[match.currentGame]!!.players[3]?.taken?.get(match.games[match.currentGame]!!.currentRound) == null && match.settingPlayers == 4)
            || (match.games[match.currentGame]!!.players[4]?.taken?.get(match.games[match.currentGame]!!.currentRound) == null && match.settingPlayers == 4)
        ) { Toast.makeText(this, getString(R.string.incompleted_round), Toast.LENGTH_SHORT).show() }
        else {
            calculatePoints()
            saveToFire()
            if(
                (match.settingGames == 1 && match.games[match.currentGame]!!.currentRound >= 16)
                || (match.settingGames == 4 && match.games[match.currentGame]!!.currentRound >= 13)
            ) { endGame() }
            else {
                match.games[match.currentGame]!!.currentRound++
                setPlayerInRound()
                calculatePoints()
                saveToFire()
            }
        }
    }

    private fun buttonPlanClick(it: View) {
        val amount = Integer.parseInt(it.tag.toString())

        if(match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.planned[match.games[match.currentGame]!!.currentRound] == null) {
            match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.planned[match.games[match.currentGame]!!.currentRound] = amount
            setNextPlayer()
        } else if(match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.taken[match.games[match.currentGame]!!.currentRound] == null) {
            match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.taken[match.games[match.currentGame]!!.currentRound] = amount
            setNextPlayer()
        }

        saveToFire()
    }

    private fun buttonBackToGame() {
        //ukryj przyciski końca gry i pokaż przyciski gry
        buttonsEndPanel.visibility = View.GONE
        buttonsPanel.visibility = View.VISIBLE
        match.games[match.currentGame]!!.ended = false
        saveToFire()
    }

    private fun buttonNextGame() {
        //zwiększenie licznika rundy
        if(match.settingGames != 1 && ++match.currentGame <= 4) {
            if(match.games[match.currentGame] == null) {
                //utworzenie gry
                match.games[match.currentGame] = Game()
                match.games[match.currentGame]!!.players[1] = Player()
                match.games[match.currentGame]!!.players[2] = Player()
                if(match.settingPlayers == 4) {
                    match.games[match.currentGame]!!.players[3] = Player()
                    match.games[match.currentGame]!!.players[4] = Player()
                }

                //ustawienie pierwszego gracza
                when(match.settingPlayers) {
                    2 -> {
                        match.games[match.currentGame]!!.currentPlayer = when (match.currentGame) {
                            1 -> 1
                            2 -> 2
                            3 -> 1
                            4 -> 2
                            else -> 1
                        }
                    }
                    4 -> match.games[match.currentGame]!!.currentPlayer = match.currentGame
                }

                //ustawienie pierwszego atutu czyli braku
                match.games[match.currentGame]!!.atuts[1] = 0
            }
            //przygotowanie planszy
            saveToFire()
        } else {
            startActivity(Intent(this, SummaryActivity::class.java))
            finish()
        }
    }
    //endregion

    private fun presetBoard() {
        title = getString(R.string.gameActivityTitle, match.currentGame)

        setButtonsVisibility()
        hideUselessRounds()
        setAtut()
        markActivePlayerAndClearOthers()
        updatePlannedAndMarkGoodOrBad()
        setPlayerPointsAndNames()
        disableButtonToPlan()
    }

    private fun setPlayerPointsAndNames() {
        //wypisz punktację
        editTextPlayer1.setText(
            getString(
                R.string.player_name_and_points,
                match.games[match.currentGame]!!.players[1]?.points,
                match.playerNames[1]
            )
        )
        editTextPlayer2.setText(
            getString(
                R.string.player_name_and_points,
                match.games[match.currentGame]!!.players[2]?.points,
                match.playerNames[2]
            )
        )
        if(match.settingPlayers == 4) {
            editTextPlayer3.setText(
                getString(
                    R.string.player_name_and_points,
                    match.games[match.currentGame]!!.players[3]?.points,
                    match.playerNames[3]
                )
            )
            editTextPlayer4.setText(
                getString(
                    R.string.player_name_and_points,
                    match.games[match.currentGame]!!.players[4]?.points,
                    match.playerNames[4]
                )
            )
        }
        else {
            editTextPlayer3.visibility = View.INVISIBLE
            editTextPlayer4.visibility = View.INVISIBLE
        }
    }

    private fun setButtonsVisibility() {
        if(match.games[match.currentGame]!!.ended) {
            buttonsPanel.visibility = View.GONE
            buttonsEndPanel.visibility = View.VISIBLE
        } else {
            buttonsPanel.visibility = View.VISIBLE
            buttonsEndPanel.visibility = View.GONE
        }

        if(match.settingGames == 1 || match.currentGame >= 4) { buttonNextGame.text = getString(R.string.end_match) }
        else { buttonNextGame.text = getString(R.string.next_game) }
    }

    private fun hideUselessRounds() {
        if(match.settingGames == 4) {
            round14Panel.visibility = View.GONE
            round15Panel.visibility = View.GONE
            round16Panel.visibility = View.GONE
        }
    }

    private fun setAtut() {
        //jeśli brak atutu to losuj i przydziel
        if(match.games[match.currentGame]!!.atuts[match.games[match.currentGame]!!.currentRound] == null) {
            match.games[match.currentGame]!!.atuts[match.games[match.currentGame]!!.currentRound] = when((1..4).random()) {
                1 -> 1
                2 -> 2
                3 -> 3
                4 -> 4
                else -> 0
            }
            saveToFire()
        }

        //wyświetl atut
        when(match.games[match.currentGame]!!.atuts[match.games[match.currentGame]!!.currentRound]) {
            0 -> imageViewAtut.setBackgroundResource(R.drawable.none)
            1 -> imageViewAtut.setBackgroundResource(R.drawable.karo)
            2 -> imageViewAtut.setBackgroundResource(R.drawable.kier)
            3 -> imageViewAtut.setBackgroundResource(R.drawable.pik)
            4 -> imageViewAtut.setBackgroundResource(R.drawable.trefl)
            else -> imageViewAtut.setBackgroundResource(R.drawable.none)
        }
    }

    private fun markActivePlayerAndClearOthers() {
        //wyczyść obramowania wszystkich graczy
        for (round in 1..match.roundsInGame!!) {
            for(player in 1..4) {
                val textView: TextView = findViewById(
                    resources.getIdentifier(
                        "textViewRound" + round.toString() + "Player" + player.toString(),
                        "id",
                        packageName
                    )
                )
                textView.setBackgroundResource(0)
            }
        }

        //zaznacz obecnego gracza jeśli gra jest w toku
        if(!match.games[match.currentGame]!!.ended) {
            val textView: TextView = findViewById(
                resources.getIdentifier(
                    "textViewRound" + match.games[match.currentGame]!!.currentRound.toString() + "Player" + match.games[match.currentGame]!!.currentPlayer.toString()
                        .toString(), "id", packageName
                )
            )
            textView.setBackgroundResource(R.drawable.tv_border)
        }
    }

    private fun updatePlannedAndMarkGoodOrBad() {
        for (round in 1..match.roundsInGame!!) {
            for(player in 1..4) {
                if(!(player > 2 && match.settingPlayers == 2)) {
                    val textView: TextView = findViewById(
                        resources.getIdentifier(
                            "textViewRound" + round.toString() + "Player" + player.toString(),
                            "id",
                            packageName
                        )
                    )
                    val playerPlanned = match.games[match.currentGame]!!.players[player]!!.planned[round]?.toString() ?: ""
                    val playerTaken = match.games[match.currentGame]!!.players[player]!!.taken[round]?.toString() ?: ""
                    if(round <= match.games[match.currentGame]!!.currentRound) { textView.text = getString(
                        R.string.player_planned_and_taken,
                        playerTaken,
                        playerPlanned
                    ) }
                    else {textView.text = ""}
                    when {
                        match.games[match.currentGame]!!.players[player]!!.taken[round] == null -> { textView.setTextColor(
                            Color.BLACK
                        ) }
                        match.games[match.currentGame]!!.players[player]!!.taken[round]!! - match.games[match.currentGame]!!.players[player]!!.planned[round]!! == 0 -> { textView.setTextColor(
                            Color.GREEN
                        ) }
                        else -> {textView.setTextColor(Color.RED)}
                    }
                }
            }
        }
    }

    private fun disableButtonToPlan() {
        //odblokuj zablokowany
        if(match.games[match.currentGame]!!.toDisabling != null && match.games[match.currentGame]!!.toDisabling!! >= 0) {
            val resID = resources.getIdentifier(
                "buttonPlan${match.games[match.currentGame]!!.toDisabling}",
                "id",
                packageName
            )
            val button: Button = findViewById(resID)
            button.isEnabled = true
        }

        //zablokuj jak jest potrzeba
        var plannedCount = 0
        var currentPlanned = 0

        for(player in 1..4) {
            if(match.games[match.currentGame]!!.players[player]?.planned?.get(match.games[match.currentGame]!!.currentRound) != null) {
                currentPlanned += match.games[match.currentGame]!!.players[player]!!.planned[match.games[match.currentGame]!!.currentRound]!!
                plannedCount++
            }
        }

        if(plannedCount == match.settingPlayers!! - 1) {
            match.games[match.currentGame]!!.toDisabling = match.games[match.currentGame]!!.currentCards[match.games[match.currentGame]!!.currentRound] - currentPlanned
            if(match.games[match.currentGame]!!.toDisabling != null && match.games[match.currentGame]!!.toDisabling!! >= 0) {
                val button: Button = findViewById(
                    resources.getIdentifier(
                        "buttonPlan${match.games[match.currentGame]!!.toDisabling}",
                        "id",
                        packageName
                    )
                )
                button.isEnabled = false
            }
        }
    }

    private fun calculatePoints() {
        for(player in 1..4) { match.games[match.currentGame]!!.players[player]?.points = 0 }

        for (i in 1 until match.games[match.currentGame]!!.currentRound) {
            for (player in 1..4) {
                if (
                    (match.games[match.currentGame]!!.players[player]?.taken?.get(i) != null)
                    && (match.games[match.currentGame]!!.players[player]?.planned?.get(i) == match.games[match.currentGame]!!.players[player]?.taken?.get(
                        i
                    ))
                ) {
                    match.games[match.currentGame]!!.players[player]!!.points += (10 + match.games[match.currentGame]!!.players[player]!!.planned[i]!!)
                }
            }
        }
    }

    private fun setPlayerInRound() {
        when(match.settingPlayers) {
            2 -> {
                match.games[match.currentGame]!!.currentPlayer =
                    when (match.games[match.currentGame]!!.currentRound % 2) {
                        0 -> 2
                        1 -> 1
                        else -> 1
                    }
            }
            4 -> {
                match.games[match.currentGame]!!.currentPlayer =
                    when (match.games[match.currentGame]!!.currentRound % 4) {
                        0 -> 4
                        1 -> 1
                        2 -> 2
                        3 -> 3
                        else -> 1
                    }
            }
        }
    }

    private fun setNextPlayer() {
        if(++match.games[match.currentGame]!!.currentPlayer > match.settingPlayers!!) { match.games[match.currentGame]!!.currentPlayer = 1 }
    }

    private fun endGame() {
        //zamknij grę w zmiennej
        match.games[match.currentGame]!!.ended = true

        //zapisz do bazy
        saveToFire()
    }

    private fun decodeJsonToMatch(value: String): Match {
        val gson = GsonBuilder().create()
        return gson.fromJson(value, Match::class.java)
    }

    private fun saveToFire() {
        val gson = GsonBuilder().create()
        fireMatch.setValue(gson.toJson(match))
    }
}
