package gg.tropic.practice.replication

data class MapReplication(
    val server: String,
    val associatedMapName: String,
    val name: String,
    val inUse: Boolean = false
)
