package org.example.gemswap.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import org.example.gemswap.domain.GameRound
import org.example.gemswap.domain.HistoryRepository

class HistoryViewModel(
    historyRepository: HistoryRepository
) : ViewModel() {

    val rounds: StateFlow<List<GameRound>> = historyRepository.observeAllRounds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}