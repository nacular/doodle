package com.nectar.doodle.theme

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import kotlin.reflect.KClass


inline fun <reified T: View> uiMapping(noinline renderer: (T) -> Renderer<T>) = UIPair(T::class, renderer)

class UIPair<T: View>(val type: KClass<T>, val renderer: (T) -> Renderer<T>)

open class ConfigurableTheme(settings: Set<UIPair<*>>): Theme {

    private val uis: MutableMap<KClass<*>, dynamic> = mutableMapOf()

    init {
        settings.forEach {
            uis[it.type] = it.renderer
        }
    }

    override fun install(display: Display, all: Sequence<View>) {
        all.forEach { view ->
            val r = uis[view::class]

//            view::class.supertypes

            if (r != null) {
                r.invoke().install(view)
            }

//            uis[view::class]?.invoke()?.install(view)
        }
    }
}
