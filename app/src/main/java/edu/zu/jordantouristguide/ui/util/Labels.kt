package edu.zu.jordantouristguide.ui.util

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Attractions
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.Governorate
import java.text.NumberFormat

/** True when the app is currently shown in Arabic. Used to pick nameAr/nameEn. */
@Composable
@ReadOnlyComposable
fun isArabic(): Boolean = LocalConfiguration.current.locales[0].language == "ar"

@StringRes
fun Category.labelRes(): Int = when (this) {
    Category.HISTORICAL -> R.string.category_historical
    Category.NATURE -> R.string.category_nature
    Category.RELIGIOUS -> R.string.category_religious
    Category.ENTERTAINMENT -> R.string.category_entertainment
    Category.RESTAURANTS -> R.string.category_restaurants
}

val Category.icon: ImageVector
    get() = when (this) {
        Category.HISTORICAL -> Icons.Outlined.AccountBalance
        Category.NATURE -> Icons.Outlined.Landscape
        Category.RELIGIOUS -> Icons.Outlined.Mosque
        Category.ENTERTAINMENT -> Icons.Outlined.Attractions
        Category.RESTAURANTS -> Icons.Outlined.Restaurant
    }

@StringRes
fun Governorate.labelRes(): Int = when (this) {
    Governorate.AMMAN -> R.string.gov_amman
    Governorate.IRBID -> R.string.gov_irbid
    Governorate.ZARQA -> R.string.gov_zarqa
    Governorate.BALQA -> R.string.gov_balqa
    Governorate.MADABA -> R.string.gov_madaba
    Governorate.MAFRAQ -> R.string.gov_mafraq
    Governorate.JERASH -> R.string.gov_jerash
    Governorate.AJLOUN -> R.string.gov_ajloun
    Governorate.KARAK -> R.string.gov_karak
    Governorate.TAFILAH -> R.string.gov_tafilah
    Governorate.MAAN -> R.string.gov_maan
    Governorate.AQABA -> R.string.gov_aqaba
}

/** "50 JOD" / "٥٠ دينار", or "Free". */
@Composable
fun formatPrice(amount: Double): String {
    if (amount == 0.0) return stringResource(R.string.free)
    val format = NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0]).apply {
        maximumFractionDigits = 2
    }
    return stringResource(R.string.price_jod, format.format(amount))
}

/** Rating with one decimal, using the current locale's digits. */
@Composable
fun formatRating(rating: Double): String =
    NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0]).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(rating)
