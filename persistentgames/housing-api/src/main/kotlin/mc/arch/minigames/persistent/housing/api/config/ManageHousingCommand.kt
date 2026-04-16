package mc.arch.minigames.persistent.housing.api.config

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.command.CommandSender

@AutoRegister
@CommandAlias("managehousing")
@CommandPermission("housing.admin")
object ManageHousingCommand : ScalaCommand()
{
    @Subcommand("toggle-creating")
    fun onToggleCreating(sender: CommandSender)
    {
        val config = HousingConfigurationDataSync.cached()
        config.allowHouseCreation = !config.allowHouseCreation
        HousingConfigurationDataSync.sync(config)

        val status = if (config.allowHouseCreation) "${CC.GREEN}enabled" else "${CC.RED}disabled"
        sender.sendMessage("${CC.YELLOW}House creation is now $status${CC.YELLOW}.")
    }

    @Subcommand("toggle-visiting")
    fun onToggleVisiting(sender: CommandSender)
    {
        val config = HousingConfigurationDataSync.cached()
        config.allowVisiting = !config.allowVisiting
        HousingConfigurationDataSync.sync(config)

        val status = if (config.allowVisiting) "${CC.GREEN}enabled" else "${CC.RED}disabled"
        sender.sendMessage("${CC.YELLOW}House visiting is now $status${CC.YELLOW}.")
    }

    @Subcommand("set-cost")
    fun onSetCost(sender: CommandSender, cost: Long)
    {
        if (cost < 0)
        {
            sender.sendMessage("${CC.RED}Cost must be a positive number!")
            return
        }

        val config = HousingConfigurationDataSync.cached()
        config.featureHouseCostPerWeek = cost
        HousingConfigurationDataSync.sync(config)

        sender.sendMessage("${CC.YELLOW}Feature house cost set to ${CC.GOLD}${Numbers.format(cost)} Gems${CC.YELLOW} per week.")
    }

    @Subcommand("status")
    fun onStatus(sender: CommandSender)
    {
        val config = HousingConfigurationDataSync.cached()

        sender.sendMessage("${CC.YELLOW}${CC.STRIKE_THROUGH}${"-".repeat(40)}")
        sender.sendMessage("${CC.GOLD}${CC.BOLD}Housing Configuration")
        sender.sendMessage("")
        sender.sendMessage("${CC.YELLOW}House Creation: ${if (config.allowHouseCreation) "${CC.GREEN}Enabled" else "${CC.RED}Disabled"}")
        sender.sendMessage("${CC.YELLOW}Visiting: ${if (config.allowVisiting) "${CC.GREEN}Enabled" else "${CC.RED}Disabled"}")
        sender.sendMessage("${CC.YELLOW}Feature Cost: ${CC.GOLD}${Numbers.format(config.featureHouseCostPerWeek)} Gems${CC.YELLOW}/week")
        sender.sendMessage("${CC.YELLOW}${CC.STRIKE_THROUGH}${"-".repeat(40)}")
    }
}
