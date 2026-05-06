package mc.arch.minigames.versioned.legacy

import mc.arch.minigames.versioned.generics.ServerProvider
import net.minecraft.server.v1_8_R3.MinecraftServer

/**
 * @author GrowlyX
 * @since 2026-05-06
 */
object LegacyServerProvider : ServerProvider
{
    override fun currentTick(): Int = MinecraftServer.currentTick
}
