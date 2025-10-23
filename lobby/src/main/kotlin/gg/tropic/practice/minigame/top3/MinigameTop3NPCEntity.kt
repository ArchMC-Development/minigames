package gg.tropic.practice.minigame.top3

import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Location

/**
 * @author Subham
 * @since 6/29/25
 */
class MinigameTop3NPCEntity(
    val position: String,
    val statistic: String,
    val playerName: String?,
    location: Location
) : NpcEntity(
    lines = listOf("Top 3"),
    location = location
)
{
    init
    {
        persistent = false
    }

    fun configure()
    {
        initializeData()
        EntityHandler.trackEntity(this)

        if (playerName == null)
        {
            updateLines(listOf(
                "${CC.B_WHITE}$position",
                "${CC.RED}???",
                "${CC.GRAY}$statistic"
            ))
            updateTexture(
                "ewogICJ0aW1lc3RhbXAiIDogMTc0NTYwNjQ0MjQzMCwKICAicHJvZmlsZUlkIiA6ICI2YWVmMjM3Y2RhNDY0Y2QzYTdiZDcxYjg3YzFlMDEwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLYWlqdW5va2kiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI5YjRiZDM5MzVhM2VlZTJiZjhjZmI3ZWM4M2NlNTdlY2IxNDg4OGU1ZTJiYzc1MzE5NDU2ZjJjYzYxMjU5YyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                "wF5EANk0hpmwjlAPzUehnejZWYw3hft/65NaTRv8/olx9eHQMNyRAiQ4nnHJPp6DVyc/0Huwk5ve5653P9MZlDaGF7b6VJ6si/BteR2yCYLEGlhv8DQsQhp8cC7bojUwxn0u7j7l8NQOae0uuEv8yh1s6oLzSKIfnYxc05YttBt528ET8JQk7W3UepCOAUvvnC4mSHpj3QY4Qu/6AeD2wQqZ/V+gugtTBD1uKgSnC5Jq3r7h4T5OjVXMpyGVP4KScHDhGqmq+HOxzEZ+iu2m4mnIaxVOjOLgc3lWpMKiFpUevfHg2QywS3DXnaZCg1kh8d8SOOtBRuDhHwREAJKjkK2yZK7d5hp1LZOyEAPs3T3fj16bh+lGeZeC/Rw3khlWYnuATTGNV7zNrKH+E+iZCRazhU/NW738KFsF4BLouBN2YrvMIdhWJF/YtOtYMOJuQ5jZiBbQontvUKurTgD/V/Nq15GQMZQek7lERiGOxH6zoyqoX8NkeeQeqj3KEHdm9t0/o1WZt3S63YAA0+2OrSrTJr7zNITkrveRNEOk8l/0drGTl2sVz3z5XrCVnlDPj81Z+IC/kddhTXwF756Jqi1X4XFkH0FFQUvp2C5BGnwzAl4XcjfFK9GQ02zzkuTVkvuCsC/5NpYia+IOnQpR947wyGWZXgWx0RWep93QCc8=",
            )
        } else
        {
            updateLines(listOf(
                "${CC.B_WHITE}$position",
                "${CC.RED}$playerName",
                "${CC.GRAY}$statistic"
            ))

            updateTextureByUsername(playerName) { complete, _ ->
                if (!complete)
                {
                    // Question mark skin
                    updateTexture(
                        "ewogICJ0aW1lc3RhbXAiIDogMTc0NTYwNjQ0MjQzMCwKICAicHJvZmlsZUlkIiA6ICI2YWVmMjM3Y2RhNDY0Y2QzYTdiZDcxYjg3YzFlMDEwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLYWlqdW5va2kiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI5YjRiZDM5MzVhM2VlZTJiZjhjZmI3ZWM4M2NlNTdlY2IxNDg4OGU1ZTJiYzc1MzE5NDU2ZjJjYzYxMjU5YyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                        "wF5EANk0hpmwjlAPzUehnejZWYw3hft/65NaTRv8/olx9eHQMNyRAiQ4nnHJPp6DVyc/0Huwk5ve5653P9MZlDaGF7b6VJ6si/BteR2yCYLEGlhv8DQsQhp8cC7bojUwxn0u7j7l8NQOae0uuEv8yh1s6oLzSKIfnYxc05YttBt528ET8JQk7W3UepCOAUvvnC4mSHpj3QY4Qu/6AeD2wQqZ/V+gugtTBD1uKgSnC5Jq3r7h4T5OjVXMpyGVP4KScHDhGqmq+HOxzEZ+iu2m4mnIaxVOjOLgc3lWpMKiFpUevfHg2QywS3DXnaZCg1kh8d8SOOtBRuDhHwREAJKjkK2yZK7d5hp1LZOyEAPs3T3fj16bh+lGeZeC/Rw3khlWYnuATTGNV7zNrKH+E+iZCRazhU/NW738KFsF4BLouBN2YrvMIdhWJF/YtOtYMOJuQ5jZiBbQontvUKurTgD/V/Nq15GQMZQek7lERiGOxH6zoyqoX8NkeeQeqj3KEHdm9t0/o1WZt3S63YAA0+2OrSrTJr7zNITkrveRNEOk8l/0drGTl2sVz3z5XrCVnlDPj81Z+IC/kddhTXwF756Jqi1X4XFkH0FFQUvp2C5BGnwzAl4XcjfFK9GQ02zzkuTVkvuCsC/5NpYia+IOnQpR947wyGWZXgWx0RWep93QCc8=",
                    )
                }
            }
        }

        updateForCurrentWatchers()
    }
}
