package com.nectar.doodle.controls.table

import com.nectar.doodle.core.View

interface Column<T> {
    val header        : View?
    val width         : Double
    val minWidth      : Double
    val maxWidth      : Double?
    var preferredWidth: Double?
//        val comparator  : Comparator<T>?
}