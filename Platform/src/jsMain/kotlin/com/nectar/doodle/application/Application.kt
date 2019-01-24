package com.nectar.doodle.application

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.impl.DisplayImpl
import com.nectar.doodle.deviceinput.KeyboardFocusManager
import com.nectar.doodle.deviceinput.MouseInputManager
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.HtmlFactoryImpl
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.SvgFactoryImpl
import com.nectar.doodle.dom.SystemStyler
import com.nectar.doodle.dom.SystemStylerImpl
import com.nectar.doodle.drawing.CanvasFactory
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextFactoryImpl
import com.nectar.doodle.drawing.impl.CanvasFactoryImpl
import com.nectar.doodle.drawing.impl.GraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.RealGraphicsDevice
import com.nectar.doodle.drawing.impl.RealGraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.RenderManagerImpl
import com.nectar.doodle.drawing.impl.VectorBackgroundFactory
import com.nectar.doodle.drawing.impl.VectorBackgroundFactoryImpl
import com.nectar.doodle.event.KeyEvent.Companion.VK_TAB
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.event.KeyState.Type.Down
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import com.nectar.doodle.focus.impl.FocusManagerImpl
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.scheduler.impl.AnimationSchedulerImpl
import com.nectar.doodle.scheduler.impl.SchedulerImpl
import com.nectar.doodle.scheduler.impl.StrandImpl
import com.nectar.doodle.system.KeyInputService
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
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
import org.kodein.di.Kodein
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.instanceOrNull
import org.kodein.di.erased.singleton
import org.w3c.dom.HTMLElement
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
abstract class Application(root: HTMLElement = document.body!!, modules: Set<Module> = emptySet()) {
    protected abstract fun run(display: Display)

    protected val injector = Kodein.direct {
        bind<SystemStyler>() with instance(SystemStylerImpl())

        bind<Timer>                    () with singleton { PerformanceTimer          (                                                                ) }
        bind<Strand>                   () with singleton { StrandImpl                (instance(), instance()                                          ) }
        bind<Display>                  () with singleton { DisplayImpl               (instance(), root                                                ) }
        bind<Scheduler>                () with singleton { SchedulerImpl             (instance()                                                      ) }
        bind<SvgFactory>               () with singleton { SvgFactoryImpl            (root                                                            ) }
        bind<HtmlFactory>              () with singleton { HtmlFactoryImpl           (root                                                            ) }
        bind<TextFactory>              () with singleton { TextFactoryImpl           (instance()                                                      ) }
        bind<CanvasFactory>            () with singleton { CanvasFactoryImpl         (instance(), instance(), instance(), instance()                  ) }
        bind<RenderManager>            () with singleton { RenderManagerImpl         (instance(), instance(), instance(), instanceOrNull(), instance()) }
        bind<GraphicsDevice<*>>        () with singleton { RealGraphicsDevice        (instance()                                                      ) }
        bind<AnimationScheduler>       () with singleton { AnimationSchedulerImpl    (                                                                ) } // FIXME: Provide fallback in case not supported
        bind<VectorBackgroundFactory>  () with singleton { VectorBackgroundFactoryImpl(instance(), instance(), instance()                             ) }
        bind<GraphicsSurfaceFactory<*>>() with singleton { RealGraphicsSurfaceFactory(instance(), instance()                                          ) }

        modules.forEach {
            import(it, allowOverride = true)
        }
    }

    init {
        injector.instance<SystemStyler> ()
        injector.instance<RenderManager>()

        injector.instanceOrNull<MouseInputManager>   ()
        injector.instanceOrNull<KeyboardFocusManager>()

        run(injector.instance())
    }
}

val mouseModule = Module {
    bind<MouseInputService>        () with singleton { MouseInputServiceImpl          (instance()            ) }
    bind<MouseInputManager>        () with singleton { MouseInputManager              (instance(), instance()) }
    bind<MouseInputServiceStrategy>() with singleton { MouseInputServiceStrategyWebkit(instance()            ) }
}

val focusModule = Module(allowSilentOverride = true) {
    bind<FocusManager>() with singleton { FocusManagerImpl() }
}

val keyboardModule = Module {
    import(focusModule)

    val keys = mutableMapOf<TraversalType, Set<KeyState>>()

    keys[Forward ] = setOf(KeyState(VK_TAB, VK_TAB.toChar(), emptySet(),   Down))
    keys[Backward] = setOf(KeyState(VK_TAB, VK_TAB.toChar(), setOf(Shift), Down))

    bind<KeyInputService>        () with singleton { KeyInputServiceImpl          (instance()                   ) }
    bind<KeyboardFocusManager>   () with singleton { KeyboardFocusManager         (instance(), instance(), keys ) }
    bind<KeyInputServiceStrategy>() with singleton { KeyInputServiceStrategyWebkit(instance()                   ) }
}

val themeModule = Module {
    bind<InternalThemeManager>() with singleton { ThemeManagerImpl              (instance()) }
    bind<ThemeManager>        () with singleton { instance<InternalThemeManager>(          ) }
}