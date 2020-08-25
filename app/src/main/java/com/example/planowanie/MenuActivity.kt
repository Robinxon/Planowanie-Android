package com.example.planowanie

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.activity_new_game.*

class MenuActivity: AppCompatActivity() {
    //inicjalizacja bazy danych firebase i jej zmiennych
    private val fireDatabase = Firebase.database
    private lateinit var fireMatch: DatabaseReference

    //incjalizacja zmiennych globalnych
    private var loadedMatch: Match? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //inicjalizacja widoku
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        //opcje bazy danych
        Firebase.database.setPersistenceEnabled(true)
        fireMatch = fireDatabase.getReference("match")

        //testowe dane do bazy
        val gson = GsonBuilder().create()
        var match = Match()
        val json = gson.toJson(match)
        fireMatch.setValue(json)

        //dodanie obrysów do przycisków
        menuContinue.setBackgroundResource(R.drawable.tv_border)
        menuNew.setBackgroundResource(R.drawable.tv_border)
        menuPlayers.setBackgroundResource(R.drawable.tv_border)
        menuStats.setBackgroundResource(R.drawable.tv_border)
        menuHistory.setBackgroundResource(R.drawable.tv_border)

        //dodanie listenerów do przycisków
        menuContinue.setOnClickListener { menuContinue() }
        menuNew.setOnClickListener { menuNew() }
        menuPlayers.setOnClickListener { menuPlayers() }
        menuStats.setOnClickListener { menuStats() }
        menuHistory.setOnClickListener { menuHistory() }

        //dodanie listenerów do zmiennych na serwerze
        fireMatch.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot
                    .getValue<String>()
                Log.d("database_test", "Match is: $value")
                if(!value.isNullOrEmpty()) {
                    loadedMatch = decodeJsonToMatch(value)
                    menuContinueDescription.text = loadedMatch?.player1Name ?: "none"
                } else {
                    menuContinueDescription.text = R.string.no_saved_game.toString()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("database_test", "Failed to read value Match.", error.toException())
            }
        })
    }

    //region Funkcje przycisków
    private fun menuContinue() {
        Toast.makeText(this, "ok cont", Toast.LENGTH_SHORT ).show()
    }

    private fun menuNew() {
        if(loadedMatch != null){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Wykryto zapisaną grę!")
            builder.setMessage("Na tym urządzeniu zapisana jest niezakończona gra. Czy napewno chcesz ją nadpisać?")

            builder.setPositiveButton("Tak") { _, _ -> //nadpisanie starej gry
                startActivity(Intent(this, NewGameActivity::class.java))
            }

            builder.setNegativeButton("Nie") { _, _ -> //kontynuowanie starej gry
                startActivity(Intent(this, GameActivity::class.java))
            }

            builder.show()
        } else {
            startActivity(Intent(this, NewGameActivity::class.java))
        }
    }

    private fun menuPlayers() {
        //startActivity(Intent(this, PlayersActivity::class.java))
    }

    private fun menuStats() {
        //startActivity(Intent(this, StatsActivity::class.java))
    }

    private fun menuHistory() {
        //startActivity(Intent(this, HistoryActivity::class.java))
    }
    //endregion

    private fun decodeJsonToMatch(value: String): Match {
        val gson = GsonBuilder().create()
        return gson.fromJson(value, Match::class.java)
    }
}