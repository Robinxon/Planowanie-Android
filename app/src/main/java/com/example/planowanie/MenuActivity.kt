package com.example.planowanie

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //inicjalizacja widoku
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

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

        //inizjalizacja bazy danych firebase
        val fireDatabase = Firebase.database

        //przypisanie wartości z bazy do zmiennych
        val fireMatch = fireDatabase.getReference("match")
    }

    private fun menuContinue() {
        Toast.makeText(this, "ok cont", Toast.LENGTH_SHORT ).show()
    }

    private fun menuNew() {
        val intent = Intent(this, NewGameActivity::class.java)
        startActivity(intent)
    }

    private fun menuPlayers() {
        Toast.makeText(this, "ok players", Toast.LENGTH_SHORT ).show()
    }

    private fun menuStats() {
        Toast.makeText(this, "ok stats", Toast.LENGTH_SHORT ).show()
    }

    private fun menuHistory() {
        Toast.makeText(this, "ok history", Toast.LENGTH_SHORT ).show()
    }
}