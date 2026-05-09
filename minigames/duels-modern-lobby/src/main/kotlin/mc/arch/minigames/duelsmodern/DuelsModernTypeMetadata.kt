package mc.arch.minigames.duelsmodern

import com.cryptomorin.xseries.XMaterial
import gg.tropic.practice.configuration.minigame.type.LobbyNPCSkinType
import gg.tropic.practice.minigame.MiniGameTypeMetadata

object DuelsModernTypeMetadata : MiniGameTypeMetadata(
    internalId = "duels-modern",
    displayName = "Modern Duels",
    item = XMaterial.DIAMOND_SWORD,
    lobbyGroup = "duelsmodernlobby",
    gameModes = mapOf(),
    autoJoinSkinValue = LobbyNPCSkinType.DUELS.value,
    autoJoinSkinSignature = LobbyNPCSkinType.DUELS.signature
)
