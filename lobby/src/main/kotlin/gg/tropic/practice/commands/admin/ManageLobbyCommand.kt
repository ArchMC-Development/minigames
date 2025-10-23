package gg.tropic.practice.commands.admin

import gg.scala.aware.thread.AwareThreadContext
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.tropic.practice.PracticeLobby
import gg.tropic.practice.communications.PracticePlayerCommsService
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.group.KitGroup
import gg.tropic.practice.kit.group.KitGroupService
import gg.tropic.practice.map.MapService
import gg.tropic.practice.menu.manage.ManageLobbyMenu
import gg.tropic.practice.practiceGroup
import gg.tropic.practice.region.Region
import gg.tropic.practice.suffixWhenDev
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/13/2023
 */
@AutoRegister
@CommandAlias("managelobby")
@CommandPermission("practice.lobby.commands.admin")
object ManageLobbyCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: PracticeLobby

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("manage")
    fun onManage(player: ScalaPlayer) = ManageLobbyMenu().openMenu(player)

    @Subcommand("request-application-reboot")
    @Description("Request an application reboot.")
    fun onRequestAppReboot(player: ScalaPlayer)
    {
        PracticePlayerCommsService.createMessage("reboot-app")
            .publish(
                AwareThreadContext.ASYNC,
                channel = "communications-application".suffixWhenDev()
            )

        player.sendMessage("${CC.GREEN}We've requested an application reboot, please wait a moment.")
    }

    @Subcommand("restrict-game-regions")
    @Description("Restrict games to a particular region.")
    fun onRequestRegionRestriction(
        player: ScalaPlayer, @Optional region: Region?
    )
    {
        PracticePlayerCommsService
            .createMessage(
                "force-specific-region",
                "region-id" to (region?.name ?: "__RESET__")
            )
            .publish(
                AwareThreadContext.ASYNC,
                channel = "communications-gamequeue".suffixWhenDev()
            )

        player.sendMessage(
            "${CC.GREEN}We've requested a region restriction to: ${CC.WHITE}${region ?: "Unrestricted"}."
        )
    }

    @Subcommand("enter-build-mode")
    @Description("Allow yourself to build in spawn.")
    fun onJoinBuildMode(player: ScalaPlayer)
    {
        player.bukkit().setMetadata("builder", FixedMetadataValue(plugin, true))
        player.sendMessage(
            "${CC.GREEN}You are now in build mode."
        )
    }

    @Subcommand("fix-kit-groups")
    fun fixKitGroups(player: ScalaPlayer)
    {
        val kitGroups = KitGroupService.cached()
        MapService.maps().forEach {
            for (associatedKitGroup in it.associatedKitGroups)
            {
                if (kitGroups.groups.none { group -> group.id == associatedKitGroup })
                {
                    player.sendMessage("Fixing $associatedKitGroup")
                    kitGroups.add(KitGroup(
                        id = associatedKitGroup,
                        contains = mutableListOf(associatedKitGroup)
                    ))
                }
            }
        }

        KitGroupService.sync(kitGroups)
        KitService.cached().apply {
            val cached = KitGroupService.cached()
            for (value in kits.values)
            {
                if (KitGroupService.groupsOf(value).isEmpty())
                {
                    player.sendMessage("Adding ${value.id} to default")
                    cached.groups.firstOrNull { group -> group.id == "__default__" }
                        ?.let {
                            it.contains += value.id
                        }
                }
            }

            KitGroupService.sync(cached)
        }
    }

    @Subcommand("leave-dev-mode")
    @Description("Leave dev mode.")
    fun onLeaveDevMode(player: ScalaPlayer)
    {
        PracticePlayerCommsService
            .createMessage(
                "leave-dev-mode",
                "uniqueId" to player.uniqueId.toString()
            )
            .publish(
                AwareThreadContext.ASYNC,
                channel = "communications-gamequeue".suffixWhenDev()
            )

        player.sendMessage(
            "${CC.RED}You will no longer be sent to dev instances."
        )
    }

    @AssignPermission
    @Subcommand("export-player-list")
    @Description("Export a list of MIP players.")
    fun onExportPlayerList(player: ScalaPlayer) = CompletableFuture
        .supplyAsync {
            ServerContainer
                .getServersInGroupCasted<GameServer>(practiceGroup().suffixWhenDev())
                .flatMap {
                    it.getPlayers()!!
                }
        }
        .thenAccept {
            val export = File(plugin.dataFolder, "player-list.json")
            if (export.exists())
            {
                export.delete()
            }

            export.writeText(Serializers.gson.toJson(it))
            player.sendMessage("${CC.GREEN}Exported player list!")
        }
}
