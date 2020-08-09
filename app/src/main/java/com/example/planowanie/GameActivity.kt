package com.example.planowanie

import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*


class GameActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        imageViewAtut.setBackgroundResource(R.drawable.none)

        //textViewRound1Player1.setBackgroundColor(Color.BLACK)
        //textViewRound1Player1.setTextColor(Color.RED)
        val match = intent.getSerializableExtra("Match") as Match
        editTextPlayer1.setText(match.player1Name)
        editTextPlayer2.setText(match.player2Name)
        editTextPlayer3.setText(match.player3Name)
        editTextPlayer4.setText(match.player4Name)
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