package mc.arch.minigames.persistent.housing.game.schematic

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.schematics.Schematic
import gg.tropic.practice.schematics.SchematicUtil
import mc.arch.minigames.persistent.housing.api.island.HousingMapType

@Service
object HousingSchematicService
{
    private val schematics = mutableMapOf<HousingMapType, Schematic>()

    fun findSchematicsOf(type: HousingMapType) = schematics[type]
        ?: schematics.values.first()

    @Configure
    fun configure()
    {
        HousingMapType.entries.forEach { type ->
            val base = SchematicUtil
                .loadSchematicFromStorage(type.mongoName)
                .join()

            schematics[type] = base
        }
    }
}
