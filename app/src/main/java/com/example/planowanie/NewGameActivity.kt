package com.example.planowanie

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_new_game.*

class NewGameActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)


        //test firebase database
        //database = Firebase.database.reference
        //database.child("users").child("user1").setValue("pass12345")

        val fireDatabase = Firebase.database
        val myRef = fireDatabase.getReference("message")

        myRef.setValue("Hello, World!")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot
                    .getValue<String>()
                d("database_test", "Value is: $value")
                buttonPlay.text = value
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                d("database_test", "Failed to read value.", error.toException())
            }
        })

        editTextPlayer3Name.visibility = View.GONE
        editTextPlayer4Name.visibility = View.GONE

        radioGroupPlayerCount.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = findViewById(checkedId)
            when(radio.text) {
                "2 graczy" -> {
                    editTextPlayer3Name.visibility = View.GONE
                    editTextPlayer4Name.visibility = View.GONE
                }
                "4 graczy" -> {
                    editTextPlayer3Name.visibility = View.VISIBLE
                    editTextPlayer4Name.visibility = View.VISIBLE
                }
            }
        }

        buttonPlay.setOnClickListener {
            //test bazy
            myRef.setValue(editTextPlayer1Name.text.toString())

            //inicjacja parsera GSON, bazy danych
            val gson = GsonBuilder().create()
            val database = getSharedPreferences("database", Context.MODE_PRIVATE)

            //próba wczytania gry z pamięci
            val savedGame = database.getString("matchJson", null)
            if(savedGame != null) { //jeśli gra pobierze się z pamięci
                val loadedMatch = gson.fromJson(savedGame, Match::class.java)
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Wykryto zapisaną grę!")
                builder.setMessage("Na tym urządzeniu zapisana jest niezakończona gra. Czy chcesz ją kontynuować?")

                builder.setPositiveButton("Tak") { _, _ -> //kontynuacja starej gry
                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("loadedMatch", loadedMatch)
                    startActivity(intent)
                }

                builder.setNegativeButton("Nie") { _, _ -> //rozpoczęcie nowej gry
                    startNewGame()
                }
                builder.show()
            }
            else {
                startNewGame()
            }
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.mainMenuStats -> {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }*/

    private fun startNewGame() {
        //inicjacja parsera GSON, bazy danych
        val gson = GsonBuilder().create()
        val database = getSharedPreferences("database", Context.MODE_PRIVATE)

        //utworzenie meczu
        val match = Match()

        val radioPlayer: RadioButton = findViewById(radioGroupPlayerCount.checkedRadioButtonId)
        when (radioPlayer.text) {
            "2 graczy" -> match.settingPlayers = 2
            "4 graczy" -> match.settingPlayers = 4
            else -> match.settingPlayers = 111
        }

        val radioGame: RadioButton = findViewById(radioGroupGameType.checkedRadioButtonId)
        when (radioGame.text) {
            "gra pojedyńcza" -> match.settingGames = 1
            "gra poczwórna" -> match.settingGames = 4
        }

        //zapisanie nazw graczy
        match.player1Name = editTextPlayer1Name.text.toString()
        match.player2Name = editTextPlayer2Name.text.toString()
        if(match.settingPlayers == 4) {
            match.player3Name = editTextPlayer3Name.text.toString()
            match.player4Name = editTextPlayer4Name.text.toString()
        }

        //utworzenie gier
        match.game1 = Game()
        match.game1.player1 = Player()
        match.game1.player2 = Player()
        if(match.settingPlayers == 4) {
            match.game1.player3 = Player()
            match.game1.player4 = Player()
        }

        if(match.settingGames == 4) {
            match.game2 = Game()
            match.game2.player1 = Player()
            match.game2.player2 = Player()
            if(match.settingPlayers == 4) {
                match.game2.player3 = Player()
                match.game2.player4 = Player()
            }

            match.game3 = Game()
            match.game3.player1 = Player()
            match.game3.player2 = Player()
            if(match.settingPlayers == 4) {
                match.game3.player3 = Player()
                match.game3.player4 = Player()
            }

            match.game4 = Game()
            match.game4.player1 = Player()
            match.game4.player2 = Player()
            if(match.settingPlayers == 4) {
                match.game4.player3 = Player()
                match.game4.player4 = Player()
            }
        }

        match.game1.currentPlayer = 1
        match.date = java.util.Calendar.getInstance()

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Match", match)
        startActivity(intent)
        finish()
    }
}