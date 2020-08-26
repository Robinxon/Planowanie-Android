package pl.robinxon.planowanie

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.robinxon.planowanie.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_new_game.*

class NewGameActivity : AppCompatActivity() {
    //inicjalizacja bazy danych firebase i jej zmiennych
    private val fireDatabase = Firebase.database
    private lateinit var fireMatch: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        //inicjalizacja widoku
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)
        actionBar?.setDisplayHomeAsUpEnabled(true);

        //opcje bazy danych
        fireMatch = fireDatabase.getReference("match")

        //ustawienie listenera dla radio buttonów
        radioGroupPlayerCount.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = findViewById(checkedId)
            when(resources.getResourceEntryName(radio.id)) {
                "radioButton2Players" -> {
                    editTextPlayer3Name.visibility = View.GONE
                    editTextPlayer4Name.visibility = View.GONE
                }
                "radioButton4Players" -> {
                    editTextPlayer3Name.visibility = View.VISIBLE
                    editTextPlayer4Name.visibility = View.VISIBLE
                }
            }
        }

        //dodanie listenerów do przycisków
        buttonPlay.setOnClickListener { startNewGame() }
    }

    //region Overridy funkcji
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    //endregion

    //region Funkcje przycisków
    private fun startNewGame() {
        //sprawdzenie poprawności
        if(
            editTextPlayer1Name.text.toString() == ""
            || editTextPlayer2Name.text.toString() == ""
            || (editTextPlayer3Name.visibility == View.VISIBLE && editTextPlayer3Name.text.toString() == "")
            || (editTextPlayer4Name.visibility == View.VISIBLE && editTextPlayer4Name.text.toString() == "")
        ) {
            Toast.makeText(this, resources.getString(R.string.input_all_player_names), Toast.LENGTH_SHORT ).show()
            return
        }
        //utworzenie klasy meczu
        val match = Match()

        //zapisanie liczby graczy
        val radioPlayer: RadioButton = findViewById(radioGroupPlayerCount.checkedRadioButtonId)
        when (resources.getResourceEntryName(radioPlayer.id)) {
            "radioButton2Players" -> match.settingPlayers = 2
            "radioButton4Players" -> match.settingPlayers = 4
        }

        //zapisanie liczby gier
        val radioGame: RadioButton = findViewById(radioGroupGameType.checkedRadioButtonId)
        when (resources.getResourceEntryName(radioGame.id)) {
            "radioButton1Round" -> match.settingGames = 1
            "radioButton4Rounds" -> match.settingGames = 4
        }

        //zapisanie nazw graczy
        match.playerNames[1] = editTextPlayer1Name.text?.toString()
        match.playerNames[2] = editTextPlayer2Name.text?.toString()
        if(match.settingPlayers == 4) {
            match.playerNames[3] = editTextPlayer3Name.text?.toString()
            match.playerNames[4] = editTextPlayer4Name.text?.toString()
        }

        //utworzenie gier
        match.games[1] = Game()
        match.games[1]!!.players[1] = Player()
        match.games[1]!!.players[2] = Player()
        if(match.settingPlayers == 4) {
            match.games[1]!!.players[3] = Player()
            match.games[1]!!.players[4] = Player()
        }

        //ustawienie pierwszego gracza
        match.games[1]!!.currentPlayer = 1

        //ustawienie pierwszego atutu czyli braku
        match.games[1]!!.atuts[1] = 0

        //przekonwertowanie meczu do json i zapisanie w bazie
        val gson = GsonBuilder().create()
        fireMatch.setValue(gson.toJson(match))

        //otwarcie aktywności gry i zamknięcie poprzednich
        startActivity(Intent(this, GameActivity::class.java))
        sendBroadcast(Intent("finish_activity_menu"))
        finish()
    }
    //endregion
}