package com.nectar.doodle.theme

import com.nectar.doodle.controls.spinner.MutableListModel
import com.nectar.doodle.controls.spinner.MutableSpinner
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.constrain

/**
 * Created by Nicholas Eddy on 8/30/18.
 */
class ThemePicker(themeManager: ThemeManager): View() {
    private val model   = MutableListModel<Theme>()
    private val spinner = MutableSpinner(model)

    init {
        update(emptySet(), themeManager.themes)

        themeManager.themes.changed += { _,removed,added ->
            update(removed, added)
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

    private fun update(removed: Set<Theme>, added: Set<Theme>) {
        model.values.batch {
            removeAll(removed)
            addAll   (added  )
        }
    }
}