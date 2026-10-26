package com.example.buildingfexfrontend.support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.support.domain.model.ChatMessage
import com.example.buildingfexfrontend.support.domain.model.SupportChat

@Composable
fun AdminSupportScreen(container: AppContainer) {
    val viewModel: AdminSupportViewModel = appViewModel { AdminSupportViewModel(container.support) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val activeChat = state.activeChat
    if (activeChat != null) {
        ChatDetailView(
            chat = activeChat,
            input = state.input,
            sending = state.sending,
            ownRole = "admin",
            onInputChanged = viewModel::onInputChanged,
            onSend = viewModel::send,
            onBack = viewModel::closeChat,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = string("sup.admin.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("sup.admin.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)
        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            state.chats.isEmpty() -> EmptyState(string("sup.admin.empty"))
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.chats, key = { it.id }) { chat ->
                    ChatRow(chat = chat, onClick = { viewModel.openChat(chat.id) })
                }
            }
        }
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("sup.dialog.title"),
            message = message,
            confirmText = string("sup.dialog.ack"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
private fun ChatRow(chat: SupportChat, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = chat.topic.let { if (it.isBlank() || it == "Soporte") string("sup.defaultTopic") else it },
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = chat.residentName.ifBlank { string("sup.chat.residentFallback") },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = string("sup.chat.lastActivity")
                        .replace("{date}", Dates.displayDateTime(chat.updatedAt))
                        .replace("{n}", chat.messages.size.toString()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ChatDetailView(
    chat: SupportChat,
    input: String,
    sending: Boolean,
    ownRole: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = string("sup.action.back"))
            }
            Column {
                Text(
                    text = chat.topic.let { if (it.isBlank() || it == "Soporte") string("sup.defaultTopic") else it },
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = chat.residentName.ifBlank { string("sup.chat.residentFallback") },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (chat.messages.isEmpty()) {
            EmptyState(string("sup.chat.empty"))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(chat.messages, key = { it.id }) { message ->
                    MessageBubble(message = message, isOwn = message.authorRole == ownRole)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text(string("sup.field.messagePlaceholder")) },
                maxLines = 3,
            )
            IconButton(onClick = onSend, enabled = !sending && input.isNotBlank()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = string("sup.action.send"),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isOwn: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isOwn) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(10.dp),
        ) {
            Column {
                if (message.authorName.isNullOrBlank().not()) {
                    Text(
                        text = message.authorName.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(message.body, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = Dates.displayDateTime(message.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
