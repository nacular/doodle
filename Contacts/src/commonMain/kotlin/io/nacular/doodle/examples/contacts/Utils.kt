package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.form.FieldVisualizer
import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.Form.Invalid
import io.nacular.doodle.controls.form.Form.Valid
import io.nacular.doodle.controls.form.field
import io.nacular.doodle.controls.form.textField
import io.nacular.doodle.controls.form.verticalLayout
import io.nacular.doodle.controls.icons.PathIcon
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.path
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.theme.native.NativeTextFieldBehaviorModifier
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlin.math.floor

/**
 * Simple string to color mapper from: https://github.com/zenozeng/color-hash/blob/main/lib/bkdr-hash.ts
 */
fun String.toColor(): Color {
    val seed           = 131
    val seed2          = 237
    var hash           = 0
    val str            = this + 'x' // make hash more sensitive for short string like 'a', 'b', 'c'
    val maxSafeInteger = floor((0xffffffu / seed2.toUInt()).toDouble())

    str.forEach {
        if (hash > maxSafeInteger) {
            hash = floor((hash / seed2).toDouble()).toInt()
        }
        hash = hash * seed + it.code
    }

    return Color(hash.toUInt())
}

fun editForm(
    name           : String? = null,
    phoneNumber    : String? = null,
    assets         : AppConfig,
    textFieldStyler: NativeTextFieldStyler,
    pathMetrics    : PathMetrics,
    button         : Button,
    nameChanged    : (String) -> Unit = {},
    result         : (newName: String, newNumber: String) -> Unit,
) = Form {
    this(
        (name?.let        { Valid(it) } ?: Invalid()) to formTextField(assets, textFieldStyler, pathMetrics, "Name",         assets.nameIcon,  Regex(".+")        ) { textChanged += { _,_,new -> nameChanged(new) } },
        (phoneNumber?.let { Valid(it) } ?: Invalid()) to formTextField(assets, textFieldStyler, pathMetrics, "Phone Number", assets.phoneIcon, Regex("[\\s,0-9]+")),
        onInvalid = { button.enabled = false }
    ) { name, phone ->
        result(name, phone)
    }
}.apply {
    font   = assets.small
    layout = verticalLayout(this, spacing = 32.0, itemHeight = 33.0)
}

/**
 * Field used in Contact create/edit forms.
 */
fun formTextField(
    assets          : AppConfig,
    textFieldStyler : NativeTextFieldStyler,
    pathMetrics     : PathMetrics,
    placeHolder     : String,
    icon            : String,
    regex           : Regex,
    config          : TextField.() -> Unit = {},
) = iconField(pathMetrics, icon) {

    textField(regex) {
        textField.placeHolder      = placeHolder
        textField.placeHolderColor = assets.placeHolder
        textField.acceptsThemes    = false
        textField.behavior         = textFieldStyler(textField, object: NativeTextFieldBehaviorModifier {
            init {
                textField.acceptsThemes   = false
                textField.borderVisible   = false
                textField.backgroundColor = Transparent
            }

            override fun renderBackground(textField: TextField, canvas: Canvas) {
                canvas.line(start = Point(0.0, textField.height - 1.0), end = Point(textField.width, textField.height - 1.0), Stroke(thickness = 1.0, fill = assets.outline.paint))
            }
        })

        config(textField)
    }
}

/**
 * A form field with an icon to its left.
 */
private fun <T> iconField(pathMetrics: PathMetrics, path: String, visualizer: () -> FieldVisualizer<T>) = field<T> {
    container {
        val icon        = PathIcon<View>(path(path)!!, pathMetrics = pathMetrics)
        val iconSize    = icon.size(this)
        focusable       = false
        foregroundColor = Black

        this += visualizer()(this@field).also {
            it.sizePreferencesChanged += { _, _, _ ->
                relayout()
            }
        }

        render = {
            icon.render(this@container, this, at = Point(0.0, (height - iconSize.height) / 2))
        }

        layout = constrain(children[0]) {
            it.edges eq parent.edges + Insets(left = iconSize.width + 24)
//            fill(Insets(left = iconSize.width + 24))(it)
        }
    }
}

/** Inset used for various UI elements */
const val INSET = 16.0