package gg.tropic.practice.kit.feature

/**
 * @author GrowlyX
 * @since 9/17/2023
 */
enum class FeatureFlag(
    val schema: MutableMap<String, String> = mutableMapOf(),
    val incompatibleWith: () -> Set<FeatureFlag> = { emptySet() },
    val requires: Set<FeatureFlag> = setOf(),
    val availableLifecycles: Set<GameLifecycle> = GameLifecycle.entries.toSet(),
    val description: List<String> = listOf()
)
{
    Ranked(
        description = listOf(
            "Enables ranked resources for",
            "this kit. Includes ranked elo",
            "leaderboard calculations, etc.",
        )
    ),
    SpawnProtection(
        description = listOf(
            "Players are invincible when",
            "they spawn for N seconds.",
        ),
        schema = mutableMapOf("time" to "3")
    ),
    Development(
        description = listOf(
            "Makes this kit available only to",
            "developers."
        )
    ),
    PreventDropAnyItems(
        description = listOf(
            "Prevents players from dropping",
            "items from their inventory.",
        )
    ),
    QueueSizes(
        // another example: 3:Ranked,2:Casual,10:Ranked+Casual
        schema = mutableMapOf("sizes" to "1:Casual+Ranked"),
        description = listOf(
            "Define which queue types this kit",
            "is eligible for.",
            "",
            "Examples:",
            "3:Ranked,2:Casual,10:Ranked+Casual",
            "1:Casual+Ranked"
        )
    ),
    HeartsBelowNameTag(
        description = listOf(
            "All player healths are shown",
            "below the nametag.",
            "",
            "This also enables the feature",
            "allowing for hotbar health",
            "notifications.",
        )
    ),
    ExplodePlacedBlocks(
        description = listOf(
            "Allows any explosion event",
            "from removing placed blocks.",
        )
    ),
    DoNotTakeFallDamage(
        description = listOf(
            "Prevents you from taking fall",
            "damage in a match.",
        )
    ),
    DoNotTakeDamage(
        schema = mutableMapOf("doDamageTick" to "false"),
        description = listOf(
            "Prevents you from losing",
            "any health in a match.",
            "",
            "Has an optional feature to",
            "disable/enable the damage tick."
        )
    ),
    DoNotDropInventoryOnDeath(
        description = listOf(
            "Prevents you from dropping",
            "your inventory items on",
            "death.",
        )
    ),
    DoNotTakeHunger(
        description = listOf(
            "Prevents you from losing any",
            "hunger in a match.",
        )
    ),
    MiniGameType(
        schema = mutableMapOf(
            "id" to "skywars"
        ),
        description = listOf(
            "Allows for custom scoreboards &",
            "functionality to be enabled."
        ),
        availableLifecycles = setOf(
            GameLifecycle.MiniGame
        )
    ),
    DoNotRemoveArmor(
        description = listOf(
            "Prevents you taking off your",
            "armor in a match.",
        )
    ),
    HardcoreHealing(
        description = listOf(
            "Prevents you from naturally",
            "regenerating health.",
        )
    ),
    KnockbackProfile(
        description = listOf(
            "Use a custom knockback profile",
            "through Tropic Spigot."
        ),
        schema = mutableMapOf("profile" to "default")
    ),
    RequiresBuildMap(
        description = listOf(
            "A ID flag to represent any",
            "build matches. However, this",
            "doesn't really affect much.",
        )
    ),
    HeadshotMultiplier(
        schema = mutableMapOf(
            "multiplier" to "1.0"
        ),
        description = listOf(
            "Enables a harm multiplier for",
            "the player shot if the shot",
            "is a headshot."
        )
    ),
    DeathBelowYAxis(
        schema = mutableMapOf(
            "level" to "95"
        ),
        description = listOf(
            "Automatically removes you from",
            "the match if you drop below",
            "a certain Y-Axis level.",
        )
    ),
    BuildLimit(
        schema = mutableMapOf(
            "blocks" to "16"
        ),
        description = listOf(
            "Sets the build limit to:",
            "blocks+startingPosition"
        )
    ),
    PlaceBlocks(
        description = listOf(
            "Enables players in the match",
            "to place blocks.",
        )
    ),
    ArrowRefund(
        description = listOf(
            "Returns arrows in-match when used",
            "after a specific time duration.",
        ),
        schema = mutableMapOf(
            "time" to "3",
            "preventArrowPickups" to "true"
        )
    ),
    BlockRefund(
        description = listOf(
            "Returns blocks to the player who",
            "placed them when a player or another",
            "flag / feature breaks them."
        )
    ),
    DoNotDropBrokenBlocks(
        description = listOf(
            "Drops broken blocks on",
            "the ground."
        )
    ),
    BreakAllBlocks(
        incompatibleWith = { setOf(BreakPlacedBlocks, BreakSpecificBlockTypes) },
        requires = setOf(RequiresBuildMap),
        description = listOf(
            "Enables players in the match",
            "to place blocks.",
        )
    ),
    BreakPlacedBlocks(
        incompatibleWith = { setOf(BreakAllBlocks) },
        requires = setOf(RequiresBuildMap),
        description = listOf(
            "Enables players in the match",
            "to break the blocks that they",
            "placed.",
        )
    ),
    BreakSpecificBlockTypes(
        incompatibleWith = { setOf(BreakAllBlocks) },
        requires = setOf(RequiresBuildMap),
        schema = mutableMapOf(
            // Default for bridges LMAO
            "types" to "STAINED_CLAY:11,STAINDED_CLAY:14"
        ),
        description = listOf(
            "Allows players to break certain",
            "non-placed blocks in the match."
        )
    ),
    EnderPearlCooldown(
        schema = mutableMapOf("duration" to "15"),
        description = listOf(
            "Adds a cooldown to enderpearl",
            "throwing.",
        )
    ),
    FrozenOnGameStart(
        description = listOf(
            "Prevents players from moving",
            "at the start of a match.",
        )
    ),
    NewlyCreated(
        description = listOf(
            "Gives the kit a nice identifier",
            "in any kit-related menu, saying it",
            "is a new kit.",
        )
    ),
    MenuOrderWeight(
        schema = mutableMapOf("weight" to "0"),
        description = listOf(
            "Controls the sorting order of",
            "the kit in any kit-related menu.",
        )
    ),
    ExpirePlacedBlocksAfterNSeconds(
        schema = mutableMapOf("time" to "10", "return" to "false"),
        requires = setOf(PlaceBlocks),
        description = listOf(
            "Automatically removes and returns",
            "a block placed by a player after",
            "N amount of seconds.",
        )
    ),
    DeathOnLiquidInteraction(
        description = listOf(
            "Removes the player from the game",
            "if they touch a liquid mid-match.",
        )
    ),
    EntityDisguise(
        schema = mutableMapOf("type" to "IRON_GOLEM"),
        description = listOf(
            "Disguises the player as a particular",
            "entity during the match.",
        )
    ),
    RedBlueTeams(
        description = listOf(
            "Identifies A/B teams as Red/Blue.",
        )
    ),

    // multi-round
    CountDownTimeBeforeRoundStart(
        schema = mutableMapOf("value" to "5"),
        availableLifecycles = setOf(
            GameLifecycle.RoundBound
        ),
        description = listOf(
            "Configures the count-down time",
            "before a round starts.",
        )
    ),

    // ImmediateRespawnOnDeath may be implied in game logic!
    ImmediateRespawnOnDeath(
        incompatibleWith = { setOf(StartNewRoundOnDeath) },
        availableLifecycles = setOf(
            GameLifecycle.RoundBound,
            GameLifecycle.ObjectivePlusSoulBound,
            GameLifecycle.ObjectiveBound
        ),
        description = listOf(
            "Immediately respawns the player",
            "upon death.",
        )
    ),
    StartNewRoundOnDeath(
        incompatibleWith = { setOf(ImmediateRespawnOnDeath, SpectateAfterDeath) },
        availableLifecycles = setOf(
            GameLifecycle.RoundBound
        ),
        description = listOf(
            "Starts a new round when the",
            "player dies.",
        )
    ),
    RoundsRequiredToCompleteGame(
        schema = mutableMapOf("value" to "2"),
        availableLifecycles = setOf(
            GameLifecycle.RoundBound
        ),
        description = listOf(
            "Sets the number of rounds required",
            "to complete the game.",
        )
    ),

    SpectateAfterDeath(
        incompatibleWith = { setOf(ImmediateRespawnOnDeath) },
        availableLifecycles = setOf(
            GameLifecycle.RoundBound,
            GameLifecycle.ObjectivePlusSoulBound,
            GameLifecycle.ObjectiveBound
        ),
        description = listOf(
            "Enters the player into spectator",
            "mode for a particular amount of",
            "time when the player dies.",
        )
    ),
    ClearIllegalBlocksOnRespawn(
        incompatibleWith = { setOf(ImmediateRespawnOnDeath) },
        availableLifecycles = setOf(
            GameLifecycle.RoundBound,
            GameLifecycle.ObjectivePlusSoulBound,
            GameLifecycle.ObjectiveBound
        ),
        description = listOf(
            "Prevents the player from being",
            "trapped in blocks if their spawn",
            "point is blocked.",
        )
    ),
    TimeUserSpectatesAfterDeath(
        schema = mutableMapOf("value" to "3"),
        requires = setOf(SpectateAfterDeath),
        availableLifecycles = setOf(
            GameLifecycle.RoundBound,
            GameLifecycle.ObjectivePlusSoulBound,
            GameLifecycle.ObjectiveBound
        ),
        description = listOf(
            "Sets the amount of time a user will",
            "spectate upon their death.",
        )
    ),

    WinWhenNHitsReached(
        schema = mutableMapOf("hits" to "100"),
        availableLifecycles = setOf(
            GameLifecycle.ObjectiveBound,
            GameLifecycle.ObjectivePlusSoulBound
        ),
        description = listOf(
            "The user wins when they hit",
            "a certain N number of hits.",
        )
    ),
    StartNewRoundOnPortalEnter(
        incompatibleWith = { setOf(StartNewRoundOnDeath) },
        availableLifecycles = setOf(
            GameLifecycle.RoundBound
        ),
        description = listOf(
            "Starts a new round when the",
            "player enters a portal.",
        )
    ),
    RemovePlacedBlocksOnRoundStart(
        availableLifecycles = setOf(
            GameLifecycle.RoundBound
        ),
        description = listOf(
            "Removes placed blocks when a",
            "new round starts.",
        )
    ),
    FlyOnWin(
        incompatibleWith = { setOf(SpectateAfterDeath) },
        description = listOf(
            "The winner will fly after the",
            "game has been won."
        )
    )
}
