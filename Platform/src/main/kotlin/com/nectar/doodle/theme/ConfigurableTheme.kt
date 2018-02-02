package com.nectar.doodle.theme;

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import kotlin.reflect.KClass


inline fun <reified T: Gizmo> uiMapping(noinline renderer: (T) -> Renderer<T>) = UIPair(T::class, renderer)

class UIPair<T: Gizmo>(val type: KClass<T>, val renderer: (T) -> Renderer<T>)

open class ConfigurableTheme(settings: Set<UIPair<*>>): Theme {

    private val uis: MutableMap<KClass<*>, dynamic> = mutableMapOf()

    init {
        settings.forEach {
            uis[it.type] = it.renderer
        }
    }

    override fun install(display: Display, all: Sequence<Gizmo>) {
        all.forEach { gizmo ->
            val r = uis[gizmo::class]

//            gizmo::class.supertypes

            if (r != null) {
                r.invoke().install(gizmo)
            }

//            uis[gizmo::class]?.invoke()?.install(gizmo)
        }
    }

    override fun uninstall(display: Display, all: Sequence<Gizmo>) {
        all.forEach { gizmo ->
            uis[gizmo::class]?.invoke()?.uninstsall(gizmo)
        }
    }
}
