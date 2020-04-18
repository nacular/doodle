package com.nectar.doodle.themes

import com.nectar.doodle.core.View
import com.nectar.doodle.theme.InternalThemeManager
import com.nectar.doodle.theme.Theme
import com.nectar.doodle.theme.ThemeManager
import com.nectar.doodle.theme.ThemeManagerImpl
import com.nectar.doodle.themes.Modules.BehaviorResult.Matched
import com.nectar.doodle.themes.Modules.BehaviorResult.NotMatched
import com.nectar.doodle.themes.adhoc.AdhocTheme
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.Kodein.Builder
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.inSet
import org.kodein.di.erased.instance
import org.kodein.di.erased.setBinding
import org.kodein.di.erased.singleton
import org.kodein.di.erasedSet
import kotlin.reflect.KClass

/**
 * Created by Nicholas Eddy on 4/15/20.
 */

class Modules {
    enum class BehaviorResult { Matched, NotMatched }

    interface BehaviorResolver {
        val theme: KClass<out Theme>? get() = null

        operator fun invoke(view: View): BehaviorResult
    }

    companion object {
        val themeModule = Module(allowSilentOverride = true, name = "Theme") {
            bind<ThemeManager>        () with singleton { instance<InternalThemeManager>() }
            bind<InternalThemeManager>() with singleton { ThemeManagerImpl(instance()) }
        }

        val adhocThemeModule = Module(name = "AdhocTheme") {
            importOnce(themeModule, allowOverride = true)

            bind() from setBinding<BehaviorResolver>()

            bind<AdhocTheme>() with singleton { AdhocTheme(Instance(erasedSet())) }
        }

        inline fun <reified T: View> Builder.bindBehavior(theme: KClass<out Theme>? = null, crossinline block: DKodein.(T) -> Unit) {
            importOnce(adhocThemeModule, allowOverride = true)

            bind<BehaviorResolver>().inSet() with singleton {
                object: BehaviorResolver {
                    override val theme = theme

                    override fun invoke(view: View) = when (view) {
                        is T -> Matched.also { block(this@singleton, view) }
                        else -> NotMatched
                    }
                }
            }
        }
    }
}