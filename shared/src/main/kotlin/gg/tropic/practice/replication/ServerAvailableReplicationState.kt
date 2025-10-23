package gg.tropic.practice.replication

data class ServerAvailableReplicationState(
    val replications: Map<String, List<MapReplication>>
)
