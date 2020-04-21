package com.nectar.doodle.themes.adhoc

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.themes.Modules.BehaviorResult
import com.nectar.doodle.theme.Theme
import com.nectar.doodle.themes.Modules
import com.nectar.doodle.themes.Modules.BehaviorResolver
import org.kodein.di.DKodein
import org.kodein.di.erasedSet

open class AdhocTheme internal constructor(behaviors: Iterable<BehaviorResolver>): Theme {
    private val behaviors = behaviors.reversed()

    override fun install(display: Display, all: Sequence<View>) = all.forEach { view ->
        behaviors.firstOrNull { it(view) == BehaviorResult.Matched }
    }

    override fun toString() = this::class.simpleName ?: ""
}