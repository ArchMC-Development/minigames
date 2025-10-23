package gg.tropic.practice.ugc

import gg.scala.commons.annotations.Model
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.UUID

/**
 * @author Subham
 * @since 7/18/25
 */
@Model
data class PersistentWorld(
    @Indexed
    val owner: UUID,
    @Indexed
    val contentId: String,
    @Indexed
    val worldType: WorldInstanceProviderType = WorldInstanceProviderType.REALM,
    val created: Long = System.currentTimeMillis(),
    var lastSaved: Long = System.currentTimeMillis(),
    override val identifier: UUID = UUID.randomUUID()
) : IDataStoreObject
