package com.nectar.doodle.themes.material

import com.nectar.doodle.animation.Animator
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.themes.basic.BasicTheme
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.FontDetector
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.themes.Modules
import com.nectar.doodle.themes.Modules.BehaviorResolver
import com.nectar.doodle.themes.Modules.BehaviorResult
import com.nectar.doodle.themes.Modules.BehaviorResult.Matched
import com.nectar.doodle.themes.Modules.BehaviorResult.NotMatched
import com.nectar.doodle.themes.Modules.Companion.bindBehavior
import com.nectar.doodle.themes.Modules.Companion.bindConditionalBehavior
import com.nectar.doodle.themes.adhoc.AdhocTheme
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
            bindBehavior<Button>(MaterialTheme::class) { it.behavior = MaterialButtonBehavior(instance(), instance(), instance(), textColor = white, backgroundColor = Color(0x6200EEu), cornerRadius = 4.0) }
//            bindBehavior<Button>(MaterialTheme::class) { it.behavior = MaterialButtonBehaviorBase(instance(), instance(), instance(), textColor = Color(0x6200EEu), cornerRadius = 4.0) }
        }
    }
}