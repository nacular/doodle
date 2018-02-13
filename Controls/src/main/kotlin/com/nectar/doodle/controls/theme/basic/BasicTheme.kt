package com.nectar.doodle.controls.theme.basic

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.erased.bind
import com.github.salomonbrys.kodein.erased.singleton
import com.nectar.doodle.controls.ProgressBar
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.theme.Theme

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
private val defaultBackgroundColor = Color(0xcccccc)
private val darkBackgroundColor    = Color(0xaaaaaa)

class BasicTheme: Theme {

    private val progressBarUI by lazy { BasicProgressBarUI(defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor)}

    override fun install(display: Display, all: Sequence<Gizmo>) = all.forEach {
        when (it) {
            is ProgressBar -> it.renderer = progressBarUI.apply { install(it) }
        }
    }

    override fun uninstall(display: Display, all: Sequence<Gizmo>) = all.forEach {
        when (it) {
            is ProgressBar -> it.renderer?.uninstall(it)
        }
    }
}

val basicThemeModule = Kodein.Module {
    bind<BasicTheme>() with singleton { BasicTheme() }
}