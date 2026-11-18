package edu.zu.jordantouristguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.ui.util.icon

/**
 * Loads a place photo with Coil. While loading (or if the image fails, e.g.
 * offline) it shows a sand-to-rose gradient with the category icon instead
 * of an empty box.
 */
@Composable
fun PlaceImage(
    url: String?,
    category: Category,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { ImageFallback(category) },
        error = { ImageFallback(category) },
    )
}

@Composable
private fun ImageFallback(category: Category) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(colors.tertiaryContainer, colors.primaryContainer))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = colors.primary.copy(alpha = 0.55f),
            modifier = Modifier.size(40.dp),
        )
    }
}
