package mc.arch.minigames.persistent.housing.game.menu.house.visitation

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.formatName
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.model.VisitationStatus
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class HouseVisitationRuleMenu(val house: PlayerHouse): Menu("Visitation Rules")
{
    init
    {
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 45

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        var index = 0
        val visitationRules = VisitationStatus.entries

        for (int in 11..15)
        {
            val visitationRule = visitationRules[index]
            val enabled = house.visitationStatuses[visitationRule] == true

            buttons[int] = ItemBuilder.of(icon(visitationRule))
                .name("${CC.GREEN}${visitationRule.formatName()}")
                .addToLore(
                    "${CC.GRAY}${visitationRule.description}",
                    if (enabled) "${CC.RED}Click to disable" else "${CC.GREEN}Click to enable"
                ).toButton { _, _ ->
                    toggleVisitationStatus(visitationRule, !enabled, house)
                }

            buttons[int+9] = ItemBuilder.of(if (enabled) XMaterial.LIME_DYE else XMaterial.GRAY_DYE)
                .name(if (enabled) "${CC.GREEN}Enabled" else "${CC.RED}Disabled")
                .addToLore(
                    "${CC.GRAY}${visitationRule.description}",
                    if (enabled) "${CC.RED}Click to disable" else "${CC.GREEN}Click to enable"
                ).toButton { _, _ ->
                    toggleVisitationStatus(visitationRule, !enabled, house)
                }

            index++
        }

        return buttons
    }

    fun toggleVisitationStatus(status: VisitationStatus, value: Boolean, house: PlayerHouse)
    {
        /**
         * For private status, what we are looking for is
         * if they are turning private status on, we want
         * to remove the house from all existing rules
         * because they only want themselves in.
         *
         * If they are turning the private status off,
         * presuming there are no other rules in place,
         * we want to make it public.
         */
        if (status == VisitationStatus.PRIVATE)
        {
            house.visitationStatuses[status] = value

            if (value)
            {
                VisitationStatus.entries.forEach {
                    if (it == status) return@forEach

                    house.visitationStatuses[it] = false
                }
            } else
            {
                if (VisitationStatus.entries.none { house.visitationStatusApplies(it) })
                {
                    house.visitationStatuses[VisitationStatus.PUBLIC] = true
                }
            }
        }

        /**
         * For public status, we basically just want to
         * make sure that everyone can join and private
         * status is turned off.
         *
         * On Hypixel, they auto turn-on every single
         * targeted visitation status, so we do that too
         */
        if (status == VisitationStatus.PUBLIC)
        {
            house.visitationStatuses[status] = value

            if (value)
            {
                VisitationStatus.entries.forEach {
                    house.visitationStatuses[it] = it != VisitationStatus.PRIVATE
                }
            } else
            {
                if (VisitationStatus.entries.none { house.visitationStatusApplies(it) })
                {
                    house.visitationStatuses[VisitationStatus.PRIVATE] = true
                }
            }
        }

        /**
         * For these, if it isn't public and isn't private,
         * it follows super similar rule.
         *
         * If it's public, and you disable it, nuke it.
         */
        if (status != VisitationStatus.PUBLIC && status != VisitationStatus.PRIVATE)
        {
            house.visitationStatuses[status] = value

            if (value)
            {
                if (house.visitationStatusApplies(VisitationStatus.PRIVATE))
                {
                    house.visitationStatuses[VisitationStatus.PRIVATE] = false
                }
            } else
            {
                if (house.visitationStatusApplies(VisitationStatus.PUBLIC))
                {
                    house.visitationStatuses[VisitationStatus.PUBLIC] = false
                }
            }
        }

        // After every single iteration, if the house has no visitation
        // statuses on, just make private
        if (VisitationStatus.entries.none { house.visitationStatusApplies(it) })
        {
            house.visitationStatuses[VisitationStatus.PRIVATE] = true
        }
    }

    fun icon(visitationStatus: VisitationStatus) = when (visitationStatus)
    {
        VisitationStatus.PUBLIC -> XMaterial.GREEN_DYE
        VisitationStatus.PARTY -> XMaterial.MAGENTA_DYE
        VisitationStatus.FRIENDS -> XMaterial.BLUE_DYE
        VisitationStatus.GUILD -> XMaterial.YELLOW_DYE
        else ->
        {
            XMaterial.RED_DYE
        }
    }
}