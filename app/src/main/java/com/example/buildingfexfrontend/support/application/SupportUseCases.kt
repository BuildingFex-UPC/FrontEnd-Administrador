package com.example.buildingfexfrontend.support.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.support.domain.model.NewMessage
import com.example.buildingfexfrontend.support.domain.model.SupportChat
import com.example.buildingfexfrontend.support.domain.repository.SupportRepository

class SupportUseCases(
    private val repository: SupportRepository,
    private val session: SessionRepository,
) {
    val allChats = ListAllChatsUseCase(repository)
    val openChat = OpenChatUseCase(repository)
    val sendAdminMessage = SendMessageUseCase(repository, session, authorRole = "admin")
}

class ListAllChatsUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(): List<SupportChat> = repository.listChatsForAdmin()
}

class OpenChatUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(chatId: String): SupportChat = repository.getChat(chatId)
}

class SendMessageUseCase(
    private val repository: SupportRepository,
    private val session: SessionRepository,
    private val authorRole: String,
) {
    suspend operator fun invoke(chatId: String, body: String): SupportChat {
        val clean = body.trim()
        if (clean.isBlank()) {
            throw AppException("MESSAGE_EMPTY", "Escribe un mensaje.")
        }
        return repository.appendMessage(
            chatId = chatId,
            message = NewMessage(
                authorRole = authorRole,
                body = clean,
                authorName = session.current()?.profile?.name,
            ),
        )
    }
}
