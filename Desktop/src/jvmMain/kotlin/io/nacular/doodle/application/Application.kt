package io.nacular.doodle.application

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.core.impl.DisplayImpl
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.deviceinput.KeyboardFocusManager
import io.nacular.doodle.deviceinput.PointerInputManager
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.DesktopRenderManagerImpl
import io.nacular.doodle.drawing.impl.GraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.RealGraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.drawing.impl.RealGraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.TextMetricsImpl
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.impl.AnimationSchedulerImpl
import io.nacular.doodle.scheduler.impl.SchedulerImpl
import io.nacular.doodle.time.Timer
import io.nacular.doodle.time.impl.TimerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.datetime.Clock
import org.jetbrains.skija.Font
import org.jetbrains.skija.FontMgr
import org.jetbrains.skija.FontSlant.UPRIGHT
import org.jetbrains.skija.FontStyle
import org.jetbrains.skija.PathMeasure
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skiko.SkiaWindow
import org.kodein.di.Copy.All
import org.kodein.di.DI.Companion.direct
import org.kodein.di.DI.Module
import org.kodein.di.DirectDI
import org.kodein.di.bind
import org.kodein.di.bindFactory
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.bindings.Singleton
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.type.generic
import javax.swing.JFrame.EXIT_ON_CLOSE

/**
 * Created by Nicholas Eddy on 5/14/21.
 */
public inline fun <reified T: Application> application(
        allowDefaultDarkMode: Boolean     = false,
        modules             : List<Module> = emptyList(),
        noinline creator    : NoArgBindingDI<*>.() -> T): Application = createApplication(direct {
    // FIXME: change when https://youtrack.jetbrains.com/issue/KT-39225 fixed
    bind<Application> { Singleton(scope, contextType, explicitContext, generic(), null, true, creator) } //singleton(creator = creator)
//    bind<Application>() with Singleton(scope, contextType, explicitContext, generic(), null, true, creator)
}, allowDefaultDarkMode, modules)

public fun createApplication(
        injector            : DirectDI,
        allowDefaultDarkMode: Boolean,
        modules             : List<Module>): Application = ApplicationHolderImpl(injector, allowDefaultDarkMode = allowDefaultDarkMode, modules = modules)

private open class ApplicationHolderImpl protected constructor(
        previousInjector    : DirectDI,
        allowDefaultDarkMode: Boolean      = false,
        modules             : List<Module> = emptyList(),
        private val isNested: Boolean      = false): Application {

    private inner class ShutdownHook: Thread() {
        override fun run() {
            // TODO: Should this be run in the UI thread?
            shutdown()
        }
    }

    private var focusManager   : FocusManager? = null
    private val appScope       = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val skiaWindow     = SkiaWindow().apply {
        defaultCloseOperation = EXIT_ON_CLOSE
    }
    private val defaultFont    = Font(Typeface.makeFromName("menlo", FontStyle(300, 5, UPRIGHT)), 13f)
    private val fontCollection = FontCollection().apply {
        setDefaultFontManager(FontMgr.getDefault())
    }

    protected var injector = direct {
        extend(previousInjector, copy = All)

        bindInstance                                               { appScope                                                                                                       }
        bindInstance                                               { Clock.System                                                                                                   }
        bindInstance                                               { skiaWindow                                                                                                     }
        bindFactory<Unit, PathMeasure>                             { PathMeasure               (                                                                                  ) }
        bindSingleton<Timer>                                       { TimerImpl                 (instance()                                                                        ) }
//      bindSingleton<Strand>                                      { StrandImpl                (instance(), instance()                                                            ) }
        bindSingleton<Display>                                     { DisplayImpl               (instance(), Dispatchers.Swing, instance(), defaultFont, fontCollection, instance()) }
        bindSingleton<Scheduler>                                   { SchedulerImpl             (instance(), instance()                                                            ) }
        bindSingleton<TextMetrics>                                 { TextMetricsImpl           (defaultFont, fontCollection                                                       ) }
        bindSingleton<RenderManager>                               { DesktopRenderManagerImpl  (instance(), instance(), instanceOrNull(), instanceOrNull(), instance()            ) }
        bindSingleton<AnimationScheduler>                          { AnimationSchedulerImpl    (instance(), Dispatchers.Swing, instance()                                         ) }
        bindSingleton<GraphicsDevice<RealGraphicsSurface>>         { RealGraphicsDevice        (instance()                                                                        ) }
        bindSingleton<GraphicsSurfaceFactory<RealGraphicsSurface>> { RealGraphicsSurfaceFactory(instance(), defaultFont, fontCollection                                           ) }

        // TODO: Can this be handled better?
        bindSingleton                                              { instance<Display>           () as DisplayImpl                                                                 }
        bindSingleton<InternalDisplay>                             { instance<DisplayImpl>       ()                                                                                }
        bindSingleton                                              { instance<AnimationScheduler>() as AnimationSchedulerImpl                                                      }

        modules.forEach {
            import(it, allowOverride = true)
        }
    }

    private var isShutdown  = false
    private var application = null as Application?

    private var focusListener: ((FocusManager, View?, View?) -> Unit)? = null

    init {
        System.setProperty("skiko.vsync.enabled", "false")
//        System.setProperty("skiko.fps.enabled",   "true" )
//        System.setProperty("skiko.renderApi", "OPENGL")

        Runtime.getRuntime().addShutdownHook(ShutdownHook())
    }

    protected fun run() {
        injector.instance      <RenderManager>       ()
        injector.instanceOrNull<PointerInputManager> ()
        injector.instanceOrNull<KeyboardFocusManager>()
        injector.instanceOrNull<DragManager>         ()

        application = injector.instance()

//        focusManager = injector.instanceOrNull()

//        if (!isNested && root != document.body) {
//            val nativeFocusManager = injector.instanceOrNull<NativeFocusManager>()
//
//            focusManager?.let {
//                it.focusChanged += { _: FocusManager, _: View?, new: View? ->
//                    when {
//                        new == null                                -> root.blur ()
//                        nativeFocusManager?.hasFocusOwner == false -> root.focus()
//                    }
//                }.also { focusListener = it }
//            }
//        }

//        if (root != document.body) {
//            (focusManager as? FocusManagerImpl)?.enabled = false
//        }
    }

    override fun shutdown() {
        if (isShutdown) {
            return
        }

        application?.shutdown()

        (injector.instance<Scheduler> () as? SchedulerImpl)?.shutdown()
        injector.instance<DisplayImpl>().shutdown()
        injector.instance<AnimationSchedulerImpl>().shutdown()
        injector.instanceOrNull<DragManager>             ()?.shutdown()
        injector.instanceOrNull<PointerInputManager>     ()?.shutdown()
        injector.instanceOrNull<KeyboardFocusManager>    ()?.shutdown()
//        injector.instanceOrNull<AccessibilityManagerImpl>()?.shutdown()

//        if (!isNested && root != document.body) {
//            root.stopMonitoringSize()
//
//            focusManager?.let { focusManager ->
//                focusListener?.let { focusManager.focusChanged -= it }
//            }
//
//            focusManager = null
//        }

        injector = direct {}

        isShutdown = true
    }

    companion object {
        operator fun invoke(previousInjector    : DirectDI,
                            allowDefaultDarkMode: Boolean      = false,
                            modules             : List<Module> = emptyList()): Application {
            return ApplicationHolderImpl(previousInjector, allowDefaultDarkMode, modules).apply {
                appScope.launch(Dispatchers.Swing) {
                    run()
                }
            }
        }
    }
}