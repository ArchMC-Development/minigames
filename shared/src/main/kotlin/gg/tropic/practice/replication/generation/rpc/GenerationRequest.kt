package gg.tropic.practice.replication.generation.rpc

import gg.tropic.practice.expectation.GameExpectation

data class GenerationRequest(
    val server: String,
    val requirement: GenerationRequirement,
    val map: String,
    val expectation: GameExpectation
)
