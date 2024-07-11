package com.sim

import kotlin.math.ln
import kotlin.random.Random

fun exponentialRandom(lambda: Double): Double {
    if (lambda <= 0) {
        throw IllegalArgumentException("Lambda must be positive")
    }
    val uniformRandom = Random.nextDouble()
    return -ln(1 - uniformRandom) / lambda
}

fun calculateIQR(arr: DoubleArray): Pair<Double, Double> {
    val sortedArr = arr.sorted()
    val n = sortedArr.size

    val q1 = if (n % 2 == 0) {
        (sortedArr[n / 4 - 1] + sortedArr[n / 4]) / 2
    } else {
        sortedArr[n / 4]
    }

    val q3 = if (n % 2 == 0) {
        (sortedArr[3 * n / 4 - 1] + sortedArr[3 * n / 4]) / 2
    } else {
        sortedArr[3 * n / 4]
    }

    return q1 to q3
}