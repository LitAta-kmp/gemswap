package org.example.gemswap.di

import org.koin.dsl.module
import org.example.gemswap.data.local.DatabaseDriverFactory
import org.example.gemswap.data.local.GemSwapDatabase
import org.example.gemswap.data.local.SqlDelightHistoryRepository
import org.example.gemswap.domain.HistoryRepository

val androidModule = module {
    single { DatabaseDriverFactory(context = get()) }
    single { GemSwapDatabase(driver = get<DatabaseDriverFactory>().createDriver()) }
    single<HistoryRepository> { SqlDelightHistoryRepository(database = get()) }
}