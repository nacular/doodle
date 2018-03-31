//package com.nectar.doodle.animation.units
//
//import com.nectar.doodle.units.Measure
//import com.nectar.doodle.units.Unit
//import com.nectar.doodle.units.times
//
///**
// * Created by Nicholas Eddy on 3/30/18.
// */
//
//interface Distance
//
//val unit = Unit<Distance>(" u"                          )
//
//val Int.   unit: Measure<Distance> get() = this * com.nectar.doodle.animation.units.unit
//val Float. unit: Measure<Distance> get() = this * com.nectar.doodle.animation.units.unit
//val Long.  unit: Measure<Distance> get() = this * com.nectar.doodle.animation.units.unit
//val Double.unit: Measure<Distance> get() = this * com.nectar.doodle.animation.units.unit
//
//
//class CompoundUnit<N, D, T>(display: String, multiplier: Double = 1.0): Unit<T>(display, multiplier) {
//
//}
//
//class CompoundMeasure<N, D, T>(private val numerator: Measure<N>, private val denominator: Unit<D>): Measure<CompoundUnit<N, D, T>>(0, ) {
//
//    override fun toString() = "$numerator/$denominator"
//
//}