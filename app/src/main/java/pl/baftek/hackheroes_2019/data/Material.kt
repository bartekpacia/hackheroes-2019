package pl.baftek.hackheroes_2019.data

import androidx.annotation.DrawableRes
import pl.baftek.hackheroes_2019.R

enum class Material(val type: String, val decayTime: String, @DrawableRes val image: Int) {
    UNKNOWN("Nieznany", "-", R.drawable.question_mark),
    PLASTIC("Plastik", "100-1000 lat", R.drawable.yellow_bin),
    PAPER("Papier", "2-6 tygodni", R.drawable.blue_bin),
    GLASS("Szkło", "nigdy", R.drawable.green_bin)
}