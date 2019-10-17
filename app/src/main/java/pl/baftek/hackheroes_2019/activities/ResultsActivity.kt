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

private const val TAG = "ResultsActivity"

// Jeśli istnieje konkurs na najdurniejszy sposób działania
// Wygrałem

class ResultsActivity : AppCompatActivity() {

    private val plasticRegex: Array<String> = arrayOf("Plastic bottle", "Water bottle", "Water", "Bottle")
    private val paperRegex: Array<String> =
        arrayOf("Paper", "Cardboard", "Text", "Notebook", "Drawing", "Paper product")
    private val glassRegex: Array<String> = arrayOf("Glass bottle")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val results = intent?.getParcelableArrayListExtra<VisionLabel>("results")

        if (results == null) {
            Toast.makeText(this, "results null", LENGTH_SHORT).show()
            finish()
            return
        }

        var plastics = 0
        var papers = 0
        var glasses = 0

        plasticRegex.forEach { regex ->
            results.forEach {
                if (it.text.contains(Regex(regex))) {
                    plastics++
                }
            }
        }

        paperRegex.forEach { regex ->
            results.forEach {
                if (it.text.contains(Regex(regex))) {
                    papers++
                }
            }
        }

        glassRegex.forEach { regex ->
            results.forEach {
                if (it.text.contains(Regex(regex))) {
                    glasses++
                }
            }
        }

        var material: Material = Material.UNKNOWN

        if (plastics > papers && plastics > glasses) material = Material.PLASTIC
        if (papers > plastics && papers > glasses) material = Material.PAPER
        if (glasses > plastics && glasses > papers) material = Material.GLASS

        Log.d(TAG, "plastics: $plastics papers: $papers")


        textMaterialType.text = material.type
        textDecayTime.text = "Czas rozkładu: " + material.decayTime

        Glide.with(this).load(material.image).into(imageRecycleBin)
    }
}