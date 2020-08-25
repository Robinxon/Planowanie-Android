package pl.robinxon.planowanie

import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import pl.robinxon.planowanie.R
import kotlinx.android.synthetic.main.activity_stats.*

class StatsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        actionBar?.setDisplayHomeAsUpEnabled(true);

        addPlayerStats()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addPlayerStats() {
        //stałe
        val dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8F, resources.displayMetrics).toInt()

        for (i in 1..20){
            //player layout
            val player1Layout = LinearLayout(this)
            player1Layout.setPadding(dp8, dp8, dp8, dp8)
            player1Layout.setBackgroundResource(R.drawable.tv_border)
            val paramsLayout: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            paramsLayout.setMargins(0, 0, 0, dp8)
            player1Layout.layoutParams = paramsLayout

            //text view
            val textPlayerName = TextView(this)
            val paramsText: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textPlayerName.layoutParams = paramsText
            textPlayerName.setTextColor(resources.getColor(R.color.black))
            textPlayerName.text = "test"

            //dadanie do layoutu głównego
            player1Layout.addView(textPlayerName)
            usersStats.addView(player1Layout)
        }
    }

}
