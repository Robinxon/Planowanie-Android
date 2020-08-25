package com.example.planowanie

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
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.activity_new_game.*

class MenuActivity: AppCompatActivity() {
    //inizjalizacja bazy danych firebase
    private val fireDatabase = Firebase.database

    //przypisanie wartości z bazy do zmiennych
    private lateinit var fireMatch: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        //inicjalizacja widoku
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        //opcje bazy danych
        Firebase.database.setPersistenceEnabled(true)
        fireMatch = fireDatabase.getReference("match")

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
                Log.d("database_test", "Value is: $value")
                if(value.isNullOrEmpty()) {
                    menuContinueDescription.text = R.string.no_saved_game.toString()
                } else {
                    menuContinueDescription.text = value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("database_test", "Failed to read value.", error.toException())
            }
        })
    }

    private fun menuContinue() {
        fireMatch.setValue("continue")
        Toast.makeText(this, "ok cont", Toast.LENGTH_SHORT ).show()
    }

    private fun menuNew() {
        val intent = Intent(this, NewGameActivity::class.java)
        startActivity(intent)
    }

    private fun menuPlayers() {
        fireMatch.setValue("players")
        Toast.makeText(this, "ok players", Toast.LENGTH_SHORT ).show()
    }

    private fun menuStats() {
        Toast.makeText(this, "ok stats", Toast.LENGTH_SHORT ).show()
    }

    private fun menuHistory() {
        Toast.makeText(this, "ok history", Toast.LENGTH_SHORT ).show()
    }
}