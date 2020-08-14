package com.example.planowanie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        actionBar?.setDisplayHomeAsUpEnabled(true);
        intent.getIntExtra("currentGame", 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}