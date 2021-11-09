package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.form.Form.Field
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.observable


public interface FieldVisualizer<T> {
    public operator fun invoke(field: Field<T>): View
}

public fun <T> field(block: Field<T>.() -> View): FieldVisualizer<T> = object: FieldVisualizer<T> {
    override fun invoke(field: Field<T>) = block(field)
}

public class Form private constructor(first: Field<*>, vararg rest: Field<*>, stateChanged: PropertyObserver<Form, State>): View() {
    public sealed class FieldState<T>
    public class Invalid<T>: FieldState<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Invalid<*>) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
    @Suppress("EqualsOrHashCode")
    public class Valid<T>(public val value: T): FieldState<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Valid<*>) return false

            if (value != other.value) return false

            return true
        }
    }

    public class Field<T>(internal val visualizer: FieldVisualizer<T>, initial: FieldState<T> = Invalid()) {
        internal var index = 0

        public var value: FieldState<T> = initial
            internal set(new) {
                if (new == field) {
                    return
                }

                field = new
                form.updateState()
            }

        public fun valueOr(default: T): T = when (val v = value) {
            is Valid<T> -> v.value
            else        -> default
        }

        internal lateinit var form: Form
    }

    private sealed class State
    private object NotReady: State()
    private class Ready(val values: List<*>): State() {
        override fun equals(other: Any?): Boolean {
            if (this === other ) return true
            if (other !is Ready) return false
            if (values != other.values) return false

            return true
        }

        override fun hashCode(): Int = values.hashCode()
    }

    private val fields = listOf(first) + rest

    private var state: State by observable(NotReady) { old,new ->
        if (old != new) {
            stateChanged(this, old, new)
        }
    }

    init {
        fields.forEachIndexed { index, field ->
            field.form  = this
            field.index = index
        }

        children += fields.map { field ->
            (field as Field<Any>).visualizer.invoke(field)
        }

        updateState()
    }

    private fun updateState() {
        state = when {
            fields.all { it.value is Valid } -> Ready(fields.map { (it.value as Valid).value })
            else                             -> NotReady
        }
    }

    public override var insets: Insets get() = super.insets; set(new) { super.insets = new }

    public override var layout: Layout? = null

    public override fun relayout() { super.relayout() }

    public var behavior: Behavior<Form>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = behavior?.contains(this, point) ?: super.contains(point)

    public companion object {

        public fun <T> field(            visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer                )
        public fun <T> field(initial: T, visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer, Valid(initial))

        public class FormDefinition internal constructor(internal val form: Form)

        public class FormBuildContext internal constructor() {
            public infix fun <T> T.to(visualizer: FieldVisualizer<T>): Field<T> = field(initial = this, visualizer)
            public infix fun <T> FieldState<T>.to(visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer, initial = this)
            public operator fun <T> FieldVisualizer<T>.unaryPlus(): Field<T> = Field(this, initial = Invalid())

            public operator fun <A> invoke(
                    a        : Field<A>,
                    onInvalid: ( ) -> Unit = {       },
                    onReady  : (A) -> Unit = { _, -> }): FormDefinition = FormDefinition(Form(a) { _,_,state ->
                when (state) {
                    is Ready -> onReady  (state.values[0] as A)
                    else     -> onInvalid()
                }
            })

            public operator fun <A, B> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    onInvalid: (   ) -> Unit = {        },
                    onReady  : (A,B) -> Unit = { _,_ -> }): FormDefinition = FormDefinition(Form(a, b) { _,_,state ->
                var i = 0

                when (state) {
                    is Ready -> onReady  (state.values[i++] as A, state.values[i] as B)
                    else     -> onInvalid()
                }
            })

            public operator fun <A, B, C> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    onInvalid: (     ) -> Unit = {          },
                    onReady  : (A,B,C) -> Unit = { _,_,_ -> }): FormDefinition = FormDefinition(Form(a, b, c) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i] as C)
                    else     -> onInvalid()
                }
            })

            public operator fun <A, B, C, D> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    d        : Field<D>,
                    onInvalid: (          ) -> Unit = {            },
                    onReady  : (A, B, C, D) -> Unit = { _,_,_,_ -> }): FormDefinition = FormDefinition(Form(a, b, c, d) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i++] as C, state.values[i] as D)
                    else     -> onInvalid()
                }
            })

            public operator fun <A, B, C, D, E> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    d        : Field<D>,
                    e        : Field<E>,
                    onInvalid: (             ) -> Unit = {              },
                    onReady  : (A, B, C, D, E) -> Unit = { _,_,_,_,_ -> }): FormDefinition = FormDefinition(Form(a, b, c, d, e) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i++] as C, state.values[i++] as D, state.values[i] as E)
                    else     -> onInvalid()
                }
            })

            public operator fun invoke(
                           first    : Field<*>,
                           second   : Field<*>,
                    vararg rest     : Field<*>,
                           onInvalid: (       ) -> Unit = {},
                           onReady  : (List<*>) -> Unit = {}): FormDefinition = FormDefinition(Form(first, second, *rest) { _,_,state ->
                when (state) {
                    is Ready -> onReady(state.values)
                    else     -> onInvalid()
                }
            })
        }

        public operator fun invoke(builder: FormBuildContext.() -> FormDefinition): Form = builder(FormBuildContext()).form
    }
}

public inline fun <R, T> Field<T>.fold(
    onValid: (value: T) -> R,
    default: R
): R = when (val v = value) {
    is Form.Valid<T> -> onValid(v.value)
    else -> default
}

public inline fun <T, R> Field<T>.mapValue(
    onValid: (value: T) -> R,
): Form.FieldState<R> = when (val v = value) {
    is Form.Valid<T> -> Form.Valid(onValid(v.value))
    else -> Form.Invalid()
}
