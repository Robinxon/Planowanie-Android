package com.example.planowanie

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        menuContinue.setBackgroundResource(R.drawable.tv_border)
        menuNew.setBackgroundResource(R.drawable.tv_border)

        menuContinue.setOnClickListener {
            Toast.makeText(this, "ok cont", Toast.LENGTH_SHORT ).show()
        }

        menuNew.setOnClickListener {
            val intent = Intent(this, NewGameActivity::class.java)
            startActivity(intent)
        }
    }
}