package com.example.planowanie

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        imageViewAtut.setBackgroundResource(R.drawable.none)
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