package com.example.planowanie

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.kaushikthedeveloper.doublebackpress.DoubleBackPress
import com.kaushikthedeveloper.doublebackpress.helper.DoubleBackPressAction
import com.kaushikthedeveloper.doublebackpress.helper.FirstBackPressAction
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.system.exitProcess


class GameActivity: AppCompatActivity() {
    private lateinit var match: Match
    private lateinit var currentGameObject: Game
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //przygotowanie gry
        match = if(intent.getSerializableExtra("loadedMatch") != null) {
            intent.getSerializableExtra("loadedMatch") as Match
        } else {
            intent.getSerializableExtra("Match") as Match
        }

        currentGameObject = when(match.currentGame) {
            1 -> match.game1
            2 -> match.game2
            3 -> match.game3
            4 -> match.game4
            else -> TODO() //gra zakończona lub błędna
        }

        editTextPlayer1.setText(getString(R.string.playerNameAndPoints, match.player1Name, 0))
        editTextPlayer2.setText(getString(R.string.playerNameAndPoints, match.player2Name, 0))
        if(match.settingPlayers == 4) {
            editTextPlayer3.setText(getString(R.string.playerNameAndPoints, match.player3Name, 0))
            editTextPlayer4.setText(getString(R.string.playerNameAndPoints, match.player4Name, 0))
        }
        else {
            editTextPlayer3.visibility = View.INVISIBLE
            editTextPlayer4.visibility = View.INVISIBLE
        }

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

        buttonToggle.setOnClickListener {
            nextPlayer()
            saveIntoLocal()
        }

        buttonClear.setOnClickListener {
            clearPlayer()
            saveIntoLocal()
        }

        buttonNextRound.setOnClickListener {
            nextRound()
            saveIntoLocal()
        }

        buttonPreviousRound.setOnClickListener {
            previousRound()
            saveIntoLocal()
        }

        for(i in 0..13) {
            val resID = resources.getIdentifier("buttonPlan$i", "id", packageName)
            val button: Button = findViewById(resID)
            button.setOnClickListener {
                buttonClick(it)
                saveIntoLocal()
            }
        }

        //nasłuchiwanie na zakończenie aktywności
        val broadcastReceiver = object : BroadcastReceiver() {

            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish_activity") {
                    finish()
                    // DO WHATEVER YOU WANT.
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish_activity"))

        gameStart()
    }

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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private var firstBackPressAction: FirstBackPressAction = FirstBackPressAction {
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
    }

    private fun nextPlayer() {
        if(++currentGameObject.currentPlayer > match.settingPlayers) {
            currentGameObject.currentPlayer = 1
        }
        markActivePlayer()
        updateText()
        saveIntoLocal()
    }

    private fun setPlayer() {
        if(match.settingPlayers == 2) {
            currentGameObject.currentPlayer = when(currentGameObject.currentRound % 2) {
                0 -> 2
                1 -> 1
                else -> 1
            }
        }
        if(match.settingPlayers == 4) {
            currentGameObject.currentPlayer = when(currentGameObject.currentRound % 4) {
                0 -> 4
                1 -> 1
                2 -> 2
                3 -> 3
                else -> 1
            }
        }
        markActivePlayer()
        updateText()
        saveIntoLocal()
    }

    private fun nextRound() {
        var warning = false
        if(match.settingPlayers == 2) {
            if( currentGameObject.player1.planned[currentGameObject.currentRound] == -1 || currentGameObject.player1.taken[currentGameObject.currentRound] == -1 ||
                currentGameObject.player2.planned[currentGameObject.currentRound] == -1 || currentGameObject.player2.taken[currentGameObject.currentRound] == -1) {
                warning = true
            }
        }
        if(match.settingPlayers == 4) {
            if( currentGameObject.player1.planned[currentGameObject.currentRound] == -1 || currentGameObject.player1.taken[currentGameObject.currentRound] == -1 ||
                currentGameObject.player2.planned[currentGameObject.currentRound] == -1 || currentGameObject.player2.taken[currentGameObject.currentRound] == -1 ||
                currentGameObject.player3.planned[currentGameObject.currentRound] == -1 || currentGameObject.player3.taken[currentGameObject.currentRound] == -1 ||
                currentGameObject.player4.planned[currentGameObject.currentRound] == -1 || currentGameObject.player4.taken[currentGameObject.currentRound] == -1 ) {
                warning = true
            }
        }
        if(warning) {
            val t = Toast.makeText(this, "Dane rundy niekompletne", Toast.LENGTH_SHORT)
            t.setGravity(Gravity.BOTTOM, 0, 0)
            t.show()
        } else {
            calculatePoints()
            updatePoints()
            markRoundInactive()
            if(match.settingGames == 1) {
                if(currentGameObject.currentRound >= 16) {
                    endGame()
                } else {
                    currentGameObject.currentRound++
                    setAtut()
                    setPlayer()
                }
            } else if (match.settingGames == 4) {
                if(currentGameObject.currentRound >= 13) {
                    endGame()
                } else {
                    currentGameObject.currentRound++
                    setAtut()
                    setPlayer()
                }
            }
            if(currentGameObject.currentCards > 1) {
                currentGameObject.currentCards--
            }
        }
    }

    private fun previousRound() {
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
    }

    private fun gameStart() {
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
    }

    private fun buttonClick(it: View) {
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
    }

    private fun setAtut() {
        if(currentGameObject.currentRound == 1) {
            imageViewAtut.setBackgroundResource(R.drawable.none)
            currentGameObject.atuts[currentGameObject.currentRound] = 0
        } else {
            if(currentGameObject.atuts[currentGameObject.currentRound] == -1) {
                when((1..4).random()) {
                    1 -> {
                        imageViewAtut.setBackgroundResource(R.drawable.karo)
                        currentGameObject.atuts[currentGameObject.currentRound] = 1
                    }
                    2 -> {
                        imageViewAtut.setBackgroundResource(R.drawable.kier)
                        currentGameObject.atuts[currentGameObject.currentRound] = 2
                    }
                    3 -> {
                        imageViewAtut.setBackgroundResource(R.drawable.pik)
                        currentGameObject.atuts[currentGameObject.currentRound] = 3
                    }
                    4 -> {
                        imageViewAtut.setBackgroundResource(R.drawable.trefl)
                        currentGameObject.atuts[currentGameObject.currentRound] = 4
                    }
                }
            } else {
                when(currentGameObject.atuts[currentGameObject.currentRound]) {
                    0 -> imageViewAtut.setBackgroundResource(R.drawable.none)
                    1 -> imageViewAtut.setBackgroundResource(R.drawable.karo)
                    2 -> imageViewAtut.setBackgroundResource(R.drawable.kier)
                    3 -> imageViewAtut.setBackgroundResource(R.drawable.pik)
                    4 -> imageViewAtut.setBackgroundResource(R.drawable.trefl)
                }
            }
        }
    }

    private fun setPlanned(plan: Int) {
        when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.planned[currentGameObject.currentRound] = plan
            2 -> currentGameObject.player2.planned[currentGameObject.currentRound] = plan
            3 -> currentGameObject.player3.planned[currentGameObject.currentRound] = plan
            4 -> currentGameObject.player4.planned[currentGameObject.currentRound] = plan
        }

        calculatePlanned()
    }

    private fun setTaken(take: Int) {
        when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.taken[currentGameObject.currentRound] = take
            2 -> currentGameObject.player2.taken[currentGameObject.currentRound] = take
            3 -> currentGameObject.player3.taken[currentGameObject.currentRound] = take
            4 -> currentGameObject.player4.taken[currentGameObject.currentRound] = take
        }

        markAsGoodOrBad()
    }

    private fun markActivePlayer() {
        val resID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + currentGameObject.currentPlayer.toString(), "id", packageName)
        val textView: TextView = findViewById(resID)
        textView.setBackgroundResource(R.drawable.tv_border)

        var isTaken = when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.taken[currentGameObject.currentRound]
            2 -> currentGameObject.player2.taken[currentGameObject.currentRound]
            3 -> currentGameObject.player3.taken[currentGameObject.currentRound]
            4 -> currentGameObject.player4.taken[currentGameObject.currentRound]
            else -> -1
        }

        var previousPlayer = currentGameObject.currentPlayer - 1
        if(previousPlayer <= 0) {
            previousPlayer = match.settingPlayers
        }
        var isTakenPrevious = when(previousPlayer) {
            1 -> currentGameObject.player1.taken[currentGameObject.currentRound]
            2 -> currentGameObject.player2.taken[currentGameObject.currentRound]
            3 -> currentGameObject.player3.taken[currentGameObject.currentRound]
            4 -> currentGameObject.player4.taken[currentGameObject.currentRound]
            else -> -1
        }
        val previousResID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + previousPlayer.toString(), "id", packageName)
        val previousTextView: TextView = findViewById(previousResID)
        previousTextView.setBackgroundResource(0)
        if(isTakenPrevious == -1) {
            previousTextView.setTextColor(Color.BLACK)
        }
    }

    private fun markRoundInactive() {
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
    }

    private fun markAsGoodOrBad() {
        for (i in 1..currentGameObject.currentRound) {
            //player 1
            val resID1 = resources.getIdentifier("textViewRound" + i.toString() + "Player1", "id", packageName)
            val textView1: TextView = findViewById(resID1)
            if(currentGameObject.player1.taken[i] == -1) {
                textView1.setTextColor(Color.BLACK)
            } else if(currentGameObject.player1.taken[i] - currentGameObject.player1.planned[i] == 0) {
                textView1.setTextColor(Color.GREEN)
            } else {
                textView1.setTextColor(Color.RED)
            }

            //player 2
            val resID2 = resources.getIdentifier("textViewRound" + i.toString() + "Player2", "id", packageName)
            val textView2: TextView = findViewById(resID2)
            if(currentGameObject.player2.taken[i] == -1) {
                textView2.setTextColor(Color.BLACK)
            } else if(currentGameObject.player2.taken[i] - currentGameObject.player2.planned[i] == 0) {
                textView2.setTextColor(Color.GREEN)
            } else {
                textView2.setTextColor(Color.RED)
            }

            if(match.settingPlayers == 4) {
                //player 3
                val resID3 = resources.getIdentifier("textViewRound" + i.toString() + "Player3", "id", packageName)
                val textView3: TextView = findViewById(resID3)
                if(currentGameObject.player3.taken[i] == -1) {
                    textView3.setTextColor(Color.BLACK)
                } else if(currentGameObject.player3.taken[i] - currentGameObject.player3.planned[i] == 0) {
                    textView3.setTextColor(Color.GREEN)
                } else {
                    textView3.setTextColor(Color.RED)
                }

                //player 4
                val resID4 = resources.getIdentifier("textViewRound" + i.toString() + "Player4", "id", packageName)
                val textView4: TextView = findViewById(resID4)
                if(currentGameObject.player4.taken[i] == -1) {
                    textView4.setTextColor(Color.BLACK)
                } else if(currentGameObject.player4.taken[i] - currentGameObject.player4.planned[i] == 0) {
                    textView4.setTextColor(Color.GREEN)
                } else {
                    textView4.setTextColor(Color.RED)
                }
            }
        }
    }

    private fun clearText() {
        var roundsToClear: Int = 13
        if(match.settingGames == 1)  {roundsToClear = 16}
        if(match.settingGames == 4)  {roundsToClear = 13}
        for (i in 1..roundsToClear) {
            //player 1
            val resID1 = resources.getIdentifier("textViewRound" + i.toString() + "Player1", "id", packageName)
            val textView1: TextView = findViewById(resID1)
            textView1.text = ""
            textView1.setBackgroundResource(0)

            //player 2
            val resID2 = resources.getIdentifier("textViewRound" + i.toString() + "Player2", "id", packageName)
            val textView2: TextView = findViewById(resID2)
            textView2.text = ""
            textView2.setBackgroundResource(0)

            if(match.settingPlayers == 4) {
                //player 3
                val resID3 = resources.getIdentifier("textViewRound" + i.toString() + "Player3", "id", packageName)
                val textView3: TextView = findViewById(resID3)
                textView3.text = ""
                textView3.setBackgroundResource(0)

                //player 4
                val resID4 = resources.getIdentifier("textViewRound" + i.toString() + "Player4", "id", packageName)
                val textView4: TextView = findViewById(resID4)
                textView4.text = ""
                textView4.setBackgroundResource(0)
            }
        }
    }

    private fun updateText() {
        for (i in 1..currentGameObject.currentRound) {
            //player 1
            val resID1 = resources.getIdentifier("textViewRound" + i.toString() + "Player1", "id", packageName)
            val textView1: TextView = findViewById(resID1)
            var player1Planned = currentGameObject.player1.planned[i].toString()
            if(currentGameObject.player1.planned[i] == -1) {player1Planned = ""}
            var player1Taken = currentGameObject.player1.taken[i].toString()
            if(currentGameObject.player1.taken[i] == -1) {player1Taken = ""}
            textView1.text = getString(R.string.playerPlannedAndTaken, player1Taken, player1Planned)

            //player 2
            val resID2 = resources.getIdentifier("textViewRound" + i.toString() + "Player2", "id", packageName)
            val textView2: TextView = findViewById(resID2)
            var player2Planned = currentGameObject.player2.planned[i].toString()
            if(currentGameObject.player2.planned[i] == -1) {player2Planned = ""}
            var player2Taken = currentGameObject.player2.taken[i].toString()
            if(currentGameObject.player2.taken[i] == -1) {player2Taken = ""}
            textView2.text = getString(R.string.playerPlannedAndTaken, player2Taken, player2Planned)

            if(match.settingPlayers == 4) {
                //player 3
                val resID3 = resources.getIdentifier("textViewRound" + i.toString() + "Player3", "id", packageName)
                val textView3: TextView = findViewById(resID3)
                var player3Planned = currentGameObject.player3.planned[i].toString()
                if(currentGameObject.player3.planned[i] == -1) {player3Planned = ""}
                var player3Taken = currentGameObject.player3.taken[i].toString()
                if(currentGameObject.player3.taken[i] == -1) {player3Taken = ""}
                textView3.text = getString(R.string.playerPlannedAndTaken, player3Taken, player3Planned)

                //player 4
                val resID4 = resources.getIdentifier("textViewRound" + i.toString() + "Player4", "id", packageName)
                val textView4: TextView = findViewById(resID4)
                var player4Planned = currentGameObject.player4.planned[i].toString()
                if(currentGameObject.player4.planned[i] == -1) {player4Planned = ""}
                var player4Taken = currentGameObject.player4.taken[i].toString()
                if(currentGameObject.player4.taken[i] == -1) {player4Taken = ""}
                textView4.text = getString(R.string.playerPlannedAndTaken, player4Taken, player4Planned)
            }
        }
    }

    private fun updatePoints() {
        editTextPlayer1.setText(getString(R.string.playerNameAndPoints, match.player1Name, currentGameObject.player1.points))
        editTextPlayer2.setText(getString(R.string.playerNameAndPoints, match.player2Name, currentGameObject.player2.points))
        if(match.settingPlayers == 4) {
            editTextPlayer3.setText(getString(R.string.playerNameAndPoints, match.player3Name, currentGameObject.player3.points))
            editTextPlayer4.setText(getString(R.string.playerNameAndPoints, match.player4Name, currentGameObject.player4.points))
        }
    }

    private fun clearPlayer() {
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
    }

    private fun calculatePlanned() {
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
    }

    private fun calculatePoints() {
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
    }

    private fun endGame() {
        val t = Toast.makeText(this, "Gra zakończona", Toast.LENGTH_SHORT)
        t.setGravity(Gravity.BOTTOM, 0, 0)
        t.show()

        currentGameObject.ended = true

        saveIntoLocal()
    }

    private fun saveIntoLocal() {
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
    }
}
