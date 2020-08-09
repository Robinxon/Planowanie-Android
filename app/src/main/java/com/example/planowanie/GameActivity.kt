package com.example.planowanie

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*


class GameActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //przygotowanie gry
        val match = intent.getSerializableExtra("Match") as Match

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
        //textViewRound1Player1.setBackgroundColor(Color.BLACK)
        //textViewRound1Player1.setTextColor(Color.RED)
    }

    fun randomAtut() {
        when((1..4).random()) {
            1 -> imageViewAtut.setBackgroundResource(R.drawable.karo)
            2 -> imageViewAtut.setBackgroundResource(R.drawable.kier)
            3 -> imageViewAtut.setBackgroundResource(R.drawable.pik)
            4 -> imageViewAtut.setBackgroundResource(R.drawable.trefl)
        }
    }

    fun updateText(round: Int, player: Int, text: String) {
        val resID = resources.getIdentifier("textViewRound" + round.toString() + "Player" + player.toString(), "id", packageName)
        val textView: TextView = findViewById<TextView>(resID)
        textView.text = text
    }
}