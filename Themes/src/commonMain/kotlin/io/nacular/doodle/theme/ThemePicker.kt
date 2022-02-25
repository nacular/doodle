package io.nacular.doodle.theme

import io.nacular.doodle.controls.spinner.MutableListSpinnerModel
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.constrain

/**
 * Created by Nicholas Eddy on 8/30/18.
 */
public class ThemePicker(themeManager: ThemeManager): View() {
    private val model   = MutableListSpinnerModel<Theme?>()
    private val spinner = Spinner(model)

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

        children += spinner

        layout = constrain(spinner) {
            it.top    = it.parent.top
            it.left   = it.parent.left
            it.right  = it.parent.right
            it.bottom = it.parent.bottom
        }

        spinner.changed += {
            it.value.onSuccess {
                themeManager.selected = it
            }
        }
    }

    private fun updateSelected(theme: Theme?) {
        spinner.apply {
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