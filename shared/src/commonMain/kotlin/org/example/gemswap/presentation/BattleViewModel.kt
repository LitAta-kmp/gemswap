package org.example.gemswap.presentation

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.gemswap.domain.*

class BattleViewModel(
    private val boardGenerator: BoardGenerator,
    private val swapValidator: SwapValidator,
    private val matchFinder: MatchFinder,
    private val cascadeResolver: CascadeResolver,
    private val npcMoveSelector: NpcMoveSelector
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<BattleState> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<BattleSideEffect>()
    val sideEffects: SharedFlow<BattleSideEffect> = _sideEffects.asSharedFlow()

    private var turnTimerJob: Job? = null

    fun onIntent(intent: BattleIntent) {
        when (intent) {
            is BattleIntent.StartBattle -> {
                _state.value = BattleState(
                    board = boardGenerator.generatePlayableBoard(GameRules.GRID_ROWS, GameRules.GRID_COLS, swapValidator),
                    status = BattleStatus.InProgress
                )
                startTurnTimer()
            }
            is BattleIntent.PlayerSwapRequested -> {
                if (_state.value.currentTurn == BattleTurn.PLAYER && _state.value.status == BattleStatus.InProgress) {
                    viewModelScope.launch { handlePlayerMove(intent.from, intent.to) }
                }
            }
            is BattleIntent.ActivateSkullClearAbility -> {
                val current = _state.value
                if (current.currentTurn == BattleTurn.PLAYER && current.status == BattleStatus.InProgress &&
                    (current.playerColorCounts[TileColor.GREEN] ?: 0) >= GameRules.BATTLE_ABILITY_GREEN_THRESHOLD
                ) {
                    viewModelScope.launch { activateSkullClearAbility(BattleTurn.PLAYER) }
                }
            }
            is BattleIntent.TurnTimeTicked -> handleTurnTimeout()
        }
    }

    private suspend fun handlePlayerMove(from: Position, to: Position) {
        val current = _state.value
        if (!swapValidator.isValidSwap(current.board, from, to)) {
            _sideEffects.emit(BattleSideEffect.InvalidSwap(from, to))
            return
        }
        turnTimerJob?.cancel()
        resolveMove(current.board.swapped(from, to), mover = BattleTurn.PLAYER)
    }

    private suspend fun npcTakesTurn() {
        delay(1000)

        if ((_state.value.npcColorCounts[TileColor.GREEN] ?: 0) >= GameRules.BATTLE_ABILITY_GREEN_THRESHOLD) {
            activateSkullClearAbility(BattleTurn.NPC)
        }

        val current = _state.value
        val move = npcMoveSelector.selectBestMove(current.board, current.npcHp)

        if (move == null) {
            // на практике почти невозможно благодаря generatePlayableBoard — но на всякий случай
            // просто отдаём ход обратно, не роняя игру
            _state.value = current.copy(currentTurn = BattleTurn.PLAYER, turnTimeRemainingSeconds = GameRules.BATTLE_TURN_DURATION_SECONDS)
            startTurnTimer()
            return
        }

        val (from, to) = move
        resolveMove(current.board.swapped(from, to), mover = BattleTurn.NPC)
    }

    private suspend fun resolveMove(swappedBoard: Board, mover: BattleTurn) {
        _state.value = _state.value.copy(board = swappedBoard)
        delay(150)

        val result = resolveCascadeFrom(swappedBoard, mover)
        applyDamage(mover, result.damageDealt)
        applyHeal(mover, result.healAmount)

        val current = _state.value
        val opponentDefeated = if (mover == BattleTurn.PLAYER) current.npcHp <= 0 else current.playerHp <= 0

        if (opponentDefeated) {
            turnTimerJob?.cancel()
            _state.value = current.copy(status = if (mover == BattleTurn.PLAYER) BattleStatus.PlayerWon else BattleStatus.NpcWon)
            return
        }

        if (result.bonusTurnEarned) {
            _sideEffects.emit(BattleSideEffect.BonusTurn(mover))
            _state.value = _state.value.copy(turnTimeRemainingSeconds = GameRules.BATTLE_TURN_DURATION_SECONDS)
            startTurnTimer()
            if (mover == BattleTurn.NPC) viewModelScope.launch { npcTakesTurn() }
        } else {
            val nextTurn = if (mover == BattleTurn.PLAYER) BattleTurn.NPC else BattleTurn.PLAYER
            _state.value = _state.value.copy(currentTurn = nextTurn, turnTimeRemainingSeconds = GameRules.BATTLE_TURN_DURATION_SECONDS)
            startTurnTimer()
            if (nextTurn == BattleTurn.NPC) viewModelScope.launch { npcTakesTurn() }
        }
    }

    private fun currentMoverColorCounts(mover: BattleTurn): Map<TileColor, Int> =
        if (mover == BattleTurn.PLAYER) _state.value.playerColorCounts else _state.value.npcColorCounts

    private fun applyBoardAndColorCounts(board: Board, mover: BattleTurn, counts: Map<TileColor, Int>) {
        _state.value = if (mover == BattleTurn.PLAYER) {
            _state.value.copy(board = board, playerColorCounts = counts.toMap())
        } else {
            _state.value.copy(board = board, npcColorCounts = counts.toMap())
        }
    }

    private fun applyDamage(mover: BattleTurn, amount: Int) {
        if (amount <= 0) return
        _state.value = if (mover == BattleTurn.PLAYER) {
            _state.value.copy(npcHp = (_state.value.npcHp - amount).coerceAtLeast(0))
        } else {
            _state.value.copy(playerHp = (_state.value.playerHp - amount).coerceAtLeast(0))
        }
    }

    private fun applyHeal(mover: BattleTurn, amount: Int) {
        if (amount <= 0) return
        _state.value = if (mover == BattleTurn.PLAYER) {
            _state.value.copy(playerHp = (_state.value.playerHp + amount).coerceAtMost(GameRules.BATTLE_STARTING_HP))
        } else {
            _state.value.copy(npcHp = (_state.value.npcHp + amount).coerceAtMost(GameRules.BATTLE_STARTING_HP))
        }
    }

    private suspend fun activateSkullClearAbility(mover: BattleTurn) {
        val current = _state.value
        val counts = currentMoverColorCounts(mover).toMutableMap()
        val greenCount = counts[TileColor.GREEN] ?: 0
        if (greenCount < GameRules.BATTLE_ABILITY_GREEN_THRESHOLD) return

        counts[TileColor.GREEN] = greenCount - GameRules.BATTLE_ABILITY_GREEN_THRESHOLD
        val redTilesCleared = current.board.cells.flatten().count { it?.color == TileColor.RED }
        val abilityDamage = redTilesCleared * GameRules.BATTLE_DAMAGE_PER_RED_TILE

        val clearedCells = current.board.cells.map { row ->
            row.map { tile -> if (tile?.color == TileColor.RED) null else tile }.toMutableList()
        }
        val clearedBoard = current.board.copy(cells = clearedCells)
        applyBoardAndColorCounts(clearedBoard, mover, counts)
        _sideEffects.emit(BattleSideEffect.AbilityActivated(mover))
        delay(150)

        val fallen = cascadeResolver.applyGravity(clearedBoard)
        _state.value = _state.value.copy(board = fallen)
        delay(200)
        val refilled = cascadeResolver.refill(fallen)
        _state.value = _state.value.copy(board = refilled)
        delay(150)

        val result = resolveCascadeFrom(refilled, mover)

        applyDamage(mover, abilityDamage + result.damageDealt)
        applyHeal(mover, result.healAmount)

        val afterDamage = _state.value
        val opponentDefeated = if (mover == BattleTurn.PLAYER) afterDamage.npcHp <= 0 else afterDamage.playerHp <= 0
        if (opponentDefeated) {
            turnTimerJob?.cancel()
            _state.value = afterDamage.copy(status = if (mover == BattleTurn.PLAYER) BattleStatus.PlayerWon else BattleStatus.NpcWon)
        }
        // очередь намеренно не трогаем — способность бесплатна, ход остаётся у mover-а
    }

    private fun handleTurnTimeout() {
        val current = _state.value
        if (current.status != BattleStatus.InProgress) return

        val newTime = current.turnTimeRemainingSeconds - 1
        if (newTime > 0) {
            _state.value = current.copy(turnTimeRemainingSeconds = newTime)
            return
        }

        val nextTurn = current.currentTurn.opposite()
        _state.value = current.copy(currentTurn = nextTurn, turnTimeRemainingSeconds = GameRules.BATTLE_TURN_DURATION_SECONDS)
        if (nextTurn == BattleTurn.NPC) viewModelScope.launch { npcTakesTurn() }
    }

    private fun startTurnTimer() {
        turnTimerJob?.cancel()
        turnTimerJob = viewModelScope.launch {
            while (_state.value.status == BattleStatus.InProgress) {
                delay(1000)
                handleTurnTimeout()
            }
        }
    }

    private fun initialState(): BattleState = BattleState(
        board = Board(rows = 0, cols = 0, cells = emptyList()),
        status = BattleStatus.Idle
    )
    private data class CascadeResult(
        val finalBoard: Board,
        val damageDealt: Int,
        val healAmount: Int,
        val bonusTurnEarned: Boolean
    )

    private suspend fun resolveCascadeFrom(startingBoard: Board, mover: BattleTurn): CascadeResult {
        var board = startingBoard
        var waveIndex = 1
        var matched = matchFinder.findMatches(board)
        var bonusTurnEarned = false
        var accumulatedColorCounts = currentMoverColorCounts(mover).toMutableMap()
        var damageDealt = 0
        var healAmount = 0

        while (matched.isNotEmpty() && waveIndex <= GameRules.MAX_CASCADE_WAVES) {
            val runs = matchFinder.findMatchRuns(board)
            if (runs.any { it.positions.size >= 4 }) bonusTurnEarned = true

            runs.forEach { run ->
                accumulatedColorCounts[run.color] = (accumulatedColorCounts[run.color] ?: 0) + run.positions.size
                when (run.color) {
                    TileColor.RED -> damageDealt += run.positions.size * GameRules.BATTLE_DAMAGE_PER_RED_TILE
                    TileColor.PURPLE -> healAmount += run.positions.size * GameRules.BATTLE_HEAL_PER_TILE
                    else -> {}
                }
            }

            val removed = cascadeResolver.removeMatches(board, matched)
            applyBoardAndColorCounts(removed, mover, accumulatedColorCounts)
            delay(150)

            val fallen = cascadeResolver.applyGravity(removed)
            _state.value = _state.value.copy(board = fallen)
            delay(200)

            val refilled = cascadeResolver.refill(fallen)
            board = refilled
            applyBoardAndColorCounts(refilled, mover, accumulatedColorCounts)
            delay(150)

            matched = matchFinder.findMatches(board)
            waveIndex++
        }

        if (!swapValidator.hasAnyValidMove(board)) {
            board = boardGenerator.generatePlayableBoard(GameRules.GRID_ROWS, GameRules.GRID_COLS, swapValidator)
            _state.value = _state.value.copy(board = board)
        }

        return CascadeResult(board, damageDealt, healAmount, bonusTurnEarned)
    }

    override fun onCleared() { turnTimerJob?.cancel() }
}

private fun BattleTurn.opposite(): BattleTurn = when (this) {
    BattleTurn.PLAYER -> BattleTurn.NPC
    BattleTurn.NPC -> BattleTurn.PLAYER
}