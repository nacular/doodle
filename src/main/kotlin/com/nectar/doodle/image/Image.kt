package com.nectar.doodle.image

import com.nectar.doodle.geometry.Size


interface Image {
    val size    : Size
    val source  : String
    val isLoaded: Boolean

    fun addMonitor   (monitor: Monitor)
    fun removeMonitor(monitor: Monitor)

    interface Monitor {
        fun dataLoaded(aImage: Image)
    }
}
