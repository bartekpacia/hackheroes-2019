package pl.baftek.hackheroes_2019.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VisionLabelTagged(
    var text: String,
    val confidence: Float,
    val entityId: String?,
    val material: Material
) : Parcelable