package org.example.gemswap.presentation

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.gemswap.domain.*

class GameViewModel(
    private val reducer: GameReducer,
    private val boardGenerator: BoardGenerator,
    private val swapValidator: SwapValidator,
    private val matchFinder: MatchFinder,
    private val cascadeResolver: CascadeResolver,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<GameSideEffect>()
    val sideEffects: SharedFlow<GameSideEffect> = _sideEffects.asSharedFlow()

    private var timerJob: Job? = null

    fun onIntent(intent: GameIntent) {
        if (intent is GameIntent.SwapRequested) {
            viewModelScope.launch { handleSwap(intent.from, intent.to) }
            return
        }

        val result = reducer.reduce(_state.value, intent)
        _state.value = result.state

        if (intent is GameIntent.StartGame) startTimer()

        if (intent is GameIntent.TimeTicked && result.state.status == GameStatus.Finished) {
            viewModelScope.launch { saveRoundResult(result.state.score) }
        }
    }

    private suspend fun saveRoundResult(score: Int) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        historyRepository.saveRound(score = score, playedAt = now.toString())
    }

    private suspend fun handleSwap(from: Position, to: Position) {
        val current = _state.value
        if (current.status != GameStatus.Playing) return

        if (!swapValidator.isValidSwap(current.board, from, to)) {
            _sideEffects.emit(GameSideEffect.InvalidSwap(from, to))
            return
        }

        var board = current.board.swapped(from, to)
        _state.value = current.copy(board = board)
        delay(150)

        var waveIndex = 1
        var matched = matchFinder.findMatches(board)
        var accumulatedColorCounts = current.colorCounts.toMutableMap()


        while (matched.isNotEmpty() && waveIndex <= GameRules.MAX_CASCADE_WAVES) {
            val waveColors = countColors(board, matched)
            waveColors.forEach { (color, count) ->
                accumulatedColorCounts[color] = (accumulatedColorCounts[color] ?: 0) + count
            }

            val removed = cascadeResolver.removeMatches(board, matched)
            _state.value = _state.value.copy(board = removed, colorCounts = accumulatedColorCounts.toMap())
            delay(150)

            val fallen = cascadeResolver.applyGravity(removed)
            _state.value = _state.value.copy(board = fallen)
            delay(200)

            val refilled = cascadeResolver.refill(fallen)
            val pointsGained = matched.size * GameRules.POINTS_PER_TILE * waveIndex
            _state.value = _state.value.copy(
                board = refilled,
                score = _state.value.score + pointsGained
            )

            if (waveIndex >= 2) {
                _sideEffects.emit(GameSideEffect.ComboTriggered(waveIndex))
            }

            delay(150)

            board = refilled
            matched = matchFinder.findMatches(board)
            waveIndex++
        }

        // Новая проверка — после того как каскад полностью улёгся
        if (!swapValidator.hasAnyValidMove(board)) {
            board = boardGenerator.generatePlayableBoard(
                GameRules.GRID_ROWS,
                GameRules.GRID_COLS,
                swapValidator
            )
            _state.value = _state.value.copy(board = board)
        }
    }

    private fun countColors(board: Board, positions: Set<Position>): Map<TileColor, Int> {
        return positions
            .mapNotNull { board.tileAt(it)?.color }
            .groupingBy { it }
            .eachCount()
    }
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.status == GameStatus.Playing) {
                delay(1000)
                onIntent(GameIntent.TimeTicked)
            }
        }
    }

    private fun initialState(): GameState = GameState(
        board = Board(rows = 0, cols = 0, cells = emptyList()),
        status = GameStatus.Idle
    )

    override fun onCleared() { timerJob?.cancel() }
}