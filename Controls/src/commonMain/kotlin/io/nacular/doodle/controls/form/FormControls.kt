@file:Suppress("unused")

package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.MultiSelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.buttons.ButtonGroup
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.dropdown.Dropdown
import io.nacular.doodle.controls.form.Form.Field
import io.nacular.doodle.controls.form.Form.FieldState
import io.nacular.doodle.controls.form.Form.Invalid
import io.nacular.doodle.controls.form.Form.Valid
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFit.Width
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.ListLayout
import io.nacular.doodle.layout.WidthSource
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.PassThroughEncoder
import kotlin.math.max

/**
 * Configuration for [textField] controls.
 *
 * @property textField used by the control.
 */
public class TextFieldConfig<T> internal constructor(public val textField: TextField) {
    /**
     * Called whenever the text field's input is invalid
     */
    public var onInvalid: (Throwable) -> Unit = {}

    /**
     * Called whenever the text field's input is valid
     */
    public var onValid: (T) -> Unit = {}
}

/**
 * Creates a [TextField] control that is bounded to a [Field].
 * The associated field will only be valid if the text field's input matches
 * [pattern] and [encoder.from][Encoder.from] produces a valid [T] from it.
 *
 * @param T is the type of the bounded field
 * @param pattern used to validate input to the field
 * @param encoder used to map [String] -> [T]
 * @param config used to control the resulting component
 */
public fun <T> textField(
        pattern: Regex = Regex(".*"),
        encoder: Encoder<T, String>,
        config : TextFieldConfig<T>.() -> Unit = {}): FieldVisualizer<T> = field {
    lateinit var configObject: TextFieldConfig<T>

    fun validate(field: Field<T>, value: String) {
        when {
            pattern.matches(value) -> {
                encoder.from(value).onSuccess { decoded ->
                    field.state = Valid(decoded)
                    configObject.onValid(decoded)
                }.onFailure {
                    field.state = Invalid()
                    configObject.onInvalid(it)
                }
            }
            else                   -> {
                field.state = Invalid()
                configObject.onInvalid(IllegalArgumentException("Invalid input: $value"))
            }
        }
    }

    TextField().apply {
        textChanged  += { _,_,new      -> validate(this@field, new) }
        focusChanged += { _,_,hasFocus ->
            if (!hasFocus) {
                validate(this@field, text)
            }
        }

        configObject = TextFieldConfig(this@apply)
        config(configObject)

        when (val value = this@field.state) {
            is Valid -> encoder.to(value.value).getOrNull()?.let { text = it }
        }
    }
}

/**
 * Creates a [TextField] control that is bounded to a [Field] (of type [String]).
 * The associated field will only be valid if the text field's input matches
 * [pattern].
 *
 * @param pattern used to validate input to the field
 * @param config used to control the resulting component
 */
public fun textField(pattern: Regex = Regex(".*"), config: TextFieldConfig<String>.() -> Unit = {}): FieldVisualizer<String> = textField(pattern, PassThroughEncoder(), config)

/**
 * Configuration for radio and check lists.
 */
public class OptionListConfig<T> internal constructor() {
    /** Spacing between items in the list */
    public var spacing: Double = 0.0

    /** Height of each item */
    public var itemHeight: Double? = null

    /** Provides a label for each item in the list */
    public var label: (T) -> String = { it.toString() }
}

/**
 * Creates a list of [RadioButton]s within a [ButtonGroup] that is bound to a [Field].
 * This controls lets a user select an option from a list.
 *
 * NOTE: This does not provide an initial value for the associated field, so one must
 * be provided at the form creation level if a default is desired.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param config used to control the resulting component
 */
public fun <T> radioList(
               first : T,
        vararg rest  : T,
               config: OptionListConfig<T>.() -> Unit = {}): FieldVisualizer<T> = field {
    val builder = OptionListConfig<T>().also(config)

    buildRadioList(
        first        = first,
        rest         = rest,
        spacing      = builder.spacing,
        itemHeight   = builder.itemHeight,
        label        = builder.label,
        initialValue = (state as? Valid<T>)?.value) { value, button ->
        button.selectedChanged += { _,_,selected ->
            if (selected) {
                state = Valid(value)
            }
        }
    }
}

/**
 * Creates a list of [RadioButton]s within a [ButtonGroup] that is bound to a [Field].
 * This controls lets a user select an option from a list. This control lets a user
 * ignore selection entirely and therefore the resulting type is [T]?.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param config used to control the resulting component
 */
public fun <T: Any> optionalRadioList(
               first     : T,
        vararg rest      : T,
               config: OptionListConfig<T>.() -> Unit = {}): FieldVisualizer<T?> = field {
    val builder = OptionListConfig<T>().also(config)

    buildRadioList(
            first        = first,
            rest         = rest,
            spacing      = builder.spacing,
            itemHeight   = builder.itemHeight,
            label        = builder.label,
            initialValue = this.fold({ it }, null)) { value, button ->
        button.selectedChanged += { _,_,selected ->
            if (selected) {
                state = Valid(value)
            }
        }
    }.also {
        state = Valid(null)
    }
}

/**
 * Creates a list of [CheckBox]s that is bound to a [Field]. This controls lets a user select multiple
 * options from a list. This control lets a user ignore selection entirely, which would result in an empty list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param config used to control the resulting component
 */
public fun <T> checkList(first : T, vararg rest  : T, config: OptionListConfig<T>.() -> Unit = {}): FieldVisualizer<List<T>> = field {
    val builder   = OptionListConfig<T>().also(config)
    val selection = mutableListOf<T>()

    container {
        focusable     = false
        val items     = listOf(first) + rest
        var itemIndex = 0

        this@field.fold({it}, emptyList()).forEachIndexed { _, value ->
            (itemIndex until items.size).forEach {
                itemIndex += 1

                if (items[it] == value) {
                    selection += value
                    return@forEachIndexed
                }
            }

            if (itemIndex >= items.size) {
                return@forEachIndexed
            }
        }

        state = Valid(selection)

        children  += items.map { value ->
            CheckBox(builder.label(value)).apply {
                selected         = value in selection
                selectedChanged += { _,_,selected ->
                    when {
                        selected -> selection += value
                        else     -> selection -= value
                    }

                    state = Valid(selection)
                }
                sizePreferencesChanged += { _,_,_ ->
                    relayout()
                }
            }
        }
        layout = ExpandingListLayout(this, builder.spacing, builder.itemHeight)
    }
}

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param model for the dropdown
 * @param boxItemVisualizer used to render the drop-down's box item
 * @param listItemVisualizer used to render items in the drop-down's list
 * @param config used to control the resulting component
 */
public fun <T, M: ListModel<T>> dropDown(
        model             : M,
        boxItemVisualizer : ItemVisualizer<T, IndexedItem>,
        listItemVisualizer: ItemVisualizer<T, IndexedItem> = boxItemVisualizer,
        config            : (Dropdown<T, *>) -> Unit = {}): FieldVisualizer<T> = field {
    Dropdown(model, boxItemVisualizer  = boxItemVisualizer, listItemVisualizer = listItemVisualizer).also { dropdown ->
        when (state) {
            is Valid<T> -> model.forEachIndexed { index, item ->
                if (item == state) {
                    dropdown.selection = index
                    return@forEachIndexed
                }
            }
            else        -> state = Valid(dropdown.value)
        }

        dropdown.changed += {
            state = Valid(dropdown.value)
        }
    }.also(config)
}

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param boxItemVisualizer used to render the drop-down's box item
 * @param listItemVisualizer used to render items in the drop-down's list
 * @param config used to control the resulting component
 */
public fun <T> dropDown(
               first             : T,
        vararg rest              : T,
               boxItemVisualizer : ItemVisualizer<T, IndexedItem>,
               listItemVisualizer: ItemVisualizer<T, IndexedItem> = boxItemVisualizer,
               config            : (Dropdown<T, *>) -> Unit = {}): FieldVisualizer<T> = dropDown(
        SimpleListModel(listOf(first) + rest),
        boxItemVisualizer,
        listItemVisualizer,
        config
)

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param label used to render the drop-down's box and list items
 * @param config used to control the resulting component
 */
public fun <T> dropDown(
               first : T,
        vararg rest  : T,
               label : (T) -> String = { it.toString() },
               config: (Dropdown<T, *>) -> Unit = {}
): FieldVisualizer<T> = dropDown(first, *rest, boxItemVisualizer = toString(TextVisualizer(), label), config = config)

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], in that it
 * DOES NOT set a default value and its field is [Invalid] if no initial value
 * is bound to it. The control actually has an "unselected" state when it is invalid.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param boxItemVisualizer used to render the drop-down's box item
 * @param listItemVisualizer used to render items in the drop-down's list
 * @param unselectedBoxItemVisualizer used to render the drop-down's box item when it is unselected
 * @param unselectedListItemVisualizer used to render the "unselected item" in the drop-down's list
 * @param config used to control the resulting component
 */
public fun <T: Any> dropDown(
        first                       : T,
        vararg rest                 : T,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
        config                      : (Dropdown<T?, *>) -> Unit = {}): FieldVisualizer<T> = field {
    buildDropDown(
            first                        = first,
            rest                         = rest,
            boxItemVisualizer            = boxItemVisualizer,
            listItemVisualizer           = listItemVisualizer,
            unselectedBoxItemVisualizer  = unselectedBoxItemVisualizer,
            unselectedListItemVisualizer = unselectedListItemVisualizer,
            initialValue                 = fold({it}, null)).apply {
        changed += {
            state = when (val v = value) {
                null -> Invalid( )
                else -> Valid  (v)
            }
        }

        config(this)
    }
}

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], in that it
 * DOES NOT set a default value and its field is [Invalid] if no initial value
 * is bound to it. The control actually has an "unselected" state when it is invalid.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param label used to render the drop-down's items
 * @param unselectedLabel used to render the item that represents the "unselected" state
 * @param config used to control the resulting component
 */
public fun <T: Any> dropDown(
        first          : T,
        vararg rest    : T,
        label          : (T) -> String = { it.toString() },
        unselectedLabel: String,
        config         : (Dropdown<T?, *>) -> Unit = {}
): FieldVisualizer<T> = dropDown(
        first,
        *rest,
        boxItemVisualizer           = toString(TextVisualizer(), label),
        unselectedBoxItemVisualizer = toString(TextVisualizer()) { unselectedLabel },
        config                      = config)

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [optionalRadioList]. This control lets a user
 * ignore selection entirely and therefore the resulting type is [T]?.
 *
 * @param T is the type of the bounded field
 * @param model for the dropdown
 * @param boxItemVisualizer used to render the drop-down's box item
 * @param listItemVisualizer used to render items in the drop-down's list
 * @param unselectedBoxItemVisualizer used to render the drop-down's box item when it is unselected
 * @param unselectedListItemVisualizer used to render the "unselected item" in the drop-down's list
 * @param config used to control the resulting component
 */
public fun <T: Any, M: ListModel<T>> optionalDropDown(
        model                       : M,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
        config                      : (Dropdown<T?, *>) -> Unit = {}): FieldVisualizer<T?> = field {
    buildDropDown(
            model                        = SimpleListModel(listOf(null) + model.section(0 until model.size)),
            boxItemVisualizer            = boxItemVisualizer,
            listItemVisualizer           = listItemVisualizer,
            unselectedBoxItemVisualizer  = unselectedBoxItemVisualizer,
            unselectedListItemVisualizer = unselectedListItemVisualizer,
            initialValue                 = fold({it}, null)).apply {
        changed += {
            state = Valid(value)
        }

        state = Valid(null)

        config(this)
    }
}

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [optionalRadioList]. This control lets a user
 * ignore selection entirely and therefore the resulting type is [T]?.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param boxItemVisualizer used to render the drop-down's box item
 * @param listItemVisualizer used to render items in the drop-down's list
 * @param unselectedBoxItemVisualizer used to render the drop-down's box item when it is unselected
 * @param unselectedListItemVisualizer used to render the "unselected item" in the drop-down's list
 * @param config used to control the resulting component
 */
public fun <T: Any> optionalDropDown(
        first                       : T,
        vararg rest                 : T,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
        config                      : (Dropdown<T?, *>) -> Unit = {}): FieldVisualizer<T?> = field {
    buildDropDown(
            first                        = first,
            rest                         = rest,
            boxItemVisualizer            = boxItemVisualizer,
            listItemVisualizer           = listItemVisualizer,
            unselectedBoxItemVisualizer  = unselectedBoxItemVisualizer,
            unselectedListItemVisualizer = unselectedListItemVisualizer,
            initialValue                 = fold( { it }, null )).apply {
        changed += {
            state = Valid(value)
        }

        state = Valid(null)

        config(this)
    }
}

/**
 * Creates a [Dropdown] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [optionalRadioList]. This control lets a user
 * ignore selection entirely and therefore the resulting type is [T]?.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param label used to render the drop-down's items
 * @param unselectedLabel used to render the item that represents the "unselected" state
 * @param config used to control the resulting component
 */
public fun <T: Any> optionalDropDown(
        first          : T,
        vararg rest    : T,
        label          : (T) -> String = { it.toString() },
        unselectedLabel: String,
        config                      : (Dropdown<T?, *>) -> Unit = {}
): FieldVisualizer<T?> = optionalDropDown(
        first,
        *rest,
        boxItemVisualizer           = toString(TextVisualizer(), label),
        unselectedBoxItemVisualizer = toString(TextVisualizer()) { unselectedLabel },
        config                      = config)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select multiple options from a list. This control lets a user ignore selection entirely,
 * which would result in an empty list. It is similar to a [checkList].
 *
 * @param T is the type of the items in the bounded field
 * @param model for the list
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
@Suppress("UNCHECKED_CAST")
public fun <T, M: ListModel<T>> list(
        model         : M,
        itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(TextVisualizer()),
        config        : (io.nacular.doodle.controls.list.List<T, M>) -> Unit = {}): FieldVisualizer<List<T>> = field {
    io.nacular.doodle.controls.list.List(
            model,
            itemVisualizer,
            selectionModel = MultiSelectionModel(),
            fitContent     = false
    ).apply {
        var itemIndex = 0
        val items     = model.asIterable().iterator()
        val selection = mutableListOf<Int>()

        this@field.fold({it}, emptyList()).forEach { value ->
            while (items.hasNext()) {
                val item = items.next()

                if (item == value) {
                    selection += itemIndex++
                    break
                }

                itemIndex += 1
            }

            if (itemIndex >= model.size) {
                return@forEach
            }
        }

        state            = Valid(selection.map { this[it] as T })
        focusable        = false
        isFocusCycleRoot = false

        setSelection(selection.toSet())

        selectionChanged += { _,_,new ->
            state = Valid(new.map { this[it] as T })
        }

        config(this)
    }
}

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select multiple options from a list. This control lets a user ignore selection entirely,
 * which would result in an empty list. It is similar to a [checkList].
 *
 * @param T is the type of the items in the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
public fun <T> list(
               first: T,
        vararg rest : T,
               itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(TextVisualizer()),
               config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<List<T>> = list(
    SimpleListModel(listOf(first) + rest),
    itemVisualizer,
    config
)

/**
 * Config for [named] controls.
 */
public class NamedVisualizerConfig internal constructor() {
    /**
     * Defines the layout for the named container.
     */
    public var layout: (container: View, label: Label, field: View) -> Layout? = { container, label,_ ->
        label.fitText = setOf(Width)

        ExpandingListLayout(container, 2.0, 32.0)
    }
}

/**
 * Creates a component with a [Label] and the result of [visualizer] that is bound to a [Field].
 * This control simply wraps an existing one with a configurable text label.
 *
 * @param name used in the label
 * @param visualizer being decorated
 */
public fun <T> named(
        name      : StyledText,
        visualizer: NamedVisualizerConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = field {
    container {
        val builder = NamedVisualizerConfig()
        val visualization = visualizer(builder)

        focusable = false

        val label = UninteractiveLabel(name)

        children += listOf(label, visualization(this@field)).onEach {
            it.sizePreferencesChanged += { _, _, _ ->
                relayout()
            }
        }

        layout = builder.layout(this, label, children[1])
    }
}

/**
 * Creates a component with a [Label] and the result of [visualizer] that is bound to a [Field].
 * This control simply wraps an existing one with a configurable text label.
 *
 * @param name used in the label
 * @param visualizer being decorated
 */
public fun <T> named(name: String, visualizer: NamedVisualizerConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = named(StyledText(name), visualizer)

/**
 * Creates a [Form] component that is bound to a [Field]. This control allows nesting of forms using
 * a DSL like that used for top-level forms.
 *
 * @param builder used to construct the form
 */
public fun <T> form(builder: FormControlBuildContext<T>.() -> FieldVisualizer<T>): FieldVisualizer<T> {
    return field {
        builder(FormControlBuildContext(this))(this)
    }
}

public class FormControlBuildContext<T> internal constructor(public val field: Field<T>) {
    /** @see Form.Companion.FormBuildContext.to */
    public infix fun <T> T.to(visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer, initial = Valid(this))

    /** @see Form.Companion.FormBuildContext.to */
    public infix fun <T> FieldState<T>.to(visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer, initial = this)

    /** @see Form.Companion.FormBuildContext.unaryPlus */
    public operator fun <T> FieldVisualizer<T>.unaryPlus(): Field<T> = Field(this, initial = Invalid())

    /**
     * Defines what [Layout] to use with the resulting [Form].
     */
    public var layout: (form: Form) -> Layout? = {
        ExpandingListLayout(it, 12.0, 32.0)
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A> invoke(
            a        : Field<A>,
            onInvalid: ( ) -> Unit = {},
            onReady  : (A) -> T): FieldVisualizer<T> = field {
        Form {
            this(a, onInvalid = { field.state = Invalid(); onInvalid() }) { a ->
                state = Valid(onReady(a))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B> invoke(
            a        : Field<A>,
            b        : Field<B>,
            onInvalid: (    ) -> Unit = {},
            onReady  : (A, B) -> T): FieldVisualizer<T> = field {
        Form {
            this(a, b, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b ->
                state = Valid(onReady(a, b))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C> invoke(
            a        : Field<A>,
            b        : Field<B>,
            c        : Field<C>,
            onInvalid: (       ) -> Unit = {},
            onReady  : (A, B, C) -> T): FieldVisualizer<T> = field {
        Form {
            this(a, b, c, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b, c ->
                state = Valid(onReady(a, b, c))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C, D> invoke(
            a        : Field<A>,
            b        : Field<B>,
            c        : Field<C>,
            d        : Field<D>,
            onInvalid: (          ) -> Unit = {},
            onReady  : (A, B, C, D) -> T): FieldVisualizer<T> = field {
        Form {
            this(a, b, c, d, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b, c, d ->
                state = Valid(onReady(a, b, c, d))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C, D, E> invoke(
            a        : Field<A>,
            b        : Field<B>,
            c        : Field<C>,
            d        : Field<D>,
            e        : Field<E>,
            onInvalid: (             ) -> Unit = {},
            onReady  : (A, B, C, D, E) -> T): FieldVisualizer<T> = field {
        Form {
            this(a, b, c, d, e, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b, c, d, e ->
                state = Valid(onReady(a, b, c, d, e))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T> invoke(
                   first    : Field<*>,
                   second   : Field<*>,
            vararg rest     : Field<*>,
                   onInvalid: (       ) -> Unit = {},
                   onReady  : (List<*>) -> T): FieldVisualizer<T> = field {
        Form {
            this(first, second, *rest, onInvalid = { field.state = Invalid(); onInvalid() }) { fields ->
                state = Valid(onReady(fields))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }
}

private fun <T> buildRadioList(
               first       : T,
        vararg rest        : T,
               spacing     : Double  = 0.0,
               itemHeight  : Double? = null,
               label       : (T) -> String = { it.toString() },
               initialValue: T? = null,
               config      : (T, RadioButton) -> Unit): Container = container {
    val group  = ButtonGroup()
    children  += (listOf(first) + rest).map { value ->
        RadioButton(label(value)).apply {
            group += this

            initialValue?.let {
                selected = value == it
            }

            config(value, this)
        }
    }
    focusable = false
    layout    = ExpandingListLayout(this, spacing, itemHeight)
}

private fun <T: Any, M: ListModel<T?>> buildDropDown(
        model                       : M,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
        initialValue                : T? = null
): Dropdown<T?, M> = Dropdown(
        model,
        boxItemVisualizer = itemVisualizer { item, previous, context ->
            when (item) {
                null -> unselectedBoxItemVisualizer(Unit, previous, context)
                else -> boxItemVisualizer          (item, previous, context)
            }
        },
        listItemVisualizer = itemVisualizer { item, previous, context ->
            when (item) {
                null -> unselectedListItemVisualizer(Unit, previous, context)
                else -> listItemVisualizer          (item, previous, context)
            }
        }
).apply {
    if (initialValue != null) {
        model.forEachIndexed { index, item ->
            if (item == initialValue) {
                selection = index
                return@forEachIndexed
            }
        }
    }
}

private fun <T: Any> buildDropDown(
        first                       : T,
        vararg rest                 : T,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
        initialValue                : T? = null
): Dropdown<T?, *> = buildDropDown(
        SimpleListModel(listOf(null, first) + rest),
        boxItemVisualizer,
        listItemVisualizer,
        unselectedBoxItemVisualizer,
        unselectedListItemVisualizer,
        initialValue
)

private class UninteractiveLabel(text: StyledText): Label(text) {
    override fun contains(point: Point) = false
}

private class ExpandingListLayout(private val container: View, spacing: Double, private val itemHeight: Double? = null): Layout {
    private val delegate = ListLayout(spacing = spacing, widthSource = WidthSource.Parent)

    private fun maxOrNull(first: Double?, second: Double?): Double? = when {
        first != null && second != null -> max(first, second)
        first != null                   -> first
        else                            -> second
    }

    override fun layout(container: PositionableContainer) {
        container.children.forEach { child ->
            maxOrNull(child.idealSize?.height, itemHeight)?.let { child.height = it }
        }

        delegate.layout(container)

        val size = Size(container.width, container.children.last().bounds.bottom + container.insets.bottom)
        this.container.idealSize = size
        this.container.size      = Size(size.width, max(size.height, this.container.height))
    }
}