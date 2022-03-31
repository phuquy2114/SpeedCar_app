package com.speed.car.model

import com.speed.car.interfaces.OnGpsServiceUpdate

/**
 * Created by fly on 17/04/15.
 */
class Data() {

    var isRunning = false
    var time: Long = 0
    var timeStopped: Long = 0
    var isFirstTime = false
    var distance = 0.0
    var curSpeed = 0.0

    var maxSpeed = 0.0
    private var onGpsServiceUpdate: OnGpsServiceUpdate? = null

    fun setOnGpsServiceUpdate(onGpsServiceUpdate: OnGpsServiceUpdate?) {
        this.onGpsServiceUpdate = onGpsServiceUpdate
    }

    fun update() {
        onGpsServiceUpdate!!.update()
    }

    constructor(onGpsServiceUpdate: OnGpsServiceUpdate?) : this() {
        setOnGpsServiceUpdate(onGpsServiceUpdate)
    }

    fun addDistance(distance: Double) {
        this.distance = this.distance + distance
    }

    val averageSpeed: Double
        get() {
            val average: Double = if (time <= 0) {
                0.0
            } else {
                distance / (time / 1000.0) * 3.6
            }
            return average
        }

    val averageSpeedMotion: Double
        get() {
            val motionTime = time - timeStopped
            val average = if (motionTime <= 0) {
                0.0
            } else {
                distance / (motionTime / 1000.0) * 3.6
            }
            return average
        }

    @JvmName("setCurSpeed1")
    fun setCurSpeed(curSpeed: Double) {
        this.curSpeed = curSpeed
        if (curSpeed > maxSpeed) {
            maxSpeed = curSpeed
        }
    }
}