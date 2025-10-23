package gg.tropic.practice.replication

data class ReplicationServerStatus(
    val server: String,
    val status: ServerAvailableReplicationState
)
