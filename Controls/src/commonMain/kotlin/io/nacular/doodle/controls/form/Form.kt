package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.form.Form.Field
import io.nacular.doodle.controls.form.Form.FieldState
import io.nacular.doodle.controls.form.Form.Invalid
import io.nacular.doodle.controls.form.Form.Valid
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.observable

/**
 * Defines the visualization of a [Field] within a [Form].
 */
public interface FieldVisualizer<T> {
    /**
     * @param fieldInfo the view is being associated with
     * @return a view to associate with [field]
     */
    public operator fun invoke(fieldInfo: FieldInfo<T>): View
}

public class FieldInfo<T> internal constructor(public val field: Field<T>, public val initial: FieldState<T>) {
    /**
     * Field's state
     */
    public var state: FieldState<T> get() = this.field.state; set(value) { this.field.state = value }

    /**
     * Notifies of changes to the Field's state
     */
    @Suppress("unused")
    public val stateChanged: ChangeObservers<Field<T>> = field.stateChanged
}

/**
 * Helper for creating a [FieldVisualizer] from a lambda.
 *
 * @param block is called by the returned visualizer to create a view
 * @see [FieldVisualizer.invoke]
 */
public fun <T> field(block: FieldInfo<T>.() -> View): FieldVisualizer<T> = object: FieldVisualizer<T> {
    override fun invoke(fieldInfo: FieldInfo<T>) = block(fieldInfo)
}

/**
 * A visual component that serves as a strongly-typed constructor of some arbitrary type. Forms are very similar to constructors
 * in that they have typed parameter lists (fields), and can only "create" instances when all their inputs are valid. Like
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
    public class Field<T> internal constructor(internal val visualizer: FieldVisualizer<T>, internal val initial: FieldState<T> = Invalid()) {
        internal var index = 0

        /**
         * The field's current state
         */
        public var state: FieldState<T> = Invalid()
            internal set(new) {
                if (new == field) {
                    return
                }

                field = new
                form.updateState()
                (stateChanged as ChangeObserversImpl)()
            }

        internal val stateChanged: ChangeObservers<Field<T>> = ChangeObserversImpl(this)

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
            (field as Field<Any>).visualizer.invoke(FieldInfo(field, field.initial))
        }

        updateState()
    }

    private fun updateState() {
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
             * @param a         the form's only field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A> invoke(
                    a        : Field<A>,
                    onInvalid: ( ) -> Unit,
                    onReady  : (A) -> Unit): FormDefinition = invokeInternal(a, layout = null, onInvalid, onReady)

            /**
             * Defines a [Form] with a single [Field].
             *
             * @param a         the form's only field
             * @param layout    to position field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A> invoke(
                a        : Field<A>,
                layout   : ConstraintDslContext.(Bounds) -> Unit,
                onInvalid: ( ) -> Unit,
                onReady  : (A) -> Unit): FormDefinition = invokeInternal(a, layout = layout, onInvalid, onReady)

            internal fun <A> invokeInternal(
                a        : Field<A>,
                layout   : (ConstraintDslContext.(Bounds) -> Unit)?,
                onInvalid: ( ) -> Unit,
                onReady  : (A) -> Unit): FormDefinition = FormDefinition(Form(a) { _,_,state ->
                when (state) {
                    is Ready -> onReady  (state.values[0] as A)
                    else     -> onInvalid()
                }
            }.also { form ->
                layout?.let {
                    form.layout = constrain(form.children[0], it)
                }
            })

            /**
             * Defines a [Form] with 2 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    onInvalid: (   ) -> Unit,
                    onReady  : (A,B) -> Unit): FormDefinition = invokeInternal(a, b, layout = null, onInvalid, onReady)

            /**
             * Defines a [Form] with 2 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param layout    to position fields
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B> invoke(
                a        : Field<A>,
                b        : Field<B>,
                layout   : ConstraintDslContext.(Bounds, Bounds) -> Unit,
                onInvalid: (   ) -> Unit,
                onReady  : (A,B) -> Unit): FormDefinition = invokeInternal(a, b, layout, onInvalid, onReady)

            internal fun <A, B> invokeInternal(
                a        : Field<A>,
                b        : Field<B>,
                layout   : (ConstraintDslContext.(Bounds, Bounds) -> Unit)?,
                onInvalid: (   ) -> Unit,
                onReady  : (A,B) -> Unit): FormDefinition = FormDefinition(Form(a, b) { _,_,state ->
                var i = 0

                when (state) {
                    is Ready -> onReady  (state.values[i++] as A, state.values[i] as B)
                    else     -> onInvalid()
                }
            }.also { form ->
                layout?.let {
                    form.layout = constrain(form.children[0], form.children[1], it)
                }
            })

            /**
             * Defines a [Form] with 3 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param c         the form's third field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    onInvalid: (     ) -> Unit,
                    onReady  : (A,B,C) -> Unit): FormDefinition = invokeInternal(a, b, c, layout = null, onInvalid, onReady)

            /**
             * Defines a [Form] with 3 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param c         the form's third field
             * @param layout    to position fields
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C> invoke(
                a        : Field<A>,
                b        : Field<B>,
                c        : Field<C>,
                layout   : ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit,
                onInvalid: (     ) -> Unit,
                onReady  : (A,B,C) -> Unit): FormDefinition = invokeInternal(a, b, c, layout, onInvalid, onReady)

            internal fun <A, B, C> invokeInternal(
                a        : Field<A>,
                b        : Field<B>,
                c        : Field<C>,
                layout   : (ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit)?,
                onInvalid: (     ) -> Unit,
                onReady  : (A,B,C) -> Unit): FormDefinition = FormDefinition(Form(a, b, c) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i] as C)
                    else     -> onInvalid()
                }
            }.also { form ->
                layout?.let {
                    form.layout = constrain(form.children[0], form.children[1], form.children[2], it)
                }
            })

            /**
             * Defines a [Form] with 4 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param c         the form's third field
             * @param d         the form's fourth field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C, D> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    d        : Field<D>,
                    onInvalid: (          ) -> Unit = {            },
                    onReady  : (A, B, C, D) -> Unit = { _,_,_,_ -> }): FormDefinition = invokeInternal(a, b, c, d, layout = null, onInvalid, onReady)

            /**
             * Defines a [Form] with 4 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param c         the form's third field
             * @param d         the form's fourth field
             * @param layout    to position fields
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C, D> invoke(
                a        : Field<A>,
                b        : Field<B>,
                c        : Field<C>,
                d        : Field<D>,
                layout   : (ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit)? = null,
                onInvalid: (          ) -> Unit = {            },
                onReady  : (A, B, C, D) -> Unit = { _,_,_,_ -> }): FormDefinition = invokeInternal(a, b, c, d, layout, onInvalid, onReady)

            internal fun <A, B, C, D> invokeInternal(
                a        : Field<A>,
                b        : Field<B>,
                c        : Field<C>,
                d        : Field<D>,
                layout   : (ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit)?,
                onInvalid: (          ) -> Unit = {            },
                onReady  : (A, B, C, D) -> Unit = { _,_,_,_ -> }): FormDefinition = FormDefinition(Form(a, b, c, d) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i++] as C, state.values[i] as D)
                    else     -> onInvalid()
                }
            }.also { form ->
                layout?.let {
                    form.layout = constrain(form.children[0], form.children[1], form.children[2], form.children[3], it)
                }
            })

            /**
             * Defines a [Form] with 5 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param c         the form's third field
             * @param d         the form's fourth field
             * @param e         the form's fifth field
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C, D, E> invoke(
                    a        : Field<A>,
                    b        : Field<B>,
                    c        : Field<C>,
                    d        : Field<D>,
                    e        : Field<E>,
                    onInvalid: (             ) -> Unit,
                    onReady  : (A, B, C, D, E) -> Unit): FormDefinition = invokeInternal(a, b, c, d, e, layout = null, onInvalid, onReady)

            /**
             * Defines a [Form] with a 5 [Field]s.
             *
             * @param a         the form's first field
             * @param b         the form's second field
             * @param c         the form's third field
             * @param d         the form's fourth field
             * @param e         the form's fifth field
             * @param layout    to position fields
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun <A, B, C, D, E> invoke(
                a        : Field<A>,
                b        : Field<B>,
                c        : Field<C>,
                d        : Field<D>,
                e        : Field<E>,
                layout   : ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit,
                onInvalid: (             ) -> Unit,
                onReady  : (A, B, C, D, E) -> Unit): FormDefinition = invokeInternal(a, b, c, d, e, layout, onInvalid, onReady)

            internal fun <A, B, C, D, E> invokeInternal(
                a        : Field<A>,
                b        : Field<B>,
                c        : Field<C>,
                d        : Field<D>,
                e        : Field<E>,
                layout   : (ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit)?,
                onInvalid: (             ) -> Unit,
                onReady  : (A, B, C, D, E) -> Unit): FormDefinition = FormDefinition(Form(a, b, c, d, e) { _,_,state ->
                var i = 0
                when (state) {
                    is Ready -> onReady(state.values[i++] as A, state.values[i++] as B, state.values[i++] as C, state.values[i++] as D, state.values[i] as E)
                    else     -> onInvalid()
                }
            }.also { form ->
                layout?.let {
                    form.layout = constrain(form.children[0], form.children[1], form.children[2], form.children[3], form.children[4], it)
                }
            })

            /**
             * Defines a [Form] with at least 2 [Field]s. The values provided to [onReady], though untyped,
             * are in the definition order within invoke. This allows the consumer to cast them to the expected
             * types.
             *
             * @param first     the form's first field
             * @param second    the form's second field
             * @param rest      the form's remaining fields
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun invoke(
                           first    : Field<*>,
                           second   : Field<*>,
                    vararg rest     : Field<*>,
                           onInvalid: (       ) -> Unit,
                           onReady  : (List<*>) -> Unit): FormDefinition = invokeInternal(
                first     = first,
                second    = second,
                rest      = rest,
                layout    = null,
                onInvalid = onInvalid,
                onReady   = onReady
            )

            /**
             * Defines a [Form] with at least 2 [Field]s. The values provided to [onReady], though untyped,
             * are in the definition order within invoke. This allows the consumer to cast them to the expected
             * types.
             *
             * @param first     the form's first field
             * @param second    the form's second field
             * @param rest      the form's remaining fields
             * @param layout    to position fields
             * @param onInvalid called whenever the form has new input and any of its fields are [Invalid]
             * @param onReady   called whenever the form has new input and all its fields are [Valid]
             */
            public operator fun invoke(
                       first    : Field<*>,
                       second   : Field<*>,
                vararg rest     : Field<*>,
                       layout   : ConstraintDslContext.(List<Bounds>) -> Unit,
                       onInvalid: (       ) -> Unit,
                       onReady  : (List<*>) -> Unit): FormDefinition = invokeInternal(
                first     = first,
                second    = second,
                rest      = rest,
                layout    = layout,
                onInvalid = onInvalid,
                onReady   = onReady
            )

            internal fun invokeInternal(
                       first    : Field<*>,
                       second   : Field<*>,
                vararg rest     : Field<*>,
                       layout   : (ConstraintDslContext.(List<Bounds>) -> Unit)?,
                       onInvalid: (       ) -> Unit,
                       onReady  : (List<*>) -> Unit
            ): FormDefinition = FormDefinition(Form(first, second, *rest) { _,_,state ->
                when (state) {
                    is Ready -> onReady(state.values)
                    else     -> onInvalid()
                }
            }.also { form ->
                layout?.let {
                    form.layout = constrain(form.children[0], form.children[1], others = form.children.drop(2).toTypedArray(), it)
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
public inline fun <R, T> FieldState<T>.fold(onValid: (value: T) -> R, default: R): R = when (this) {
    is Valid<T> -> onValid(value)
    else -> default
}

/**
 * Returns a [valid][Form.Valid] state from the result of [onValid] if this instance is [valid][Form.Valid] or [invalid][Form.Invalid] otherwise.
 */
@Suppress("unused")
public inline fun <T, R> FieldState<T>.map(onValid: (value: T) -> R): FieldState<R> = when (this) {
    is Valid<T> -> Valid(onValid(value))
    else -> Invalid()
}

/**
 * Does the action of [onValid] if this instance is [valid][Form.Valid].
 */
public inline fun <T> FieldState<T>.ifValid(onValid: (value: T) -> Unit): Unit = if (this is Valid) {
    onValid(this.value)
} else Unit

/**
 * Does the action of [onInvalid] if this instance is [invalid][Form.Invalid].
 */
public inline fun <T> FieldState<T>.ifInvalid(onInvalid: () -> Unit): Unit = if (this is Invalid) {
    onInvalid()
} else Unit


/**
 * Returns the result of [onValid] if this instance is [valid][Form.Valid] or [default] if it is [invalid][Form.Invalid].
 */
public inline fun <R, T> Field<T>.fold(onValid: (value: T) -> R, default: R): R = state.fold(onValid, default)

/**
 * Returns a [valid][Form.Valid] state from the result of [onValid] if this instance is [valid][Form.Valid] or [invalid][Form.Invalid] otherwise.
 */
@Suppress("unused")
public inline fun <T, R> Field<T>.mapValue(onValid: (value: T) -> R): FieldState<R> = state.map(onValid)

/**
 * Does the action of [onValid] if this instance is [valid][Form.Valid].
 */
@Suppress("unused")
public inline fun <T> Field<T>.ifValid(onValid: (value: T) -> Unit): Unit = state.ifValid(onValid)
