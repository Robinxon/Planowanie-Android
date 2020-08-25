package pl.robinxon.planowanie

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import pl.robinxon.planowanie.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
        buttonPlay.setOnClickListener { startNewGame()}
    }

    //region Overridy funkcji
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    //endregion

    //region Funkcje przycisków
    private fun startNewGame() {
        /*//inicjacja parsera GSON, bazy danych
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
        match.game1?.player1 = Player()
        match.game1?.player2 = Player()
        if(match.settingPlayers == 4) {
            match.game1?.player3 = Player()
            match.game1?.player4 = Player()
        }

        if(match.settingGames == 4) {
            match.game2 = Game()
            match.game2?.player1 = Player()
            match.game2?.player2 = Player()
            if(match.settingPlayers == 4) {
                match.game2?.player3 = Player()
                match.game2?.player4 = Player()
            }

            match.game3 = Game()
            match.game3?.player1 = Player()
            match.game3?.player2 = Player()
            if(match.settingPlayers == 4) {
                match.game3?.player3 = Player()
                match.game3?.player4 = Player()
            }

            match.game4 = Game()
            match.game4?.player1 = Player()
            match.game4?.player2 = Player()
            if(match.settingPlayers == 4) {
                match.game4?.player3 = Player()
                match.game4?.player4 = Player()
            }
        }

        match.game1?.currentPlayer = 1
        match.date = java.util.Calendar.getInstance()

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Match", match)
        startActivity(intent)
        finish()*/
    }
    //endregion
}