package pl.robinxon.planowanie

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_summary.*

class SummaryActivity: AppCompatActivity() {
    //inicjalizacja bazy danych firebase i jej zmiennych
    private val fireDatabase = Firebase.database
    private lateinit var fireMatch: DatabaseReference
    private lateinit var fireHistory: DatabaseReference

    private lateinit var match: Match
    private var listMatch = listOf<Match>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        //opcje bazy danych
        fireMatch = fireDatabase.getReference("match")
        fireHistory = fireDatabase.getReference("history")

        //dodanie listenerów do zmiennych na serwerze
        fireMatch.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot
                    .getValue<String>()
                Log.d("database_match", "Match is: $value")
                match = decodeJsonToMatch(value!!)
                setSummary()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("database_match", "Failed to read value Match.", error.toException())
            }
        })

        //listenery do przycisków
        exitToMenu.setOnClickListener{ exitToMenu() }
    }

    //region Obsługa przycisku cofania
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    //endregion

    private fun exitToMenu() {
        finish()
    }

    private fun setSummary() {
        setBackButton()
        if(match.ended) {
            loadHistoryFromFire()
            listMatch = listMatch + match
            saveHistoryToFire()
            buttonsPanel.visibility = View.VISIBLE
        }

        //ukrycie zbędnych graczy
        if(match.settingPlayers == 2){
            player3Panel.visibility = View.INVISIBLE
            player4Panel.visibility = View.INVISIBLE
        }

        //wypełnianie tekstu
        player1Name.setText(match.playerNames[1] ?: "")
        player2Name.setText(match.playerNames[2] ?: "")
        player3Name.setText(match.playerNames[3] ?: "")
        player4Name.setText(match.playerNames[4] ?: "")

        //wyliczenie punktów
        for (player in 1..4) {
            val calculate = (match.games[1]?.players?.get(player)?.points ?: 0) + (match.games[2]?.players?.get(player)?.points ?: 0) + (match.games[3]?.players?.get(player)?.points ?: 0) + (match.games[4]?.players?.get(player)?.points ?: 0)
            val textView: TextView = findViewById(resources.getIdentifier("player${player}Points", "id", packageName))
            textView.text = calculate.toString()
        }
    }

    private fun setBackButton() {
        //ukrywanie przycisku wstecz jeśli mecz jest zakończony
        if(match.ended) {
            supportActionBar?.setHomeButtonEnabled(false) // disable the button
            supportActionBar?.setDisplayHomeAsUpEnabled(false) // remove the left caret
            supportActionBar?.setDisplayShowHomeEnabled(false) // remove the icon
        } else { supportActionBar?.setDisplayHomeAsUpEnabled(true) }
    }

    private fun loadHistoryFromFire() {
        fireHistory.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                Log.d("database_history", "History is: $value")
                if(value != null) { listMatch = decodeJsonToHistory(value) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("database_history", "Failed to read value Match.", error.toException())
            }
        })
    }

    private fun saveHistoryToFire() {
        val gson = GsonBuilder().create()
        fireHistory.setValue(gson.toJson(listMatch))
    }

    private fun decodeJsonToMatch(value: String): Match {
        val gson = GsonBuilder().create()
        return gson.fromJson(value, Match::class.java)
    }

    private fun decodeJsonToHistory(value: String): List<Match> {
        val gson = GsonBuilder().create()
        return gson.fromJson<List<Match>>(value, object : TypeToken<List<Match>>() {}.type)
    }
}