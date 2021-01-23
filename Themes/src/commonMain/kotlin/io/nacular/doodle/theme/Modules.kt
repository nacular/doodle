package io.nacular.doodle.theme

import io.nacular.doodle.core.View
import io.nacular.doodle.theme.Modules.BehaviorResult.Matched
import io.nacular.doodle.theme.Modules.BehaviorResult.NotMatched
import io.nacular.doodle.theme.adhoc.DynamicTheme
import org.kodein.di.DKodein
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
public class Modules {
    public enum class BehaviorResult { Matched, NotMatched }

    public interface BehaviorResolver {
        public val theme: KClass<out Theme>? get() = null

        public operator fun invoke(view: View): BehaviorResult
    }

    public companion object {
        public val ThemeModule: Module = Module(allowSilentOverride = true, name = "Theme") {
            bind<ThemeManager>        () with singleton { instance<InternalThemeManager>() }
            bind<InternalThemeManager>() with singleton { ThemeManagerImpl(instance()) }
        }

        public val DynamicThemeModule: Module = Module(name = "DynamicThemeModule") {
            importOnce(ThemeModule, allowOverride = true)

            bind() from setBinding<BehaviorResolver>()

            bind<DynamicTheme>() with singleton { object: DynamicTheme(Instance(erasedSet())) {} }
        }

        public inline fun <reified T: View> Builder.bindBehavior(theme: KClass<out Theme>? = null, crossinline block: DKodein.(T) -> Unit): Unit = bindConditionalBehavior<T>(theme) {
            block(this, it)
            Matched
        }

        // TODO: Can this be renamed to bindBehavior in 1.4?
        public inline fun <reified T: View> Builder.bindConditionalBehavior(theme: KClass<out Theme>? = null, crossinline block: DKodein.(T) -> BehaviorResult) {
            importOnce(DynamicThemeModule, allowOverride = true)

            bind<BehaviorResolver>().inSet() with singleton {
                object: BehaviorResolver {
                    override val theme = theme

                    override fun invoke(view: View) = when (view) {
                        is T -> block(this@singleton, view)
                        else -> NotMatched
                    }
                }
            }
        }
    }
}