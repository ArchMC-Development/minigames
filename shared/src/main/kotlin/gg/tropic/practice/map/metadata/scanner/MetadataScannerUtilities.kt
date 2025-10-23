package gg.tropic.practice.map.metadata.scanner

import gg.tropic.practice.map.metadata.scanner.impl.*

/**
 * @author GrowlyX
 * @since 11/10/2022
 */
object MetadataScannerUtilities
{
    val scanners = mutableListOf(
        MapBedMetadataScanner,
        MapChestMetadataScanner,
        MapEnderChestMetadataScanner,
        MapGenMetadataScanner,
        MapLevelMetadataScanner,
        MapPortalMetadataScanner,
        MapShopKeeperMetadataScanner,
        MapSpawnMetadataScanner,
        MapSpawnProtExpandMetadataScanner,
        MapTeamUpgraderMetadataScanner,
        MapZoneMetadataScanner
    )

    fun matches(type: String) =
        scanners.firstOrNull {
            it.type.equals(type, true)
        }
}
