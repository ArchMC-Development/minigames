package gg.tropic.practice.map.metadata.sign

import org.bukkit.block.Sign

/**
 * Cross-version sign-text reader. Prefers `Sign#getSide(Side.FRONT).getLines()` (Paper
 * 1.20+, where the deprecated `getLines()` can return empty strings for component-set
 * text); falls back to `getLines()` on older servers without `Side`.
 */
object SignLineReader
{
    private val frontSideLinesAccessor: ((Sign) -> Array<String>)? = runCatching {
        val sideClass = Class.forName("org.bukkit.block.sign.Side")
        val getSide = Sign::class.java.getMethod("getSide", sideClass)
        val front = sideClass.getField("FRONT").get(null)
        val getLines = Class.forName("org.bukkit.block.sign.SignSide").getMethod("getLines")

        return@runCatching { sign: Sign ->
            @Suppress("UNCHECKED_CAST")
            getLines.invoke(getSide.invoke(sign, front)) as Array<String>
        }
    }.getOrNull()

    fun read(sign: Sign): List<String>
    {
        frontSideLinesAccessor?.let { accessor ->
            runCatching { return accessor(sign).toList() }
        }

        @Suppress("DEPRECATION")
        return sign.lines.toList()
    }
}
