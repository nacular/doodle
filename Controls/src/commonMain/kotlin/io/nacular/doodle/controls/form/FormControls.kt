package io.nacular.doodle.controls.form

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
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

public interface TextFieldConfig<T> {
    public val textField: TextField
    public var onInvalid: (Throwable) -> Unit
    public var onValid  : (T        ) -> Unit
}

public fun <T> textField(
        pattern: Regex = Regex(".*"),
        encoder: Encoder<T, String>,
        config : TextFieldConfig<T>.() -> Unit = {}): FieldVisualizer<T> = object: FieldVisualizer<T> {
    private lateinit var configObject: TextFieldConfig<T>

    override operator fun invoke(field: Field<T>) = TextField().apply {
        textChanged  += { _,_,new      -> validate(field, new) }
        focusChanged += { _,_,hasFocus ->
            if (!hasFocus) {
                validate(field, text)
            }
        }

        configObject = object: TextFieldConfig<T> {
            override val textField = this@apply
            override var onInvalid: (Throwable) -> Unit = {}
            override var onValid  : (T        ) -> Unit = {}
        }

        config(configObject)

        when (val value = field.value) {
            is Valid -> encoder.to(value.value).getOrNull()?.let { text = it }
        }
    }

    private fun validate(field: Field<T>, value: String) {
        when {
            pattern.matches(value) -> {
                encoder.from(value).onSuccess { decoded ->
                    field.value = Valid(decoded)
                    configObject.onValid(decoded)
                }.onFailure {
                    field.value = Invalid()
                    configObject.onInvalid(it)
                }
            }
            else                   -> {
                field.value = Invalid()
                configObject.onInvalid(IllegalArgumentException("Invalid input: $value"))
            }
        }
    }
}

public inline fun textField(pattern: Regex = Regex(".*"), noinline config: TextFieldConfig<String>.() -> Unit = {}): FieldVisualizer<String> = textField(pattern, PassThroughEncoder(), config)

public class RadioListConfig<T> {
    public var spacing   : Double  = 0.0
    public var itemHeight: Double? = null
    public var label     : (T) -> String = { it.toString() }
}

public fun <T> radioList(
               first : T,
        vararg rest  : T,
               config: RadioListConfig<T>.() -> Unit): FieldVisualizer<T> = object: FieldVisualizer<T> {
    override fun invoke(field: Field<T>): Container {
        val builder = RadioListConfig<T>().also(config)

        return buildRadioList(
                first        = first,
                rest         = rest,
                spacing      = builder.spacing,
                itemHeight   = builder.itemHeight,
                namer        = builder.label,
                initialValue = (field.value as? Valid<T>)?.value) { value, button ->
            button.selectedChanged += { _,_,selected ->
                if (selected) {
                    field.value = Valid(value)
                }
            }
        }
    }
}

public fun <T: Any> optionalRadioList(
               first     : T,
        vararg rest      : T,
               spacing   : Double  = 0.0,
               itemHeight: Double? = null,
               label     : (T) -> String = { it.toString() }): FieldVisualizer<T?> = object: FieldVisualizer<T?> {
    override fun invoke(field: Field<T?>) = buildRadioList(
            first        = first,
            rest         = rest,
            spacing      = spacing,
            itemHeight   = itemHeight,
            namer        = label,
            initialValue = (field.value as? Valid<T?>)?.value) { value, button ->
        button.selectedChanged += { _,_,selected ->
            if (selected) {
                field.value = Valid(value)
            }
        }
    }.also {
        field.value = Valid(null)
    }
}

public fun <T> dropDown(
               first             : T,
        vararg rest              : T,
               boxItemVisualizer : ItemVisualizer<T, IndexedItem>,
               listItemVisualizer: ItemVisualizer<T, IndexedItem> = boxItemVisualizer): FieldVisualizer<T> = object: FieldVisualizer<T> {
    override fun invoke(field: Field<T>): Dropdown<T, *> {
        val model = SimpleListModel(listOf(first) + rest)
        return Dropdown(
                model,
                boxItemVisualizer  = boxItemVisualizer,
                listItemVisualizer = listItemVisualizer).also { dropdown ->

            when (field.value) {
                is Valid<T> -> model.forEachIndexed { index, item ->
                    if (item == field.value) {
                        dropdown.selection = index
                        return@forEachIndexed
                    }
                }
                else    -> field.value = Valid(dropdown.value)
            }

            dropdown.changed += {
                field.value = Valid(dropdown.value)
            }
        }
    }
}

public fun <T> dropDown(
               first: T,
        vararg rest : T,
               label: (T) -> String = { it.toString() }
): FieldVisualizer<T> = dropDown(first, *rest, boxItemVisualizer = toString(TextVisualizer(), label))

public fun <T: Any> dropDown(
        first                       : T,
        vararg rest                 : T,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
        config                      : (Dropdown<T?, *>) -> Unit = {}): FieldVisualizer<T> = object: FieldVisualizer<T> {
    override fun invoke(field: Field<T>): Dropdown<T?, *> = buildDropDown(
            first                        = first,
            rest                         = rest,
            boxItemVisualizer            = boxItemVisualizer,
            listItemVisualizer           = listItemVisualizer,
            unselectedBoxItemVisualizer  = unselectedBoxItemVisualizer,
            unselectedListItemVisualizer = unselectedListItemVisualizer).apply {
        changed += {
            when (val v = value) {
                null -> field.value = Invalid( )
                else -> field.value = Valid  (v)
            }
        }

        config(this)
    }
}

public fun <T: Any> dropDown(
        first         : T,
        vararg rest   : T,
        label         : (T) -> String = { it.toString() },
        unselectedName: String
): FieldVisualizer<T> = dropDown(first, *rest, boxItemVisualizer = toString(TextVisualizer(), label), unselectedBoxItemVisualizer = toString(TextVisualizer()) { unselectedName })

public fun <T: Any> optionalDropDown(
        first                       : T,
        vararg rest                 : T,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer): FieldVisualizer<T?> = object: FieldVisualizer<T?> {
    override fun invoke(field: Field<T?>) = buildDropDown(
            first                        = first,
            rest                         = rest,
            boxItemVisualizer            = boxItemVisualizer,
            listItemVisualizer           = listItemVisualizer,
            unselectedBoxItemVisualizer  = unselectedBoxItemVisualizer,
            unselectedListItemVisualizer = unselectedListItemVisualizer,
            initialValue                 = (field.value as? Valid<T?>)?.value).apply {
        changed += {
            field.value = Valid(value)
        }

        field.value = Valid(null)
    }
}


public fun <T: Any> optionalDropDown(
        first         : T,
        vararg rest   : T,
        label         : (T) -> String = { it.toString() },
        unselectedName: String
): FieldVisualizer<T?> = optionalDropDown(first, *rest, boxItemVisualizer = toString(TextVisualizer(), label), unselectedBoxItemVisualizer = toString(TextVisualizer()) { unselectedName })


public fun <T> checkList(
               first     : T,
        vararg rest      : T,
               spacing   : Double  = 0.0,
               itemHeight: Double? = null,
               label     : (T) -> String = { it.toString() }): FieldVisualizer<Set<T>> = object: FieldVisualizer<Set<T>> {
    override fun invoke(field: Field<Set<T>>): View {
        val selection    = mutableSetOf<T>()
        val initialValue = (field.value as? Valid)?.value

        field.value = Valid(emptySet())

        return container {
            focusable  = false
            children  += (listOf(first) + rest).map { value ->
                CheckBox(label(value)).apply {
                    initialValue?.let {
                        selected = value in it
                    }

                    selectedChanged        += { _,_,selected ->
                        when {
                            selected -> selection += value
                            else     -> selection -= value
                        }

                        field.value = Valid(selection)
                    }
                    sizePreferencesChanged += { _,_,_ ->
                        relayout()
                    }
                }
            }
            layout = ExpandingListLayout(this, spacing, itemHeight)
        }
    }
}

public class NamedVisualizerConfig {
    public var layout: (container: View, label: Label, field: View) -> Layout? = { container, label,_ ->
        label.fitText = setOf(Width)

        ExpandingListLayout(container, 2.0, 32.0)
    }
}

public fun <T> named(
        name  : StyledText,
        config: NamedVisualizerConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = object: FieldVisualizer<T> {
    override fun invoke(field: Field<T>) = container {
        val builder = NamedVisualizerConfig()
        val visualization = config(builder)

        focusable = false

        val label = UninteractiveLabel(name)

        children += listOf(label, visualization(field)).onEach {
            it.sizePreferencesChanged += { _, _, _ ->
                relayout()
            }
        }

        layout = builder.layout(this, label, children[1])
    }
}

public fun <T> named(
        name  : String,
        config: NamedVisualizerConfig.() -> FieldVisualizer<T>): FieldVisualizer<T> = named(StyledText(name), config)

public class FormControlBuildContext<T> internal constructor(public val field: Field<T>) {
    public infix fun <T> T.to(visualizer: FieldVisualizer<T>): Field<T> = Form.field(initial = this, visualizer)
    public infix fun <T> FieldState<T>.to(visualizer: FieldVisualizer<T>): Field<T> = Field(visualizer, initial = this)
    public operator fun <T> FieldVisualizer<T>.unaryPlus(): Field<T> = Field(this, initial = Invalid())

    public var layout: (form: Form) -> Layout? = {
        ExpandingListLayout(it, 12.0, 32.0)
    }

    public operator fun <T, A> invoke(
            a        : Field<A>,
            onInvalid: ( ) -> Unit = {},
            onReady  : (A) -> T): FieldVisualizer<T> = object: FieldVisualizer<T> {
        override fun invoke(field: Field<T>) = Form {
            this(a, onInvalid = { field.value = Invalid(); onInvalid() }) { a ->
                field.value = Valid(onReady(a))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    public operator fun <T, A, B> invoke(
            a        : Field<A>,
            b        : Field<B>,
            onInvalid: (    ) -> Unit = {},
            onReady  : (A, B) -> T): FieldVisualizer<T> = object: FieldVisualizer<T> {
        override fun invoke(field: Field<T>) = Form {
            this(a, b, onInvalid = { field.value = Invalid(); onInvalid() }) { a, b ->
                field.value = Valid(onReady(a, b))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    public operator fun <T, A, B, C> invoke(
            a        : Field<A>,
            b        : Field<B>,
            c        : Field<C>,
            onInvalid: (       ) -> Unit = {},
            onReady  : (A, B, C) -> T): FieldVisualizer<T> = object: FieldVisualizer<T> {
        override fun invoke(field: Field<T>) = Form {
            this(a, b, c, onInvalid = { field.value = Invalid(); onInvalid() }) { a, b, c ->
                field.value = Valid(onReady(a, b, c))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    public operator fun <T, A, B, C, D> invoke(
            a        : Field<A>,
            b        : Field<B>,
            c        : Field<C>,
            d        : Field<D>,
            onInvalid: (          ) -> Unit = {},
            onReady  : (A, B, C, D) -> T): FieldVisualizer<T> = object: FieldVisualizer<T> {
        override fun invoke(field: Field<T>) = Form {
            this(a, b, c, d, onInvalid = { field.value = Invalid(); onInvalid() }) { a, b, c, d ->
                field.value = Valid(onReady(a, b, c, d))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    public operator fun <T, A, B, C, D, E> invoke(
            a        : Field<A>,
            b        : Field<B>,
            c        : Field<C>,
            d        : Field<D>,
            e        : Field<E>,
            onInvalid: (             ) -> Unit = {},
            onReady  : (A, B, C, D, E) -> T): FieldVisualizer<T> = object: FieldVisualizer<T> {
        override fun invoke(field: Field<T>) = Form {
            this(a, b, c, d, e, onInvalid = { field.value = Invalid(); onInvalid() }) { a, b, c, d, e ->
                field.value = Valid(onReady(a, b, c, d, e))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }

    public operator fun <T> invoke(
                   first    : Field<*>,
                   second   : Field<*>,
            vararg rest     : Field<*>,
                   onInvalid: (       ) -> Unit = {},
                   onReady  : (List<*>) -> T): FieldVisualizer<T> = object: FieldVisualizer<T> {
        override fun invoke(field: Field<T>) = Form {
            this(first, second, *rest, onInvalid = { field.value = Invalid(); onInvalid() }) { fields ->
                field.value = Valid(onReady(fields))
            }
        }.apply {
            focusable = false
            layout    = layout(this)
        }
    }
}

public fun <T> form(builder: FormControlBuildContext<T>.() -> FieldVisualizer<T>): FieldVisualizer<T> {
    return field {
        builder(FormControlBuildContext(this))(this)
    }
}

private fun <T> buildRadioList(
               first     : T,
        vararg rest      : T,
               spacing   : Double  = 0.0,
               itemHeight: Double? = null,
               namer     : (T) -> String = { it.toString() },
               initialValue: T? = null,
               config    : (T, RadioButton) -> Unit): Container = container {
    val group  = ButtonGroup()
    children  += (listOf(first) + rest).map { value ->
        RadioButton(namer(value)).apply {
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

private fun <T: Any> buildDropDown(
        first                       : T,
        vararg rest                 : T,
        boxItemVisualizer           : ItemVisualizer<T,    IndexedItem>,
        listItemVisualizer          : ItemVisualizer<T,    IndexedItem> = boxItemVisualizer,
        unselectedBoxItemVisualizer : ItemVisualizer<Unit, IndexedItem>,
        unselectedListItemVisualizer: ItemVisualizer<Unit, IndexedItem> = unselectedBoxItemVisualizer,
        initialValue                : T? = null,): Dropdown<T?, *> {
    val model = SimpleListModel(listOf(null, first) + rest)

    return Dropdown(
            model,
            boxItemVisualizer = object: ItemVisualizer<T?, IndexedItem> {
                override fun invoke(item: T?, previous: View?, context: IndexedItem): View = when (item) {
                    null -> unselectedBoxItemVisualizer(Unit, previous, context)
                    else -> boxItemVisualizer          (item, previous, context)
                }
            },
            listItemVisualizer = object: ItemVisualizer<T?, IndexedItem> {
                override fun invoke(item: T?, previous: View?, context: IndexedItem): View = when (item) {
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
}


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