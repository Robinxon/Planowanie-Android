package com.example.planowanie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_summary.*

class SummaryActivity: AppCompatActivity() {
    private lateinit var match: Match

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        actionBar?.setDisplayHomeAsUpEnabled(true);
        match = intent.getSerializableExtra("currentGame") as Match

        calculatePoints()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun calculatePoints() {
        //var playerPoints1 = match.game1.player1.points + match.game2.player1.points + match.game3?.player1.points + match.game4?.player1.points
        //tvSummaryPoints1.text = playerPoints1.toString()
    }
}