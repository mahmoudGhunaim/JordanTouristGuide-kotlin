package edu.zu.jordantouristguide.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.data.model.Governorate
import edu.zu.jordantouristguide.ui.components.CategoryChips
import edu.zu.jordantouristguide.ui.components.EmptyState
import edu.zu.jordantouristguide.ui.components.ErrorState
import edu.zu.jordantouristguide.ui.components.LoadingState
import edu.zu.jordantouristguide.ui.components.PlaceListItem
import edu.zu.jordantouristguide.ui.util.isArabic
import edu.zu.jordantouristguide.ui.util.labelRes
import edu.zu.jordantouristguide.viewmodel.SearchViewModel
import edu.zu.jordantouristguide.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPlaceClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val arabic = isArabic()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var autoFocused by rememberSaveable { mutableStateOf(false) }

    // Open the keyboard once when the user arrives from the Home search bar.
    LaunchedEffect(Unit) {
        if (!autoFocused && state.category == null) {
            focusRequester.requestFocus()
            autoFocused = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.search_clear))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .focusRequester(focusRequester),
            )

            CategoryChips(
                selected = state.category,
                onSelect = viewModel::onCategoryChange,
                modifier = Modifier.padding(top = 12.dp),
            )

            GovernorateFilter(
                selected = state.governorate,
                options = state.governorates,
                onSelect = viewModel::onGovernorateChange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            Box(Modifier.weight(1f)) {
                when (val results = state.results) {
                    is UiState.Loading -> LoadingState()
                    is UiState.Error -> ErrorState(onRetry = viewModel::load)
                    is UiState.Success -> if (results.data.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.SearchOff,
                            title = stringResource(R.string.search_empty_title),
                            message = stringResource(R.string.search_empty_message),
                            actionLabel = if (state.hasFilters) stringResource(R.string.search_reset_filters) else null,
                            onAction = viewModel::resetFilters,
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            item {
                                Text(
                                    stringResource(R.string.search_results, results.data.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            items(results.data, key = { it.id }) { place ->
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
                }
            }
        }
    }
}

/** Chip that opens a dropdown of governorates. */
@Composable
private fun GovernorateFilter(
    selected: Governorate?,
    options: List<Governorate>,
    onSelect: (Governorate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = {
                Text(
                    if (selected == null) stringResource(R.string.search_all_governorates)
                    else stringResource(selected.labelRes()),
                )
            },
            leadingIcon = { Icon(Icons.Outlined.LocationCity, contentDescription = null, Modifier.size(18.dp)) },
            trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.search_all_governorates)) },
                onClick = { onSelect(null); expanded = false },
            )
            options.forEach { governorate ->
                DropdownMenuItem(
                    text = { Text(stringResource(governorate.labelRes())) },
                    onClick = { onSelect(governorate); expanded = false },
                )
            }
        }
    }
}
