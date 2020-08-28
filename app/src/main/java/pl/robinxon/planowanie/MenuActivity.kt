package pl.robinxon.planowanie

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.android.synthetic.main.activity_menu.*
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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
        try { Firebase.database.setPersistenceEnabled(true) }
        catch (e: Exception) { Log.d("database", "persistence already enabled")}
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
                Log.d("database_test", "Match is: $value")
                if (!value.isNullOrEmpty()) {
                    loadedMatch = decodeJsonToMatch(value)
                    if(!loadedMatch!!.ended) {
                        menuContinue.visibility = View.VISIBLE
                        menuContinueDescription.text = getString(R.string.saved_match_description, loadedMatch!!.date.get(Calendar.DAY_OF_MONTH).toString(), loadedMatch!!.date.get(Calendar.MONTH).toString(), loadedMatch!!.date.get(Calendar.YEAR).toString(), loadedMatch!!.date.get(Calendar.HOUR_OF_DAY).toString(), String.format("%02d", loadedMatch!!.date.get(Calendar.MINUTE)), loadedMatch!!.playerNames[1], loadedMatch!!.playerNames[2], (loadedMatch!!.playerNames[3] ?: ""), (loadedMatch!!.playerNames[4] ?: ""))
                    } else {
                        menuContinue.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("database_test", "Failed to read value Match.", error.toException())
            }
        })

        //nasłuchiwanie na zakończenie aktywności
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish_activity_menu") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish_activity_menu"))
    }

    //region Funkcje przycisków
    private fun menuContinue() {
        startActivity(Intent(this, GameActivity::class.java))
    }

    private fun menuNew() {
        if(loadedMatch != null && !loadedMatch!!.ended){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.do_you_really_want_to_overwrite_game))
            builder.setMessage(resources.getString(R.string.overwritting_confirmation_text))

            builder.setPositiveButton(resources.getString(R.string.yes)) { _, _ -> //nadpisanie starej gry
                startActivity(Intent(this, NewGameActivity::class.java))
            }

            builder.setNegativeButton(resources.getString(R.string.no)) { _, _ -> //kontynuowanie starej gry
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