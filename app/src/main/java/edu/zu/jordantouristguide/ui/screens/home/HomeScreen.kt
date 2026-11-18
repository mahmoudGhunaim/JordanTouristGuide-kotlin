package edu.zu.jordantouristguide.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.data.model.Category
import edu.zu.jordantouristguide.ui.components.CategoryChips
import edu.zu.jordantouristguide.ui.components.EmptyState
import edu.zu.jordantouristguide.ui.components.ErrorState
import edu.zu.jordantouristguide.ui.components.FeaturedPlaceCard
import edu.zu.jordantouristguide.ui.components.LoadingState
import edu.zu.jordantouristguide.ui.components.PlaceGridCard
import edu.zu.jordantouristguide.ui.theme.DesertGoldLight
import edu.zu.jordantouristguide.ui.theme.PetraRose
import edu.zu.jordantouristguide.ui.theme.PetraRoseDark
import edu.zu.jordantouristguide.ui.util.isArabic
import edu.zu.jordantouristguide.viewmodel.HomeViewModel
import edu.zu.jordantouristguide.viewmodel.UiState

@Composable
fun HomeScreen(
    onPlaceClick: (String) -> Unit,
    onSearchClick: (Category?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val arabic = isArabic()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            HomeHeader(userName = state.userName, onSearchClick = { onSearchClick(null) })
        }
        item(key = "chips") {
            CategoryChips(selected = state.selectedCategory, onSelect = viewModel::selectCategory)
        }

        when (val content = state.content) {
            is UiState.Loading -> item(key = "loading") { LoadingState(Modifier.height(320.dp)) }
            is UiState.Error -> item(key = "error") {
                ErrorState(onRetry = { viewModel.loadPlaces(forceRefresh = true) }, modifier = Modifier.height(380.dp))
            }
            is UiState.Success -> {
                val data = content.data
                if (state.selectedCategory == null && data.topRated.isNotEmpty()) {
                    item(key = "topRatedTitle") { SectionTitle(stringResource(R.string.home_top_rated)) }
                    item(key = "topRated") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(data.topRated, key = { it.id }) { place ->
                                FeaturedPlaceCard(
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
                }

                item(key = "allTitle") {
                    SectionTitle(
                        title = stringResource(R.string.home_all_places),
                        action = stringResource(R.string.see_all),
                        onAction = { onSearchClick(state.selectedCategory) },
                    )
                }

                if (data.places.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            icon = Icons.Outlined.TravelExplore,
                            title = stringResource(R.string.search_empty_title),
                            message = stringResource(R.string.home_empty),
                            modifier = Modifier.height(300.dp),
                        )
                    }
                } else {
                    // A simple 2-column grid: each list row holds two cards.
                    items(data.places.chunked(2), key = { row -> row.first().id }) { row ->
                        Row(
                            Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            row.forEach { place ->
                                PlaceGridCard(
                                    title = place.name(arabic),
                                    governorate = place.governorate,
                                    category = place.category,
                                    imageUrl = place.coverImage,
                                    rating = place.rating,
                                    onClick = { onPlaceClick(place.id) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/** Rose-red hero with greeting and a search field that opens the Search screen. */
@Composable
private fun HomeHeader(userName: String?, onSearchClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    // Same rose-red in light and dark theme so the white text always reads well.
                    Brush.linearGradient(listOf(PetraRose, PetraRoseDark)),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                )
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 52.dp),
        ) {
            Text(
                if (userName != null) stringResource(R.string.home_greeting_user, userName)
                else stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.titleMedium,
                color = DesertGoldLight,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
        }
        Surface(
            onClick = onSearchClick,
            shape = RoundedCornerShape(50),
            color = colors.surfaceContainerLowest,
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 26.dp)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.home_search_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
    Spacer(Modifier.height(36.dp))
}

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}
