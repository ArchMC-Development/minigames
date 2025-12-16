package mc.arch.minigames.persistent.housing.api

fun Enum<*>.formatName() =
    this.name.split("_").joinToString(" ") { string -> string.lowercase().replaceFirstChar { it.uppercaseChar() } }