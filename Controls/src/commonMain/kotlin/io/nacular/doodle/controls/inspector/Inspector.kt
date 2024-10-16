package io.nacular.doodle.controls.inspector

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.HorizontalFlowLayout
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Extractor

public abstract class Field<T>

public class NamedField<T, C>(private val name           : String,
                              private val nameVisualizer : ItemVisualizer<String, Any>,
                              private val valueVisualizer: ItemVisualizer<T, C>,
                              private val layout         : (ConstraintDslContext.(Bounds, Bounds) -> Unit)?
): ItemVisualizer<T, C> {
    override fun invoke(item: T, previous: View?, context: C): View = object: View() {
        init {
            children += listOf(nameVisualizer(name, null, Unit), valueVisualizer(item, previous, context))

            layout = when (val l = this@NamedField.layout) {
                null -> HorizontalFlowLayout(horizontalSpacing = 5.0, verticalSpacing = 5.0).also {
                    width  = children[0].width + 5.0 + children[1].width
                }
                else -> layout /*{
                    object: Layout {
                        val delegate = constrain(children[0], children[1], l)

                        override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size): Size {
                            delegate.layout(views, min, current, max)

                            return Size(current.width, max(children[0].bounds.bottom, children[1].bounds.bottom))
                        }

                    }
                }*/
            }
        }
    }
}

public interface FieldFactory<T> {
    public fun <R> field(extractor: Extractor<T, R>, visualizer: ItemVisualizer<R, Any>): Field<R>
}

public open class Inspector<T>(public val value: T, private val block: FieldFactory<T>.() -> Unit): View() {
    private class FieldImpl<T>(val view: View): Field<T>()

    private inner class FieldFactoryImpl: FieldFactory<T> {
        override fun <R> field(extractor: Extractor<T, R>, visualizer: ItemVisualizer<R, Any>) = FieldImpl<R>(visualizer(extractor(value), null, Unit)).also {
            fields += it
        }
    }

    private val fields = mutableListOf<FieldImpl<*>>()

    override var layout: Layout? = null

    public var behavior: Behavior<Inspector<T>>? by behavior { _, new ->
        new?.also {
            if (children.isEmpty()) {
                factory.apply(block)

                children += fields.map { it.view }

                if (layout == null) {
                    layout = object: Layout {
                        override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
                            var y = 0.0

                            views.forEach {
                                it.updateBounds(0.0, y, Size(current.width, 0.0), Size.Infinite).also {
                                    y += it.height
                                }
                            }

                            return current
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

    override fun contains(point: Point): Boolean = behavior?.contains(this, point) ?: super.contains(point)
}