package com.sim

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

fun main() {
    exportData("fixed.json") { 1.0 }
    exportData("exponential.json") { exponentialRandom(1.0) }
}

fun exportData(fname: String, sampler: RandGen) {
    val path = Path("graphs")
    if (!verifyPath(path)) return

    val simulations = mutableMapOf<Int, SimulationMetrics>()
    listOf(50, 80, 90, 99).forEach {
        val lambda = it / 100.0
        val simulation = Simulation(100, 10_000, { exponentialRandom(lambda) }, sampler)
        simulation.apply {
            generateArrivals()
            runSimulation()
        }
        val summarizer = Summarizer(simulation)

        val metrics = with(summarizer) {
            SimulationMetrics(
                arrived = getArrivedJobCount(),
                completed = getCompletedJobCount(),
                tisa = getAverageTimeInSystemForAllJobs().toList() + getTimeInSystemIQRForAllJobs().toList(),
                tisc = getAverageAndVarianceTimeInSystemForCompletedJobs().toList() + getTimeInSystemIQRForCompletedJobs().toList(),
                tiqa = getAverageTimeInQueueForAllJobs().toList() + getTimeInQueueIQRForAllJobs().toList(),
                tiqc = getAverageTimeInQueueForCompletedJobs().toList() + getTimeInQueueIQRForCompletedJobs().toList(),
            )
        }
        simulations[it] = metrics
        println("lambda: ${lambda}, fname: ${fname}\n" +
                "  Completed: ${metrics.completed}\n" +
                "  Time in System: ${metrics.tisc.subList(0,2).joinToString(", ", "(", ")")}\n" +
                "  Time in Queue: ${metrics.tiqc.subList(0, 2).joinToString(", ", "(", ")")}\n")
    }
    File((path / fname).toString()).writeText(Json.encodeToString(simulations))
}

fun verifyPath(path: Path): Boolean {
    if (path.exists() && !path.isDirectory()) {
        println("./graphs já existe e não é um diretório! Não é possível salvar os dados")
        return false
    }
    if (!path.exists()) path.createDirectory()
    return true
}