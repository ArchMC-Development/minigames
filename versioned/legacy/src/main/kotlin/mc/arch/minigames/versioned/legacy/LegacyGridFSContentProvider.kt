package mc.arch.minigames.versioned.legacy

import com.grinderwolf.swm.api.loaders.SlimeLoader
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
object LegacyGridFSContentProvider : SlimeLoader
{
    private val database = ScalaDataStoreShared.Companion.INSTANCE
        .getNewMongoConnection()
        .getConnection()
        .getDatabase("UGC")

    private val bucket = GridFSBuckets.create(database, "legacycontent")

    init
    {
        database.getCollection("content")
            .createIndex(
                Indexes.ascending("name"),
                IndexOptions().unique(true)
            )
    }

    override fun loadWorld(p0: String, p1: Boolean) = Locks
        .withGlobalLock("ugc", p0) {
            bucket
                .openDownloadStream(p0)
                .readAllBytes()
        }.join()

    override fun worldExists(p0: String): Boolean = Locks
        .withGlobalLock("ugc", p0) {
            bucket
                .find(Filters.eq("name", p0))
                .firstOrNull() != null
        }.join() == true

    override fun listWorlds() = listOf<String>()
    override fun saveWorld(p0: String, p1: ByteArray, p2: Boolean)
    {
        if (p0.startsWith("temporary_"))
        {
            return
        }

        Locks
            .withGlobalLock("ugc", p0) {
                bucket.openUploadStream(p0).write(p1)
            }.join()
    }

    override fun unlockWorld(p0: String?)
    {
        // Locked worlds don't matter, as world systems are managed by us
    }

    override fun isWorldLocked(p0: String?) = false
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
