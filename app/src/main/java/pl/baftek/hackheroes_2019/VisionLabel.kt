package pl.baftek.hackheroes_2019

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VisionLabel(
    var text: String,
    val confidence: Float,
    val entityId: String?
) : Parcelable