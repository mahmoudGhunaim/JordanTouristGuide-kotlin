package edu.zu.jordantouristguide.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.ui.components.EmptyState
import edu.zu.jordantouristguide.ui.components.ErrorState
import edu.zu.jordantouristguide.ui.components.LoadingState
import edu.zu.jordantouristguide.ui.components.PlaceListItem
import edu.zu.jordantouristguide.ui.util.isArabic
import edu.zu.jordantouristguide.viewmodel.FavoritesViewModel
import edu.zu.jordantouristguide.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onPlaceClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val arabic = isArabic()

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.favorites_title)) }) }) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val favorites = state) {
            is UiState.Loading -> LoadingState(modifier)
            // Room errors are very rare; "retry" simply re-opens the tab.
            is UiState.Error -> ErrorState(onRetry = onExploreClick, modifier = modifier)
            is UiState.Success -> if (favorites.data.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = stringResource(R.string.favorites_empty_title),
                    message = stringResource(R.string.favorites_empty_message),
                    actionLabel = stringResource(R.string.favorites_explore),
                    onAction = onExploreClick,
                    modifier = modifier,
                )
            } else {
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(favorites.data, key = { it.placeId }) { favorite ->
                        PlaceListItem(
                            title = favorite.name(arabic),
                            governorate = favorite.governorate,
                            category = favorite.category,
                            imageUrl = favorite.imageUrl,
                            rating = favorite.rating,
                            onClick = { onPlaceClick(favorite.placeId) },
                            modifier = Modifier.animateItem(),
                            trailing = {
                                IconButton(onClick = { viewModel.removeFavorite(favorite.placeId) }) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = stringResource(R.string.favorites_remove),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
