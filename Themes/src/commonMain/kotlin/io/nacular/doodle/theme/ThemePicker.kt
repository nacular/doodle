package io.nacular.doodle.theme

import io.nacular.doodle.controls.spinner.MutableListModel
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.constrain

/**
 * Created by Nicholas Eddy on 8/30/18.
 */
public class ThemePicker(themeManager: ThemeManager): View() {
    private val model   = MutableListModel<Theme?>()
    private val spinner = Spinner(model)

    init {
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
            themeManager.selected = it.value
        }
    }

    private fun updateSelected(theme: Theme?) {
        spinner.apply {
            while (value != theme && hasNext) {
                next()
            }

            while (value != theme && hasPrevious) {
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