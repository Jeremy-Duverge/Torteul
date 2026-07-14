package com.totoprout.torteul.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object YouTube : Route

    @Serializable
    data object Alarms : Route

    @Serializable
    data object Calendar : Route

}