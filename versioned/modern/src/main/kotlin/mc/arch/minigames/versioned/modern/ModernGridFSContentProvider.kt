package mc.arch.minigames.versioned.modern

import com.infernalsuite.asp.api.loaders.SlimeLoader
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import gg.scala.commons.consensus.Locks
import gg.scala.store.ScalaDataStoreShared

/**
 * @author Subham
 * @since 7/18/25
 */
object ModernGridFSContentProvider : SlimeLoader
{
    private val database = ScalaDataStoreShared.Companion.INSTANCE
        .getNewMongoConnection()
        .getConnection()
        .getDatabase("UGC")

    private val bucket = GridFSBuckets.create(database, "moderncontent")

    init
    {
        database.getCollection("content")
            .createIndex(
                Indexes.ascending("name"),
                IndexOptions().unique(true)
            )
    }

    override fun readWorld(worldName: String) = Locks
        .withGlobalLock("ugc", worldName) {
            bucket
                .openDownloadStream(worldName)
                .readAllBytes()
        }.join()

    override fun worldExists(p0: String): Boolean = Locks
        .withGlobalLock("ugc", p0) {
            bucket
                .find(Filters.eq("name", p0))
                .firstOrNull() != null
        }.join() == true

    override fun listWorlds() = listOf<String>()
    override fun saveWorld(worldName: String, serializedWorld: ByteArray)
    {
        if (worldName.startsWith("temporary_"))
        {
            return
        }

        Locks
            .withGlobalLock("ugc", worldName) {
                bucket.openUploadStream(worldName).write(serializedWorld)
            }.join()
    }

    override fun deleteWorld(p0: String)
    {
        if (p0.startsWith("temporary_"))
        {
            return
        }

        Locks.withGlobalLock("ugc", p0) {
            bucket
                .find(Filters.eq("name", p0))
                .firstOrNull()
                ?.let {
                    bucket.delete(it.objectId)
                }
        }.join()
    }
}
