package pl.baftek.hackheroes_2019.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_results.*
import pl.baftek.hackheroes_2019.R
import pl.baftek.hackheroes_2019.data.Material
import pl.baftek.hackheroes_2019.data.VisionLabel
import pl.baftek.hackheroes_2019.data.VisionLabelTagged

private const val TAG = "ResultsActivity"

class ResultsActivity : AppCompatActivity() {

    private val plasticRegex = Regex("Plastic bottle|Water bottle|Water|Bottle")
    private val paperRegex = Regex("Paper|Cardboard|Text|Notebook|Drawing|Paper product|") // TODO
    private val glassRegex = Regex("Glass bottle")

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

        fun tag(results: List<VisionLabel>): VisionLabelTagged? {
            for (result in results) {
                var plastic = plasticRegex.findAll(result.text, 0).toList().size
                var paper = paperRegex.findAll(result.text, 0).toList().size
                var glass = glassRegex.findAll(result.text, 0).toList().size

                Log.d(TAG, "plastic: $plastic, paper: $paper, glass: $glass")
            }

            /*
            result.text = "${result.text} ${result.confidence} (regex)"
            val match = VisionLabelTagged(result.text, result.confidence, result.entityId, material)
            matches.add(match)
             */

            return null
        }

        tag(results)

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

            if (glassRegex.containsMatchIn(result.text)) {
                material = Material.GLASS
            }

            result.text = "${result.text} ${result.confidence} (regex)"

            val match = VisionLabelTagged(result.text, result.confidence, result.entityId, material)
            matches.add(match)
        }

        if (matches.isNotEmpty()) {
            val bestMatch = matches.maxBy { it.confidence } ?: return


            matches.forEach {
                Log.d(TAG, it.toString())
            }
            Log.d(TAG, "Best match" + bestMatch)

            Toast.makeText(this, bestMatch.text, LENGTH_SHORT).show()

            textMaterialType.text = bestMatch.material.type
            textDecayTime.text = "Czas rozkładu: " + bestMatch.material.decayTime

            Glide.with(this).load(bestMatch.material.image).into(imageRecycleBin)
        }
    }
}