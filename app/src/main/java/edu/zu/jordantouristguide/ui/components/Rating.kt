package edu.zu.jordantouristguide.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.ui.theme.StarGold
import edu.zu.jordantouristguide.ui.util.formatRating
import kotlin.math.floor
import kotlin.math.round

/** Small "★ 4.8" badge used on cards. */
@Composable
fun RatingBadge(rating: Double, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(16.dp))
        Text(formatRating(rating), style = MaterialTheme.typography.labelMedium)
    }
}

/** Read-only row of 5 stars (supports half stars). */
@Composable
fun RatingStars(rating: Double, modifier: Modifier = Modifier, starSize: Dp = 18.dp) {
    Row(modifier) {
        // Round to the nearest half star: 4.9 -> 5, 4.6 -> 4.5, 4.2 -> 4.
        val rounded = round(rating * 2) / 2
        val full = floor(rounded).toInt()
        val hasHalf = rounded - full >= 0.5
        repeat(5) { index ->
            val icon = when {
                index < full -> Icons.Filled.Star
                index == full && hasHalf -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            Icon(icon, contentDescription = null, tint = StarGold, modifier = Modifier.size(starSize))
        }
    }
}

/** Tappable 1..5 star picker for the Add Review screen. */
@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (star in 1..5) {
            val label = stringResource(R.string.review_star, star)
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = label,
                tint = StarGold,
                modifier = Modifier
                    .size(48.dp)
                    .selectable(selected = star == rating, role = Role.RadioButton) { onRatingChange(star) },
            )
        }
    }
}
