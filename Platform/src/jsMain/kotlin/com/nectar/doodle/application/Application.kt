package com.nectar.doodle.application

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.impl.DisplayImpl
import com.nectar.doodle.datatransport.dragdrop.DragManager
import com.nectar.doodle.datatransport.dragdrop.impl.DragManagerImpl
import com.nectar.doodle.deviceinput.KeyboardFocusManager
import com.nectar.doodle.deviceinput.KeyboardFocusManagerImpl
import com.nectar.doodle.deviceinput.MouseInputManager
import com.nectar.doodle.deviceinput.MouseInputManagerImpl
import com.nectar.doodle.deviceinput.ViewFinder
import com.nectar.doodle.deviceinput.ViewFinderImpl
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.SvgFactoryImpl
import com.nectar.doodle.dom.SystemStyler
import com.nectar.doodle.dom.SystemStylerImpl
import com.nectar.doodle.dom.impl.ElementRulerImpl
import com.nectar.doodle.dom.impl.HtmlFactoryImpl
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.CanvasFactoryImpl
import com.nectar.doodle.drawing.impl.GraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.RealGraphicsDevice
import com.nectar.doodle.drawing.impl.RealGraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.RenderManagerImpl
import com.nectar.doodle.drawing.impl.TextFactoryImpl
import com.nectar.doodle.drawing.impl.TextMetricsImpl
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy
import com.nectar.doodle.focus.impl.FocusManagerImpl
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.scheduler.impl.AnimationSchedulerImpl
import com.nectar.doodle.scheduler.impl.SchedulerImpl
import com.nectar.doodle.scheduler.impl.StrandImpl
import com.nectar.doodle.system.KeyInputService
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemInputEvent
import com.nectar.doodle.system.impl.KeyInputServiceImpl
import com.nectar.doodle.system.impl.KeyInputServiceStrategy
import com.nectar.doodle.system.impl.KeyInputServiceStrategyWebkit
import com.nectar.doodle.system.impl.MouseInputServiceImpl
import com.nectar.doodle.system.impl.MouseInputServiceStrategy
import com.nectar.doodle.system.impl.MouseInputServiceStrategyWebkit
import com.nectar.doodle.theme.InternalThemeManager
import com.nectar.doodle.theme.ThemeManager
import com.nectar.doodle.theme.ThemeManagerImpl
import com.nectar.doodle.time.Timer
import com.nectar.doodle.time.impl.PerformanceTimer
import org.kodein.di.Copy
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.bindings.NoArgSimpleBindingKodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.instanceOrNull
import org.kodein.di.erased.singleton
import org.w3c.dom.Window
import kotlin.browser.document
import kotlin.browser.window

/**
 * Created by Nicholas Eddy on 1/22/20.
 */

interface Application {
    fun shutdown()
}

class SimpleApplication: Application {
    override fun shutdown() {
        // NO-OP
    }
}

interface ApplicationHolder {
    fun shutdown()
}

inline fun <reified T: Application> doodle(
                 root                : HTMLElement        = document.body!!,
                 allowDefaultDarkMode: Boolean            = false,
                 modules             : Set<Kodein.Module> = emptySet(),
        noinline creator             : NoArgSimpleBindingKodein<*>.() -> T): ApplicationHolder = createApplication(Kodein.direct {
    bind<Application>() with singleton(null, creator)
}, root, allowDefaultDarkMode, modules)

fun createApplication(
        injector            : DKodein,
        root                : HTMLElement,
        allowDefaultDarkMode: Boolean,
        modules             : Set<Kodein.Module>): ApplicationHolder = ApplicationHolderImpl(injector, root, allowDefaultDarkMode, modules)

internal class ApplicationHolderImpl(previousInjector: DKodein, root: HTMLElement = document.body!!, allowDefaultDarkMode: Boolean = false, modules: Set<Kodein.Module> = emptySet()): ApplicationHolder {
    private var injector = Kodein.direct {
        extend(previousInjector, copy = Copy.All)

        bind<Window>                   () with instance  ( window )

        bind<Timer>                    () with singleton { PerformanceTimer          (window.performance                                              ) }
        bind<Strand>                   () with singleton { StrandImpl                (instance(), instance()                                          ) }
        bind<Display>                  () with singleton { DisplayImpl               (instance(), instance(), root                                    ) }
        bind<Scheduler>                () with singleton { SchedulerImpl             (instance()                                                      ) }
        bind<SvgFactory>               () with singleton { SvgFactoryImpl            (root, document                                                  ) }
        bind<HtmlFactory>              () with singleton { HtmlFactoryImpl           (root, document                                                  ) }
        bind<TextFactory>              () with singleton { TextFactoryImpl           (instance()                                                      ) }
        bind<TextMetrics>              () with singleton { TextMetricsImpl           (instance(), instance(), instance()                              ) }
        bind<ElementRuler>             () with singleton { ElementRulerImpl          (instance()                                                      ) }
        bind<SystemStyler>             () with singleton { SystemStylerImpl          (instance(), document, allowDefaultDarkMode                      ) }
        bind<CanvasFactory>            () with singleton { CanvasFactoryImpl         (instance(), instance(), instance(), instance()                  ) }
        bind<RenderManager>            () with singleton { RenderManagerImpl         (instance(), instance(), instance(), instanceOrNull(), instance()) }
        bind<GraphicsDevice<*>>        () with singleton { RealGraphicsDevice        (instance()                                                      ) }
        bind<AnimationScheduler>       () with singleton { AnimationSchedulerImpl    (                                                                ) } // FIXME: Provide fallback in case not supported
        bind<GraphicsSurfaceFactory<*>>() with singleton { RealGraphicsSurfaceFactory(instance(), instance()                                          ) }

        modules.forEach {
            import(it, allowOverride = true)
        }
    }

    private var initTask    : Task
    private var application = null as Application?

    init {
        injector.instance<SystemStyler> ()
        injector.instance<RenderManager>()

        injector.instanceOrNull<MouseInputManager>   ()
        injector.instanceOrNull<KeyboardFocusManager>()
        injector.instanceOrNull<DragManager>         ()

        initTask = injector.instance<Scheduler>().now {
            application = injector.instance()
        }
    }

    override fun shutdown() {
        initTask.cancel()

        injector.instance<Display>     ().shutdown()
        injector.instance<SystemStyler>().shutdown()

        injector.instanceOrNull<DragManager>         ()?.shutdown()
        injector.instanceOrNull<MouseInputManager>   ()?.shutdown()
        injector.instanceOrNull<KeyboardFocusManager>()?.shutdown()

        application?.shutdown()

        injector = Kodein.direct {}
    }
}

class Modules {
    companion object {
        val mouseModule = Kodein.Module(allowSilentOverride = true) {
            bind<ViewFinder>() with singleton { ViewFinderImpl(instance()) }
            bind<MouseInputService>() with singleton { MouseInputServiceImpl(instance()) }
            bind<MouseInputManager>() with singleton { MouseInputManagerImpl(instance(), instance()) }
            bind<MouseInputServiceStrategy>() with singleton { MouseInputServiceStrategyWebkit(instance()) }
        }

        val focusModule = Kodein.Module(allowSilentOverride = true) {
            bind<FocusManager>() with singleton { FocusManagerImpl(instance()) }
        }

        val keyboardModule = Kodein.Module(allowSilentOverride = true) {
            import(focusModule)

            // TODO: Make this plugable
            val keys = mapOf(
                    FocusTraversalPolicy.TraversalType.Forward to setOf(KeyState(KeyEvent.VK_TAB, KeyEvent.VK_TAB.toChar(), emptySet(), KeyState.Type.Down)),
                    FocusTraversalPolicy.TraversalType.Backward to setOf(KeyState(KeyEvent.VK_TAB, KeyEvent.VK_TAB.toChar(), setOf(SystemInputEvent.Modifier.Shift), KeyState.Type.Down))
            )

            bind<KeyInputService>() with singleton { KeyInputServiceImpl(instance()) }
            bind<KeyboardFocusManager>() with singleton { KeyboardFocusManagerImpl(instance(), instance(), keys) }
            bind<KeyInputServiceStrategy>() with singleton { KeyInputServiceStrategyWebkit(instance()) }
        }

        val themeModule = Kodein.Module(allowSilentOverride = true) {
            bind<InternalThemeManager>() with singleton { ThemeManagerImpl(instance()) }
            bind<ThemeManager>() with singleton { instance<InternalThemeManager>() }
        }

        val dragDropModule = Kodein.Module(allowSilentOverride = true) {
            import(mouseModule)

            bind<DragManager>() with singleton { DragManagerImpl(instance(), instance(), instance(), instance(), instance()) }
        }
    }
}