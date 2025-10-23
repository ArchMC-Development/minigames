package gg.tropic.practice.replacements

fun String.template(
    replacement: (String, String) -> String
): String
{
    val pattern = Regex("<([^_]+)_([^>]+)>")

    return pattern.replace(this) { matchResult ->
        val key = matchResult.groupValues[1]
        val data = matchResult.groupValues[2]
        replacement(key, data)
    }
}
