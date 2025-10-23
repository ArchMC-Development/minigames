package gg.tropic.practice.replication.generation.rpc

import gg.tropic.practice.replication.ReplicationResultStatus

data class GenerationResult(
    val status: ReplicationResultStatus,
    val message: String? = null
)
