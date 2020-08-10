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

        buttonClear.setOnClickListener() {
            clearPlayer()
        }

        for(i in 0..13) {
            val resID = resources.getIdentifier("buttonPlan$i", "id", packageName)
            val button: Button = findViewById(resID)
            button.setOnClickListener {
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
        textView.setBackgroundColor(Color.BLACK)

        var isTaken = when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.taken[currentGameObject.currentRound]
            2 -> currentGameObject.player2.taken[currentGameObject.currentRound]
            3 -> currentGameObject.player3.taken[currentGameObject.currentRound]
            4 -> currentGameObject.player4.taken[currentGameObject.currentRound]
            else -> -1
        }

        if(isTaken == -1) {
            textView.setTextColor(Color.WHITE)
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
        previousTextView.setBackgroundColor(Color.TRANSPARENT)
        if(isTakenPrevious == -1) {
            previousTextView.setTextColor(Color.BLACK)
        }
    }

    private fun markAsGoodOrBad() {
        val resID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + currentGameObject.currentPlayer.toString(), "id", packageName)
        val textView: TextView = findViewById(resID)

        var result = when(currentGameObject.currentPlayer) {
            1 -> currentGameObject.player1.taken[currentGameObject.currentRound] - currentGameObject.player1.planned[currentGameObject.currentRound]
            2 -> currentGameObject.player2.taken[currentGameObject.currentRound] - currentGameObject.player2.planned[currentGameObject.currentRound]
            3 -> currentGameObject.player3.taken[currentGameObject.currentRound] - currentGameObject.player3.planned[currentGameObject.currentRound]
            4 -> currentGameObject.player4.taken[currentGameObject.currentRound] - currentGameObject.player4.planned[currentGameObject.currentRound]
            else -> 14
        }

        if(result == 0) {
            textView.setTextColor(Color.GREEN)
        } else {
            textView.setTextColor(Color.RED)
        }
    }

    private fun updateText() {
        val resID = resources.getIdentifier("textViewRound" + currentGameObject.currentRound.toString() + "Player" + currentGameObject.currentPlayer.toString(), "id", packageName)
        val textView: TextView = findViewById(resID)
        when(currentGameObject.currentPlayer) {
            1 -> {
                if(currentGameObject.player1.taken[currentGameObject.currentRound] == -1) {
                    textView.text = "/" +  currentGameObject.player1.planned[currentGameObject.currentRound].toString()
                } else {
                    textView.text = currentGameObject.player1.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player1.planned[currentGameObject.currentRound].toString()
                }
            }
            2 -> {
                if(currentGameObject.player2.taken[currentGameObject.currentRound] == -1) {
                    textView.text = "/" +  currentGameObject.player2.planned[currentGameObject.currentRound].toString()
                } else {
                    textView.text = currentGameObject.player2.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player2.planned[currentGameObject.currentRound].toString()
                }
            }
            3 -> {
                if(currentGameObject.player3.taken[currentGameObject.currentRound] == -1) {
                    textView.text = "/" +  currentGameObject.player3.planned[currentGameObject.currentRound].toString()
                } else {
                    textView.text = currentGameObject.player3.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player3.planned[currentGameObject.currentRound].toString()
                }
            }
            4 -> {
                if(currentGameObject.player4.taken[currentGameObject.currentRound] == -1) {
                    textView.text = "/" +  currentGameObject.player4.planned[currentGameObject.currentRound].toString()
                } else {
                    textView.text = currentGameObject.player4.taken[currentGameObject.currentRound].toString() + "/" +  currentGameObject.player4.planned[currentGameObject.currentRound].toString()
                }
            }
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
    }

    private fun calculatePlanned() {
        if(currentGameObject.toDisabling != -1) {
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
}