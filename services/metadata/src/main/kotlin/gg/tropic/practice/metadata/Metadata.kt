package gg.tropic.practice.metadata

/**
 * @author GrowlyX
 * @since 3/28/2025
 */
object Metadata
{
    private val writer by lazy { MetadataWriter() }
    private val reader by lazy { MetadataReader() }

    fun reader() = reader
    fun writer() = writer
}
