package pl.robinxon.planowanie

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_new_game.*
import java.util.*

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
        fireMatch = fireDatabase.getReference(Constants.FIRE_MATCH)

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
            Toast.makeText(
                this,
                resources.getString(R.string.input_all_player_names),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        //utworzenie obiektu meczu
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
            "radioButton1Round" -> {
                match.settingGames = 1
                match.roundsInGame = 16
            }
            "radioButton4Rounds" -> {
                match.settingGames = 4
                match.roundsInGame = 13
            }
        }

        //zapisanie nazw graczy
        match.playerNames?.set(1, editTextPlayer1Name.text.toString())
        match.playerNames?.set(2, editTextPlayer2Name.text.toString())
        if(match.settingPlayers == 4) {
            match.playerNames?.set(3, editTextPlayer3Name.text.toString())
            match.playerNames?.set(4, editTextPlayer4Name.text.toString())
        }

        //utworzenie gry
        match.games?.set(1, Game())
        match.games?.get(1)!!.players?.set(1, Player())
        match.games!![1].players?.set(2, Player())
        if(match.settingPlayers == 4) {
            match.games!![1].players?.set(3, Player())
            match.games!![1].players?.set(4, Player())
        }

        //ustawienie pierwszego gracza
        match.games!![1].currentPlayer = 1

        //ustawienie wstępnego atutu
        match.games!![1].atuts?.set(
            1, when (match.settingPlayers) {
                2 -> {
                    when ((1..4).random()) {
                        1 -> 1
                        2 -> 2
                        3 -> 3
                        4 -> 4
                        else -> 0
                    }
                }
                4 -> 0
                else -> 0
            }
        )

        //ustawienie daty startu gry
        val calendar: Calendar = GregorianCalendar()
        val trialTime = Date()
        calendar.time = trialTime
        match.date = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "-" + String.format("%02d", calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR).toString() + " " + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE))

        //przekonwertowanie meczu do json i zapisanie w bazie
        fireMatch.setValue(match)

        //otwarcie aktywności gry i zamknięcie poprzednich
        startActivity(Intent(this, GameActivity::class.java))
        sendBroadcast(Intent("finish_activity_menu"))
        finish()
    }
    //endregion
}