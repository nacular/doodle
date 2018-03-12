package com.nectar.doodle.application

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.Kodein.Module
import com.github.salomonbrys.kodein.erased.bind
import com.github.salomonbrys.kodein.erased.instance
import com.github.salomonbrys.kodein.erased.instanceOrNull
import com.github.salomonbrys.kodein.erased.singleton
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
import com.nectar.doodle.drawing.CanvasFactoryImpl
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextFactoryImpl
import com.nectar.doodle.drawing.impl.GraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.RealGraphicsDevice
import com.nectar.doodle.drawing.impl.RealGraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.RenderManagerImpl
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.event.KeyState.Type.Down
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import com.nectar.doodle.focus.impl.FocusManagerImpl
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.impl.SchedulerImpl
import com.nectar.doodle.system.KeyInputService
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.impl.KeyInputServiceImpl
import com.nectar.doodle.system.impl.KeyInputServiceStrategy
import com.nectar.doodle.system.impl.KeyInputServiceStrategyWebkit
import com.nectar.doodle.system.impl.MouseInputServiceImpl
import com.nectar.doodle.system.impl.MouseInputServiceStrategy
import com.nectar.doodle.system.impl.MouseInputServiceStrategyWebkit
import com.nectar.doodle.theme.ThemeManager
import com.nectar.doodle.theme.ThemeManagerImpl
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
abstract class Application(modules: Set<Module> = setOf(mouseModule)) {
    abstract fun run(display: Display)

    val injector = Kodein {
        bind<SystemStyler>() with instance  ( SystemStylerImpl() )

        bind<Display>     () with singleton { DisplayImpl    (instance(), document.body!!) }
        bind<Scheduler>   () with singleton { SchedulerImpl  (                           ) }
        bind<HtmlFactory> () with singleton { HtmlFactoryImpl(                           ) }
        bind<TextFactory> () with singleton { TextFactoryImpl(instance()                 ) }

        import(renderModule)

        modules.forEach {
            import(it, allowOverride = true)
        }
    }

    init {
        injector.instance<SystemStyler> ()
        injector.instance<RenderManager>()

        injector.instanceOrNull<MouseInputManager>()

        run(injector.instance())
    }
}

private val renderModule = Module {
    bind<SvgFactory>               () with singleton { SvgFactoryImpl            (                                                    ) }
    bind<CanvasFactory>            () with singleton { CanvasFactoryImpl         (instance(), instance()                              ) }
    bind<RenderManager>            () with singleton { RenderManagerImpl         (instance(), instance(), instanceOrNull(), instance()) }
    bind<GraphicsDevice<*>>        () with singleton { RealGraphicsDevice        (instance()                                          ) }
    bind<GraphicsSurfaceFactory<*>>() with singleton { RealGraphicsSurfaceFactory(instance(), instance()                              ) }
}

val mouseModule = Module {
    bind<MouseInputService>        () with singleton { MouseInputServiceImpl          (instance()             ) }
    bind<MouseInputManager>        () with singleton { MouseInputManager              (instance(), instance() ) }
    bind<MouseInputServiceStrategy>() with singleton { MouseInputServiceStrategyWebkit(instance()             ) }
}

val focusModule = Module {
    bind<FocusManager>() with singleton { FocusManagerImpl(instance()) }
}

val keyboardModule = Module {
    import(focusModule)

    val keys = mutableMapOf<TraversalType, Set<KeyState>>()

    keys[Forward ] = setOf(KeyState(KeyEvent.VK_TAB, KeyEvent.VK_TAB.toChar(), emptySet(),   Down))
    keys[Backward] = setOf(KeyState(KeyEvent.VK_TAB, KeyEvent.VK_TAB.toChar(), setOf(Shift), Down))

    bind<KeyInputService>        () with singleton { KeyInputServiceImpl          (instance()                   ) }
    bind<KeyboardFocusManager>   () with singleton { KeyboardFocusManager         (instance(), instance(), keys ) }
    bind<KeyInputServiceStrategy>() with singleton { KeyInputServiceStrategyWebkit(instance()                   ) }
}

val themeModule = Module {
    bind<ThemeManager>() with singleton { ThemeManagerImpl(instance()) }
}