package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.PushButton
import com.nectar.doodle.controls.spinner.Model
import com.nectar.doodle.controls.spinner.Spinner
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.Insets.Companion.None
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.core.Behavior

/**
 * Created by Nicholas Eddy on 3/15/18.
 */
data class Config(val components: List<View>, val layout: () -> Layout, val insets: Insets? = null)

interface SpinnerBehavior<T, M: Model<T>>: Behavior<Spinner<T, M>> {
    fun components(spinner: Spinner<T, M>): Config
}

abstract class CommonSpinnerBehavior(private val insets: Insets = None, private val labelFactory: LabelFactory): SpinnerBehavior<Any, Model<Any>> {
    override fun components(spinner: Spinner<Any, Model<Any>>): Config {
        val center = labelFactory(spinner.value.toString()).apply { fitText = emptySet() }
        val up     = PushButton("-").apply { enabled = spinner.hasPrevious }
        val down   = PushButton("+").apply { enabled = spinner.hasNext     }

        spinner.changed += {
            center.text  = it.value.toString() // TODO: Define string converter?
            up.enabled   = it.hasPrevious
            down.enabled = it.hasNext
        }

        up.fired += {
            spinner.previous()
        }

        down.fired += {
            spinner.next()
        }

        up.width = 40.0 // TODO: Move into layout

        val layout = {
            constrain(center, up, down) { center, up, down ->
                center.top    = center.parent.top  + insets.top
                center.left   = center.parent.left + insets.left
                center.right  = up.right - up.width
                center.bottom = center.parent.bottom - insets.bottom

                up.top        = up.parent.top     //center.top
                up.right      = up.parent.right   //- insets.right
                up.bottom     = up.parent.centerY

                down.top      = up.bottom
                down.left     = up.left
                down.right    = up.right
                down.bottom   = up.parent.bottom //center.bottom
            }
        }

        return Config(listOf(center, up, down), layout, insets)
    }
}