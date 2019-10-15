package pl.baftek.hackheroes_2019.activities

import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_results.*
import pl.baftek.hackheroes_2019.R
import pl.baftek.hackheroes_2019.data.Material
import pl.baftek.hackheroes_2019.data.VisionLabel
import pl.baftek.hackheroes_2019.data.VisionLabelTagged

class ResultsActivity : AppCompatActivity() {

    private val plasticRegex = Regex("Plastic bottle|Water bottle|Water|Bottle")
    private val paperRegex = Regex("Paper|Cardboard") // TODO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val results = intent?.getParcelableArrayListExtra<VisionLabel>("results")

        if (results == null) {
            Toast.makeText(this, "results null", LENGTH_SHORT).show()
            finish()
            return
        }


        val matches: ArrayList<VisionLabelTagged> = arrayListOf()

        for (result in results) {

            var material: Material = Material.UNKNOWN

            // Plastic
            if (plasticRegex.containsMatchIn(result.text)) {
                material = Material.PLASTIC
            }

            // Paper
            if (paperRegex.containsMatchIn(result.text)) {
                material = Material.PAPER
            }

            result.text = "${result.text} ${result.confidence} (regex)"

            val match = VisionLabelTagged(result.text, result.confidence, result.entityId, material)
            matches.add(match)
        }

        if (matches.isNotEmpty()) {
            val bestMatch = matches.maxBy { it.confidence } ?: return
            Toast.makeText(this, bestMatch.text, LENGTH_SHORT).show()

            textMaterialType.text = bestMatch.material.type
            textDecayTime.text = "Czas rozkładu: " + bestMatch.material.decayTime
        }
    }
}