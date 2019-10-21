package pl.baftek.hackheroes_2019.data

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import pl.baftek.hackheroes_2019.R

enum class Material(val type: String, val decayTime: String, @DrawableRes val image: Int, @ColorRes val color: Int) {
    UNKNOWN("Nieznany", "-", R.drawable.unknown_bin, android.R.color.darker_gray),
    PLASTIC("Plastik", "100-1000 lat", R.drawable.yellow_bin, android.R.color.holo_orange_light),
    PAPER("Papier", "2-6 tygodni", R.drawable.blue_bin, android.R.color.holo_blue_light),
    GLASS("Szkło", "nigdy", R.drawable.green_bin, android.R.color.holo_green_dark)
}