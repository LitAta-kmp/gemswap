package org.example.gemswap.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.example.gemswap.domain.*
import org.example.gemswap.presentation.BattleReducer
import org.example.gemswap.presentation.BattleViewModel
import org.example.gemswap.presentation.GameReducer
import org.example.gemswap.presentation.GameViewModel
import org.example.gemswap.presentation.HistoryViewModel

val appModule = module {
    single { TileIdGenerator() }
    single { BoardGenerator(idGenerator = get()) }
    single { SwapValidator() }
    single { MatchFinder() }
    single { CascadeResolver(idGenerator = get()) }
    single {
        GameReducer(
            boardGenerator = get(),
            swapValidator = get(),
            matchFinder = get(),
            cascadeResolver = get()
        )
    }
    single { NpcMoveSelector(swapValidator = get(), matchFinder = get()) }
    single { BattleReducer(boardGenerator = get(), swapValidator = get(), cascadeResolver = get()) }

    viewModelOf(::GameViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::BattleViewModel)
}