package com.example.planowanie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_summary.*

class SummaryActivity: AppCompatActivity() {
    private lateinit var match: Match

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        actionBar?.setDisplayHomeAsUpEnabled(true);
        //match = intent.getSerializableExtra("currentGame") as Match

        //wypełnij nazwy graczy
        /*playerName1.setText(match.player1Name)
        playerName2.setText(match.player2Name)
        if(match.settingPlayers == 4) {
            playerName3.setText(match.player3Name)
            playerName4.setText(match.player4Name)
        }*/

        //wypełnij punkty
        /*when(match.settingGames) {
            1 -> {
                tvSummaryPoints1.text = match.game1.player1.points.toString()
                tvSummaryPoints2.text = match.game1.player2.points.toString()
                if(match.settingPlayers == 4) {
                    tvSummaryPoints3.text = match.game1.player3.points.toString()
                    tvSummaryPoints4.text = match.game1.player4.points.toString()
                }
            }
            4 -> {
                tvSummaryPoints1.text = (match.game1.player1.points + match.game2.player1.points + match.game3.player1.points + match.game4.player1.points).toString()
                tvSummaryPoints2.text = (match.game1.player2.points + match.game2.player2.points + match.game3.player2.points + match.game4.player2.points).toString()
                if(match.settingPlayers == 4) {
                    tvSummaryPoints3.text = (match.game1.player3.points + match.game2.player3.points + match.game3.player3.points + match.game4.player3.points).toString()
                    tvSummaryPoints4.text = (match.game1.player4.points + match.game2.player4.points + match.game3.player4.points + match.game4.player4.points).toString()
                }
            }
        }*/

        //listener
        buttonEndGame.setOnClickListener {
            saveAndEndGame()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun saveAndEndGame() {
        val gson = GsonBuilder().create()
        val database = getSharedPreferences("database", Context.MODE_PRIVATE)

        var listMatch = listOf<Match>()
        val listMatchString = database.getString("historyJson", null)
        val matchType = object : TypeToken<List<Match>>() {}.type
        if(listMatchString != null) {
            listMatch = gson.fromJson<List<Match>>(listMatchString, matchType)
        }

        listMatch = listMatch + match

        val json = gson.toJson(listMatch)

        database.edit().apply {
            putString("historyJson", json)
        }.apply()

        database.edit().remove("matchJson").apply()
        Toast.makeText(this, "Gra zapisana pomyślnie!", Toast.LENGTH_SHORT).show()
        val intentToClose = Intent("finish_activity")
        sendBroadcast(intentToClose)
        val intent = Intent(this, NewGameActivity::class.java)
        startActivity(intent)
        finish()
    }
}