package io.nacular.doodle.application

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.core.impl.DisplayImpl
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.drawing.impl.GraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.RealGraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.drawing.impl.RealGraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.RenderManagerImpl
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
import kotlinx.datetime.Clock
import org.jetbrains.skija.Font
import org.jetbrains.skiko.SkiaWindow
import org.kodein.di.Copy.All
import org.kodein.di.DI.Companion.direct
import org.kodein.di.DI.Module
import org.kodein.di.DirectDI
import org.kodein.di.bind
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.bindings.Singleton
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.type.generic

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
    private var focusManager: FocusManager? = null

    private val appScope    = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val skiaWindow  = SkiaWindow()
    private val defaultFont = Font()

    protected var injector = direct {
        extend(previousInjector, copy = All)

        bindInstance                                               { appScope }
        bindInstance                                               { Clock.System  }
        bindInstance                                               { skiaWindow }
        bindSingleton<Timer>                                       { TimerImpl          (instance()                                                    ) }
//        bindSingleton<Strand>                                      { StrandImpl                (instance(), instance()                                                ) }
        bindSingleton<Display>                                     { DisplayImpl               (instance(), skiaWindow, defaultFont                                                            ) }
        bindSingleton<Scheduler>                                   { SchedulerImpl             (instance(), instance()                                                ) }
//        bindSingleton<TextMetrics>                                 { TextMetricsImpl           (instance(), instance(), instance(), instance(), cacheLength = 1000    ) }
//        bindSingleton<CanvasFactory>                               { CanvasFactoryImpl         (instance(), instance(), instance(), instance(), instance()            ) }
        bindSingleton<RenderManager>                               { RenderManagerImpl         (instance(), instance(), instanceOrNull(), instanceOrNull(), instance()) }
        bindSingleton<AnimationScheduler>                          { AnimationSchedulerImpl    (instance(), instance()                                                ) } // FIXME: Provide fallback in case not supported
        bindSingleton<GraphicsDevice<RealGraphicsSurface>>         { RealGraphicsDevice        (instance()                                                            ) }
        bindSingleton<GraphicsSurfaceFactory<RealGraphicsSurface>> { RealGraphicsSurfaceFactory(instance(), defaultFont                                               ) }

        // TODO: Can this be handled better?
        bindSingleton                                              { instance<Display>     () as DisplayImpl }
        bindSingleton<InternalDisplay>                             { instance<DisplayImpl> ()                }

        modules.forEach {
            import(it, allowOverride = true)
        }
    }

    private var isShutdown  = false
    private var application = null as Application?

//    private fun onUnload(@Suppress("UNUSED_PARAMETER") event: Event? = null) {
//        shutdown()
//    }

    private var focusListener: ((FocusManager, View?, View?) -> Unit)? = null

    protected fun run() {
        injector.instance<RenderManager>()

//        injector.instanceOrNull<PointerInputManager> ()
//        injector.instanceOrNull<KeyboardFocusManager>()
//        injector.instanceOrNull<DragManager>         ()

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

//        if (!isNested) {
//            injector.instance<SystemStyler>().shutdown()
//        }
//
//        injector.instanceOrNull<DragManager>             ()?.shutdown()
//        injector.instanceOrNull<PointerInputManager>     ()?.shutdown()
//        injector.instanceOrNull<KeyboardFocusManager>    ()?.shutdown()
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

//    private class AsyncAppWrapper(previousInjector: DirectDI, allowDefaultDarkMode: Boolean= false, modules: List<Module> = emptyList()): Application {
//        private  var jobId : Int
//        lateinit var holder: ApplicationHolderImpl
//
//        init {
//            jobId = window.setTimeout({
//                holder = ApplicationHolderImpl(previousInjector, document.body!!, allowDefaultDarkMode, modules)
//                holder.run()
//            })
//        }
//
//        override fun shutdown() {
//            when {
//                ::holder.isInitialized -> holder.shutdown()
//                else                   -> window.clearTimeout(jobId)
//            }
//        }
//    }

    companion object {
        operator fun invoke(previousInjector    : DirectDI,
                allowDefaultDarkMode: Boolean      = false,
                modules             : List<Module> = emptyList()): Application {
            return ApplicationHolderImpl(previousInjector, allowDefaultDarkMode, modules).apply { run() }
        }
    }
}