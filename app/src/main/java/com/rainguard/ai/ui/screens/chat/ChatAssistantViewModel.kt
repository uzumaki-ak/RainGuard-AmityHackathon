package com.rainguard.ai.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainguard.ai.data.model.ChatMessage
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val currentInput: String = ""
)

@HiltViewModel
class ChatAssistantViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        // Add welcome message
        _state.value = _state.value.copy(
            messages = listOf(
                ChatMessage(
                    text = "Hello! I'm RainGuardAI Assistant. How can I help you stay safe today?",
                    isUser = false,
                    sources = listOf("system")
                )
            )
        )
    }

    fun setInput(text: String) {
        _state.value = _state.value.copy(currentInput = text)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage,
            currentInput = "",
            isTyping = true
        )

        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(text)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + result.data,
                        isTyping = false
                    )
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Chat error")
                    _state.value = _state.value.copy(isTyping = false)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun sendQuickSuggestion(suggestion: String) {
        sendMessage(suggestion)
    }
}