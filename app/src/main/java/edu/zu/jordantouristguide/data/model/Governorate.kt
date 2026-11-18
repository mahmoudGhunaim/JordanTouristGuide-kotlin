package edu.zu.jordantouristguide.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The 12 governorates of Jordan. Display names live in strings.xml (see ui/util/Labels.kt). */
@Serializable
enum class Governorate(val key: String) {
    @SerialName("Amman") AMMAN("Amman"),
    @SerialName("Irbid") IRBID("Irbid"),
    @SerialName("Zarqa") ZARQA("Zarqa"),
    @SerialName("Balqa") BALQA("Balqa"),
    @SerialName("Madaba") MADABA("Madaba"),
    @SerialName("Mafraq") MAFRAQ("Mafraq"),
    @SerialName("Jerash") JERASH("Jerash"),
    @SerialName("Ajloun") AJLOUN("Ajloun"),
    @SerialName("Karak") KARAK("Karak"),
    @SerialName("Tafilah") TAFILAH("Tafilah"),
    @SerialName("Maan") MAAN("Maan"),
    @SerialName("Aqaba") AQABA("Aqaba");

    companion object {
        fun fromKey(key: String?): Governorate? = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}
