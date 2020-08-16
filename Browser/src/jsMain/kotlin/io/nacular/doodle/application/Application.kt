package io.nacular.doodle.application

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.accessibility.AccessibilityManagerImpl
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.core.impl.DisplayImpl
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.deviceinput.KeyboardFocusManager
import io.nacular.doodle.deviceinput.PointerInputManager
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.SvgFactoryImpl
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.dom.SystemStylerImpl
import io.nacular.doodle.dom.impl.ElementRulerImpl
import io.nacular.doodle.dom.impl.HtmlFactoryImpl
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.CanvasFactoryImpl
import io.nacular.doodle.drawing.impl.GraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.RealGraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.RenderManagerImpl
import io.nacular.doodle.drawing.impl.TextFactoryImpl
import io.nacular.doodle.drawing.impl.TextMetricsImpl
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.NativeFocusManager
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Strand
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.scheduler.impl.AnimationSchedulerImpl
import io.nacular.doodle.scheduler.impl.SchedulerImpl
import io.nacular.doodle.scheduler.impl.StrandImpl
import io.nacular.doodle.startMonitoringSize
import io.nacular.doodle.stopMonitoringSize
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.impl.PointerInputServiceStrategy
import io.nacular.doodle.system.impl.PointerInputServiceStrategy.EventHandler
import io.nacular.doodle.time.Timer
import io.nacular.doodle.time.impl.PerformanceTimer
import org.kodein.di.Copy
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.Kodein.Module
import org.kodein.di.bindings.NoArgSimpleBindingKodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.instanceOrNull
import org.kodein.di.erased.singleton
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.Window
import org.w3c.dom.asList
import kotlin.browser.document
import kotlin.browser.window

/**
 * Created by Nicholas Eddy on 1/22/20.
 */
inline fun <reified T: Application> application(
                 root                : HTMLElement = document.body!!,
                 allowDefaultDarkMode: Boolean     = false,
                 modules             : List<Module> = emptyList(),
        noinline creator             : NoArgSimpleBindingKodein<*>.() -> T): Application = createApplication(Kodein.direct {
    bind<Application>() with singleton(creator = creator)
}, root, allowDefaultDarkMode, modules)

inline fun <reified T: Application> nestedApplication(
                 view                : ApplicationView,
                 root                : HTMLElement  = document.body!!,
                 allowDefaultDarkMode: Boolean      = false,
                 modules             : List<Module> = emptyList(),
        noinline creator             : NoArgSimpleBindingKodein<*>.() -> T): Application = createNestedApplication(view, Kodein.direct {
    bind<Application>() with singleton(creator = creator)
}, root, allowDefaultDarkMode, modules)

fun createApplication(
        injector            : DKodein,
        root                : HTMLElement,
        allowDefaultDarkMode: Boolean,
        modules             : List<Module>): Application = ApplicationHolderImpl(injector, root, allowDefaultDarkMode, modules)

fun createNestedApplication(
        view                : ApplicationView,
        injector            : DKodein,
        root                : HTMLElement,
        allowDefaultDarkMode: Boolean,
        modules             : List<Module>): Application = NestedApplicationHolder(view, injector, root, allowDefaultDarkMode, modules)

private class NestedPointerInputStrategy(private val view: ApplicationView, private val delegate: PointerInputServiceStrategy): PointerInputServiceStrategy by(delegate) {
    override fun startUp(handler: EventHandler) {
        // Provide an adapter to handle mapping pointer location correctly based on ApplicationView's orientation
        delegate.startUp(object: EventHandler {
            override fun handle(event: SystemPointerEvent) {
                handler.handle(SystemPointerEvent(
                        event.type,
                        view.fromAbsolute(pointerLocation),
                        event.buttons,
                        event.clickCount,
                        event.modifiers,
                        event.nativeScrollPanel))
            }
        })
    }
}

private class NestedApplicationHolder(
        view                : ApplicationView,
        previousInjector    : DKodein,
        root                : HTMLElement = document.body!!,
        allowDefaultDarkMode: Boolean = false,
        modules             : List<Module> = emptyList()): ApplicationHolderImpl(previousInjector, root, allowDefaultDarkMode, modules, isNested = true) {

    init {
        injector.instanceOrNull<PointerInputServiceStrategy>()?.let {
            // TODO: Find better way to handle this
            it.nested = true
            injector = Kodein.direct {
                extend(injector, copy = Copy.All)

                bind<PointerInputServiceStrategy>(overrides = true) with singleton { NestedPointerInputStrategy(view, it) }
            }
        }

        val display = injector.instance<Display>()

        display.contentDirection = view.contentDirection

        view.contentDirectionChanged += {
            display.contentDirection = view.contentDirection
        }

        run()
    }
}

private open class ApplicationHolderImpl protected constructor(
                    previousInjector    : DKodein,
        private val root                : HTMLElement  = document.body!!,
                    allowDefaultDarkMode: Boolean      = false,
                    modules             : List<Module> = emptyList(),
        private val isNested            : Boolean      = false): Application {
    protected var injector = Kodein.direct {
        extend(previousInjector, copy = Copy.All)

        if (!isNested && root != document.body) {
            root.startMonitoringSize()
            root.tabIndex = 0
        }

        bind<Window>                   () with instance  ( window )

        bind<Timer>                    () with singleton { PerformanceTimer          (window.performance                                                    ) }
        bind<Strand>                   () with singleton { StrandImpl                (instance(), instance()                                                ) }
        bind<Display>                  () with singleton { DisplayImpl               (instance(), instance(), root                                          ) }
        bind<Scheduler>                () with singleton { SchedulerImpl             (instance(), instance()                                                ) }
        bind<SvgFactory>               () with singleton { SvgFactoryImpl            (root, document                                                        ) }
        bind<HtmlFactory>              () with singleton { HtmlFactoryImpl           (root, document                                                        ) }
        bind<TextFactory>              () with singleton { TextFactoryImpl           (instance()                                                            ) }
        bind<TextMetrics>              () with singleton { TextMetricsImpl           (instance(), instance(), instance()                                    ) }
        bind<ElementRuler>             () with singleton { ElementRulerImpl          (instance()                                                            ) }
        if (!isNested) {
            bind<SystemStyler>() with singleton { SystemStylerImpl(instance(), document, allowDefaultDarkMode) }
        }
        bind<CanvasFactory>            () with singleton { CanvasFactoryImpl         (instance(), instance(), instance(), instance()                        ) }
        bind<RenderManager>            () with singleton { RenderManagerImpl         (instance(), instance(), instanceOrNull(), instanceOrNull(), instance()) }
        bind<GraphicsDevice<*>>        () with singleton { RealGraphicsDevice        (instance()                                                            ) }
        bind<AnimationScheduler>       () with singleton { AnimationSchedulerImpl    (instance()                                                            ) } // FIXME: Provide fallback in case not supported
        bind<GraphicsSurfaceFactory<*>>() with singleton { RealGraphicsSurfaceFactory(instance(), instance()                                                ) }

        // TODO: Can this be handled better?
        bind<DisplayImpl>              () with singleton { instance<Display>     () as DisplayImpl }
        bind<InternalDisplay>          () with singleton { instance<DisplayImpl> ()                }

        modules.forEach {
            import(it, allowOverride = true)
        }
    }

    private var initTask    = null as Task?
    private var isShutdown  = false
    private var application = null as Application?

    private fun onUnload(@Suppress("UNUSED_PARAMETER") event: Event? = null) {
        shutdown()
    }

    private var mutations: MutationObserver? = null

    private var focusListener: ((FocusManager, View?, View?) -> Unit)? = null

    protected fun run() {
        window.addEventListener("unload", ::onUnload)

        root.parentNode?.let { parent ->
            mutations = MutationObserver { mutations, _ ->
                mutations.flatMap { it.removedNodes.asList() }.firstOrNull { root == it }?.let {
                    shutdown()
                }
            }.apply {
                observe(parent, object: MutationObserverInit {
                    override var childList: Boolean? = true
                })
            }
        }

        // Initialize framework components
        if (!isNested) {
            injector.instance<SystemStyler>()
        }
        injector.instance<RenderManager>()

        injector.instanceOrNull<PointerInputManager>   ()
        injector.instanceOrNull<KeyboardFocusManager>()
        injector.instanceOrNull<DragManager>         ()

        initTask = injector.instance<Scheduler>().now {
            application = injector.instance()
        }

        if (!isNested && root != document.body) {
            val nativeFocusManager = injector.instanceOrNull<NativeFocusManager>()

            injector.instanceOrNull<FocusManager>()?.let {
                it.focusChanged += { _: FocusManager, _: View?, new: View? ->
                    when {
                        new == null                                -> root.blur ()
                        nativeFocusManager?.hasFocusOwner == false -> root.focus()
                    }
                }.also { focusListener = it }
            }
        }
    }

    override fun shutdown() {
        if (isShutdown) {
            return
        }

        window.removeEventListener("unload", ::onUnload)

        mutations?.disconnect()

        initTask?.cancel()

        injector.instance<DisplayImpl>().shutdown()

        if (!isNested) {
            injector.instance<SystemStyler>().shutdown()
        }

        injector.instanceOrNull<DragManager>             ()?.shutdown()
        injector.instanceOrNull<PointerInputManager>     ()?.shutdown()
        injector.instanceOrNull<KeyboardFocusManager>    ()?.shutdown()
        injector.instanceOrNull<AccessibilityManagerImpl>()?.shutdown()

        if (!isNested && root != document.body) {
            root.stopMonitoringSize()

            injector.instanceOrNull<FocusManager>()?.let { focusManager ->
                focusListener?.let { focusManager.focusChanged -= it }
            }
        }

        application?.shutdown()

        injector = Kodein.direct {}

        isShutdown = true
    }

    companion object {
        operator fun invoke(previousInjector    : DKodein,
                            root                : HTMLElement  = document.body!!,
                            allowDefaultDarkMode: Boolean      = false,
                            modules             : List<Module> = emptyList()): ApplicationHolderImpl {
            return ApplicationHolderImpl(previousInjector, root, allowDefaultDarkMode, modules).apply { run() }
        }
    }
}