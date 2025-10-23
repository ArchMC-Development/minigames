package gg.tropic.practice.scoreboard.configuration

fun primaryColor(): String = LobbyScoreboardConfigurationService.cached().primaryColor
fun secondaryColor(): String = LobbyScoreboardConfigurationService.cached().secondaryColor