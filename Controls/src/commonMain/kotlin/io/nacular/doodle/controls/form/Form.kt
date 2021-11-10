package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.form.Form.Field
import io.nacular.doodle.controls.form.Form.FieldState
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.observable

/**
 * Defines the visualization of a [Field] within a [Form].
 */
public interface FieldVisualizer<T> {
    /**
     * @return a view to associate with [field]
     */
    public operator fun invoke(field: Field<T>): View
}

/**
 * Helper for creating a [FieldVisualizer] from a lambda.
 *
 * @param block is called by the returned visualizer to create a view
 */
public fun <T> field(block: Field<T>.() -> View): FieldVisualizer<T> = object: FieldVisualizer<T> {
    override fun invoke(field: Field<T>) = block(field)
}

/**
 * A visual component that serves as a strongly-typed constructor of some arbitrary type. Forms are very similar to constructors
 * in that they have typed parameter lists (fields), and can only create instances when all their inputs are valid. Like
 * any constructor, a Form can have optional fields, default values, and arbitrary types for its fields.
 *
 * Forms also have a `Behavior`, `Layout` and some other properties of a Container to allow customization.
 */
@Suppress("UNCHECKED_CAST")
public class Form private constructor(first: Field<*>, vararg rest: Field<*>, stateChanged: PropertyObserver<Form, State>): View() {

    /**
     * The current state of a [Field].
     */
    public sealed class FieldState<T>

    /**
     * [Field]s with this state are invalid and will prevent their [Form] from
     * becoming ready.
     */
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

    /**
     * [Field]s with this state have a value and no longer block their [Form] from
     * becoming ready.
     */
    @Suppress("EqualsOrHashCode")
    public class Valid<T>(public val value: T): FieldState<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Valid<*>) return false

            if (value != other.value) return false

            return true
        }
    }

    /**
     * An entry within a [Form] that represents a single parameter and value that will be presented
     * when the form becomes ready.
     */
    public class Field<T> internal constructor(internal val visualizer: FieldVisualizer<T>, initial: FieldState<T> = Invalid()) {
        internal var index = 0

        /**
         * The field's current state
         */
        public var state: FieldState<T> = initial
            internal set(new) {
                if (new == field) {
                    return
                }

                field = new
                form.updateState()
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

    // Used to ignore any calls to update state until after all fields have been
    // initialized. This is important to allow all fields to validate initial data
    private var initializing = true

    init {
        fields.forEachIndexed { index, field ->
            field.form  = this
            field.index = index
        }

        children += fields.map { field ->
            (field as Field<Any>).visualizer.invoke(field)
        }

        initializing = false

        updateState()
    }

    private fun updateState() {
        if (initializing) {
            return
        }

        state = when {
            fields.all { it.state is Valid } -> Ready(fields.map { (it.state as Valid).value })
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

        public class FormDefinition internal constructor(internal val form: Form)

        public class FormBuildContext internal constructor() {
            /**
             * DSL for binding an arbitrary value to a [FieldVisualizer] to create a [Field] with
             * an initial state of `Valid(this)` within a [Form].
             *
             * NOTE: the [FieldVisualizer] ultimately decides what inputs are valid, so the value
             * of [T] used may be ignored if the visualizer deemed it invalid.
             *
             * ```kotlin
             * Form {
             *     this(
             *         someValue to field { ... }, // trys to bind someValue to the created Field
             *         ...
             *     ) { first, ... ->
             *         ...
             *     }
             * }
             * ```
             */
            public infix fun <T> T.to(visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer, initial = Valid(this))

            /**
             * DSL for binding a [FieldState] to a [FieldVisualizer] to create a [Field] with
             * an initial state equal to the given FieldState within a [Form].
             *
             * NOTE: the [FieldVisualizer] ultimately decides what inputs are valid, so a [Valid] [FieldState]
             * may be ignored if the visualizer deems it invalid.
             *
             * ```kotlin
             * Form {
             *     this(
             *         someField to field { ... }, // trys to bind someField to the created Field
             *         ...
             *     ) { first, ... ->
             *         ...
             *     }
             * }
             * ```
             */
            public infix fun <T> FieldState<T>.to(visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer, initial = this)

            /**
             * DSL for adding a [Field] with an initial value of [Invalid] to a [Form].
             * The [FieldVisualizer] can put the created field into a [Valid] state; so
             * there is no guarantee that the created field is actually invalid.
             *
             * ```kotlin
             * Form {
             *     this(
             *         + field { ... }, // Field defaults to invalid unless its visualizer sets a valid value
             *         ...
             *     ) { first, ... ->
             *         ...
             *     }
             * }
             * ```
             */
            public operator fun <T> FieldVisualizer<T>.unaryPlus(): Field<T> = Field(this, initial = Invalid())

            /**
             * Defines a [Form] with a single [Field].
             *
             * @param a the form's only field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A> invoke(
                    a        : Field<A>,
                    onInvalid: ( ) -> Unit,
                    onReady  : (A) -> Unit): FormDefinition = FormDefinition(Form(a) { _,_,state ->
                when (state) {
                    is Ready -> onReady  (state.values[0] as A)
                    else     -> onInvalid()
                }
            })

            /**
             * Defines a [Form] with a 2 [Field]s.
             *
             * @param a the form's first field
             * @param b the form's second field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    onInvalid: (   ) -> Unit,
                    onReady  : (A,B) -> Unit): FormDefinition = FormDefinition(Form(a, b) { _,_,state ->
                var i = 0

                when (state) {
                    is Ready -> onReady  (state.values[i++] as A, state.values[i] as B)
                    else     -> onInvalid()
                }
            })

            /**
             * Defines a [Form] with a 3 [Field]s.
             *
             * @param a the form's first field
             * @param b the form's second field
             * @param c the form's third field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    onInvalid: (     ) -> Unit,
                    onReady  : (A,B,C) -> Unit): FormDefinition = FormDefinition(Form(a, b, c) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i] as C)
                    else     -> onInvalid()
                }
            })

            /**
             * Defines a [Form] with a 4 [Field]s.
             *
             * @param a the form's first field
             * @param b the form's second field
             * @param c the form's third field
             * @param d the form's fourth field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady called whenever the form has new input and all its fields are [Valid]
             */
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

            /**
             * Defines a [Form] with a 5 [Field]s.
             *
             * @param a the form's first field
             * @param b the form's second field
             * @param c the form's third field
             * @param d the form's fourth field
             * @param e the form's fifth field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C, D, E> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    d        : Field<D>,
                    e        : Field<E>,
                    onInvalid: (             ) -> Unit,
                    onReady  : (A, B, C, D, E) -> Unit): FormDefinition = FormDefinition(Form(a, b, c, d, e) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i++] as C, state.values[i++] as D, state.values[i] as E)
                    else     -> onInvalid()
                }
            })

            /**
             * Defines a weakly-typed [Form] with at least 2 [Field]s.
             *
             * @param first the form's first field
             * @param second the form's second field
             * @param rest the form's remaining fields
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun invoke(
                           first    : Field<*>,
                           second   : Field<*>,
                    vararg rest     : Field<*>,
                           onInvalid: (       ) -> Unit,
                           onReady  : (List<*>) -> Unit): FormDefinition = FormDefinition(Form(first, second, *rest) { _,_,state ->
                when (state) {
                    is Ready -> onReady(state.values)
                    else     -> onInvalid()
                }
            })
        }

        /**
         * [Form] builder DSL that allows constructs as follows:
         *
         * ```kotlin
         * Form {
         *     this(
         *         someValue to field { ... },
         *         +            field { ... }
         *     ) { first, second ->
         *         SomeType(first, second)
         *     }
         * }
         * ```
         */
        public operator fun invoke(builder: FormBuildContext.() -> FormDefinition): Form = builder(FormBuildContext()).form
    }
}

/**
 * Returns the result of [onValid] if this instance is [valid][Form.Valid] or [default] if it is [invalid][Form.Invalid].
 */
public inline fun <R, T> Field<T>.fold(
    onValid: (value: T) -> R,
    default: R
): R = when (val v = state) {
    is Form.Valid<T> -> onValid(v.value)
    else -> default
}

/**
 * Returns a [valid][Form.Valid] state from the result of [onValid] if this instance is [valid][Form.Valid] or [invalid][Form.Invalid] otherwise.
 */
@Suppress("unused")
public inline fun <T, R> Field<T>.mapValue(
    onValid: (value: T) -> R,
): FieldState<R> = when (val v = state) {
    is Form.Valid<T> -> Form.Valid(onValid(v.value))
    else -> Form.Invalid()
}
