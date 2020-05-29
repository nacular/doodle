package com.nectar.doodle.theme.material

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.theme.Modules.BehaviorResolver
import com.nectar.doodle.theme.Modules.Companion.bindBehavior
import com.nectar.doodle.theme.adhoc.AdhocTheme
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton
import org.kodein.di.erasedSet

/**
 * Created by Nicholas Eddy on 1/8/20.
 */
class MaterialTheme(behaviors: Iterable<BehaviorResolver>): AdhocTheme(behaviors.filter { it.theme == MaterialTheme::class }) {
    override fun toString() = this::class.simpleName ?: ""

    companion object {
        val materialTheme = Module(name = "MaterialTheme") {
            bind<MaterialTheme>() with singleton { MaterialTheme(Instance(erasedSet())) }
        }

        val materialButtonBehavior = Module(name = "MaterialButtonBehavior") {
            bindBehavior<Button>(MaterialTheme::class) { it.behavior = MaterialButtonBehavior(instance(), instance(), instance(), textColor = White, backgroundColor = Color(0x6200EEu), cornerRadius = 4.0) }
        }
    }
}