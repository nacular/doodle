package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.ConstraintBlockContext
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.HorizontalFlowLayout
import io.nacular.doodle.layout.constrain
import kotlin.math.max

// FIXME: Centralize
typealias Extractor<T, R>  = T.() -> R

abstract class Field<T> {
    internal abstract val view: View
}

class NamedField<T, C>(private val name           : String,
                       private val nameVisualizer : ItemVisualizer<String, Any>,
                       private val valueVisualizer: ItemVisualizer<T, C>,
                       private val layout         : (ConstraintBlockContext.(Constraints, Constraints) -> Unit)?
): ItemVisualizer<T, C> {
    override fun invoke(item: T, previous: View?, context: C) = object: View() {
        init {
            children += listOf(nameVisualizer(name, null, Unit), valueVisualizer(item, previous, context))

            layout = when (val l = this@NamedField.layout) {
                null -> HorizontalFlowLayout().also {
                    width  = children[0].width + children[1].width
                    height = 50.0 //max(children[0].height, children[1].height)
                }
                else -> {
                    object : Layout {
                        val delegate = constrain(children[0], children[1], l)

                        override fun layout(container: PositionableContainer) {
                            delegate.layout(container)

                            height = max(children[0].bounds.bottom, children[1].bounds.bottom)
                        }
                    }.also {
                        // FIXME
                        height = max(children[0].bounds.bottom, children[1].bounds.bottom)
                    }
                }
            }
        }
    }
}

interface FieldFactory<T> {
    fun <R> field(extractor: Extractor<T, R>, visualizer: ItemVisualizer<R, Any>): Field<R>
}

interface FieldPositioner<T> {
    fun invoke(form: Form<T>, fields: List<Field<*>>)
}

open class Form<T>(val value: T, private val block: FieldFactory<T>.() -> Unit): View() {
    private class FieldImpl<T>(override val view: View): Field<T>()

    private inner class FieldFactoryImpl: FieldFactory<T> {
        override fun <R> field(extractor: Extractor<T, R>,
                              visualizer: ItemVisualizer<R, Any>
        ): Field<R> = FieldImpl<R>(visualizer(extractor(value), null, Unit)).also {
            fields += it
        }
    }

    private val fields = mutableListOf<FieldImpl<*>>()

    var fieldPositioner: FieldPositioner<T>? = null

    var behavior: Behavior<Form<T>>? by behavior { _, new ->
        new?.also {
            if (children.isEmpty()) {
                factory.apply(block)

                children += fields.map { it.view }

                if (layout == null) {
                    layout = object: Layout {
                        override fun layout(container: PositionableContainer) {
                            var y = 0.0

                            container.children.forEach {
                                it.bounds = Rectangle(0.0, y, container.width, it.height)
                                y += it.height
                            }
                        }
                    }
                }
            }
        }
    }

    protected val factory: FieldFactory<T> = FieldFactoryImpl()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }
}