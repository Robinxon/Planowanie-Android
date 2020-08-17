package com.example.planowanie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class StatsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        actionBar?.setDisplayHomeAsUpEnabled(true);
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}