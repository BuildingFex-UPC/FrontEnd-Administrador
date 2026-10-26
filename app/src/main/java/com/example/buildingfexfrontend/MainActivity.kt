package com.example.buildingfexfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.components.AppButton
import com.example.buildingfexfrontend.iam.presentation.AuthScreen
import com.example.buildingfexfrontend.shell.presentation.AppShell
import com.example.buildingfexfrontend.ui.theme.BuildingFexFrontEndTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = BuildingFexApp.from(this).container
        setContent {
            BuildingFexFrontEndTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot(container)
                }
            }
        }
    }
}

/** Routes between login, the admin shell and the resident-app blocker. */
@Composable
private fun AppRoot(container: AppContainer) {
    val session by container.session.session.collectAsStateWithLifecycle()
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        when {
            session == null -> AuthScreen(container)
            // Residents now live in the Flutter resident app: block the session here.
            session?.profile?.isResident == true -> ResidentBlockedScreen(
                onLogout = { container.session.clear() },
            )
            else -> AppShell(container)
        }
    }
}

@Composable
private fun ResidentBlockedScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_buildingfex),
            contentDescription = "BuildingFex",
            modifier = Modifier
                .width(200.dp)
                .height(61.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = string("err.RESIDENT_LOGIN_BLOCKED"),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        AppButton(text = string("set.session.logout"), onClick = onLogout, destructive = true)
    }
}
