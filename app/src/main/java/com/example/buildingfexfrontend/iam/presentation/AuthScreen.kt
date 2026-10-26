package com.example.buildingfexfrontend.iam.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.buildingfexfrontend.R
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppButton
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.FormError
import com.example.buildingfexfrontend.core.ui.components.VerticalGap

@Composable
fun AuthScreen(container: AppContainer) {
    val viewModel: AuthViewModel = appViewModel { AuthViewModel(container.auth) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VerticalGap(24)
        Image(
            painter = painterResource(R.drawable.logo_buildingfex),
            contentDescription = "BuildingFex",
            modifier = Modifier
                .width(220.dp)
                .height(67.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
        )
        VerticalGap(16)
        Text(
            text = string("auth.tagline.main"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalGap(24)

        TabRow(selectedTabIndex = state.tab.ordinal) {
            Tab(
                selected = state.tab == AuthTab.LOGIN,
                onClick = { viewModel.onTabChange(AuthTab.LOGIN) },
                text = { Text(string("auth.tab.login")) },
            )
            Tab(
                selected = state.tab == AuthTab.REGISTER,
                onClick = { viewModel.onTabChange(AuthTab.REGISTER) },
                text = { Text(string("auth.tab.register")) },
            )
        }
        VerticalGap(20)
        if (state.tab == AuthTab.LOGIN) {
            LoginForm(state, viewModel)
        } else {
            RegisterForm(state, viewModel)
        }
    }
}

@Composable
private fun LoginForm(state: AuthUiState, viewModel: AuthViewModel) {
    Column {
        AppTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = string("auth.field.email"),
            isError = state.emailError != null,
            supportingText = state.emailError,
        )
        VerticalGap(12)
        AppTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = string("auth.field.password"),
            password = true,
            isError = state.passwordError != null,
            supportingText = state.passwordError,
        )
        state.error?.let {
            VerticalGap(8)
            FormError(it)
        }
        VerticalGap(20)
        AppButton(text = string("auth.button.login"), onClick = viewModel::login, loading = state.loading)
    }
}

@Composable
private fun RegisterForm(state: AuthUiState, viewModel: AuthViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(
            value = state.regName,
            onValueChange = { viewModel.onRegFieldChange("name", it) },
            label = string("auth.field.name"),
        )
        AppTextField(
            value = state.regEmail,
            onValueChange = { viewModel.onRegFieldChange("email", it) },
            label = string("auth.field.email"),
        )
        AppTextField(
            value = state.regPassword,
            onValueChange = { viewModel.onRegFieldChange("password", it) },
            label = string("auth.field.password"),
            password = true,
        )
        AppTextField(
            value = state.regDni,
            onValueChange = { viewModel.onRegFieldChange("dni", it) },
            label = string("auth.field.dni"),
        )
        AppTextField(
            value = state.regAddress,
            onValueChange = { viewModel.onRegFieldChange("address", it) },
            label = string("auth.field.address"),
        )
        AppTextField(
            value = state.regCompany,
            onValueChange = { viewModel.onRegFieldChange("company", it) },
            label = string("auth.field.company"),
        )
        AppTextField(
            value = state.regRuc,
            onValueChange = { viewModel.onRegFieldChange("ruc", it) },
            label = string("auth.field.ruc"),
        )
        state.error?.let { FormError(it) }
        VerticalGap(4)
        AppButton(
            text = string("auth.button.register"),
            onClick = viewModel::registerAdmin,
            loading = state.loading,
        )
    }
}
