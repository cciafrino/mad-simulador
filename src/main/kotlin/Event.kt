package com.sim

data class Job(val jobId: Int, val serverId: Int) {
}

data class JobMetrics(
    val job: Job,
    val arrivalTime: Double,
    var queueTime: Double? = null,
    val departureTime: Double? = null,
) {
    val timeInSystem: Double?
        get() = departureTime?.let{ it - arrivalTime }
    val servingTime: Double?
        get() = timeInSystem?.let {
            it - queueTime!!
        }
}

sealed interface EventInterface {
    val job: Job
    val time: Double
}

sealed class Event: EventInterface, Comparable<Event> {
    override fun compareTo(other: Event): Int = this.time.compareTo(other.time)
    data class Arrival(override val job: Job, override var time: Double): Event()
    data class Departure(override val job: Job, override var time: Double): Event()
}