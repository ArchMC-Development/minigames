package gg.tropic.practice.commands.customizers

import com.cryptomorin.xseries.XPotion
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.tropic.practice.kit.Kit
import gg.tropic.practice.kit.KitService
import gg.tropic.practice.kit.feature.FeatureFlag
import gg.tropic.practice.kit.group.KitGroup
import gg.tropic.practice.kit.group.KitGroupService
import gg.tropic.practice.statistics.Statistic
import gg.tropic.practice.statistics.StatisticService
import net.evilblock.cubed.util.CC
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

/**
 * @author GrowlyX
 * @since 9/22/2023
 */
object KitCommandCustomizers
{
    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager)
    {
        manager.commandContexts.registerContext(Kit::class.java) {
            val arg = it.popFirstArg()

            KitService.cached().kits[arg]
                ?: throw ConditionFailedException(
                    "No kit with the ID ${CC.YELLOW}$arg${CC.RED} exists."
                )
        }

        manager.commandCompletions
            .registerAsyncCompletion("kits") {
                KitService.cached().kits.keys
            }

        manager.commandCompletions
            .registerAsyncCompletion("stranger-feature-flags-schemakeys") {
                val kit = it.getContextValue(Kit::class.java)
                val flag = it.getContextValue(FeatureFlag::class.java)
                val entry = kit.features[flag] ?: emptyMap()

                flag.schema.keys
                    .filterNot { key ->
                        key in entry.keys
                    }
            }

        manager.commandCompletions
            .registerAsyncCompletion("existing-feature-flags-schemakeys") {
                val kit = it.getContextValue(Kit::class.java)
                val flag = it.getContextValue(FeatureFlag::class.java)
                val entry = kit.features[flag] ?: emptyMap()

                flag.schema.keys
                    .filter { key ->
                        key in entry.keys
                    }
            }

        manager.commandCompletions
            .registerAsyncCompletion("stranger-feature-flags") {
                val kit = it.getContextValue(Kit::class.java)

                FeatureFlag.entries
                    .filter { flag ->
                        kit.lifecycle() in flag.availableLifecycles
                    }
                    .filterNot { flag ->
                        flag in kit.features
                    }
                    .map(FeatureFlag::name)
            }

        manager.commandCompletions
            .registerAsyncCompletion("existing-feature-flags") {
                val kit = it.getContextValue(Kit::class.java)

                FeatureFlag.entries
                    .filter { flag ->
                        flag in kit.features
                    }
                    .map(FeatureFlag::name)
            }

        manager.commandCompletions
            .registerAsyncCompletion("stranger-kit-groups-to-kit") {
                val kit = it.getContextValue(Kit::class.java)

                KitGroupService.cached()
                    .groups
                    .filterNot { group ->
                        kit.id in group.contains
                    }
                    .map(KitGroup::id)
            }

        manager.commandCompletions
            .registerAsyncCompletion(
                "effects"
            ) {
                XPotion.entries.map { it.name }
            }

        manager.commandContexts
            .registerContext(
                PotionEffectType::class.java
            ) {
                val arg = it.popFirstArg()

                runCatching {
                    XPotion.valueOf(arg).potionEffectType
                }.getOrNull()
                    ?: throw ConditionFailedException(
                        "No potion effect with the ID $arg exists."
                    )
            }

        manager.commandCompletions
            .registerAsyncCompletion(
                "effects"
            ) {
                PotionType.entries.map { it.name }
            }

        manager.commandContexts
            .registerContext(
                PotionType::class.java
            ) {
                val arg = it.popFirstArg()

                runCatching {
                    PotionType.valueOf(arg)
                }.getOrNull()
                    ?: throw ConditionFailedException(
                        "No potion type with the ID $arg exists."
                    )
            }

        manager.commandCompletions
            .registerAsyncCompletion("associated-kit-groups-with-kit") {
                val kit = it.getContextValue(Kit::class.java)

                KitGroupService.cached()
                    .groups
                    .filter { group ->
                        kit.id in group.contains
                    }
                    .map(KitGroup::id)
            }
    }
}
