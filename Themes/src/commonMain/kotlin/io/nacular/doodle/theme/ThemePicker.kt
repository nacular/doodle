package io.nacular.doodle.theme

import io.nacular.doodle.controls.spinner.MutableListSpinButtonModel
import io.nacular.doodle.controls.spinner.SpinButton
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.constraints.constrain

/**
 * Simple View that uses a [SpinButton] internally to allow switching between the themes within
 * a [ThemeManager].
 *
 * @param themeManager to manage
 */
public class ThemePicker(themeManager: ThemeManager): View() {
    private val model      = MutableListSpinButtonModel<Theme?>()
    private val spinButton = SpinButton(model)

    /** Human-understandable text to represent the current value if the number is insufficient. */
    public var valueAccessibilityLabeler: ((Result<Theme?>) -> String)? get() = spinButton.valueAccessibilityLabeler; set(new) {
        spinButton.valueAccessibilityLabeler = new
    }

    init {
        focusable = false

        updateAvailableThemes(emptySet(), themeManager.themes)

        updateSelected(themeManager.selected)

        themeManager.selectionChanged += { _,_,new ->
            updateSelected(new)
        }

        themeManager.themes.changed += { _,removed,added ->
            updateAvailableThemes(removed, added)
        }

        children += spinButton

        layout = constrain(spinButton) {
            it.top    eq 0
            it.left   eq 0
            it.right  eq parent.right
            it.bottom eq parent.bottom
        }

        spinButton.changed += {
            it.value.onSuccess {
                themeManager.selected = it
            }
        }
    }

    private fun updateSelected(theme: Theme?) {
        spinButton.apply {
            while (value.getOrNull() != theme && hasNext) {
                next()
            }

            while (value.getOrNull() != theme && hasPrevious) {
                previous()
            }
        }
    }

    private fun updateAvailableThemes(removed: Set<Theme>, added: Set<Theme?>) {
        model.values.batch {
            removeAll(removed)
            addAll   (added  )
        }
    }
}