package io.nacular.doodle.theme.material

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.kodein.di.erasedSet

/**
 * Created by Nicholas Eddy on 1/8/20.
 */
public class MaterialTheme(behaviors: Iterable<BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == MaterialTheme::class }) {
    override fun toString(): String = this::class.simpleName ?: ""

    public companion object {
        public class FontConfig(public val source: String, public val timeout: Measure<Time>)

        public val materialTheme: Module = Module(name = "MaterialTheme") {
            bind<MaterialTheme>() with singleton { MaterialTheme(Instance(erasedSet())) }
        }

        public fun materialButtonBehavior(config: FontConfig? = null): Module = Module(name = "MaterialButtonBehavior") {
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