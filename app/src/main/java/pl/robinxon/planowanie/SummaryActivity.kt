package pl.robinxon.planowanie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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

    }

    //region Obsługa przycisku cofania
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    //endregion

    private fun setSummary() {
        setBackButton()
        if(match.ended) {
            loadHistoryFromFire()
            listMatch = listMatch + match
            saveHistoryToFire()
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
                if(value != null) { listMatch = decodeJsonToHistory(value!!) }
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