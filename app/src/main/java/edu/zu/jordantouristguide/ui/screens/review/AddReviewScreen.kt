package edu.zu.jordantouristguide.ui.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.ui.components.EmptyState
import edu.zu.jordantouristguide.ui.components.LoadingState
import edu.zu.jordantouristguide.ui.components.StarRatingInput
import edu.zu.jordantouristguide.ui.util.isArabic
import edu.zu.jordantouristguide.viewmodel.AddReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    onBack: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AddReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val postedMessage = stringResource(R.string.review_posted)
    val placeName = if (isArabic()) state.placeNameAr else state.placeName

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            Toast.makeText(context, postedMessage, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.review_title))
                        placeName?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when {
            state.isCheckingUser -> LoadingState(modifier)

            // Guests are asked to log in instead of seeing the form.
            state.user == null -> EmptyState(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.review_guest_title),
                message = stringResource(R.string.review_guest_message),
                actionLabel = stringResource(R.string.auth_login),
                onAction = onLoginClick,
                modifier = modifier,
            )

            else -> Column(
                modifier
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.review_rate_prompt), style = MaterialTheme.typography.titleLarge)
                StarRatingInput(rating = state.rating, onRatingChange = viewModel::onRatingChange)
                if (state.ratingError) {
                    Text(
                        stringResource(R.string.review_error_rating),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                OutlinedTextField(
                    value = state.comment,
                    onValueChange = viewModel::onCommentChange,
                    label = { Text(stringResource(R.string.review_comment)) },
                    placeholder = { Text(stringResource(R.string.review_comment_hint)) },
                    isError = state.commentError,
                    supportingText = {
                        Text(
                            if (state.commentError) stringResource(R.string.review_error_comment)
                            else "${state.comment.length} / ${AddReviewViewModel.MAX_COMMENT_LENGTH}",
                        )
                    },
                    minLines = 5,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                )

                if (state.saveError) {
                    Text(
                        stringResource(R.string.review_error_save),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    onClick = viewModel::submit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.review_submit))
                    }
                }
            }
        }
    }
}
