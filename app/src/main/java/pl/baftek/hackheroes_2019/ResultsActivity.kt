package pl.baftek.hackheroes_2019

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_results.*

class ResultsActivity : AppCompatActivity() {

    private var adapter: ArrayAdapter<String>? = null

    private val plasticRegex = Regex("Plastic bottle|Water bottle|Water")
    // TODO: private val paperRegex = Regex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val results = intent?.getParcelableArrayListExtra<VisionLabel>("results")

        if (results == null) {
            Toast.makeText(this, "results null", LENGTH_SHORT).show()
            finish()
            return
        }

        adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, results.map { visionLabel -> visionLabel.text })
        listResults.adapter = adapter


        val matches: ArrayList<VisionLabel> = arrayListOf()

        for (result in results) {
            if (plasticRegex.containsMatchIn(result.text)) {

                result.text = "${result.text} ${result.confidence} (regex)"
                matches.add(result)
            }
        }

        if (matches.isNotEmpty()) {
            adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, matches.map { visionLabel -> visionLabel.text })
            listResults.adapter = adapter
        }
    }
}