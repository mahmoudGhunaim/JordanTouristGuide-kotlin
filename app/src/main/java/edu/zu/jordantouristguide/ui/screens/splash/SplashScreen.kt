package edu.zu.jordantouristguide.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.zu.jordantouristguide.R
import edu.zu.jordantouristguide.ui.theme.DesertGold
import edu.zu.jordantouristguide.ui.theme.PetraRose
import edu.zu.jordantouristguide.ui.theme.PetraRoseDark
import edu.zu.jordantouristguide.viewmodel.SplashViewModel
import edu.zu.jordantouristguide.viewmodel.StartDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/** Branded splash: logo animates in, then we move to Onboarding / Login / Home. */
@Composable
fun SplashScreen(
    onFinished: (StartDestination) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700))
        delay(600)
        // Wait until the ViewModel has decided where to go, then leave the splash.
        onFinished(viewModel.destination.filterNotNull().first())
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetraRose, PetraRoseDark, Color(0xFF5C2418)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(progress.value).scale(0.85f + 0.15f * progress.value),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(180.dp),
            )
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = DesertGold,
            )
        }
    }
}
