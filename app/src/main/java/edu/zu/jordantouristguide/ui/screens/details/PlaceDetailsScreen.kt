package edu.zu.jordantouristguide.ui.screens.details

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.data.model.Place
import edu.zu.jordantouristguide.data.model.Review
import edu.zu.jordantouristguide.ui.components.EmptyState
import edu.zu.jordantouristguide.ui.components.ErrorState
import edu.zu.jordantouristguide.ui.components.LoadingState
import edu.zu.jordantouristguide.ui.components.PlaceImage
import edu.zu.jordantouristguide.ui.components.RatingStars
import edu.zu.jordantouristguide.ui.util.formatPrice
import edu.zu.jordantouristguide.ui.util.formatRating
import edu.zu.jordantouristguide.ui.util.icon
import edu.zu.jordantouristguide.ui.util.isArabic
import edu.zu.jordantouristguide.ui.util.labelRes
import edu.zu.jordantouristguide.ui.util.openInGoogleMaps
import edu.zu.jordantouristguide.viewmodel.PlaceDetailsUiState
import edu.zu.jordantouristguide.viewmodel.PlaceDetailsViewModel
import edu.zu.jordantouristguide.viewmodel.UiState
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun PlaceDetailsScreen(
    onBack: () -> Unit,
    onWriteReview: (String) -> Unit,
    onLoginRequired: () -> Unit,
    viewModel: PlaceDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showLoginDialog by rememberSaveable { mutableStateOf(false) }

    val savedMessage = stringResource(R.string.details_saved)
    val removedMessage = stringResource(R.string.details_unsaved)

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            when (val place = state.place) {
                is UiState.Loading -> LoadingState()
                is UiState.Error -> Column(Modifier.statusBarsPadding()) {
                    BackButton(onBack, Modifier.padding(8.dp))
                    if (state.isNotFound) {
                        EmptyState(
                            icon = Icons.Outlined.SearchOff,
                            title = stringResource(R.string.search_empty_title),
                            message = stringResource(R.string.details_not_found),
                        )
                    } else {
                        ErrorState(onRetry = viewModel::load)
                    }
                }
                is UiState.Success -> DetailsContent(
                    place = place.data,
                    state = state,
                    onBack = onBack,
                    onToggleFavorite = {
                        val message = if (state.isFavorite) removedMessage else savedMessage
                        viewModel.toggleFavorite()
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onWriteReview = {
                        if (state.isLoggedIn) onWriteReview(place.data.id) else showLoginDialog = true
                    },
                    onMapsUnavailable = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
                )
            }
        }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text(stringResource(R.string.login_required_title)) },
            text = { Text(stringResource(R.string.login_required_message)) },
            confirmButton = {
                Button(onClick = { showLoginDialog = false; onLoginRequired() }) { Text(stringResource(R.string.auth_login)) }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun DetailsContent(
    place: Place,
    state: PlaceDetailsUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWriteReview: () -> Unit,
    onMapsUnavailable: (String) -> Unit,
) {
    val arabic = isArabic()
    val context = LocalContext.current
    val noMapsMessage = stringResource(R.string.details_no_maps_app)
    val title = place.name(arabic)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item(key = "gallery") {
            ImageGallery(place, title, state.isFavorite, onBack, onToggleFavorite)
        }

        item(key = "title") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(place.category.labelRes())) },
                        leadingIcon = { Icon(place.category.icon, contentDescription = null, Modifier.size(18.dp)) },
                    )
                    AssistChip(onClick = {}, label = { Text(stringResource(place.governorate.labelRes())) })
                }
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(place.rating)
                    Spacer(Modifier.width(8.dp))
                    Text(formatRating(place.rating), style = MaterialTheme.typography.titleMedium)
                    state.reviews?.let {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.details_reviews_count, it.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item(key = "info") {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.details_hours),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(place.openingHours, style = MaterialTheme.typography.bodyMedium)
                }
                InfoCard(
                    icon = Icons.Outlined.ConfirmationNumber,
                    title = stringResource(R.string.details_tickets),
                    modifier = Modifier.weight(1f),
                ) {
                    val foreign = place.ticketPrice.foreign
                    if (foreign == null) {
                        PriceLine(stringResource(R.string.details_ticket_everyone), formatPrice(place.ticketPrice.local))
                    } else {
                        PriceLine(stringResource(R.string.details_ticket_local), formatPrice(place.ticketPrice.local))
                        PriceLine(stringResource(R.string.details_ticket_foreign), formatPrice(foreign))
                    }
                }
            }
        }

        item(key = "about") {
            Section(stringResource(R.string.details_about)) {
                Text(place.description(arabic), style = MaterialTheme.typography.bodyLarge)
            }
        }

        item(key = "location") {
            Section(stringResource(R.string.details_location)) {
                FilledTonalButton(
                    onClick = {
                        if (!openInGoogleMaps(context, place.latitude, place.longitude, place.nameEn)) {
                            onMapsUnavailable(noMapsMessage)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Outlined.Directions, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.details_open_in_maps))
                }
            }
        }

        item(key = "reviewsHeader") {
            HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.details_reviews),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onWriteReview) {
                    Icon(Icons.Outlined.RateReview, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.details_write_review))
                }
            }
        }

        val reviews = state.reviews
        when {
            reviews == null -> item(key = "reviewsLoading") {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            reviews.isEmpty() -> item(key = "reviewsEmpty") {
                Text(
                    stringResource(R.string.details_no_reviews),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
            else -> items(reviews, key = { it.id }) { review -> ReviewItem(review) }
        }
    }
}

@Composable
private fun ImageGallery(
    place: Place,
    title: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val images = place.imageUrls.ifEmpty { listOf<String?>(null) }
    val pagerState = rememberPagerState { images.size }

    Box(Modifier.fillMaxWidth().height(320.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            PlaceImage(images[page], place.category, contentDescription = title, modifier = Modifier.fillMaxSize())
        }
        // Darken the top so the buttons stay visible on bright photos.
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent))),
        )
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BackButton(onBack)
            IconButton(
                onClick = onToggleFavorite,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            ) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.details_remove_favorite else R.string.details_add_favorite,
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (images.size > 1) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(images.size) { index ->
                        Box(
                            Modifier
                                .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                                .background(
                                    if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.5f),
                                    CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onBack,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun PriceLine(label: String, price: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(price, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        content()
    }
}

@Composable
private fun ReviewItem(review: Review) {
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(review.timestamp, locale) {
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(review.timestamp))
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(
            Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                review.userName.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.userName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RatingStars(review.rating.toDouble(), starSize = 14.dp)
            Spacer(Modifier.height(4.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
