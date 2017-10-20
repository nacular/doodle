package com.zinoti.jaz.image

import com.zinoti.jaz.geometry.Size


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
