package com.sim

import kotlinx.serialization.Serializable

@Serializable
data class SimulationMetrics(
    val arrived: Int,
    val completed: Int,
    val tisc: List<Double>,
    val tisa: List<Double>,
    val tiqc: List<Double>,
    val tiqa: List<Double>,
)