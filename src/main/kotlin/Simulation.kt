package com.sim

import java.util.*
import com.sim.Event.Arrival
import com.sim.Event.Departure
import kotlin.math.max


typealias RandGen = () -> Double
typealias OnSimulationEndCallback = Simulation.() -> Unit

class Simulation(
    val n: Int,
    val t: Int,
    private val arrivalFun: RandGen,
    private val departureFun: RandGen,
    private var onSimulationEnd: OnSimulationEndCallback? = null
) {
    private val lastArrivals = Array(n) { 0.0 }
    private val lastDepartures = Array(n) { 0.0 }
    private val serverQueues = Array(n) { LinkedList<JobMetrics>() }
    private val eventHeap = PriorityQueue<Event>()
    val timesInSystem = LinkedList<JobMetrics>()
    val completions = Array(n) { 0 }
    val arrivals = Array(n) { 0 }
    var jobCounter = 0

    fun generateArrivals() {
        var availableArrivals = getAvailableArrivals()
        while (availableArrivals.isNotEmpty()) {
            val nextArrivalServerId = availableArrivals[availableArrivals.indices.random()]
            eventHeap.add(Arrival(createJob(nextArrivalServerId), getNextArrivalTime(nextArrivalServerId)))
            availableArrivals = getAvailableArrivals()
        }
    }

    fun runSimulation() {
        while (eventHeap.isNotEmpty()) {
            val nextEvent = eventHeap.poll()
            if (nextEvent.time > t) break
            processEvent(nextEvent)
        }
        onSimulationEnd?.let { it() }
    }

    private fun processEvent(event: Event) {
        when (event) {
            is Arrival -> processArrival(event)
            is Departure -> processDeparture(event)
        }
    }

    private fun processArrival(arrival: Arrival) {
        val job = arrival.job
        val serverId = job.serverId
        val serverQueue = serverQueues[serverId]
        var timeInQueue: Double? = null
        if (serverQueue.isEmpty()) {
            timeInQueue = 0.0
            eventHeap.add(Departure(job, getNextDepartureTime(arrival.time, serverId)))
        }
        serverQueue.add(JobMetrics(job = job, arrivalTime = arrival.time, queueTime = timeInQueue))
        arrivals[serverId] += 1
    }

    private fun processDeparture(departure: Departure) {
        val job = departure.job
        val serverId = job.serverId
        val serverQueue = serverQueues[serverId]
        serverQueue.pop().let { jobMetrics ->
            timesInSystem.add(jobMetrics.copy(departureTime = departure.time))
            completions[serverId] += 1
        }
        serverQueue.peek()?.let { jobMetrics ->
            jobMetrics.queueTime = departure.time - jobMetrics.arrivalTime
            eventHeap.add(Departure(jobMetrics.job, getNextDepartureTime(jobMetrics.arrivalTime, serverId)))
        }
    }

    private fun getAvailableArrivals() = (0 until n).filter { lastArrivals[it] < t }

    private fun getNextDepartureTime(arrivalTime: Double, serverId: Int): Double {
        val next = max(arrivalTime, lastDepartures[serverId]) + departureFun()
        lastDepartures[serverId] = next
        return next
    }
    private fun getNextArrivalTime(serverId: Int): Double {
        val next = lastArrivals[serverId] + arrivalFun()
        lastArrivals[serverId] = next
        return next
    }

    private fun createJob(serverId: Int) = Job(jobCounter, serverId).apply { jobCounter += 1 }

    fun getNotCompletedJobsArrivals(): List<JobMetrics> {
        return serverQueues.fold(emptyList()) { jobMetrics, serverJobMetrics ->
            jobMetrics + serverJobMetrics.toList()
        }
    }

    fun setOnSimulationEndListener(callback: OnSimulationEndCallback) {
        onSimulationEnd = callback
    }
}