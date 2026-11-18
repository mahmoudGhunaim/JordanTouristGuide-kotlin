package edu.zu.jordantouristguide.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.zu.jordantouristguide.BuildConfig
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.ui.components.ErrorState
import edu.zu.jordantouristguide.ui.components.LoadingState
import edu.zu.jordantouristguide.ui.components.PlaceImage
import edu.zu.jordantouristguide.ui.components.PlaceListItem
import edu.zu.jordantouristguide.ui.components.RatingBadge
import edu.zu.jordantouristguide.ui.util.isArabic
import edu.zu.jordantouristguide.ui.util.labelRes
import edu.zu.jordantouristguide.viewmodel.MapViewModel
import edu.zu.jordantouristguide.viewmodel.UiState

/** Centre of Jordan, zoomed to show the whole country. */
private val JORDAN_CENTER = LatLng(31.1, 36.0)
private const val JORDAN_ZOOM = 6.6f

@Composable
fun MapScreen(
    onPlaceClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        when (val places = state.places) {
            is UiState.Loading -> LoadingState(Modifier.statusBarsPadding())
            is UiState.Error -> ErrorState(onRetry = viewModel::load, modifier = Modifier.statusBarsPadding())
            is UiState.Success -> if (BuildConfig.MAPS_KEY_CONFIGURED) {
                PlacesMap(
                    places = places.data,
                    onMarkerClick = viewModel::selectPlace,
                    onMapClick = { viewModel.selectPlace(null) },
                )
            } else {
                // Without an API key Google Maps shows a blank grid, so list the places instead.
                NoMapKeyPlaceList(places = places.data, onPlaceClick = onPlaceClick)
            }
        }

        // Keep the last tapped place so the card still has content while it slides out.
        var lastSelected by remember { mutableStateOf<Place?>(null) }
        state.selectedPlace?.let { lastSelected = it }

        AnimatedVisibility(
            visible = state.selectedPlace != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            val place = lastSelected ?: return@AnimatedVisibility
            SelectedPlaceCard(
                place = place,
                onDetails = { onPlaceClick(place.id) },
                onClose = { viewModel.selectPlace(null) },
            )
        }
    }
}

@Composable
private fun PlacesMap(
    places: List<Place>,
    onMarkerClick: (Place) -> Unit,
    onMapClick: () -> Unit,
) {
    val arabic = isArabic()
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(JORDAN_CENTER, JORDAN_ZOOM)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        onMapClick = { onMapClick() },
    ) {
        places.forEach { place ->
            Marker(
                state = remember(place.id) { MarkerState(LatLng(place.latitude, place.longitude)) },
                title = place.name(arabic),
                icon = BitmapDescriptorFactory.defaultMarker(place.category.markerHue()),
                onClick = {
                    onMarkerClick(place)
                    true // we show our own card instead of the default info window
                },
            )
        }
    }
}

@Composable
private fun SelectedPlaceCard(place: Place, onDetails: () -> Unit, onClose: () -> Unit) {
    val arabic = isArabic()
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PlaceImage(
                place.coverImage,
                place.category,
                contentDescription = null,
                modifier = Modifier.size(84.dp).clip(MaterialTheme.shapes.medium),
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    place.name(arabic),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(place.category.labelRes()) + " · " + stringResource(place.governorate.labelRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    RatingBadge(place.rating)
                }
                Button(onClick = onDetails, modifier = Modifier.padding(top = 6.dp)) {
                    Text(stringResource(R.string.map_view_details))
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.map_close_card))
            }
        }
    }
}

@Composable
private fun NoMapKeyPlaceList(places: List<Place>, onPlaceClick: (String) -> Unit) {
    val arabic = isArabic()
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.map_no_key),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
        items(places.sortedBy { it.governorate.ordinal }, key = { it.id }) { place ->
            PlaceListItem(
                title = place.name(arabic),
                governorate = place.governorate,
                category = place.category,
                imageUrl = place.coverImage,
                rating = place.rating,
                onClick = { onPlaceClick(place.id) },
            )
        }
    }
}

/** Marker colour per category, so the map is easy to read. */
private fun Category.markerHue(): Float = when (this) {
    Category.HISTORICAL -> BitmapDescriptorFactory.HUE_RED
    Category.NATURE -> BitmapDescriptorFactory.HUE_GREEN
    Category.RELIGIOUS -> BitmapDescriptorFactory.HUE_VIOLET
    Category.ENTERTAINMENT -> BitmapDescriptorFactory.HUE_AZURE
    Category.RESTAURANTS -> BitmapDescriptorFactory.HUE_ORANGE
}
