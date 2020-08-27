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

        //STARY KOD


        /*
        buttonClear.setOnClickListener {
            clearPlayer()
            saveIntoLocal()
        }



        buttonPreviousRound.setOnClickListener {
            previousRound()
            saveIntoLocal()
        }*/



        /*

        gameStart()*/
    }

    private fun decodeJsonToMatch(value: String): Match {
        val gson = GsonBuilder().create()
        return gson.fromJson(value, Match::class.java)
    }

    //region Funkcje przycisków
    private fun buttonToggle() {
        if(++match.games[match.currentGame]!!.currentPlayer > match.settingPlayers!!) { match.games[match.currentGame]!!.currentPlayer = 1 }
        saveToFire()
    }

    private fun buttonNextRound() {
        if(
            match.games[match.currentGame]!!.players[1]!!.taken[match.games[match.currentGame]!!.currentRound] == null
            || match.games[match.currentGame]!!.players[2]!!.taken[match.games[match.currentGame]!!.currentRound] == null
            || (match.games[match.currentGame]!!.players[3]!!.taken[match.games[match.currentGame]!!.currentRound] == null && match.settingPlayers == 4)
            || (match.games[match.currentGame]!!.players[4]!!.taken[match.games[match.currentGame]!!.currentRound] == null && match.settingPlayers == 4)
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
                saveToFire()
            }
        }
    }

    private fun buttonPlanClick(it: View) {
        val amount = Integer.parseInt(it.tag.toString())

        if(match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.planned[match.games[match.currentGame]!!.currentRound] == null) {
            match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.planned[match.games[match.currentGame]!!.currentRound] = amount
        } else if(match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.taken[match.games[match.currentGame]!!.currentRound] == null) {
            match.games[match.currentGame]!!.players[match.games[match.currentGame]!!.currentPlayer]!!.taken[match.games[match.currentGame]!!.currentRound] = amount
        }

        saveToFire()
    }
    //endregion

    private fun presetBoard() {
        setPlayerPointsAndNames()
        hideUselessRounds()
        setAtut()
        markActivePlayerAndClearOthers()
        updatePlannedAndMarkGoodOrBad()
    }

    private fun setPlayerPointsAndNames() {
        editTextPlayer1.setText(getString(R.string.player_name_and_points,0, match.playerNames[1]))
        editTextPlayer2.setText(getString(R.string.player_name_and_points,0, match.playerNames[2]))
        if(match.settingPlayers == 4) {
            editTextPlayer3.setText(getString(R.string.player_name_and_points, 0, match.playerNames[3]))
            editTextPlayer4.setText(getString(R.string.player_name_and_points, 0, match.playerNames[4]))
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

    /*private fun gameStart() {
        title = getString(R.string.gameActivityTitle, match.currentGame)

        currentGameObject = when(match.currentGame) {
            1 -> match.game1
            2 -> match.game2
            3 -> match.game3
            4 -> match.game4
            else -> TODO() //gra zakończona lub błędna
        }

        clearText()
        setPlayer()
        markAsGoodOrBad()
        calculatePoints()
        calculatePlanned()
        updateText()
        updatePoints()
        setAtut()
        saveIntoLocal()
    }*/

    /*private fun buttonClick(it: View) {
        val tag = it.tag as String
        val planned = when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.planned[currentGameObject.currentRound]
            2 -> currentGameObject.player2.planned[currentGameObject.currentRound]
            3 -> currentGameObject.player3.planned[currentGameObject.currentRound]
            4 -> currentGameObject.player4.planned[currentGameObject.currentRound]
            else -> TODO() //currentplayer błędny
        }
        val taken = when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.taken[currentGameObject.currentRound]
            2 -> currentGameObject.player2.taken[currentGameObject.currentRound]
            3 -> currentGameObject.player3.taken[currentGameObject.currentRound]
            4 -> currentGameObject.player4.taken[currentGameObject.currentRound]
            else -> TODO() //currentplayer błędny
        }

        if(planned == -1) {
            setPlanned(tag.toInt())
        } else if(taken == -1) {
            setTaken(tag.toInt())
        }

        updateText()
        nextPlayer()
    }*/

    /*private fun markRoundInactive() {
        for (i in 1..currentGameObject.currentRound) {
            var resID1 = resources.getIdentifier("textViewRound" + i.toString() + "Player1", "id", packageName)
            var textView1: TextView = findViewById(resID1)
            textView1.setBackgroundResource(0)

            var resID2 = resources.getIdentifier("textViewRound" + i.toString() + "Player2", "id", packageName)
            var textView2: TextView = findViewById(resID2)
            textView2.setBackgroundResource(0)

            var resID3 = resources.getIdentifier("textViewRound" + i.toString() + "Player3", "id", packageName)
            var textView3: TextView = findViewById(resID3)
            textView3.setBackgroundResource(0)

            var resID4 = resources.getIdentifier("textViewRound" + i.toString() + "Player4", "id", packageName)
            var textView4: TextView = findViewById(resID4)
            textView4.setBackgroundResource(0)
        }
    }*/

    /*private fun updatePoints() {
        editTextPlayer1.setText(getString(R.string.playerNameAndPoints, match.player1Name, currentGameObject.player1.points))
        editTextPlayer2.setText(getString(R.string.playerNameAndPoints, match.player2Name, currentGameObject.player2.points))
        if(match.settingPlayers == 4) {
            editTextPlayer3.setText(getString(R.string.playerNameAndPoints, match.player3Name, currentGameObject.player3.points))
            editTextPlayer4.setText(getString(R.string.playerNameAndPoints, match.player4Name, currentGameObject.player4.points))
        }
    }*/

    /*private fun clearPlayer() {
        val resID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + currentGameObject.currentPlayer.toString(), "id", packageName)
        val textView: TextView = findViewById(resID)
        textView.text = ""

        when(currentGameObject.currentPlayer) {
            1 -> {
                currentGameObject.player1.taken[currentGameObject.currentRound] = -1
                currentGameObject.player1.planned[currentGameObject.currentRound] = -1
            }
            2 -> {
                currentGameObject.player2.taken[currentGameObject.currentRound] = -1
                currentGameObject.player2.planned[currentGameObject.currentRound] = -1
            }
            3 -> {
                currentGameObject.player3.taken[currentGameObject.currentRound] = -1
                currentGameObject.player3.planned[currentGameObject.currentRound] = -1
            }
            4 -> {
                currentGameObject.player4.taken[currentGameObject.currentRound] = -1
                currentGameObject.player4.planned[currentGameObject.currentRound] = -1
            }
        }

        calculatePlanned()
        markActivePlayer()
        updateText()
    }*/

    /*private fun calculatePlanned() {
        if(currentGameObject.toDisabling >= 0) {
            val resID = resources.getIdentifier("buttonPlan${currentGameObject.toDisabling}", "id", packageName)
            val button: Button = findViewById(resID)
            button.isEnabled = true
        }

        var plannedCount = 0
        var currentPlanned = 0
        if(currentGameObject.player1.planned[currentGameObject.currentRound] != -1) {
            currentPlanned += currentGameObject.player1.planned[currentGameObject.currentRound]
            plannedCount++
        }
        if(currentGameObject.player2.planned[currentGameObject.currentRound] != -1) {
            currentPlanned += currentGameObject.player2.planned[currentGameObject.currentRound]
            plannedCount++
        }
        if(match.settingPlayers == 4) {
            if(currentGameObject.player3.planned[currentGameObject.currentRound] != -1) {
                currentPlanned += currentGameObject.player3.planned[currentGameObject.currentRound]
                plannedCount++
            }
            if(currentGameObject.player4.planned[currentGameObject.currentRound] != -1) {
                currentPlanned += currentGameObject.player4.planned[currentGameObject.currentRound]
                plannedCount++
            }
        }

        if(plannedCount == match.settingPlayers - 1) {
            currentGameObject.toDisabling = currentGameObject.currentCards - currentPlanned
            if(currentGameObject.toDisabling >= 0) {
                val resID = resources.getIdentifier("buttonPlan${currentGameObject.toDisabling}", "id", packageName)
                val button: Button = findViewById(resID)
                button.isEnabled = false
            }
        }
    }*/

    /*private fun calculatePoints() {
        currentGameObject.player1.points = 0
        currentGameObject.player2.points = 0
        if(match.settingPlayers == 4) {
            currentGameObject.player3.points = 0
            currentGameObject.player4.points = 0
        }
        for (i in 1..currentGameObject.currentRound) {
            if((currentGameObject.player1.planned[i] == currentGameObject.player1.taken[i]) && (currentGameObject.player1.taken[i] != -1)) {
                currentGameObject.player1.points += (10 + currentGameObject.player1.planned[i])
            }
            if((currentGameObject.player2.planned[i] == currentGameObject.player2.taken[i]) && (currentGameObject.player1.taken[i] != -1)) {
                currentGameObject.player2.points += (10 + currentGameObject.player2.planned[i])
            }
            if(match.settingPlayers == 4) {
                if((currentGameObject.player3.planned[i] == currentGameObject.player3.taken[i]) && (currentGameObject.player1.taken[i] != -1)) {
                    currentGameObject.player3.points += (10 + currentGameObject.player3.planned[i])
                }
                if((currentGameObject.player4.planned[i] == currentGameObject.player4.taken[i]) && (currentGameObject.player1.taken[i] != -1)) {
                    currentGameObject.player4.points += (10 + currentGameObject.player4.planned[i])
                }
            }
        }
        updatePoints()
    }*/

    /*private fun endGame() {
        val t = Toast.makeText(this, "Gra zakończona", Toast.LENGTH_SHORT)
        t.setGravity(Gravity.BOTTOM, 0, 0)
        t.show()

        currentGameObject.ended = true

        saveIntoLocal()
    }*/

    /*private fun saveIntoLocal() {
        val gson = GsonBuilder().create()
        val database = getSharedPreferences("database", Context.MODE_PRIVATE)

        when(match.currentGame) {
            1 -> match.game1 = currentGameObject
            2 -> match.game2 = currentGameObject
            3 -> match.game3 = currentGameObject
            4 -> match.game4 = currentGameObject
        }

        val json = gson.toJson(match)

        database.edit().apply {
            putString("matchJson", json)
        }.apply()
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

    /*private var firstBackPressAction: FirstBackPressAction = FirstBackPressAction {
        Toast.makeText(this, "Naciśnij ponownie, aby zamknąć aplikację", Toast.LENGTH_SHORT).show()
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
    }*/
}
