@file:Suppress("unused")

package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.BasicConfinedRangeModel
import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ByteTypeConverter
import io.nacular.doodle.controls.ConfinedRangeModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.DoubleTypeConverter
import io.nacular.doodle.controls.FloatTypeConverter
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.IntProgressionModel
import io.nacular.doodle.controls.IntTypeConverter
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.LongTypeConverter
import io.nacular.doodle.controls.MultiSelectionModel
import io.nacular.doodle.controls.ShortTypeConverter
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.StringVisualizer
import io.nacular.doodle.controls.buttons.ButtonGroup
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.buttons.Switch
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.controls.form.Form.Field
import io.nacular.doodle.controls.form.Form.FieldState
import io.nacular.doodle.controls.form.Form.Invalid
import io.nacular.doodle.controls.form.Form.Valid
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.range.InvertibleFunction
import io.nacular.doodle.controls.range.LinearFunction
import io.nacular.doodle.controls.range.RangeSlider
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.selectbox.SelectBox
import io.nacular.doodle.controls.spinbutton.IntSpinButtonModel
import io.nacular.doodle.controls.spinbutton.ListSpinButtonModel
import io.nacular.doodle.controls.spinbutton.SpinButton
import io.nacular.doodle.controls.spinbutton.SpinButtonModel
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.toString
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.ContainerBuilder
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.scrollTo
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.ListLayout
import io.nacular.doodle.layout.WidthSource
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.CharInterpolator
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.Interpolator
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.PassThroughEncoder
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.doodle.utils.interpolator
import io.nacular.doodle.utils.observable
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Units
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.jvm.JvmName

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
            when {
                !hasFocus -> validate(field, text)
                else      -> parent?.scrollTo(bounds)
            }
        }

        configObject = TextFieldConfig(this@apply)
        config(configObject)

        when {
            initial is Valid && validator(initial.value) -> encoder.encode(initial.value).getOrNull()?.let {
                text = it
                if (text.isBlank()) {
                    validate(field, it)
                }
            }
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
        + label
        + checkBox()

        focusable = false

        this.layout        = buttonItemLayout(button = children[1], label = children[0])
        this.preferredSize = { _,max -> Size(max.width, children.maxOf { it.idealSize.height }) }
    }
}

/**
 * Creates a [CheckBox] control that is bound to a [Field] (of type [Boolean]).
 *
 * @param label used for the checkbox
 */
public fun check(label: String): FieldVisualizer<Boolean> = field {
    checkBox(label)
}

/**
 * Creates a [Switch] control that is bound to a [Field] (of type [Boolean]).
 *
 * @param label used to annotate the switch
 */
public fun switch(label: View): FieldVisualizer<Boolean> = field {
    container {
        + label
        + Switch().apply {
            accessibilityLabelProvider = label

            initial.ifValid { selected = it }

            selectedChanged += { _,_,_ ->
                state = Valid(selected)
            }

            focusChanged += { _,_,focused ->
                if (focused) {
                    parent?.scrollTo(bounds)
                }
            }

            state = Valid(selected)

            suggestSize(Size(30, 20))
        }

        focusable = false

        layout = constrain(children[0], children[1]) { label, switch ->
            switch.left    greaterEq 0
            switch.right   eq parent.right strength Strong
            switch.centerY eq parent.centerY
            switch.width.preserve
            switch.height.preserve

            label.left     eq 0
            label.centerY  eq switch.centerY
            label.height.preserve

            parent.height  eq max(label.bottom, switch.bottom)
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
public class SliderConfig<T> internal constructor(public val slider: Slider<T>) where T: Comparable<T>

// region Number

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public fun slider(
    range      : ClosedRange<Int>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Int>.() -> Unit = {}
): FieldVisualizer<Int> = slider(BasicConfinedValueModel(range), orientation, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public fun slider(
    model      : ConfinedValueModel<Int>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Int>.() -> Unit = {},
): FieldVisualizer<Int> = slider(model, orientation, IntTypeConverter, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderFloat")
public fun slider(
    range      : ClosedRange<Float>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Float>.() -> Unit = {}
): FieldVisualizer<Float> = slider(BasicConfinedValueModel(range), orientation, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderFloat")
public fun slider(
    model      : ConfinedValueModel<Float>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Float>.() -> Unit = {},
): FieldVisualizer<Float> = slider(model, orientation, FloatTypeConverter, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderDouble")
public fun slider(
    range      : ClosedRange<Double>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Double>.() -> Unit = {}
): FieldVisualizer<Double> = slider(BasicConfinedValueModel(range), orientation, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderDouble")
public fun slider(
    model      : ConfinedValueModel<Double>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Double>.() -> Unit = {},
): FieldVisualizer<Double> = slider(model, orientation, DoubleTypeConverter, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderLong")
public fun slider(
    range      : ClosedRange<Long>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Long>.() -> Unit = {}
): FieldVisualizer<Long> = slider(BasicConfinedValueModel(range), orientation, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderLong")
public fun slider(
    model      : ConfinedValueModel<Long>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Long>.() -> Unit = {},
): FieldVisualizer<Long> = slider(model, orientation, LongTypeConverter, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderShort")
public fun slider(
    range      : ClosedRange<Short>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Short>.() -> Unit = {}
): FieldVisualizer<Short> = slider(BasicConfinedValueModel(range), orientation, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderShort")
public fun slider(
    model      : ConfinedValueModel<Short>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Short>.() -> Unit = {},
): FieldVisualizer<Short> = slider(model, orientation, ShortTypeConverter, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderByte")
public fun slider(
    range      : ClosedRange<Byte>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Byte>.() -> Unit = {}
): FieldVisualizer<Byte> = slider(BasicConfinedValueModel(range), orientation, config)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderByte")
public fun slider(
    model      : ConfinedValueModel<Byte>,
    orientation: Orientation                  = Horizontal,
    config     : SliderConfig<Byte>.() -> Unit = {},
): FieldVisualizer<Byte> = slider(model, orientation, ByteTypeConverter, config)

// endregion

// region Char

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderChar")
public fun slider(
    range      : ClosedRange<Char>,
    orientation: Orientation = Horizontal,
    config     : SliderConfig<Char>.() -> Unit = {}
): FieldVisualizer<Char> = slider(
    model        = BasicConfinedValueModel(range) as ConfinedValueModel<Char>,
    orientation  = orientation,
    interpolator = CharInterpolator,
    config       = config
)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderChar")
public fun slider(
    model      : ConfinedValueModel<Char>,
    orientation: Orientation = Horizontal,
    config     : SliderConfig<Char>.() -> Unit = {},
): FieldVisualizer<Char> = slider(model, orientation, CharInterpolator, config)

// endregion

// region Measure<T>

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderMeasure")
public fun <T: Units> slider(
    range      : ClosedRange<Measure<T>>,
    orientation: Orientation = Horizontal,
    config     : SliderConfig<Measure<T>>.() -> Unit = {}
): FieldVisualizer<Measure<T>> = slider(
    model       = BasicConfinedValueModel(range) as ConfinedValueModel<Measure<T>>,
    orientation = orientation,
    config      = config
)

/**
 * Creates a [Slider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("sliderMeasure")
public fun <T: Units> slider(
    model      : ConfinedValueModel<Measure<T>>,
    orientation: Orientation = Horizontal,
    config     : SliderConfig<Measure<T>>.() -> Unit = {},
): FieldVisualizer<Measure<T>> = slider(model, orientation, model.value.units.interpolator, config)

// endregion

// region T

public fun <T> slider(
    range       : ClosedRange<T>,
    orientation : Orientation = Horizontal,
    interpolator: Interpolator<T>,
    config      : SliderConfig<T>.() -> Unit = {},
): FieldVisualizer<T> where T: Comparable<T> = slider(
    model        = BasicConfinedValueModel(range) as ConfinedValueModel<T>,
    orientation  = orientation,
    interpolator = interpolator,
    config       = config
)

public fun <T> slider(
    model       : ConfinedValueModel<T>,
    orientation : Orientation = Horizontal,
    interpolator: Interpolator<T>,
    config      : SliderConfig<T>.() -> Unit = {},
): FieldVisualizer<T> where T: Comparable<T> = field {
    Slider(model, interpolator, orientation).apply {
        config(SliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }

        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
        }
    }
}

// endregion

// endregion

// region CircularSlider

/**
 * Configuration used to customize [circularSlider] controls.
 *
 * @property slider within the control
 */
public class CircularSliderConfig<T> internal constructor(public val slider: CircularSlider<T>) where T: Comparable<T>

// region Number

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
public fun circularSlider(
    model : ConfinedValueModel<Int>,
    config: CircularSliderConfig<Int>.() -> Unit = {},
): FieldVisualizer<Int> = circularSlider(model, IntTypeConverter, config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. In this control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
public fun circularSlider(
    range : ClosedRange<Int>,
    config: CircularSliderConfig<Int>.() -> Unit = {}
): FieldVisualizer<Int> = circularSlider(BasicConfinedValueModel(range) as ConfinedValueModel<Int>, config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderFloat")
public fun circularSlider(
    model : ConfinedValueModel<Float>,
    config: CircularSliderConfig<Float>.() -> Unit = {},
): FieldVisualizer<Float> = circularSlider(model, FloatTypeConverter, config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. In this control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderFloat")
public fun circularSlider(
    range : ClosedRange<Float>,
    config: CircularSliderConfig<Float>.() -> Unit = {}
): FieldVisualizer<Float> = circularSlider(BasicConfinedValueModel(range), config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderDouble")
public fun circularSlider(
    model : ConfinedValueModel<Double>,
    config: CircularSliderConfig<Double>.() -> Unit = {},
): FieldVisualizer<Double> = circularSlider(model, DoubleTypeConverter, config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. In this control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderDouble")
public fun circularSlider(
    range : ClosedRange<Double>,
    config: CircularSliderConfig<Double>.() -> Unit = {}
): FieldVisualizer<Double> = circularSlider(BasicConfinedValueModel(range), config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderLong")
public fun circularSlider(
    model : ConfinedValueModel<Long>,
    config: CircularSliderConfig<Long>.() -> Unit = {},
): FieldVisualizer<Long> = circularSlider(model, LongTypeConverter, config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. In this control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderLong")
public fun circularSlider(
    range : ClosedRange<Long>,
    config: CircularSliderConfig<Long>.() -> Unit = {}
): FieldVisualizer<Long> = circularSlider(BasicConfinedValueModel(range), config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderShort")
public fun circularSlider(
    model : ConfinedValueModel<Short>,
    config: CircularSliderConfig<Short>.() -> Unit = {},
): FieldVisualizer<Short> = circularSlider(model, ShortTypeConverter, config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. In this control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderShort")
public fun circularSlider(
    range : ClosedRange<Short>,
    config: CircularSliderConfig<Short>.() -> Unit = {}
): FieldVisualizer<Short> = circularSlider(BasicConfinedValueModel(range), config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderByte")
public fun circularSlider(
    model : ConfinedValueModel<Byte>,
    config: CircularSliderConfig<Byte>.() -> Unit = {},
): FieldVisualizer<Byte> = circularSlider(model, ByteTypeConverter, config)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. In this control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderByte")
public fun circularSlider(
    range : ClosedRange<Byte>,
    config: CircularSliderConfig<Byte>.() -> Unit = {}
): FieldVisualizer<Byte> = circularSlider(BasicConfinedValueModel(range) as ConfinedValueModel<Byte>, config)

// endregion

// region Char

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderChar")
public fun circularSlider(
    range : ClosedRange<Char>,
    config: CircularSliderConfig<Char>.() -> Unit = {}
): FieldVisualizer<Char> = circularSlider(
    model            = BasicConfinedValueModel(range) as ConfinedValueModel<Char>,
    config           = config,
    numberTypeConverter = CharInterpolator
)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderChar")
public fun circularSlider(
    model : ConfinedValueModel<Char>,
    config: CircularSliderConfig<Char>.() -> Unit = {},
): FieldVisualizer<Char> = circularSlider(model, CharInterpolator, config)

// endregion

// region Measure<T>

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderMeasure")
public fun <T: Units> circularSlider(
    range : ClosedRange<Measure<T>>,
    config: CircularSliderConfig<Measure<T>>.() -> Unit = {}
): FieldVisualizer<Measure<T>> = circularSlider(
    model  = BasicConfinedValueModel(range) as ConfinedValueModel<Measure<T>>,
    config = config
)

/**
 * Creates a [CircularSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularSliderMeasure")
public fun <T: Units> circularSlider(
    model : ConfinedValueModel<Measure<T>>,
    config: CircularSliderConfig<Measure<T>>.() -> Unit = {},
): FieldVisualizer<Measure<T>> = circularSlider(model, model.value.units.interpolator, config)

// endregion

// region T

/**
 * @see circularSlider
 */
public fun <T> circularSlider(
    range           : ClosedRange<T>,
    numberTypeConverter: Interpolator<T>,
    config          : CircularSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<T> where T: Comparable<T> = circularSlider(
    model            = BasicConfinedValueModel(range) as ConfinedValueModel<T>,
    numberTypeConverter = numberTypeConverter,
    config           = config
)

/**
 * @see circularSlider
 */
public fun <T> circularSlider(
    model           : ConfinedValueModel<T>,
    numberTypeConverter: Interpolator<T>,
    config          : CircularSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<T> where T: Comparable<T> = field {
    CircularSlider(model, numberTypeConverter).apply {
        config(CircularSliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }

        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
        }
    }
}

// endregion

// endregion

// endregion

// region ClosedRange<Number>

// region Slider

/**
 * Configuration used to customize [rangeSlider] controls.
 *
 * @property slider within the control
 */
public class RangeSliderConfig<T> internal constructor(public val slider: RangeSlider<T>) where T: Comparable<T>

// region Number

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public fun rangeSlider(
    model      : ConfinedRangeModel<Int>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Int>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Int>> = rangeSlider(model, IntTypeConverter, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
public fun rangeSlider(
    range      : ClosedRange<Int>,
    orientation: Orientation                       = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Int>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Int>> = rangeSlider(BasicConfinedRangeModel(range) as ConfinedRangeModel<Int>, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderFloat")
public fun rangeSlider(
    model      : ConfinedRangeModel<Float>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Float>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Float>> = rangeSlider(model, FloatTypeConverter, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderFloat")
public fun rangeSlider(
    range      : ClosedRange<Float>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Float>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Float>> = rangeSlider(BasicConfinedRangeModel(range) as ConfinedRangeModel<Float>, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderDouble")
public fun rangeSlider(
    model      : ConfinedRangeModel<Double>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Double>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Double>> = rangeSlider(model, DoubleTypeConverter, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderDouble")
public fun rangeSlider(
    range      : ClosedRange<Double>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Double>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Double>> = rangeSlider(BasicConfinedRangeModel(range) as ConfinedRangeModel<Double>, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderLong")
public fun rangeSlider(
    model      : ConfinedRangeModel<Long>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Long>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Long>> = rangeSlider(model, LongTypeConverter, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderLong")
public fun rangeSlider(
    range      : ClosedRange<Long>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Long>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Long>> = rangeSlider(BasicConfinedRangeModel(range) as ConfinedRangeModel<Long>, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderShort")
public fun rangeSlider(
    model      : ConfinedRangeModel<Short>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Short>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Short>> = rangeSlider(model, ShortTypeConverter, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderShort")
public fun rangeSlider(
    range      : ClosedRange<Short>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Short>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Short>> = rangeSlider(BasicConfinedRangeModel(range), orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderByte")
public fun rangeSlider(
    model      : ConfinedRangeModel<Byte>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Byte>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Byte>> = rangeSlider(model, ByteTypeConverter, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderByte")
public fun rangeSlider(
    range      : ClosedRange<Byte>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Byte>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Byte>> = rangeSlider(BasicConfinedRangeModel(range), orientation, function, config)

// endregion

// region Char

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderChar")
public fun rangeSlider(
    model      : ConfinedRangeModel<Char>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Char>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Char>> = rangeSlider(model, CharInterpolator, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderChar")
public fun rangeSlider(
    range      : ClosedRange<Char>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Char>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Char>> = rangeSlider(
    model       = BasicConfinedRangeModel(range),
    orientation = orientation,
    converter   = CharInterpolator,
    config      = config,
    function    = function
)

// endregion

// region Measure<T>

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderMeasure")
public fun <T: Units> rangeSlider(
    model      : ConfinedRangeModel<Measure<T>>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Measure<T>>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Measure<T>>> = rangeSlider(model, model.range.start.units.interpolator, orientation, function, config)

/**
 * Creates a [RangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param orientation of the Slider
 * @param config for the Slider
 */
@JvmName("rangeSliderMeasure")
public fun <T: Units> rangeSlider(
    range      : ClosedRange<Measure<T>>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<Measure<T>>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Measure<T>>> = rangeSlider(
    model       = BasicConfinedRangeModel(range),
    orientation = orientation,
    converter   = range.start.units.interpolator,
    config      = config,
    function    = function
)

// endregion

// region T


/**
 * @see rangeSlider
 */
public fun <T> rangeSlider(
    range      : ClosedRange<T>,
    converter  : Interpolator<T>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<ClosedRange<T>> where T: Comparable<T> = rangeSlider(
    model       = BasicConfinedRangeModel(range) as ConfinedRangeModel<T>,
    orientation = orientation,
    converter   = converter,
    config      = config,
    function    = function
)

/**
 * @see rangeSlider
 */
public fun <T> rangeSlider(
    model      : ConfinedRangeModel<T>,
    converter  : Interpolator<T>,
    orientation: Orientation = Horizontal,
    function   : InvertibleFunction = LinearFunction,
    config     : RangeSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<ClosedRange<T>> where T: Comparable<T> = field {
    RangeSlider(model, converter, orientation, function).apply {
        config(RangeSliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }

        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
        }
    }
}

// endregion

// endregion

// region CircularSlider

/**
 * Configuration used to customize [slider] controls.
 *
 * @property slider within the control
 */
public class CircularRangeSliderConfig<T> internal constructor(public val slider: CircularRangeSlider<T>) where T: Comparable<T>

// region Number

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
public fun circularRangeSlider(
    range : ClosedRange<Int>,
    function: InvertibleFunction = LinearFunction,
    config: CircularRangeSliderConfig<Int>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Int>> = circularRangeSlider(BasicConfinedRangeModel(range), function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
public fun circularRangeSlider(
    model   : ConfinedRangeModel<Int>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Int>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Int>> = circularRangeSlider(model, IntTypeConverter, function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderFloat")
public fun circularRangeSlider(
    range   : ClosedRange<Float>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Float>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Float>> = circularRangeSlider(BasicConfinedRangeModel(range), function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderFloat")
public fun circularRangeSlider(
    model   : ConfinedRangeModel<Float>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Float>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Float>> = circularRangeSlider(model, FloatTypeConverter, function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderDouble")
public fun circularRangeSlider(
    range   : ClosedRange<Double>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Double>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Double>> = circularRangeSlider(BasicConfinedRangeModel(range), function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderDouble")
public fun circularRangeSlider(
    model   : ConfinedRangeModel<Double>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Double>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Double>> = circularRangeSlider(model, DoubleTypeConverter, function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderLong")
public fun circularRangeSlider(
    range   : ClosedRange<Long>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Long>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Long>> = circularRangeSlider(BasicConfinedRangeModel(range), function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderLong")
public fun circularRangeSlider(
    model   : ConfinedRangeModel<Long>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Long>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Long>> = circularRangeSlider(model, LongTypeConverter, function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderShort")
public fun circularRangeSlider(
    range   : ClosedRange<Short>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Short>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Short>> = circularRangeSlider(BasicConfinedRangeModel(range), function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderShort")
public fun circularRangeSlider(
    model   : ConfinedRangeModel<Short>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Short>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Short>> = circularRangeSlider(model, ShortTypeConverter, function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderByte")
public fun circularRangeSlider(
    range   : ClosedRange<Byte>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Byte>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Byte>> = circularRangeSlider(BasicConfinedRangeModel(range), function, config)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderByte")
public fun circularRangeSlider(
    model   : ConfinedRangeModel<Byte>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Byte>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Byte>> = circularRangeSlider(model, ByteTypeConverter, function, config)

// endregion

// region Char

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderChar")
public fun circularRangeSlider(
    range   : ClosedRange<Char>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Char>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Char>> = circularRangeSlider(
    model    = BasicConfinedRangeModel(range) as ConfinedRangeModel<Char>,
    config   = config,
    function = function
)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderChar")
public fun circularRangeSlider(
    model   : ConfinedRangeModel<Char>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Char>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Char>> = circularRangeSlider(model, CharInterpolator, function, config)

// endregion

// region Measure<T>

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param range for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderMeasure")
public fun <T: Units> circularRangeSlider(
    range   : ClosedRange<Measure<T>>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Measure<T>>.() -> Unit = {}
): FieldVisualizer<ClosedRange<Measure<T>>> = circularRangeSlider(
    model    = BasicConfinedRangeModel(range) as ConfinedRangeModel<Measure<T>>,
    config   = config,
    function = function
)

/**
 * Creates a [CircularRangeSlider] control that is bound to a [Field]. This control lets the user select a
 * value within a range.
 *
 * @param model for the Slider
 * @param config for the Slider
 */
@JvmName("circularRangeSliderMeasure")
public fun <T: Units> circularRangeSlider(
    model   : ConfinedRangeModel<Measure<T>>,
    function: InvertibleFunction = LinearFunction,
    config  : CircularRangeSliderConfig<Measure<T>>.() -> Unit = {},
): FieldVisualizer<ClosedRange<Measure<T>>> = circularRangeSlider(model, model.range.start.units.interpolator, function, config)

// endregion

// region T

/**
 * @see circularRangeSlider
 */
public fun <T> circularRangeSlider(
    range    : ClosedRange<T>,
    converter: Interpolator<T>,
    function : InvertibleFunction = LinearFunction,
    config   : CircularRangeSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<ClosedRange<T>> where T: Comparable<T> = circularRangeSlider(
    model     = BasicConfinedRangeModel(range) as ConfinedRangeModel<T>,
    config    = config,
    converter = converter,
    function  = function
)

/**
 * @see circularRangeSlider
 */
public fun <T> circularRangeSlider(
    model    : ConfinedRangeModel<T>,
    converter: Interpolator<T>,
    function : InvertibleFunction = LinearFunction,
    config   : CircularRangeSliderConfig<T>.() -> Unit = {},
): FieldVisualizer<ClosedRange<T>> where T: Comparable<T> = field {
    CircularRangeSlider(model, converter, function).apply {
        config(CircularRangeSliderConfig(this))

        initial.ifValid { value = it }

        state = Valid(value)

        changed += { _,_,new -> state = Valid(new) }

        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
        }
    }
}

// endregion

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

        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
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

        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
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
     * Provides a label for each item in the list. This is shorthand for using [visualizer].
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

// region SelectBox

/**
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param model for the select box
 * @param boxItemVisualizer used to render the drop-down's box item
 * @param listItemVisualizer used to render items in the drop-down's list
 * @param config used to control the resulting component
 */
public fun <T, M: ListModel<T>> selectBox(
    model             : M,
    boxItemVisualizer : ItemVisualizer<T, IndexedItem>,
    listItemVisualizer: ItemVisualizer<T, IndexedItem> = boxItemVisualizer,
    config            : (SelectBox<T, *>) -> Unit = {}): FieldVisualizer<T> = field {
    SelectBox(model, boxItemVisualizer  = boxItemVisualizer, listItemVisualizer = listItemVisualizer).also { selectBox ->
        initial.ifValid {
            model.forEachIndexed { index, item ->
                if (item == it) {
                    selectBox.selection = index
                    return@forEachIndexed
                }
            }
        }

        selectBox.changed += {
            state = selectBox.value.fold(onSuccess = { Valid(it) }, onFailure = { Invalid() })
        }

        state = selectBox.value.fold(onSuccess = { Valid(it) }, onFailure = { Invalid() })
    }.also(config)
}

/**
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
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
public fun <T> selectBox(
    first             : T,
    vararg rest       : T,
    boxItemVisualizer : ItemVisualizer<T, IndexedItem>,
    listItemVisualizer: ItemVisualizer<T, IndexedItem> = boxItemVisualizer,
    config            : (SelectBox<T, *>) -> Unit = {}): FieldVisualizer<T> = selectBox(
    SimpleListModel(listOf(first) + rest),
    boxItemVisualizer,
    listItemVisualizer,
    config
)

/**
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
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
public fun <T> selectBox(
    first : T,
    vararg rest  : T,
    label : (T) -> String = { "$it" },
    config: (SelectBox<T, *>) -> Unit = {}
): FieldVisualizer<T> = selectBox(first, *rest, boxItemVisualizer = toString(StringVisualizer(), label), config = config)

/**
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
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
public fun <T: Any> selectBox(
    first                       : T,
    vararg rest                 : T,
    boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
    listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
    unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
    unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
    config                      : (SelectBox<T?, *>) -> Unit = {}): FieldVisualizer<T> = field {
    val model = SimpleListModel(listOf(null, first) + rest)

    buildSelectBox(
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
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
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
public fun <T: Any> selectBox(
           first          : T,
    vararg rest           : T,
           label          : (T) -> String = { "$it" },
           unselectedLabel: String,
           config         : (SelectBox<T?, *>) -> Unit = {}
): FieldVisualizer<T> = selectBox(
    first,
    *rest,
    boxItemVisualizer           = toString(StringVisualizer(), label),
    unselectedBoxItemVisualizer = toString(StringVisualizer()) { unselectedLabel },
    config                      = config
)

/**
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [optionalRadioList]. This control lets a user
 * ignore selection entirely and therefore the resulting type is [T]?.
 *
 * @param T is the type of the bounded field
 * @param model for the select box
 * @param boxItemVisualizer used to render the drop-down's box item
 * @param listItemVisualizer used to render items in the drop-down's list
 * @param unselectedBoxItemVisualizer used to render the drop-down's box item when it is unselected
 * @param unselectedListItemVisualizer used to render the "unselected item" in the drop-down's list
 * @param config used to control the resulting component
 */
public fun <T: Any, M: ListModel<T>> optionalSelectBox(
    model                       : M,
    boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
    listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
    unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
    unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
    config                      : (SelectBox<T?, *>) -> Unit = {}): FieldVisualizer<T?> = field {
    buildSelectBox(
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
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
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
public fun <T: Any> optionalSelectBox(
    first                       : T,
    vararg rest                        : T,
    boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
    listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
    unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
    unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
    config                      : (SelectBox<T?, *>) -> Unit = {}): FieldVisualizer<T?> = field {
    val model = SimpleListModel(listOf(null, first) + rest)

    buildSelectBox(
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
 * Creates a [SelectBox] control that is bound to a [Field]. This control lets a user
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
public fun <T: Any> optionalSelectBox(
    first          : T,
    vararg rest           : T,
    label          : (T) -> String = { "$it" },
    unselectedLabel: String,
    config         : (SelectBox<T?, *>) -> Unit = {}
): FieldVisualizer<T?> = optionalSelectBox(
    first,
    *rest,
    boxItemVisualizer           = toString(StringVisualizer(), label),
    unselectedBoxItemVisualizer = toString(StringVisualizer()) { unselectedLabel },
    config                      = config
)
// endregion

// region SpinButton

/**
 * Creates a [SpinButton] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param model for the select box
 * @param itemVisualizer used to render the spin-button's box item
 * @param config used to control the resulting component
 */
public fun <T, M: SpinButtonModel<T>> spinButton(
    model         : M,
    itemVisualizer: ItemVisualizer<T, SpinButton<T, M>> = toString(StringVisualizer()),
    config        : (SpinButton<T, M>) -> Unit = {}
): FieldVisualizer<T> = field {
    SpinButton(model, itemVisualizer = itemVisualizer).also { spinButton ->
        spinButton.changed += {
            state = spinButton.value.fold(
                onSuccess = { Valid(it) },
                onFailure = { Invalid() }
            )
        }

        state = spinButton.value.fold(
            onSuccess = { Valid(it) },
            onFailure = { Invalid() }
        )

        spinButton.focusChanged += { _,_,focused ->
            if (focused) {
                spinButton.parent?.scrollTo(spinButton.bounds)
            }
        }
    }.also(config)
}

/**
 * Creates a [SpinButton] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param itemVisualizer used to render the spin-button's box item
 * @param config used to control the resulting component
 */
public fun <T> spinButton(
           first         : T,
    vararg rest          : T,
           itemVisualizer: ItemVisualizer<T, SpinButton<T, *>>,
           config        : (SpinButton<T, *>) -> Unit = {}
): FieldVisualizer<T> = spinButton(
    ListSpinButtonModel(listOf(first) + rest),
    itemVisualizer,
    config
)

/**
 * Creates a [SpinButton] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param values in the spin-button
 * @param itemVisualizer used to render the spin-button's box item
 * @param config used to control the resulting component
 */
public fun <T> spinButton(
    values         : List<T>,
    itemVisualizer: ItemVisualizer<T, SpinButton<T, *>>,
    config        : (SpinButton<T, *>) -> Unit = {}
): FieldVisualizer<T> = spinButton(
    ListSpinButtonModel(values),
    itemVisualizer,
    config
)

/**
 * Creates a [SpinButton] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param label used to render the spin-button's box and list items
 * @param config used to control the resulting component
 */
public fun <T> spinButton(
           first : T,
    vararg rest  : T,
           label : (T) -> String = { "$it" },
           config: (SpinButton<T, *>) -> Unit = {}
): FieldVisualizer<T> = spinButton(first, *rest, itemVisualizer = toString(StringVisualizer(), label), config = config)


/**
 * Creates a [SpinButton] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param T is the type of the bounded field
 * @param values in the spin-button
 * @param label used to render the spin-button's box and list items
 * @param config used to control the resulting component
 */
public fun <T> spinButton(
    values: List<T>,
    label : (T) -> String = { "$it" },
    config: (SpinButton<T, *>) -> Unit = {}
): FieldVisualizer<T> = spinButton(values, toString(StringVisualizer(), label), config)

/**
 * Creates a [SpinButton] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param progression to use for the underlying model
 * @param itemVisualizer used to render the spin-button's box item
 * @param config used to control the resulting component
 */
public fun spinButton(
    progression   : IntProgression,
    itemVisualizer: ItemVisualizer<Int, SpinButton<Int, *>>,
    config        : (SpinButton<Int, *>) -> Unit = {}
): FieldVisualizer<Int> = spinButton(IntSpinButtonModel(progression), itemVisualizer, config)

/**
 * Creates a [SpinButton] control that is bound to a [Field]. This control lets a user
 * select a single item within a list. It is similar to [radioList], except it
 * DOES set a default value and its field is therefore ALWAYS [Valid].
 *
 * This control is useful when a meaningful default exists for an option list.
 *
 * @param progression to use for the underlying model
 * @param label used to render the spin-button's box and list items
 * @param config used to control the resulting component
 */
public fun spinButton(
    progression   : IntProgression,
    label         : (Int) -> String = { "$it" },
    config        : (SpinButton<Int, *>) -> Unit = {}
): FieldVisualizer<Int> = spinButton(progression, toString(StringVisualizer(), label), config)

// endregion

// region List

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list. This control lets a user ignore selection entirely,
 * which would result in a `null` value. This behaves like [optionalRadioList].
 *
 * @param T is the type of the items in the bounded field
 * @param model for the list
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
public fun <T, M: ListModel<T>> optionalSingleChoiceList(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    config        : (io.nacular.doodle.controls.list.List<T, M>) -> Unit = {}): FieldVisualizer<T?> = field {
    io.nacular.doodle.controls.list.List(
        model,
        itemVisualizer,
        selectionModel = SingleItemSelectionModel(),
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
 * which would result in a `null` value. This behaves like [optionalRadioList].
 *
 * @param T is the type of the items in the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
public fun <T> optionalSingleChoiceList(
    first: T,
    vararg rest : T,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<T?> = optionalSingleChoiceList(
    SimpleListModel(listOf(first) + rest),
    itemVisualizer,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list. This control lets a user ignore selection entirely,
 * which would result in a `null` value. This behaves like [optionalRadioList].
 *
 * @param progression to use for values
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
public fun optionalSingleChoiceList(
    progression   : IntProgression,
    itemVisualizer: ItemVisualizer<Int, IndexedItem> = toString(StringVisualizer()),
    config        : (io.nacular.doodle.controls.list.List<Int, *>) -> Unit = {}): FieldVisualizer<Int?> = optionalSingleChoiceList(
    IntProgressionModel(progression),
    itemVisualizer,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list. This behaves like [radioList].
 *
 * @param T is the type of the items in the bounded field
 * @param first item in the list
 * @param rest of the items in the list
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
public fun <T, M: ListModel<T>> singleChoiceList(
    first: T,
    vararg rest : T,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<T> = singleChoiceList(
    SimpleListModel(listOf(first) + rest),
    itemVisualizer,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select a single option from a list.
 *
 * @param progression to use for values
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
public fun singleChoiceList(
    progression: IntProgression,
    itemVisualizer: ItemVisualizer<Int, IndexedItem> = toString(StringVisualizer()),
    config        : (io.nacular.doodle.controls.list.List<Int, *>) -> Unit = {}): FieldVisualizer<Int> = singleChoiceList(
    IntProgressionModel(progression),
    itemVisualizer,
    config
)
private fun <T, M: ListModel<T>> singleChoiceList(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
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
public fun <T> form(builder: FormControlBuildContext<T>.() -> FieldVisualizer<T>): FieldVisualizer<T> = field {
    builder(FormControlBuildContext(field, initial))(this)
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
): FieldVisualizer<List<T>> = buildToggleList(first, rest = rest, config) {
    CheckBox().apply {
        suggestWidth(16.0)
        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
        }
    }
}

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
@Suppress("LocalVariableName")
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
    Switch().apply {
        focusChanged += { _,_,focused ->
            if (focused) {
                parent?.scrollTo(bounds)
            }
        }
    }
}

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
public fun <T, M: ListModel<T>> list(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
    config        : (io.nacular.doodle.controls.list.List<T, M>) -> Unit = {}): FieldVisualizer<List<T>> = field {
    io.nacular.doodle.controls.list.List(
        model,
        itemVisualizer,
        selectionModel = MultiSelectionModel(),
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
 * @param config used to control the resulting component
 */
public fun <T> list(
           first         : T,
    vararg rest          : T,
           itemVisualizer: ItemVisualizer<T, IndexedItem> = toString(StringVisualizer()),
           config        : (io.nacular.doodle.controls.list.List<T, *>) -> Unit = {}): FieldVisualizer<List<T>> = list(
    SimpleListModel(listOf(first) + rest),
    itemVisualizer,
    config
)

/**
 * Creates a [List][io.nacular.doodle.controls.list.List] control that is bound to a [Field]. This controls
 * lets a user select multiple options from a list. This control lets a user ignore selection entirely,
 * which would result in an empty list. It is similar to a [checkList].
 *
 * @param progression to use for values
 * @param itemVisualizer used to render items in the list
 * @param config used to control the resulting component
 */
public fun list(
    progression   : IntProgression,
    itemVisualizer: ItemVisualizer<Int, IndexedItem> = toString(StringVisualizer()),
    config        : (io.nacular.doodle.controls.list.List<Int, *>) -> Unit = {}): FieldVisualizer<List<Int>> = list(
    IntProgressionModel(progression),
    itemVisualizer,
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
    public fun defaultLayout(spacing: Double = DEFAULT_SPACING, itemHeight: Double? = null): (container: View, field: View) -> Layout? = { _,_ ->
        label.fitText = setOf(Width)

        expandingVerticalLayout(spacing, itemHeight)
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
 * Only appends the indicator after a field is initially (or becomes) [invalid][Invalid]
 * and it loses focus.
 *
 * @see labeled
 * @param text to append to field name
 * @param focusTarget to monitor for focus events, or `null` if the [labeled] content is the right target.
 */
public class WhenInvalidFocusLost(text: StyledText, internal val focusTarget: View? = null): RequiredIndicatorStyle(text) {
    /**
     * @param text to append to field name
     */
    public constructor(text: String = "*"): this(StyledText(text))
}

/**
 * Only appends the indicator if explicitly requested.
 * and it loses focus.
 *
 * @see labeled
 * @param text to append to field name
 */
public class WhenManuallySet(text: StyledText): RequiredIndicatorStyle(text) {

    internal val indicatorVisibilityChanged: ChangeObserversImpl<WhenManuallySet> by lazy { ChangeObserversImpl(this) }

    /**
     * @param text to append to field name
     */
    public constructor(text: String = "*"): this(StyledText(text))

    /**
     * Used to explicitly set the required indicator's visibility.
     *
     * Defaults to `false`.
     */
    public var indicatorVisible: Boolean by observable(false) { _,_ -> indicatorVisibilityChanged() }
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
        lateinit var content: View

        val label         = NonInteractiveLabel(name)
        val builder       = NamedConfig(label)
        val visualization = visualizer(builder)

        insets    = builder.insets
        render    = { builder.render(this, this@container) }
        focusable = false

        + listOf(label, visualization(this@field).also { it.accessibilityLabelProvider = label; content = it })

        showRequired?.let { updateRequiredText(it, label, name, content) }

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

        expandingVerticalLayout(DEFAULT_SPACING)
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
        lateinit var content: View

        val nameLabel     = NonInteractiveLabel(name)
        val helperLabel   = NonInteractiveLabel(help)
        val builder       = LabeledConfig(nameLabel, helperLabel)
        val visualization = visualizer(builder)

        insets    = builder.insets
        render    = { builder.render(this, this@container) }
        focusable = false

        + listOf(nameLabel, visualization(this@field).also { it.accessibilityLabelProvider = nameLabel; content = it }, helperLabel)

        showRequired?.let { updateRequiredText(it, nameLabel, name, content) }

        this.layout = builder.layout(this, children[1])
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
        + visualizer(this)(this@field)

        layout    = verticalLayout()
        focusable = false
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
        expandingVerticalLayout(DEFAULT_FORM_SPACING)
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
        onReady  : (A) -> T): FieldVisualizer<T> = invokeInternal(a, layout = null, onInvalid, onReady)

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A> invoke(
        a        : Field<A>,
        layout   : ConstraintDslContext.(Bounds) -> Unit,
        onInvalid: ( ) -> Unit = {},
        onReady  : (A) -> T): FieldVisualizer<T> = invokeInternal(a, layout, onInvalid, onReady)

    private fun <T, A> invokeInternal(
        a        : Field<A>,
        layout   : (ConstraintDslContext.(Bounds) -> Unit)?,
        onInvalid: ( ) -> Unit = {},
        onReady  : (A) -> T): FieldVisualizer<T> = field {
        Form {
            invokeInternal(a, layout, onInvalid = { field.state = Invalid(); onInvalid() }) { a ->
                state = Valid(onReady(a))
            }
        }.apply { layout?.let { this@FormControlBuildContext.layout = { it.layout } }; configure(this) }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B> invoke(
        a        : Field<A>,
        b        : Field<B>,
        onInvalid: (    ) -> Unit = {},
        onReady  : (A, B) -> T): FieldVisualizer<T> = invokeInternal(a, b, layout = null, onInvalid, onReady)

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B> invoke(
        a        : Field<A>,
        b        : Field<B>,
        layout   : ConstraintDslContext.(Bounds, Bounds) -> Unit,
        onInvalid: (    ) -> Unit = {},
        onReady  : (A, B) -> T): FieldVisualizer<T> = invokeInternal(a, b, layout, onInvalid, onReady)

    private fun <T, A, B> invokeInternal(
        a        : Field<A>,
        b        : Field<B>,
        layout   : (ConstraintDslContext.(Bounds, Bounds) -> Unit)?,
        onInvalid: (    ) -> Unit = {},
        onReady  : (A, B) -> T): FieldVisualizer<T> = field {
        Form {
            invokeInternal(a, b, layout, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b ->
                state = Valid(onReady(a, b))
            }
        }.apply { layout?.let { this@FormControlBuildContext.layout = { it.layout } }; configure(this) }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C> invoke(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        onInvalid: (       ) -> Unit = {},
        onReady  : (A, B, C) -> T): FieldVisualizer<T> = invokeInternal(a, b, c, layout = null, onInvalid, onReady)

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C> invoke(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        layout   : ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit,
        onInvalid: (       ) -> Unit = {},
        onReady  : (A, B, C) -> T): FieldVisualizer<T> = invokeInternal(a, b, c, layout, onInvalid, onReady)

    public fun <T, A, B, C> invokeInternal(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        layout   : (ConstraintDslContext.(Bounds, Bounds, Bounds) -> Unit)?,
        onInvalid: (       ) -> Unit = {},
        onReady  : (A, B, C) -> T): FieldVisualizer<T> = field {
        Form {
            invokeInternal(a, b, c, layout, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b, c ->
                state = Valid(onReady(a, b, c))
            }
        }.apply { layout?.let { this@FormControlBuildContext.layout = { it.layout } }; configure(this) }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C, D> invoke(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        d        : Field<D>,
        onInvalid: (          ) -> Unit = {},
        onReady  : (A, B, C, D) -> T): FieldVisualizer<T> = invokeInternal(a, b, c, d, layout = null, onInvalid, onReady)

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C, D> invoke(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        d        : Field<D>,
        layout   : ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit,
        onInvalid: (          ) -> Unit = {},
        onReady  : (A, B, C, D) -> T): FieldVisualizer<T> = invokeInternal(a, b, c, d, layout, onInvalid, onReady)

    private fun <T, A, B, C, D> invokeInternal(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        d        : Field<D>,
        layout   : (ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds) -> Unit)?,
        onInvalid: (          ) -> Unit = {},
        onReady  : (A, B, C, D) -> T): FieldVisualizer<T> = field {
        Form {
            invokeInternal(a, b, c, d, layout, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b, c, d ->
                state = Valid(onReady(a, b, c, d))
            }
        }.apply { layout?.let { this@FormControlBuildContext.layout = { it.layout } }; configure(this) }
    }

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C, D, E> invoke(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        d        : Field<D>,
        e        : Field<E>,
        onInvalid: (             ) -> Unit = {},
        onReady  : (A, B, C, D, E) -> T): FieldVisualizer<T> = invokeInternal(a, b, c, d, e, layout = null, onInvalid, onReady)

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T, A, B, C, D, E> invoke(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        d        : Field<D>,
        e        : Field<E>,
        layout   : ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit,
        onInvalid: (             ) -> Unit = {},
        onReady  : (A, B, C, D, E) -> T): FieldVisualizer<T> = invokeInternal(a, b, c, d, e, layout, onInvalid, onReady)

    private fun <T, A, B, C, D, E> invokeInternal(
        a        : Field<A>,
        b        : Field<B>,
        c        : Field<C>,
        d        : Field<D>,
        e        : Field<E>,
        layout   : (ConstraintDslContext.(Bounds, Bounds, Bounds, Bounds, Bounds) -> Unit)?,
        onInvalid: (             ) -> Unit = {},
        onReady  : (A, B, C, D, E) -> T): FieldVisualizer<T> = field {
        Form {
            invokeInternal(a, b, c, d, e, layout, onInvalid = { field.state = Invalid(); onInvalid() }) { a, b, c, d, e ->
                state = Valid(onReady(a, b, c, d, e))
            }
        }.apply { layout?.let { this@FormControlBuildContext.layout = { it.layout } }; configure(this) }
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

    /** @see Form.Companion.FormBuildContext.invoke */
    public operator fun <T> invoke(
               first    : Field<*>,
               second   : Field<*>,
        vararg rest     : Field<*>,
               layout   : ConstraintDslContext.(List<Bounds>) -> Unit,
               onInvalid: (       ) -> Unit = {},
               onReady  : (List<*>) -> T): FieldVisualizer<T> = invokeInternal(
        first     = first,
        second    = second,
        rest      = rest,
        layout    = layout,
        onInvalid = onInvalid,
        onReady   = onReady
    )

    private fun <T> invokeInternal(
               first    : Field<*>,
               second   : Field<*>,
        vararg rest     : Field<*>,
               layout   : (ConstraintDslContext.(List<Bounds>) -> Unit)?,
               onInvalid: (       ) -> Unit = {},
               onReady  : (List<*>) -> T): FieldVisualizer<T> = field {
        Form {
            invokeInternal(first, second, *rest, layout = layout, onInvalid = { field.state = Invalid(); onInvalid() }) { fields ->
                state = Valid(onReady(fields))
            }
        }.apply { layout?.let { this@FormControlBuildContext.layout = { it.layout } }; configure(this) }
    }

    private fun configure(form: Form) {
        form.layout    = layout(form)
        form.insets    = insets
        form.behavior  = behavior
        form.focusable = false
    }
}

public fun verticalLayout(spacing: Double = 2.0, itemHeight: Double? = null): Layout = expandingVerticalLayout(spacing, itemHeight)

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

        + items.map { value ->
            container {
                focusable = false
                val visualizedValue = builder.visualizer(value)

                + visualizedValue
                + toggleBuilder().apply {
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

                this.layout        = (layout(children[1] as ToggleButton, children[0]) ?: buttonItemLayout(button = children[1], label = children[0]))
                this.preferredSize = { _,max -> Size(max.width, children.maxOf { it.idealSize.height }) }
            }
        }

        this.layout = expandingVerticalLayout(builder.spacing, builder.itemHeight)
    }
}

private fun <T> buildRadioList(
           first           : T,
    vararg rest            : T,
           optionListConfig: OptionListConfig<T>,
           initialValue    : T? = null,
           allowDeselectAll: Boolean = false,
           config          : (T, RadioButton) -> Unit
) = container {
    val group = ButtonGroup(allowDeselectAll = allowDeselectAll)

    + (listOf(first) + rest).map { value ->
        container {
            focusable = false
            val visualizedValue = optionListConfig.visualizer(value)

            + visualizedValue
            + RadioButton().apply {
                group += this

                initialValue?.let {
                    selected = value == it
                }

                config(value, this)

                suggestWidth(16.0)
                accessibilityLabelProvider = visualizedValue
            }

            this.layout        = buttonItemLayout(button = children[1], label = children[0])
            this.preferredSize = { _,max -> Size(max.width, children.maxOf { it.idealSize.height }) }
        }
    }

    insets    = optionListConfig.insets
    render    = { optionListConfig.render(this, this@container) }
    focusable = false
    layout    = expandingVerticalLayout(optionListConfig.spacing, optionListConfig.itemHeight)
}

private fun <T: Any, M: ListModel<T?>> buildSelectBox(
    model                       : M,
    boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
    listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
    unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
    unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
    initialValue                : T? = null
): SelectBox<T?, M> = SelectBox(
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

private class NonInteractiveLabel(text: StyledText): Label(text) {
    init {
        focusable     = false
        textAlignment = Start
    }

    override fun contains(point: Point) = false
}

private fun FieldInfo<Boolean>.checkBox(label: String? = null) = CheckBox(label ?: "").apply {
    initial.ifValid { selected = it }

    selectedChanged += { _,_,_ ->
        state = Valid(selected)
    }

    focusChanged += { _,_,focused ->
        if (focused) {
            parent?.scrollTo(bounds)
        }
    }

    state = Valid(selected)
}

private fun expandingVerticalLayout(spacing: Double, itemHeight: Double? = null) = ListLayout(
    spacing     = spacing,
    widthSource = WidthSource.Parent,
    minHeight   = itemHeight ?: 0.0,
    maxHeight   = itemHeight ?: POSITIVE_INFINITY
)

@Suppress("LocalVariableName")
private fun buttonItemLayout(button: View, label: View, labelOffset: Double = 26.0) = constrain(button, label) { button_, label_ ->
    button_.top    eq 0
    button_.width  eq parent.width
    button_.height eq parent.height
    label_.left    eq labelOffset
    label_.centerY eq button_.centerY
}

private fun <T> FieldInfo<T>.updateRequiredText(
    showRequired: RequiredIndicatorStyle,
    label       : Label,
    name        : StyledText,
    content     : View
) {
    if ((showRequired is Always || (showRequired is WhenInvalid && state is Invalid<T>)) && showRequired.text.isNotBlank()) {
        label.styledText = name.copy() + showRequired.text
    }

    when (showRequired) {
        is WhenInvalidFocusLost -> (showRequired.focusTarget ?: content).focusChanged += { _,_,focused ->
            if (!focused && state is Invalid<T> && showRequired.text.isNotBlank()) {
                label.styledText = name.copy() + showRequired.text
            }
        }
        is WhenManuallySet -> showRequired.indicatorVisibilityChanged += {
            if (showRequired.indicatorVisible) {
                label.styledText = name.copy() + showRequired.text
            }
        }
        else              -> {}
    }

    stateChanged += {
        when {
            it.state is Invalid<T> &&
                    showRequired.text.isNotBlank() &&
                    (showRequired !is WhenInvalidFocusLost || !content.hasFocus) -> label.styledText = name.copy() + showRequired.text
            showRequired !is Always && showRequired !is WhenManuallySet          -> label.styledText = name.copy()
        }
    }
}

private const val DEFAULT_HEIGHT       = 32.0
private const val DEFAULT_SPACING      =  2.0
private const val DEFAULT_FORM_SPACING = 12.0

// endregion