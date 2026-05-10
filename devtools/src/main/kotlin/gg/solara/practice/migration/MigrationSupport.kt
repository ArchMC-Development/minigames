package gg.solara.practice.migration

import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import gg.scala.store.ScalaDataStoreShared
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ExecutionException

internal const val LEGACY_PENDING_BUCKET = "legacy-pending-import"
internal const val MODERN_PENDING_BUCKET = "modern-pending-import"

private val classLoader = MapLegacyExporter::class.java.classLoader

internal val swmAvailable: Boolean = runCatching {
    Class.forName("com.grinderwolf.swm.api.SlimePlugin", false, classLoader)
}.isSuccess

internal val aspAvailable: Boolean = runCatching {
    Class.forName("com.infernalsuite.asp.api.AdvancedSlimePaperAPI", false, classLoader)
}.isSuccess

internal fun pendingBucket(name: String): GridFSBucket =
    ScalaDataStoreShared.INSTANCE.getNewMongoConnection().getConnection().getDatabase("UGC").let { db ->
        db.getCollection("$name.files")
            .createIndex(Indexes.ascending("filename"), IndexOptions().unique(true))
        GridFSBuckets.create(db, name)
    }

internal fun unwrap(throwable: Throwable): Throwable
{
    var cursor: Throwable = throwable
    while (cursor is InvocationTargetException || cursor is ExecutionException)
    {
        cursor = cursor.cause ?: break
    }
    return cursor
}
