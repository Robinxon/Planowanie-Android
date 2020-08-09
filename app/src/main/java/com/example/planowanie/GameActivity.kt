package com.example.planowanie

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*


class GameActivity: AppCompatActivity() {
    private lateinit var match: Match
    private lateinit var currentGameObject: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //przygotowanie gry
        match = intent.getSerializableExtra("Match") as Match
        imageViewAtut.setBackgroundResource(R.drawable.none)

        editTextPlayer1.setText(match.player1Name)
        editTextPlayer2.setText(match.player2Name)
        if(match.settingPlayers == 4) {
            editTextPlayer3.setText(match.player3Name)
            editTextPlayer4.setText(match.player4Name)
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
        }

        for(i in 0..13) {
            val resID = resources.getIdentifier("buttonPlan$i", "id", packageName)
            val button: Button = findViewById(resID)
            button.setOnClickListener {
                val tag = it.tag as String
                setPlanned(tag.toInt())
                updateText()
                nextPlayer()
            }
        }

        gameStart()
    }

    private fun nextPlayer() {
        if(++currentGameObject.currentPlayer > match.settingPlayers) {
            currentGameObject.currentPlayer = 1
        }
        markActivePlayer()
    }

    private fun gameStart() {
        currentGameObject = when(match.currentGame) {
            1 -> match.game1
            2 -> match.game2
            3 -> match.game3
            4 -> match.game4
            else -> TODO() //gra zakończona lub błędna
        }

        currentGameObject.currentPlayer = match.currentGame
        markActivePlayer()
    }

    private fun randomAtut() {
        when((1..4).random()) {
            1 -> imageViewAtut.setBackgroundResource(R.drawable.karo)
            2 -> imageViewAtut.setBackgroundResource(R.drawable.kier)
            3 -> imageViewAtut.setBackgroundResource(R.drawable.pik)
            4 -> imageViewAtut.setBackgroundResource(R.drawable.trefl)
        }
    }

    private fun setPlanned(plan: Int) {
        when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.planned[currentGameObject.currentRound] = plan
            2 -> currentGameObject.player2.planned[currentGameObject.currentRound] = plan
            3 -> currentGameObject.player3.planned[currentGameObject.currentRound] = plan
            4 -> currentGameObject.player4.planned[currentGameObject.currentRound] = plan
        }
    }

    private fun markActivePlayer() {
        val resID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + currentGameObject.currentPlayer.toString(), "id", packageName)
        val textView: TextView = findViewById(resID)
        textView.setBackgroundColor(Color.BLACK)
        textView.setTextColor(Color.WHITE)

        var previousPlayer = currentGameObject.currentPlayer - 1
        if(previousPlayer <= 0) {
            previousPlayer = match.settingPlayers
        }
        val previousResID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + previousPlayer.toString(), "id", packageName)
        val previousTextView: TextView = findViewById(previousResID)
        previousTextView.setBackgroundColor(Color.TRANSPARENT)
        previousTextView.setTextColor(Color.BLACK)
    }

    private fun updateText() {
        val resID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + currentGameObject.currentPlayer.toString(), "id", packageName)
        val textView: TextView = findViewById(resID)
        when(currentGameObject.currentPlayer) {
            1 -> textView.text = currentGameObject.player1.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player1.planned[currentGameObject.currentRound].toString()
            2 -> textView.text = currentGameObject.player2.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player2.planned[currentGameObject.currentRound].toString()
            3 -> textView.text = currentGameObject.player3.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player3.planned[currentGameObject.currentRound].toString()
            4 -> textView.text = currentGameObject.player4.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player4.planned[currentGameObject.currentRound].toString()
        }

    }
}