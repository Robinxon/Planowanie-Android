package pl.robinxon.planowanie

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
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
import pl.robinxon.planowanie.R
import com.google.gson.GsonBuilder
import com.kaushikthedeveloper.doublebackpress.DoubleBackPress
import com.kaushikthedeveloper.doublebackpress.helper.DoubleBackPressAction
import com.kaushikthedeveloper.doublebackpress.helper.FirstBackPressAction
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_menu.*
import java.util.*
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
        for(i in 0..13) {
            val resID = resources.getIdentifier("buttonPlan$i", "id", packageName)
            val button: Button = findViewById(resID)
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

    private fun buttonPreviousRound() {}

    private fun buttonNextRound() {
        if(
            match.games[match.currentGame]!!.players[1]!!.taken[match.games[match.currentGame]!!.currentRound] == null
            || match.games[match.currentGame]!!.players[2]!!.taken[match.games[match.currentGame]!!.currentRound] == null
            || (match.games[match.currentGame]!!.players[3]?.taken?.get(match.games[match.currentGame]!!.currentRound) == null && match.settingPlayers == 4)
            || (match.games[match.currentGame]!!.players[4]?.taken?.get(match.games[match.currentGame]!!.currentRound) == null && match.settingPlayers == 4)
        ) { val t = Toast.makeText(this, getString(R.string.incompleted_round), Toast.LENGTH_SHORT).show() }
        else {
            //calculate points
            //clear marked players
            if(
                (match.settingGames == 1 && match.games[match.currentGame]!!.currentRound >= 16)
                || (match.settingGames == 1 && match.games[match.currentGame]!!.currentRound >= 13)
            ) { /*endGame()*/ }
            else {
                match.games[match.currentGame]!!.currentRound++
                if(match.games[match.currentGame]!!.currentCards > 1) { match.games[match.currentGame]!!.currentCards-- }
                setNextPlayer()
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
    //endregion

    private fun presetBoard() {
        title = getString(R.string.gameActivityTitle, match.currentGame)

        hideUselessRounds()
        setAtut()
        markActivePlayerAndClearOthers()
        updatePlannedAndMarkGoodOrBad()
        setPlayerPointsAndNames()
        disableButtonToPlan()
    }

    private fun setPlayerPointsAndNames() {
        //wypisz punktację
        editTextPlayer1.setText(getString(R.string.player_name_and_points, match.games[match.currentGame]!!.players[1]?.points, match.playerNames[1]))
        editTextPlayer2.setText(getString(R.string.player_name_and_points, match.games[match.currentGame]!!.players[2]?.points, match.playerNames[2]))
        if(match.settingPlayers == 4) {
            editTextPlayer3.setText(getString(R.string.player_name_and_points, match.games[match.currentGame]!!.players[3]?.points, match.playerNames[3]))
            editTextPlayer4.setText(getString(R.string.player_name_and_points, match.games[match.currentGame]!!.players[4]?.points, match.playerNames[4]))
        }
        else {
            editTextPlayer3.visibility = View.INVISIBLE
            editTextPlayer4.visibility = View.INVISIBLE
        }
    }

    private fun hideUselessRounds() {
        if(match.settingGames == 4) {
            textViewRound14.visibility = View.GONE
            textViewRound14Player1.visibility = View.GONE
            textViewRound14Player2.visibility = View.GONE
            textViewRound14Player3.visibility = View.GONE
            textViewRound14Player4.visibility = View.GONE
            textViewRound15.visibility = View.GONE
            textViewRound15Player1.visibility = View.GONE
            textViewRound15Player2.visibility = View.GONE
            textViewRound15Player3.visibility = View.GONE
            textViewRound15Player4.visibility = View.GONE
            textViewRound16.visibility = View.GONE
            textViewRound16Player1.visibility = View.GONE
            textViewRound16Player2.visibility = View.GONE
            textViewRound16Player3.visibility = View.GONE
            textViewRound16Player4.visibility = View.GONE
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
                val textView: TextView = findViewById(resources.getIdentifier("textViewRound" + round.toString() + "Player" + player.toString(), "id", packageName))
                textView.setBackgroundResource(0)
            }
        }

        //zaznacz obecnego gracza
        val textView: TextView = findViewById(resources.getIdentifier("textViewRound" + match.games[match.currentGame]!!.currentRound.toString() + "Player" + match.games[match.currentGame]!!.currentPlayer.toString().toString(), "id", packageName))
        textView.setBackgroundResource(R.drawable.tv_border)
    }

    private fun updatePlannedAndMarkGoodOrBad() {
        for (round in 1..match.roundsInGame!!) {
            for(player in 1..4) {
                if(!(player > 2 && match.settingPlayers == 2)) {
                    val textView: TextView = findViewById(resources.getIdentifier("textViewRound" + round.toString() + "Player" + player.toString(), "id", packageName))
                    val playerPlanned = match.games[match.currentGame]!!.players[player]!!.planned[round]?.toString() ?: ""
                    val playerTaken = match.games[match.currentGame]!!.players[player]!!.taken[round]?.toString() ?: ""
                    if(round <= match.games[match.currentGame]!!.currentRound) { textView.text = getString(R.string.player_planned_and_taken, playerTaken, playerPlanned) }
                    else {textView.text = ""}
                    when {
                        match.games[match.currentGame]!!.players[player]!!.taken[round] == null -> { textView.setTextColor(Color.BLACK) }
                        match.games[match.currentGame]!!.players[player]!!.taken[round]!! - match.games[match.currentGame]!!.players[player]!!.planned[round]!! == 0 -> { textView.setTextColor(Color.GREEN) }
                        else -> {textView.setTextColor(Color.RED)}
                    }
                }
            }
        }
    }

    private fun disableButtonToPlan() {
        //odblokuj zablokowany
        if(match.games[match.currentGame]!!.toDisabling != null) {
            val resID = resources.getIdentifier("buttonPlan${match.games[match.currentGame]!!.toDisabling}", "id", packageName)
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
            match.games[match.currentGame]!!.toDisabling = match.games[match.currentGame]!!.currentCards - currentPlanned
            if(match.games[match.currentGame]!!.toDisabling != null) {
                val resID = resources.getIdentifier("buttonPlan${match.games[match.currentGame]!!.toDisabling}", "id", packageName)
                val button: Button = findViewById(resID)
                button.isEnabled = false
            }
        }
    }

    private fun setNextPlayer() {
        if(++match.games[match.currentGame]!!.currentPlayer > match.settingPlayers!!) { match.games[match.currentGame]!!.currentPlayer = 1 }
    }

    private fun calculatePoints() {
        for(player in 1..4) { match.games[match.currentGame]!!.players[player]?.points = 0 }

        for (i in 1..match.games[match.currentGame]!!.currentRound) {
            for (player in 1..4) {
                if (
                    (match.games[match.currentGame]!!.players[player]?.taken?.get(i) != null)
                    && (match.games[match.currentGame]!!.players[player]?.planned?.get(i) == match.games[match.currentGame]!!.players[player]?.taken?.get(i))
                ) {
                    match.games[match.currentGame]!!.players[player]!!.points += (10 + match.games[match.currentGame]!!.players[player]!!.planned[i]!!)
                }
            }
        }
    }

    private fun decodeJsonToMatch(value: String): Match {
        val gson = GsonBuilder().create()
        return gson.fromJson(value, Match::class.java)
    }

    private fun saveToFire() {
        val gson = GsonBuilder().create()
        fireMatch.setValue(gson.toJson(match))
    }

    /*private fun previousRound() {
        if(currentGameObject.currentRound != 1) {
            markRoundInactive()
            if(!currentGameObject.ended) {currentGameObject.currentRound--}
            currentGameObject.ended = false
            currentGameObject.currentCards++
            if(currentGameObject.player1.planned[currentGameObject.currentRound] == currentGameObject.player1.taken[currentGameObject.currentRound]) {
                currentGameObject.player1.points -= (10 + currentGameObject.player1.planned[currentGameObject.currentRound])
            }
            if(currentGameObject.player2.planned[currentGameObject.currentRound] == currentGameObject.player2.taken[currentGameObject.currentRound]) {
                currentGameObject.player2.points -= (10 + currentGameObject.player2.planned[currentGameObject.currentRound])
            }
            if(match.settingPlayers == 4) {
                if (currentGameObject.player3.planned[currentGameObject.currentRound] == currentGameObject.player3.taken[currentGameObject.currentRound]) {
                    currentGameObject.player3.points -= (10 + currentGameObject.player3.planned[currentGameObject.currentRound])
                }
                if (currentGameObject.player4.planned[currentGameObject.currentRound] == currentGameObject.player4.taken[currentGameObject.currentRound]) {
                    currentGameObject.player4.points -= (10 + currentGameObject.player4.planned[currentGameObject.currentRound])
                }
            }
            setAtut()
            updatePoints()
            setPlayer()
        }
    }*/





    /*private fun endGame() {
        val t = Toast.makeText(this, "Gra zakończona", Toast.LENGTH_SHORT)
        t.setGravity(Gravity.BOTTOM, 0, 0)
        t.show()

        currentGameObject.ended = true

        saveIntoLocal()
    }*/

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.game_menu, menu)
        if(match.settingGames == 1) {
            menu.findItem(R.id.gameMenuGame2).isVisible = false
            menu.findItem(R.id.gameMenuGame3).isVisible = false
            menu.findItem(R.id.gameMenuGame4).isVisible = false
        }
        return true
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.gameMenuGame1 -> {
            saveIntoLocal()
            match.currentGame = 1
            gameStart()
            true
        }

        R.id.gameMenuGame2 -> {
            saveIntoLocal()
            match.currentGame = 2
            gameStart()
            true
        }

        R.id.gameMenuGame3 -> {
            saveIntoLocal()
            match.currentGame = 3
            gameStart()
            true
        }

        R.id.gameMenuGame4 -> {
            saveIntoLocal()
            match.currentGame = 4
            gameStart()
            true
        }

        R.id.gameSummary -> {
            saveIntoLocal()
            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtra("currentGame", match)
            startActivity(intent)
            true
        }

        R.id.gameMenuExit -> {
            saveIntoLocal()
            val intent = Intent(this, NewGameActivity::class.java)
            startActivity(intent)
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }*/
}
