package pl.baftek.hackheroes_2019

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_results.*

class ResultsActivity : AppCompatActivity() {

    private var adapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val results = intent?.getStringArrayExtra("results")

        if (results != null) {
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, results)
            listResults.adapter = adapter
        } else {
            Toast.makeText(this, "results null", LENGTH_SHORT).show()
        }
    }
}
