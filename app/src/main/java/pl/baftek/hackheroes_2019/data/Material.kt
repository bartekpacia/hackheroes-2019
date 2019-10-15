package pl.baftek.hackheroes_2019.data

enum class Material(val type: String, val decayTime: String) {
    UNKNOWN("Nieznany", "-"),
    PLASTIC("Plastik", "100-1000 lat"),
    PAPER("Papier", "2-6 tygodni")
}