package com.nectar.doodle.themes.adhoc

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.themes.Modules.BehaviorResult
import com.nectar.doodle.theme.Theme
import com.nectar.doodle.themes.Modules
import com.nectar.doodle.themes.Modules.BehaviorResolver
import org.kodein.di.DKodein
import org.kodein.di.erasedSet

open class AdhocTheme internal constructor(private val behaviors: Iterable<BehaviorResolver>): Theme {
    override fun install(display: Display, all: Sequence<View>) = all.forEach { view ->
        behaviors.lastOrNull { it(view) == BehaviorResult.Matched }
    }

    override fun toString() = this::class.simpleName ?: ""
}