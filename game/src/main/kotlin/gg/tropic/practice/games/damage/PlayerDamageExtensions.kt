package gg.tropic.practice.games.damage

import org.bukkit.entity.Player

fun Player.getEliminationDetails(alternative: EliminationCause) = PlayerDamageTracker.determineEliminationCause(this, alternative)
