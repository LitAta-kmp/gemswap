package org.example.gemswap.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Home : Routes

    @Serializable
    data object Game : Routes
    @Serializable
    data object History : Routes
    @Serializable
    data object Battle : Routes
    @Serializable
    data class Results(val score: Int) : Routes
}