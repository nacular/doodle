package com.nectar.doodle.controls

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.scheduler.Strand
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/6/19.
 */


interface Model<T>: Iterable<T> {
    val size: Int

    operator fun get(index: Int): T?

    fun isEmpty (                       ) = size == 0
    fun section (range: ClosedRange<Int>): kotlin.collections.List<T>
    fun contains(value: T               ): Boolean
}

open class ListModel<T>(private val list: List<T>): Model<T> {

    override val size get() = list.size

    override fun get     (index: Int             ) = list.getOrNull(index)
    override fun section (range: ClosedRange<Int>) = list.subList (range.start, range.endInclusive + 1)
    override fun contains(value: T               ) = list.contains(value                              )
    override fun iterator(                       ) = list.iterator(                                   )
}

class Table<T: Any, M: Model<T>>(private val strand: Strand, private val model: M, extractor: (T) -> Any, vararg extractors: (T) -> Any): View() {
    private inner class FieldModel<A>(private val model: M, private val extractor: (T) -> A): com.nectar.doodle.controls.list.Model<A> {
        override val size get() = model.size

        override fun get(index: Int) = model[index]?.let(extractor)

        override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

        override fun contains(value: A) = value in model.map(extractor)

        override fun iterator() = model.map(extractor).iterator()
    }

    init {
        val selectionModel = SingleItemSelectionModel<Int>()

        children += listOf(extractor, *extractors).map {
            com.nectar.doodle.controls.list.List(strand, FieldModel(model, it), selectionModel).apply { width = 100.0 }
        }

        // FIXME: Move to Behavior
        layout = object: Layout() {
            override fun layout(positionable: Positionable) {
                var width  = 0.0
                var height = 0.0

                positionable.children.forEach {
                    it.position = Point(width, 0.0)

                    width  += it.width
                    height  = max(height, it.height)
                }

                positionable.size = Size(width, height)
            }
        }
    }

    companion object {
//        operator fun invoke(
//                strand        : Strand,
//                progression   : IntProgression
////                selectionModel: SelectionModel<Int>? = null,
////                fitContent    : Boolean              = true,
//                /*cacheLength   : Int                  = 10*/) =
//                Table(strand, progression.toList())

        operator fun <T: Any> invoke(
                strand        : Strand,
                values        : List<T>,
                extractor     : (T) -> Any,
                vararg extractors: (T) -> Any
//                selectionModel: SelectionModel<Int>? = null,
//                fitContent    : Boolean              = true,
                /*cacheLength   : Int                  = 10*/): Table<T, Model<T>> = Table(strand, ListModel(values), extractor, *extractors)
    }
}