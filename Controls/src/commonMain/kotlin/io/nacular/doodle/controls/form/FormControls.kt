@file:Suppress("unused")

package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.BasicConfinedRangeModel
import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedRangeModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.IntProgressionModel
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.MultiSelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.StringVisualizer
import io.nacular.doodle.controls.buttons.ButtonGroup
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.buttons.Switch
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.dropdown.Dropdown
import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.controls.form.Form.Field
import io.nacular.doodle.controls.form.Form.FieldState
import io.nacular.doodle.controls.form.Form.Invalid
import io.nacular.doodle.controls.form.Form.Valid
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.range.RangeSlider
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.spinner.ListSpinnerModel
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.spinner.SpinnerModel
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.ContainerBuilder
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.SizePreferences
import io.nacular.doodle.core.container
import io.nacular.doodle.core.then
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.ListLayout
import io.nacular.doodle.layout.WidthSource
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.Dimension
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.PassThroughEncoder
import io.nacular.doodle.utils.observable
import kotlin.math.max
import kotlin.reflect.KClass

// region String

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
 * [pattern] and [encoder.decode][Encoder.decode] produces a valid [T] from it.
 *
 * @param T is the type of the bounded field
 * @param pattern used to validate input to the field
 * @param encoder used to map [String] -> [T]
 * @param validator used to validate value from [encoder]
 * @param config used to control the resulting component
 */
public fun <T> textField(
        pattern  : Regex = Regex(".*"),
        encoder  : Encoder<T, String>,
        validator: (T) -> Boolean = { true },
        config   : TextFieldConfig<T>.() -> Unit = {}): FieldVisualizer<T> = field {
    lateinit var configObject: TextFieldConfig<T>

    fun invalidateField(field: Field<T>, error: Throwable, notify: Boolean = true) {
        field.state = Invalid()
        if (notify) configObject.onInvalid(error)
    }

    fun validate(field: Field<T>, value: String, notify: Boolean = true) {
        when {
            pattern.matches(value) -> {
                encoder.decode(value).onSuccess { decoded ->
                    when {
                        validator(decoded) -> {
                            field.state = Valid(decoded)
                            if (notify) configObject.onValid(decoded)
                        }
                        else -> invalidateField(field, IllegalArgumentException("Invalid"), notify)
                    }
                }.onFailure {
                    invalidateField(field, it, notify)
                }
            }
            else                   -> {
                invalidateField(field, IllegalArgumentException("Must match $pattern"), notify)
            }
        }
    }

    TextField().apply {
        textChanged  += { _,_,new      -> validate(field, new) }
        focusChanged += { _,_,hasFocus ->
            if (!hasFocus) {
                validate(field, text)
            }
        }

        configObject = TextFieldConfig(this@apply)
        config(configObject)

        when {
            initial is Valid && validator(initial.value) -> encoder.encode(initial.value).getOrNull()?.let { text = it }
            else                                         -> validate(field, text, notify = false)
        }
    }
}

/**
 * Creates a [TextField] control that is bounded to a [Field] (of type [String]).
 * The associated field will only be valid if the text field's input matches
 * [pattern].
 *
 * @param pattern used to validate input to the field
 * @param validator used to validate value after [pattern]
 * @param config used to control the resulting component
 */
public fun textField(
        pattern  : Regex = Regex(".*"),
        validator: (String) -> Boolean = { true },
        config   : TextFieldConfig<String>.() -> Unit = {}
): FieldVisualizer<String> = textField(pattern, PassThroughEncoder(), validator, config)

// endregion

// region Boolean
/**
 * Creates a [CheckBox] control that is bound to a [Field] (of type [Boolean]).
 *
 * @param label used to annotate the switch
 */
public fun check(label: View): FieldVisualizer<Boolean> = field {
    container {
        focusable = false
        this += label
        this += CheckBox().apply {
            initial.ifValid { selected = it }

            selectedChanged += { _,_,_ ->
                state = Valid(selected)
            }

            state = Valid(selected)
        }

        layout = buttonItemLayout(children[1], children[0]).then {
            idealSize = Size(width, children.maxOf { it.height })
        }
    }
}

/**
 * Creates a [CheckBox] control that is bound to a [Field] (of type [Boolean]).
 *
 * @param label used for the checkbox
 */
public fun check(label: String): FieldVisualizer<Boolean> = field {
    CheckBox(label).apply {
        initial.ifValid { selected = it }

        selectedChanged += { _,_,_ ->
            state = Valid(selected)
        }

        state = Valid(selected)
    }
}

/**
 * Creates a [Switch] control that is bound to a [Field] (of type [Boolean]).
 *
 * @param label used to annotate the switch
 */
public fun switch(label: View): FieldVisualizer<Boolean> = field {
    container {
        focusable = false
        this += label
        this += Switch().apply {
            accessibilityLabelProvider = label

            initial.ifValid { selected = it }

            selectedChanged += { _,_,_ ->
                state = Valid(selected)
            }

            size  = Size(30, 20)
            state = Valid(selected)
        }

        layout = constrain(children[0], children[1]) { label, switch ->
            switch.left    eq parent.right - switch.width.readOnly
            switch.centerY eq parent.centerY

            label.left     eq 0
            label.centerY  eq switch.centerY
        }.then {
            idealSize = Size(width, children.maxOf { it.height })
        }
    }
}

/**
 * Creates a [Switch] control that is bound to a [Field] (of type [Boolean]).
 *
 * @param text used to annotate the switch
 */
public fun switch(text: StyledText): FieldVisualizer<Boolean> = switch(Label(text))

/**
 * Creates a [Switch] control that is bound to a [Field] (of type [Boolean]).
 *
 * @param text used to annotate the switch
 */
public fun switch(text: String): FieldVisualizer<Boolean> = switch(Label(text))

// endregion

// region Number

// region Slider

/**
 * Configuration used to customize [slider] controls.
 *
 * @property slider within the control
 */
public class SliderConfig<T> internal constructor(public val slider: Slider<T>) where T: Number, T: Comparable<T>

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public inline fun <reified T> slider(
             model      : ConfinedValueModel<T>,
             orientation: Orientation = Horizontal,
    noinline config     : SliderConfig<T>.() -> Unit = {},
): FieldVisualizer<T> where T: Number, T: Comparable<T> = slider(model, orientation, config, T::class)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public inline fun <reified T> slider(
             range      : ClosedRange<T>,
             orientation: Orientation = Horizontal,
    noinline config     : SliderConfig<T>.() -> Unit = {}
): FieldVisualizer<T> where T: Number, T: Comparable<T> = slider(
    model       = BasicConfinedValueModel(range) as ConfinedValueModel<T>,
    orientation = orientation,
    config      = config
)

/**
 * @see slider
 */
public fun <T> slider(
    model      : ConfinedValueModel<T>,
    orientation: Orientation = Horizontal,
    config     : SliderConfig<T>.() -> Unit = {},
    type       : KClass<T>
): FieldVisualizer<T> where T: Number, T: Comparable<T> = field {
    Slider(model, orientation, type).apply {
        config(SliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }
    }
}

// endregion

// region CircularSlider

/**
 * Configuration used to customize [circularSlider] controls.
 *
 * @property slider within the control
 */
public class CircularSliderConfig<T> internal constructor(public val slider: CircularSlider<T>) where T: Number, T: Comparable<T>

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
public inline fun <reified T> circularSlider(
             model : ConfinedValueModel<T>,
    noinline config: CircularSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<T> where T: Number, T: Comparable<T> = circularSlider(model, config, T::class)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
public inline fun <reified T> circularSlider(
             range : ClosedRange<T>,
    noinline config: CircularSliderConfig<T>.() -> Unit = {}
): FieldVisualizer<T> where T: Number, T: Comparable<T> = circularSlider(
    model  = BasicConfinedValueModel(range) as ConfinedValueModel<T>,
    config = config
)

/**
 * @see circularSlider
 */
public fun <T> circularSlider(
    model : ConfinedValueModel<T>,
    config: CircularSliderConfig<T>.() -> Unit = {},
    type  : KClass<T>
): FieldVisualizer<T> where T: Number, T: Comparable<T> = field {
    CircularSlider(model, type).apply {
        config(CircularSliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }
    }
}

// endregion

// endregion

// region ClosedRange<Number>

// region Slider

/**
 * Configuration used to customize [rangeSlider] controls.
 *
 * @property slider within the control
 */
public class RangeSliderConfig<T> internal constructor(public val slider: RangeSlider<T>) where T: Number, T: Comparable<T>

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public inline fun <reified T> rangeSlider(
             model      : ConfinedRangeModel<T>,
             orientation: Orientation = Horizontal,
    noinline config     : RangeSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<ClosedRange<T>> where T: Number, T: Comparable<T> = rangeSlider(model, orientation, config, T::class)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public inline fun <reified T> rangeSlider(
             range      : ClosedRange<T>,
             orientation: Orientation = Horizontal,
    noinline config     : RangeSliderConfig<T>.() -> Unit = {}
): FieldVisualizer<ClosedRange<T>> where T: Number, T: Comparable<T> = rangeSlider(
    model       = BasicConfinedRangeModel(range) as ConfinedRangeModel<T>,
    orientation = orientation,
    config      = config
)

/**
 * @see rangeSlider
 */
public fun <T> rangeSlider(
    model      : ConfinedRangeModel<T>,
    orientation: Orientation = Horizontal,
    config     : RangeSliderConfig<T>.() -> Unit = {},
    type       : KClass<T>
): FieldVisualizer<ClosedRange<T>> where T: Number, T: Comparable<T> = field {
    RangeSlider(model, orientation, type).apply {
        config(RangeSliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }
    }
}

// endregion

// region CircularSlider

/**
 * Configuration used to customize [slider] controls.
 *
 * @property slider within the control
 */
public class CircularRangeSliderConfig<T> internal constructor(public val slider: CircularRangeSlider<T>) where T: Number, T: Comparable<T>

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
public inline fun <reified T> circularRangeSlider(
             model : ConfinedRangeModel<T>,
    noinline config: CircularRangeSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<ClosedRange<T>> where T: Number, T: Comparable<T> = circularRangeSlider(model, config, T::class)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
public inline fun <reified T> circularRangeSlider(
             range : ClosedRange<T>,
    noinline config: CircularRangeSliderConfig<T>.() -> Unit = {}
): FieldVisualizer<ClosedRange<T>> where T: Number, T: Comparable<T> = circularRangeSlider(
    model  = BasicConfinedRangeModel(range) as ConfinedRangeModel<T>,
    config = config
)

/**
 * @see circularRangeSlider
 */
public fun <T> circularRangeSlider(
    model : ConfinedRangeModel<T>,
    config: CircularRangeSliderConfig<T>.() -> Unit = {},
    type  : KClass<T>
): FieldVisualizer<ClosedRange<T>> where T: Number, T: Comparable<T> = field {
    CircularRangeSlider(model, type).apply {
        config(CircularRangeSliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }
    }
}

// endregion

// endregion

// region SimpleFile

/**
 * Creates a [FileSelector] control that is bound to a [Field] (of type [LocalFile]).
 *
 * @param acceptedTypes defines which file types to allow
 */
public fun file(acceptedTypes: Set<MimeType<*>> = emptySet()): FieldVisualizer<LocalFile> = field {
    FileSelector(allowMultiple = false, acceptedTypes = acceptedTypes).apply {
        this.filesLoaded += { _,_,files ->
            files.firstOrNull()?.let { state = Valid(it) }
        }
    }
}

/**
 * Creates a [FileSelector] control that is bound to a [Field] (of type List of [LocalFile]).
 *
 * @param acceptedTypes defines which file types to allow
 */
public fun files(acceptedTypes: Set<MimeType<*>> = emptySet()): FieldVisualizer<List<LocalFile>> = field {
    FileSelector(allowMultiple = true, acceptedTypes).apply {
        this.filesLoaded += { _,_,files ->
            state = Valid(files)
        }
    }
}

// endregion

// region T / T?

// region Radio Lists
/**
 * Configuration for radio and check lists.
 */
public class OptionListConfig<T> internal constructor() {
    /** Spacing between items in the list. */
    public var spacing: Double = 0.0

    /** Height of each item. */
    public var itemHeight: Double? = null

    /**
     * Specifies how much the list of items is inset from the parent container.
     */
    public var insets: Insets = Insets.None

    /**
     * Provides a label for each item in the list. This is short-hand for using [visualizer].
     */
    public var label: (T) -> String by observable({ "$it" }) { _,new ->
        visualizer = { Label(new(it)) }
    }

    /**
     * Defines how each item is represented in the list,
     */
    public var visualizer: (T) -> View = { Label(label(it)) }

    /**
     * Allows customized rendering of the container holding the list.
     */
    public var render: Canvas.(container: View) -> Unit = {}
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
    buildRadioList(
        first            = first,
        rest             = rest,
        optionListConfig = OptionListConfig<T>().also(config),
        initialValue     = initial.fold({ it }, null)) { value, button ->
        if (button.selected) {
            state = Valid(value)
        }

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
               first : T,
        vararg rest  : T,
               config: OptionListConfig<T>.() -> Unit = {}): FieldVisualizer<T?> = field {
    buildRadioList(
            first            = first,
            rest             = rest,
            allowDeselectAll = true,
            optionListConfig = OptionListConfig<T>().also(config),
            initialValue     = initial.fold({ it }, null)) { value, button ->
        if (button.selected) {
            state = Valid(value)
        }

        button.selectedChanged += { _,_,selected ->
            if (selected) {
                state = Valid(value)
            }
        }
    }.also {
        if (state is Invalid) {
            state = Valid(null)
        }
    }
}

// endregion

// region Dropdown
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
        initial.ifValid {
            model.forEachIndexed { index, item ->
                if (item == it) {
                    dropdown.selection = index
                    return@forEachIndexed
                }
            }
        }

        dropdown.changed += {
            state = dropdown.value.fold(onSuccess = { Valid(it) }, onFailure = { Invalid() })
        }

        state = dropdown.value.fold(onSuccess = { Valid(it) }, onFailure = { Invalid() })
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
           label : (T) -> String = { "$it" },
           config: (Dropdown<T, *>) -> Unit = {}
): FieldVisualizer<T> = dropDown(first, *rest, boxItemVisualizer = toString(StringVisualizer(), label), config = config)

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
    val model = SimpleListModel(listOf(null, first) + rest)

    buildDropDown(
        model                        = model,
        boxItemVisualizer            = boxItemVisualizer,
        listItemVisualizer           = listItemVisualizer,
        unselectedBoxItemVisualizer  = unselectedBoxItemVisualizer,
        unselectedListItemVisualizer = unselectedListItemVisualizer,
        initialValue                 = initial.fold({it}, null)
    ).apply {
        initial.ifValid {
            model.forEachIndexed { index, item ->
                if (item == it) {
                    selection = index
                    return@forEachIndexed
                }
            }
        }

        changed += {
            state = value.fold(
                onSuccess = { it?.let { Valid(it) } ?: Invalid() },
                onFailure = { Invalid() }
            )
        }

        state = value.fold(
            onSuccess = { it?.let { Valid(it) } ?: Invalid() },
            onFailure = { Invalid() }
        )

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
    vararg rest           : T,
           label          : (T) -> String = { "$it" },
           unselectedLabel: String,
           config         : (Dropdown<T?, *>) -> Unit = {}
): FieldVisualizer<T> = dropDown(
    first,
    *rest,
    boxItemVisualizer           = toString(StringVisualizer(), label),
    unselectedBoxItemVisualizer = toString(StringVisualizer()) { unselectedLabel },
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
        initialValue                 = initial.fold({it}, null)).apply {
        changed += {
            state = value.fold(
                onSuccess = { Valid(it) },
                onFailure = { Invalid() }
            )
        }

        state = value.fold(
            onSuccess = { Valid(it) },
            onFailure = { Invalid() }
        )

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
    vararg rest                        : T,
           boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
           listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
           unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
           unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
           config                      : (Dropdown<T?, *>) -> Unit = {}): FieldVisualizer<T?> = field {
    val model = SimpleListModel(listOf(null, first) + rest)

    buildDropDown(
        model                        = model,
        boxItemVisualizer            = boxItemVisualizer,
        listItemVisualizer           = listItemVisualizer,
        unselectedBoxItemVisualizer  = unselectedBoxItemVisualizer,
        unselectedListItemVisualizer = unselectedListItemVisualizer,
        initialValue                 = initial.fold({ it }, null)
    ).apply {
        initial.ifValid {
            model.forEachIndexed { index, item ->
                if (item == it) {
                    selection = index
                    return@forEachIndexed
                }
            }
        }

        changed += {
            state = value.fold(
                onSuccess = { Valid(it) },
                onFailure = { Invalid() }
            )
        }

        state = value.fold(
            onSuccess = { Valid(it) },
            onFailure = { Invalid() }
        )

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
    vararg rest           : T,
           label          : (T) -> String = { "$it" },
           unselectedLabel: String,
           config         : (Dropdown<T?, *>) -> Unit = {}
): FieldVisualizer<T?> = optionalDropDown(
    first,
    *rest,
    boxItemVisualizer           = toString(StringVisualizer(), label),
    unselectedBoxItemVisualizer = toString(StringVisualizer()) { unselectedLabel },
    config                      = config)

// endregion

// region Spinner
/**
 * Creates a [Spinner] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param model for the dropdown
 * @param itemVisualizer used to render the drop-down's box item
 * @param config used to control the resulting component
 */
public fun <T, M: SpinnerModel<T>> spinner(
    model         : M,
    itemVisualizer: ItemVisualizer<T, Spinner<T, M>> = toString(StringVisualizer()),
    config        : (Spinner<T, M>) -> Unit = {}): FieldVisualizer<T> = field {
    Spinner(model, itemVisualizer = itemVisualizer).also { spinner ->
        spinner.changed += {
            state = spinner.value.fold(
                onSuccess = { Valid(it) },
                onFailure = { Invalid() }
            )
        }

        state = spinner.value.fold(
            onSuccess = { Valid(it) },
            onFailure = { Invalid() }
        )
    }.also(config)
}

/**
 * Creates a [Spinner] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param itemVisualizer used to render the drop-down's box item
 * @param config used to control the resulting component
 */
public fun <T> spinner(
           first         : T,
    vararg rest          : T,
           itemVisualizer: ItemVisualizer<T, Spinner<T, *>>,
           config        : (Spinner<T, *>) -> Unit = {}): FieldVisualizer<T> = spinner(
    ListSpinnerModel(listOf(first) + rest),
    itemVisualizer,
    config
)

/**
 * Creates a [Spinner] control that is bound to a [Field]. This control lets a user
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
public fun <T> spinner(
           first : T,
    vararg rest  : T,
           label : (T) -> String = { "$it" },
           config: (Spinner<T, *>) -> Unit = {}
): FieldVisualizer<T> = spinner(first, *rest, itemVisualizer = toString(StringVisualizer(), label), config = config)

// endregion

// region List

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list. This control lets a user ignore selection entirely,
 * which would result in a `null` value. This is behaves like [optionalRadioList].
 *
 * @param T is the type of the items in the bounded field
 * @param model for the list
 * @param itemVisualizer used to render items in the list
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun <T, M: ListModel<T>> optionalSingleChoiceList(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<T, M>) -> Unit = {}): FieldVisualizer<T?> = field {
    io.nacular.doodle.controls.list.List(
        model,
        itemVisualizer,
        selectionModel = SingleItemSelectionModel(),
        fitContent     = fitContents
    ).apply {
        var itemIndex = 0
        val items     = model.asIterable().iterator()

        initial.fold({it}, null)?.let { value ->
            while (items.hasNext()) {
                val item = items.next()

                if (item == value) {
                    setSelection(setOf(itemIndex))
                    break
                }

                itemIndex += 1
            }
        }

        state            = Valid(selection.minOfOrNull { it }?.let { this[it].getOrNull() })
        isFocusCycleRoot = false

        selectionChanged += { _,_,_ ->
            state = Valid(selection.minOfOrNull { it }?.let { this[it].getOrNull() })
        }

        config(this)
    }
}

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list. This control lets a user ignore selection entirely,
 * which would result in a `null` value. This is behaves like [optionalRadioList].
 *
 * @param T is the type of the items in the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param itemVisualizer used to render items in the list
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun <T> optionalSingleChoiceList(
    first: T,
    vararg rest : T,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<T?> = optionalSingleChoiceList(
    SimpleListModel(listOf(first) + rest),
    itemVisualizer,
    fitContents,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list. This control lets a user ignore selection entirely,
 * which would result in a `null` value. This is behaves like [optionalRadioList].
 *
 * @param progression to use for values
 * @param itemVisualizer used to render items in the list
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun optionalSingleChoiceList(
    progression   : IntProgression,
    itemVisualizer: ItemVisualizer<Int, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<Int, *>) -> Unit = {}): FieldVisualizer<Int?> = optionalSingleChoiceList(
    IntProgressionModel(progression),
    itemVisualizer,
    fitContents,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list. This is behaves like [radioList].
 *
 * @param T is the type of the items in the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param itemVisualizer used to render items in the list
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun <T, M: ListModel<T>> singleChoiceList(
    first: T,
    vararg rest : T,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<T> = singleChoiceList(
    SimpleListModel(listOf(first) + rest),
    itemVisualizer,
    fitContents,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list.
 *
 * @param progression to use for values
 * @param itemVisualizer used to render items in the list
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun singleChoiceList(
    progression: IntProgression,
    itemVisualizer: ItemVisualizer<Int, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<Int, *>) -> Unit = {}): FieldVisualizer<Int> = singleChoiceList(
    IntProgressionModel(progression),
    itemVisualizer,
    fitContents,
    config
)
private fun <T, M: ListModel<T>> singleChoiceList(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<T> = field {

    class ForcedSingleItemSelectionModel<T>: MultiSelectionModel<T>() {
        override fun add(item: T): Boolean {
            var result = false

            observableSet.batch {
                clear()
                result = add(item)
            }

            return result
        }

        override fun addAll(items: Collection<T>): Boolean {
            if (observableSet.firstOrNull() in items) {
                return false
            }

            return items.lastOrNull()?.let { add(it) } ?: false
        }

        override fun replaceAll(items: Collection<T>): Boolean = items.lastOrNull()?.let { super.replaceAll(listOf(it)) } ?: false

        override fun toggle(items: Collection<T>): Boolean {
            if (observableSet.firstOrNull() in items) {
                return false
            }

            var result = false

            observableSet.batch {
                items.forEach {
                    result = remove(it)
                    if (!result) {
                        clear()
                        result = add(it)
                    }
                }
            }

            return result
        }
    }

    io.nacular.doodle.controls.list.List(
        model,
        itemVisualizer,
        selectionModel = ForcedSingleItemSelectionModel(),
        fitContent     = fitContents
    ).apply {
        val items = model.asIterable().iterator()

        initial.fold({it}, null)?.let { value ->
            var itemIndex = 0

            while (items.hasNext()) {
                val item = items.next()

                if (item == value) {
                    setSelection(setOf(itemIndex))
                    break
                }

                itemIndex += 1
            }
        }

        state            = selection.firstOrNull()?.let { Valid(this[it].getOrThrow()) } ?: Invalid()
        isFocusCycleRoot = false

        selectionChanged += { _,_,_ ->
            state = Valid(this[selection.first()].getOrThrow())
        }

        config(this)
    }
}

// endregion

// region Form<T>

/**
 * Creates a [Form] component that is bound to a [Field]. This control allows nesting of forms using
 * a DSL like that used for top-level forms.
 *
 * @param builder used to construct the form
 */
public fun <T> form(builder: FormControlBuildContext<T>.() -> FieldVisualizer<T>): FieldVisualizer<T> {
    return field {
        builder(FormControlBuildContext(field, initial))(this)
    }
}

// endregion

// endregion

// region List<T>
/**
 * Creates a list of [CheckBox]s that is bound to a [Field]. This controls lets a user select multiple
 * options from a list. This control lets a user ignore selection entirely, which would result in an empty list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param config used to control the resulting component
 */
public fun <T> checkList(
           first : T,
    vararg rest  : T,
           config: OptionListConfig<T>.() -> Unit = {}
): FieldVisualizer<List<T>> = buildToggleList(first, rest = rest, config) { CheckBox().apply { width = 16.0 } }

/**
 * Creates a list of [Switch][io.nacular.doodle.controls.buttons.Switch]es that is bound to a [Field]. This controls
 * lets a user select multiple options from a list. This control lets a user ignore selection entirely, which would
 * result in an empty list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param config used to control the resulting component
 */
public fun <T> switchList(
           first : T,
    vararg rest  : T,
           config: OptionListConfig<T>.() -> Unit = {}
): FieldVisualizer<List<T>> = buildToggleList(
        first  = first,
        rest   = rest,
        config = config,
        layout = {
            switch, label -> constrain(label, switch) { label_, switch_ ->
                switch_.width   eq 30
                switch_.height  eq 20
                switch_.right   eq parent.right
                switch_.centerY eq parent.centerY

                label_.left     eq 0
                label_.centerY  eq switch_.centerY
            }
        }
) {
    Switch()
}

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select multiple options from a list. This control lets a user ignore selection entirely,
 * which would result in an empty list. It is similar to a [checkList].
 *
 * @param T is the type of the items in the bounded field
 * @param model for the list
 * @param itemVisualizer used to render items in the list
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun <T, M: ListModel<T>> list(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<T, M>) -> Unit = {}): FieldVisualizer<List<T>> = field {
    io.nacular.doodle.controls.list.List(
        model,
        itemVisualizer,
        selectionModel = MultiSelectionModel(),
        fitContent     = fitContents
    ).apply {
        var itemIndex = 0
        val items            = model.asIterable().iterator()
        val initialSelection = mutableListOf<Int>()

        initial.fold({it}, emptyList()).forEach { value ->
            while (items.hasNext()) {
                val item = items.next()

                if (item == value) {
                    initialSelection += itemIndex++
                    break
                }

                itemIndex += 1
            }

            if (itemIndex >= model.size) {
                return@forEach
            }
        }

        state            = Valid(initialSelection.mapNotNull { this[it].getOrNull() })
        isFocusCycleRoot = false

        setSelection(initialSelection.toSet())

        selectionChanged += { _,_,_ ->
            state = Valid(selection.sorted().mapNotNull { this[it].getOrNull() })
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
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun <T> list(
           first         : T,
    vararg rest          : T,
           itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
           fitContents   : Set<Dimension> = setOf(Height),
           config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<List<T>> = list(
    SimpleListModel(listOf(first) + rest),
    itemVisualizer,
    fitContents,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select multiple options from a list. This control lets a user ignore selection entirely,
 * which would result in an empty list. It is similar to a [checkList].
 *
 * @param progression to use for values
 * @param itemVisualizer used to render items in the list
 * @param fitContents signaling whether the list should scale with its contents
 * @param config used to control the resulting component
 */
public fun list(
    progression   : IntProgression,
    itemVisualizer: ItemVisualizer<Int, IndexedItem> = toString(StringVisualizer()),
    fitContents   : Set<Dimension> = setOf(Height),
    config        : (io.nacular.doodle.controls.list.List<Int, *>) -> Unit = {}): FieldVisualizer<List<Int>> = list(
    IntProgressionModel(progression),
    itemVisualizer,
    fitContents,
    config
)

// endregion

// region Decorations
/**
 * Config for [labeled] controls.
 */
public class NamedConfig internal constructor(public val label: Label) {
    /**
     * @param spacing that determines the space between the label and field
     * @return the default layout
     */
    public fun defaultLayout(spacing: Double = DEFAULT_SPACING, itemHeight: Double = DEFAULT_HEIGHT): (container: View, field: View) -> Layout? = { container,_ ->
        label.fitText = setOf(Width)

        ExpandingVerticalLayout(container, spacing, itemHeight)
    }

    /**
     * Defines the layout for the named container.
     */
    public var layout: (container: View, field: View) -> Layout? = defaultLayout()

    /**
     * Specifies named container insets.
     */
    public var insets: Insets = Insets.None

    /**
     * Allows customized rendering of the named container.
     */
    public var render: Canvas.(container: View) -> Unit = {}
}

/**
 * Defines style of indicator to use when showing [labeled] fields as required.
 *
 * @see labeled
 */
public sealed class RequiredIndicatorStyle(internal val text: StyledText)

/**
 * Always appends the indicator.
 *
 * @see labeled
 * @param text to append to field name
 */
public class Always(text: StyledText): RequiredIndicatorStyle(text) {
    /**
     * @param text to append to field name
     */
    public constructor(text: String = "*"): this(StyledText(text))
}

/**
 * Only appends the indicator after a field is initially (or becomes) [invalid][Invalid].
 *
 * @see labeled
 * @param text to append to field name
 */
public class WhenInvalid(text: StyledText): RequiredIndicatorStyle(text) {
    /**
     * @param text to append to field name
     */
    public constructor(text: String = "*"): this(StyledText(text))
}

/**
 * Creates a component with a [Label] and the result of [visualizer] that is bound to a [Field].
 * This control simply wraps an existing one with a configurable text label.
 *
 * @param name used in the label
 * @param showRequired used to indicate whether the field is required.
 * @param visualizer being decorated
 */
public fun <T> labeled(
    name        : StyledText,
    showRequired: RequiredIndicatorStyle? = WhenInvalid(),
    visualizer  : NamedConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = field {
    container {
        val label         = UninteractiveLabel(name)
        val builder       = NamedConfig(label)
        val visualization = visualizer(builder)

        insets    = builder.insets
        render    = { builder.render(this, this@container) }
        focusable = false

        this += listOf(label, visualization(this@field).also { it.accessibilityLabelProvider = label }).onEach {
            it.sizePreferencesChanged += { _, _, _ ->
                relayout()
            }
        }

        showRequired?.let {
            if (it is Always || state is Invalid<T>) label.styledText = name.copy() + showRequired.text

            stateChanged += {
                if (it.state is Invalid<T>) label.styledText = name.copy() + showRequired.text
            }
        }

        layout = builder.layout(this, children[1])
    }
}

/**
 * Creates a component with a [Label] and the result of [visualizer] that is bound to a [Field].
 * This control simply wraps an existing one with a configurable text label.
 *
 * @param name used in the label
 * @param showRequired used to indicate whether the field is required.
 * @param visualizer being decorated
 */
public fun <T> labeled(
    name        : String,
    showRequired: RequiredIndicatorStyle? = WhenInvalid(),
    visualizer  : NamedConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = labeled(StyledText(name), showRequired, visualizer)

/**
 * Config for [labeled] controls.
 */
public class LabeledConfig internal constructor(public val name: Label, public val help: Label) {
    /**
     * Defines the layout for the named container.
     */
    public var layout: (container: View, field: View) -> Layout? = { container,_ ->
        name.fitText = setOf(Width)
        help.fitText = setOf(Width)

        ExpandingVerticalLayout(container, DEFAULT_SPACING, DEFAULT_HEIGHT)
    }

    /**
     * Specifies named container insets.
     */
    public var insets: Insets = Insets.None

    /**
     * Allows customized rendering of the named container.
     */
    public var render: Canvas.(container: View) -> Unit = {}
}

/**
 * Creates a component with a name [Label], the result of [visualizer] and a helper [Label] that is bound to a [Field].
 * This control simply wraps an existing one with configurable text labels.
 *
 * @param name used in the name label
 * @param help used as helper text
 * @param showRequired used to indicate whether the field is required.
 * @param visualizer being decorated
 */
public fun <T> labeled(
    name        : StyledText,
    help        : StyledText,
    showRequired: RequiredIndicatorStyle? = WhenInvalid(),
    visualizer  : LabeledConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = field {
    container {
        val nameLabel     = UninteractiveLabel(name)
        val helperLabel   = UninteractiveLabel(help)
        val builder       = LabeledConfig(nameLabel, helperLabel)
        val visualization = visualizer(builder)

        insets    = builder.insets
        render    = { builder.render(this, this@container) }
        focusable = false

        this += listOf(nameLabel, visualization(this@field).also { it.accessibilityLabelProvider = nameLabel }, helperLabel).onEach {
            it.sizePreferencesChanged += { _, _, _ ->
                relayout()
            }
        }

        showRequired?.let {
            if (it is Always || state is Invalid<T>)  nameLabel.styledText = name.copy() + showRequired.text

            stateChanged += {
                if (it.state is Invalid<T>) nameLabel.styledText = name.copy() + showRequired.text
            }
        }

        layout = builder.layout(this, children[1])
    }
}

/**
 * Creates a component with a name [Label], the result of [visualizer] and a helper [Label] that is bound to a [Field].
 * This control simply wraps an existing one with configurable text labels.
 *
 * @param name used in the label
 * @param help used as helper text
 * @param showRequired used to indicate whether the field is required.
 * @param visualizer being decorated
 */
public fun <T> labeled(
    name        : String,
    help        : String,
    showRequired: RequiredIndicatorStyle? = WhenInvalid(),
    visualizer  : LabeledConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = labeled(
    StyledText(name),
    StyledText(help),
    showRequired,
    visualizer
)

/**
 * Config for [scrolling] controls.
 *
 * @property scrollPanel used for scrolling
 */
public class ScrollingConfig internal constructor(public val scrollPanel: ScrollPanel)

/**
 * Creates a [ScrollPanel] with the result of [visualizer] as its content, that is bound to a [Field].
 * This control simply wraps an existing one with a configurable scroll panel.
 *
 * @param visualizer being decorated
 */
public fun <T> scrolling(visualizer: ScrollingConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = field {
    ScrollPanel().apply {
        matchContentIdealSize = false
        content               = visualizer(ScrollingConfig(this))(this@field)
    }
}

/**
 * Creates a [Container] with the result of [visualizer] as its only child, that is bound to a [Field].
 * This control simply wraps an existing one with a configurable container.
 *
 * @param visualizer being decorated
 */
public fun <T> framed(visualizer: ContainerBuilder.() -> FieldVisualizer<T>): FieldVisualizer<T> = field {
    container {
        layout    = verticalLayout(this, itemHeight = DEFAULT_HEIGHT)
        focusable = false

        this += visualizer(this)(this@field)
    }
}

// endregion

// region Public Utils
/**
 * @property initial value of the field this form is bound to.
 */
public class FormControlBuildContext<T> internal constructor(private val field: Field<T>, public val initial: FieldState<T>) {
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
        ExpandingVerticalLayout(it, DEFAULT_FORM_SPACING, DEFAULT_HEIGHT)
    }

    /**
     * Defines the insets of the resulting [Form].
     */
    public var insets: Insets = Insets.None

    /**
     * Specifies a behavior to use with the resulting [Form].
     */
    public var behavior: Behavior<Form>? = null

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A> invoke(
        a        : Field<A>,
        onInvalid: ( ) -> Unit = {},
        onReady  : (A) -> T): FieldVisualizer<T> = field {
        Form {
            this(a, onInvalid = { field.state = Invalid(); onInvalid() }) { a ->
                state = Valid(onReady(a))
            }
        }.apply { configure(this) }
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
        }.apply { configure(this) }
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
        }.apply { configure(this) }
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
        }.apply { configure(this) }
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
        }.apply { configure(this) }
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
        }.apply { configure(this) }
    }

    private fun configure(form: Form) {
        form.layout    = layout(form)
        form.insets    = insets
        form.behavior  = behavior
        form.focusable = false
    }
}

public fun verticalLayout(container: View, spacing: Double = 2.0, itemHeight: Double? = null): Layout = ExpandingVerticalLayout(container, spacing, itemHeight)

// endregion

// region Internals
private fun <T> buildToggleList(
           first        : T,
    vararg rest         : T,
           config       : OptionListConfig<T>.() -> Unit = {},
           layout       : (button: ToggleButton, label: View) -> Layout? = { _,_ ->null },
           toggleBuilder: () -> ToggleButton): FieldVisualizer<List<T>> = field {
    val builder   = OptionListConfig<T>().also(config)
    val selection = mutableListOf<T>()

    container {
        insets        = builder.insets
        render        = { builder.render(this, this@container) }
        focusable     = false
        val items     = listOf(first) + rest
        var itemIndex = 0

        initial.fold({ it }, emptyList()).forEachIndexed { _, value ->
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

        state = Valid(ArrayList(selection))

        this += items.map { value ->
            container {
                focusable = false
                val visualizedValue = builder.visualizer(value)

                this += visualizedValue
                this += toggleBuilder().apply {
                    selected         = value in selection
                    selectedChanged += { _, _, selected ->
                        when {
                            selected -> selection += value
                            else     -> selection -= value
                        }

                        state = Valid(ArrayList(selection))
                    }
                    accessibilityLabelProvider = visualizedValue
                }

                this.layout = (layout(children[1] as ToggleButton, children[0]) ?: buttonItemLayout(button = children[1], label = children[0])).then {
                    idealSize = Size(width, children.maxOf { it.height })
                }
            }
        }
        this.layout = ExpandingVerticalLayout(this, builder.spacing, builder.itemHeight)
    }
}

private fun <T> buildRadioList(
           first           : T,
    vararg rest            : T,
           optionListConfig: OptionListConfig<T>,
           initialValue    : T? = null,
           allowDeselectAll: Boolean = false,
           config          : (T, RadioButton) -> Unit): Container = container {
    insets     = optionListConfig.insets
    render     = { optionListConfig.render(this, this@container) }
    val group  = ButtonGroup(allowDeselectAll = allowDeselectAll)
    children  += (listOf(first) + rest).map { value ->
        container {
            focusable = false
            val visualizedValue = optionListConfig.visualizer(value)

            this += visualizedValue
            this += RadioButton().apply {
                group += this

                initialValue?.let {
                    selected = value == it
                }

                config(value, this)

                width                      = 16.0
                accessibilityLabelProvider = visualizedValue
            }

            layout = buttonItemLayout(button = children[1], label = children[0]).then {
                idealSize = Size(width, children.maxOf { it.height })
            }
        }
    }
    focusable = false
    layout    = ExpandingVerticalLayout(this, optionListConfig.spacing, optionListConfig.itemHeight)
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

private class UninteractiveLabel(text: StyledText): Label(text) {
    override fun contains(point: Point) = false
}

private class ExpandingVerticalLayout(private val view: View, spacing: Double, private val itemHeight: Double? = null): Layout {
    private val delegate = ListLayout(spacing = spacing, widthSource = WidthSource.Parent)

    private fun maxOrNull(first: Double?, second: Double?): Double? = when {
        first != null && second != null -> max(first, second)
        first != null                   -> first
        else                            -> second
    }

    override fun requiresLayout(child: Positionable, of: PositionableContainer, old: SizePreferences, new: SizePreferences) =
            (itemHeight == null && old.idealSize != new.idealSize) || delegate.requiresLayout(child, of, old, new)

    override fun layout(container: PositionableContainer) {
        container.children.forEach { child ->
            // TODO: Fix so that itemHeight is used over ideal height if set
            (child.idealSize?.height ?: itemHeight)?.let { child.height = it }
        }

        delegate.layout(container)

        val size       = Size(container.width, container.children.last().bounds.bottom + container.insets.bottom)
        view.size      = Size(size.width, max(size.height, this.view.height))
        view.idealSize = size
    }
}

private fun buttonItemLayout(button: View, label: View, labelOffset: Double = 26.0) = constrain(button, label) { button_, label_ ->
    button_.top    eq 0
    button_.width  eq parent.width
    button_.height eq parent.height
    label_.left    eq labelOffset
    label_.centerY eq button_.centerY
}

private const val DEFAULT_HEIGHT       = 32.0
private const val DEFAULT_SPACING      =  2.0
private const val DEFAULT_FORM_SPACING = 12.0

// endregion