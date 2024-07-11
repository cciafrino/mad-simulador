package com.sim

import kotlin.math.pow

class Summarizer(val sim: Simulation) {
    fun getCompletedJobs() = sim.timesInSystem
    fun getCompletedJobCount() = sim.timesInSystem.size
    fun getCompletedJobCountPerServer() = sim.completions

    fun getArrivedJobs(): List<JobMetrics> {
        val notCompletedJobs = sim
            .getNotCompletedJobsArrivals()
            .filter { it.arrivalTime < sim.t }
        return getCompletedJobs() + notCompletedJobs
    }
    fun getArrivedJobCount() = sim.arrivals.sum()
    fun getArrivedJobCountPerServer() = sim.arrivals

    fun getAverageAndVarianceTimeInSystemForCompletedJobs(): Pair<Double, Double> = with(getCompletedJobs()) {
        val mean = sumOf { it.tis } / this.size
        val variance = sumOf { (it.tis - mean).pow(2) } / this.size
        return (mean to variance)
    }

    fun getTimeInSystemIQRForCompletedJobs(): Pair<Double, Double> =
        calculateIQR(getCompletedJobs().map { it.tis }.toDoubleArray())

    fun getAverageTimeInSystemForCompletedJobsPerServer(): List<Double> = computeAvgTimeInSystemPerServer(getCompletedJobs())


    fun getAverageTimeInSystemForAllJobs(): Pair<Double, Double> {
        val allJobs = getArrivedJobs()
        val times = allJobs.map { it.tis }
        val mean = times.sum() / times.size
        val variance = times.sumOf {
            (it - mean).pow(2)
        } / times.size
        return (mean to variance)
    }
    fun getTimeInSystemIQRForAllJobs(): Pair<Double, Double> =
        calculateIQR(getArrivedJobs().map { it.tis }.toDoubleArray())

    fun getAverageTimeInSystemForAllJobsPerServer(): List<Double> {
        val allJobs = getArrivedJobs()
        return computeAvgTimeInSystemPerServer(allJobs)
    }

    fun getAverageTimeInQueueForCompletedJobs(): Pair<Double, Double> = with(getCompletedJobs()) {
        val mean = sumOf { it.tiq } / this.size
        val variance = sumOf { (it.tiq - mean).pow(2) } / this.size
        return (mean to variance)
    }
    fun getTimeInQueueIQRForCompletedJobs(): Pair<Double, Double> =
        calculateIQR(getCompletedJobs().map { it.tiq }.toDoubleArray())

    fun getAverageTimeInQueueForCompletedJobsPerServer(): List<Double> = computeAvgTimeInQueuePerServer(getCompletedJobs())

    fun getAverageTimeInQueueForAllJobs(): Pair<Double, Double> {
        val allJobs = getArrivedJobs()
        val times = allJobs.map { it.tiq }
        val mean = times.sum() / times.size
        val variance = times.sumOf { (it - mean).pow(2) } / times.size
        return (mean to variance)
    }

    fun getTimeInQueueIQRForAllJobs(): Pair<Double, Double> =
        calculateIQR(getArrivedJobs().map { it.tiq }.toDoubleArray())

    fun getAverageTimeInQueueForAllJobsPerServer(): List<Double> {
        val allJobs = getArrivedJobs()
        return computeAvgTimeInQueuePerServer(allJobs)
    }

    private fun computeAvgTimeInSystemPerServer(jobTimes: List<JobMetrics>): List<Double> {
        val serversCumulativeTime = Array(sim.n) { 0.0 }
        val serversJobCount = Array(sim.n) { 0 }
        jobTimes.forEach { jobMetrics ->
            val job = jobMetrics.job
            serversCumulativeTime[job.serverId] += jobMetrics.tis
            serversJobCount[job.serverId] += 1
        }
        return serversCumulativeTime
            .zip(serversJobCount)
            .asSequence()
            .filter { it.second > 0 }
            .map { (t, c) -> t / c }
            .toList()
    }

    private fun computeAvgTimeInQueuePerServer(jobTimes: List<JobMetrics>): List<Double> {
        val serversCumulativeTime = Array(sim.n) { 0.0 }
        val serversJobCount = Array(sim.n) { 0 }
        jobTimes.forEach { jobMetrics ->
            val job = jobMetrics.job
            serversCumulativeTime[job.serverId] += jobMetrics.tiq
            serversJobCount[job.serverId] += 1
        }
        return serversCumulativeTime
            .zip(serversJobCount)
            .asSequence()
            .filter { it.second > 0 }
            .map { (t, c) -> t / c }
            .toList()
    }

    val JobMetrics.tiq
        get() = queueTime ?: (sim.t - arrivalTime)
    val JobMetrics.tis
        get() = timeInSystem ?: (sim.t - arrivalTime)
}