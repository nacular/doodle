package io.nacular.doodle.theme.material

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton
import org.kodein.di.erasedSet

/**
 * Created by Nicholas Eddy on 1/8/20.
 */
class MaterialTheme(behaviors: Iterable<BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == MaterialTheme::class }) {
    override fun toString() = this::class.simpleName ?: ""

    companion object {
        class FontConfig(val source: String, val timeout: Measure<Time>)

        val materialTheme = Module(name = "MaterialTheme") {
            bind<MaterialTheme>() with singleton { MaterialTheme(Instance(erasedSet())) }
        }

        fun materialButtonBehavior(config: FontConfig? = null) = Module(name = "MaterialButtonBehavior") {
            bindBehavior<Button>(MaterialTheme::class) { it.behavior = MaterialButtonBehavior(
                    instance(),
                    instance(),
                    instance(),
                    config,
                    instance(),
                    textColor = White,
                    backgroundColor = Color(0x6200EEu),
                    cornerRadius = 4.0
            ) }
        }
    }
}