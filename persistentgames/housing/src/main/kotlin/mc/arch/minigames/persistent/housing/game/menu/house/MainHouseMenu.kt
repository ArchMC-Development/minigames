package mc.arch.minigames.persistent.housing.game.menu.house

import com.cryptomorin.xseries.XMaterial
import mc.arch.minigames.persistent.housing.api.content.HousingTime
import mc.arch.minigames.persistent.housing.api.content.HousingWeather
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.game.menu.house.events.EventActionSelectionMenu
import mc.arch.minigames.persistent.housing.game.menu.house.settings.HouseSettingsMenu
import mc.arch.minigames.persistent.housing.game.menu.house.visitation.HouseVisitationRuleMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

class MainHouseMenu(val house: PlayerHouse, val adminMenu: Boolean) : Menu("Viewing ${house.displayName}")
{
    init
    {
        if (!adminMenu)
        {
            placeholder = true
        }
    }

    companion object
    {
        fun mainMenuButton(house: PlayerHouse) =
            ItemBuilder.of(XMaterial.NETHER_STAR)
                .name("${CC.GREEN}Main Menu")
                .toButton { player, _ ->
                    MainHouseMenu(house, true).openMenu(player!!)
                }
    }

    override fun size(buttons: Map<Int, Button>): Int = if (adminMenu) 45 else 27

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        if (adminMenu)
        {
            buttons[2] = ItemBuilder.of(XMaterial.REPEATER)
                .name("${CC.GREEN}Event Actions")
                .addToLore(
                    "${CC.GRAY}Want to trigger an action",
                    "${CC.GRAY}when someone joins or breaks a block?",
                    "${CC.GRAY}Check out our custom event action system.",
                    "",
                    "${CC.YELLOW}Click to view event actions!"
                ).toButton { _, _ ->
                    EventActionSelectionMenu(house).openMenu(player)
                    Button.playNeutral(player)
                }

            buttons[3] = ItemBuilder.of(XMaterial.VILLAGER_SPAWN_EGG)
                .name("${CC.GREEN}NPC Editor")
                .addToLore(
                    "${CC.GRAY}Want to add custom NPCs to",
                    "${CC.GRAY}your realm? Edit them here!",
                    "",
                    "${CC.YELLOW}Click to view NPCs!"
                ).toButton { _, _ ->

                }

            buttons[4] = ItemBuilder.of(XMaterial.OAK_SIGN)
                .name("${CC.GREEN}Hologram Editor")
                .addToLore(
                    "${CC.GRAY}Want to add custom Holograms to",
                    "${CC.GRAY}your realm? Edit them here!",
                    "",
                    "${CC.YELLOW}Click to view Holograms!"
                ).toButton { _, _ ->

                }

            buttons[5] = ItemBuilder.of(XMaterial.FILLED_MAP)
                .name("${CC.GREEN}Groups and Permissions")
                .addToLore(
                    "${CC.GRAY}Want to add custom groups",
                    "${CC.GRAY}to your realm? Edit them here!",
                    "",
                    "${CC.YELLOW}Click to view groups!"
                ).toButton { _, _ ->

                }

            buttons[6] = ItemBuilder.of(XMaterial.COMMAND_BLOCK)
                .name("${CC.GREEN}Player Management")
                .addToLore(
                    "${CC.GRAY}Want to ban players or give",
                    "${CC.GRAY}players roles? Do it here!",
                    "",
                    "${CC.YELLOW}Click to view player management panel!"
                ).toButton { _, _ ->

                }

            buttons[11] = ItemBuilder.of(XMaterial.DEAD_BUSH)
                .name("${CC.GREEN}Weather")
                .addToLore(
                    "${CC.GRAY}Update the weather of",
                    "${CC.GRAY}your realm!",
                    "",
                    "${CC.WHITE}Currently ${(house.housingWeather ?: HousingWeather.CLEAR).displayName}",
                    "",
                    "${CC.YELLOW}Click to edit cycle options!"
                ).toButton { _, _ ->

                }

            buttons[12] = ItemBuilder.of(XMaterial.CLOCK)
                .name("${CC.GREEN}Time")
                .addToLore(
                    "${CC.GRAY}Update the time of",
                    "${CC.GRAY}your realm!",
                    "",
                    "${CC.WHITE}Currently ${(house.housingTime ?: HousingTime.DAY).displayName}",
                    "",
                    "${CC.YELLOW}Click to edit cycle options!"
                ).toButton { _, _ ->

                }

            buttons[27] = ItemBuilder.of(XMaterial.JUNGLE_DOOR)
                .name("${CC.GREEN}Travel to someone else's realm")
                .addToLore(
                    "${CC.GRAY}Want to see what others are up to?",
                    "${CC.GRAY}Come explore all open realms.",
                    "",
                    "${CC.YELLOW}Click to view realms you can join!"
                ).toButton { _, _ ->

                }


            buttons[38] = ItemBuilder.of(XMaterial.PLAYER_HEAD)
                .name("${CC.GREEN}Visiting Rules")
                .addToLore(
                    "${CC.GRAY}Allows you to select who can",
                    "${CC.GRAY}and cannot visit your realm.",
                    "",
                    "${CC.YELLOW}Click to configure!"
                ).toButton { _, _ ->
                    HouseVisitationRuleMenu(house).openMenu(player)
                    Button.playNeutral(player)
                }

            buttons[39] = ItemBuilder.of(XMaterial.COMPARATOR)
                .name("${CC.GREEN}Realm Settings")
                .addToLore(
                    "${CC.GRAY}Allows you to change and",
                    "${CC.GRAY}view specific settings about",
                    "${CC.GRAY}your realm.",
                    "",
                    "${CC.YELLOW}Click to configure!"
                ).toButton { _, _ ->
                    HouseSettingsMenu(house).openMenu(player)
                    Button.playNeutral(player)
                }
        } else
        {

        }

        return buttons
    }
}