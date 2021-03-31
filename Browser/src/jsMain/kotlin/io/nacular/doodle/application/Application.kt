package io.nacular.doodle.application

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.FontSerializerImpl
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
import io.nacular.doodle.dom.EventTarget
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
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.drawing.impl.RealGraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.RenderManagerImpl
import io.nacular.doodle.drawing.impl.TextFactoryImpl
import io.nacular.doodle.drawing.impl.TextMetricsImpl
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.NativeFocusManager
import io.nacular.doodle.focus.impl.FocusManagerImpl
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
import io.nacular.doodle.system.impl.PointerLocationResolver
import io.nacular.doodle.system.impl.PointerLocationResolverImpl
import io.nacular.doodle.time.Timer
import io.nacular.doodle.time.impl.PerformanceTimer
import io.nacular.doodle.utils.IdGenerator
import io.nacular.doodle.utils.SimpleIdGenerator
import kotlinx.browser.document
import kotlinx.browser.window
import org.kodein.di.Copy
import org.kodein.di.DI
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
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.events.FocusEvent
import kotlin.random.Random

/**
 * Created by Nicholas Eddy on 1/22/20.
 */
public inline fun <reified T: Application> application(
                 allowDefaultDarkMode: Boolean     = false,
                 modules             : List<Module> = emptyList(),
        noinline creator             : NoArgBindingDI<*>.() -> T): Application = createApplication(DI.direct {
    // FIXME: change when https://youtrack.jetbrains.com/issue/KT-39225 fixed
    bind<Application> { Singleton(scope, contextType, explicitContext, generic(), null, true, creator) } //singleton(creator = creator)
//    bind<Application>() with Singleton(scope, contextType, explicitContext, generic(), null, true, creator)
}, allowDefaultDarkMode, modules)

public inline fun <reified T: Application> application(
                 root                : HTMLElement,
                 allowDefaultDarkMode: Boolean     = false,
                 modules             : List<Module> = emptyList(),
        noinline creator             : NoArgBindingDI<*>.() -> T): Application = createApplication(DI.direct {
    // FIXME: change when https://youtrack.jetbrains.com/issue/KT-39225 fixed
    bind<Application> { Singleton(scope, contextType, explicitContext, generic(), null, true, creator) } //singleton(creator = creator)

//    bind<Application>() with Singleton(scope, contextType, explicitContext, generic(), null, true, creator)
}, root, allowDefaultDarkMode, modules)

public inline fun <reified T: Application> nestedApplication(
                 view                : ApplicationView,
                 root                : HTMLElement,
                 allowDefaultDarkMode: Boolean      = false,
                 modules             : List<Module> = emptyList(),
        noinline creator             : NoArgBindingDI<*>.() -> T): Application = createNestedApplication(view, DI.direct {
    // FIXME: change when https://youtrack.jetbrains.com/issue/KT-39225 fixed
    bind<Application> { Singleton(scope, contextType, explicitContext, generic(), null, true, creator) } //singleton(creator = creator)

//    bind<Application>() with Singleton(scope, contextType, explicitContext, generic(), null, true, creator) //singleton(creator = creator)
}, root, allowDefaultDarkMode, modules)

public fun createApplication(
        injector            : DirectDI,
        allowDefaultDarkMode: Boolean,
        modules             : List<Module>): Application = ApplicationHolderImpl(injector, allowDefaultDarkMode = allowDefaultDarkMode, modules = modules)

public fun createApplication(
        injector            : DirectDI,
        root                : HTMLElement,
        allowDefaultDarkMode: Boolean,
        modules             : List<Module>): Application = ApplicationHolderImpl(injector, root, allowDefaultDarkMode, modules)

public fun createNestedApplication(
        view                : ApplicationView,
        injector            : DirectDI,
        root                : HTMLElement,
        allowDefaultDarkMode: Boolean,
        modules             : List<Module>): Application = NestedApplicationHolder(view, injector, root, allowDefaultDarkMode, modules)

private class NestedPointerInputStrategy(private val view: ApplicationView, private val delegate: PointerInputServiceStrategy): PointerInputServiceStrategy by(delegate) {
    override fun startUp(handler: EventHandler) {
        // Provide an adapter to handle mapping pointer location correctly based on ApplicationView's orientation
        delegate.startUp(object: EventHandler {
            override fun handle(event: SystemPointerEvent) = handler.handle(
                SystemPointerEvent(
                    event.id,
                    event.type,
                    view.fromAbsolute(event.location),
                    event.buttons,
                    event.clickCount,
                    event.modifiers,
                    event.nativeScrollPanel
                )
            )
        })
    }
}

private class NestedApplicationHolder(
        view                : ApplicationView,
        previousInjector    : DirectDI,
        root                : HTMLElement,
        allowDefaultDarkMode: Boolean = false,
        modules             : List<Module> = emptyList()): ApplicationHolderImpl(previousInjector, root, allowDefaultDarkMode, modules, isNested = true) {

    init {
        (injector.instanceOrNull<PointerLocationResolver>() as? PointerLocationResolverImpl)?.let { it.nested = true } // TODO: Find better way to handle this
        injector.instanceOrNull<PointerInputServiceStrategy>()?.let {
            injector = DI.direct {
                extend(injector, copy = Copy.All)

                bindSingleton<PointerInputServiceStrategy>(overrides = true) { NestedPointerInputStrategy(view, it) }
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
                    previousInjector    : DirectDI,
        private val root                : HTMLElement,
                    allowDefaultDarkMode: Boolean      = false,
                    modules             : List<Module> = emptyList(),
        private val isNested            : Boolean      = false): Application {
    private var focusManager: FocusManager? = null

    private fun targetOutsideApp(target: EventTarget?) = target == null || (target is Node && !root.contains(target))

    private val onblur = { event: FocusEvent ->
        when {
            targetOutsideApp(event.relatedTarget) -> (focusManager as? FocusManagerImpl)?.enabled = false
        }
    }

    private val onfocus = { event: FocusEvent ->
        when {
            targetOutsideApp(event.relatedTarget) -> (focusManager as? FocusManagerImpl)?.enabled = true
        }
    }

    protected var injector = DI.direct {
        extend(previousInjector, copy = Copy.All)

        if (!isNested && root != document.body) {
            root.startMonitoringSize()
            root.tabIndex = 0
        }

        val idPrefix = when (root) {
            document.body -> ""
            else          -> root.id.takeIf { it.isNotBlank() } ?: Random.nextInt().toString()
        }

        bindInstance                                               { window }

        bindSingleton<Timer>                                       { PerformanceTimer          (window.performance                                                    ) }
        bindSingleton<Strand>                                      { StrandImpl                (instance(), instance()                                                ) }
        bindSingleton<Display>                                     { DisplayImpl               (instance(), instance(), root                                          ) }
        bindSingleton<Scheduler>                                   { SchedulerImpl             (instance(), instance()                                                ) }
        bindSingleton<SvgFactory>                                  { SvgFactoryImpl            (root, document                                                        ) }
        bindSingleton<IdGenerator>                                 { SimpleIdGenerator         (idPrefix                                                              ) }
        bindSingleton<HtmlFactory>                                 { HtmlFactoryImpl           (root, document                                                        ) }
        bindSingleton<TextFactory>                                 { TextFactoryImpl           (instance()                                                            ) }
        bindSingleton<TextMetrics>                                 { TextMetricsImpl           (instance(), instance(), instance(), instance(), cacheLength = 1000    ) }
        bindSingleton<ElementRuler>                                { ElementRulerImpl          (instance()                                                            ) }
        bindSingleton<SystemStyler>                                { SystemStylerImpl          (instance(), instance(), document, isNested, allowDefaultDarkMode      ) }
        bindSingleton<CanvasFactory>                               { CanvasFactoryImpl         (instance(), instance(), instance(), instance(), instance()            ) }
        bindSingleton<RenderManager>                               { RenderManagerImpl         (instance(), instance(), instanceOrNull(), instanceOrNull(), instance()) }
        bindSingleton<FontSerializer>                              { FontSerializerImpl        (instance()                                                            ) }
        bindSingleton<AnimationScheduler>                          { AnimationSchedulerImpl    (instance()                                                            ) } // FIXME: Provide fallback in case not supported
        bindSingleton<GraphicsDevice<RealGraphicsSurface>>         { RealGraphicsDevice        (instance()                                                            ) }
        bindSingleton<GraphicsSurfaceFactory<RealGraphicsSurface>> { RealGraphicsSurfaceFactory(instance(), instance()                                                ) }

        // TODO: Can this be handled better?
        bindSingleton<DisplayImpl>                                 { instance<Display>     () as DisplayImpl }
        bindSingleton<InternalDisplay>                             { instance<DisplayImpl> ()                }

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

        injector.instanceOrNull<PointerInputManager> ()
        injector.instanceOrNull<KeyboardFocusManager>()
        injector.instanceOrNull<DragManager>         ()

        initTask = injector.instance<Scheduler>().now {
            application = injector.instance()
        }

        root.onblur  = onblur
        root.onfocus = onfocus
        focusManager = injector.instanceOrNull()

        if (!isNested && root != document.body) {
            val nativeFocusManager = injector.instanceOrNull<NativeFocusManager>()

            focusManager?.let {
                it.focusChanged += { _: FocusManager, _: View?, new: View? ->
                    when {
                        new == null                                -> root.blur ()
                        nativeFocusManager?.hasFocusOwner == false -> root.focus()
                    }
                }.also { focusListener = it }
            }
        }

        if (root != document.body) {
            (focusManager as? FocusManagerImpl)?.enabled = false
        }
    }

    override fun shutdown() {
        if (isShutdown) {
            return
        }

        application?.shutdown()

        window.removeEventListener("unload", ::onUnload)

        mutations?.disconnect()

        initTask?.cancel()

        (injector.instance<Scheduler> () as? SchedulerImpl)?.shutdown()
        injector.instance<DisplayImpl>().shutdown()

        if (!isNested) {
            injector.instance<SystemStyler>().shutdown()
        }

        injector.instanceOrNull<DragManager>             ()?.shutdown()
        injector.instanceOrNull<PointerInputManager>     ()?.shutdown()
        injector.instanceOrNull<KeyboardFocusManager>    ()?.shutdown()
        injector.instanceOrNull<AccessibilityManagerImpl>()?.shutdown()

        root.onblur  = null
        root.onfocus = null

        if (!isNested && root != document.body) {
            root.stopMonitoringSize()

            focusManager?.let { focusManager ->
                focusListener?.let { focusManager.focusChanged -= it }
            }

            focusManager = null
        }

        injector = DI.direct {}

        isShutdown = true
    }

    private class AsyncAppWrapper(previousInjector: DirectDI, allowDefaultDarkMode: Boolean= false, modules: List<Module> = emptyList()): Application {
        private  var jobId : Int
        lateinit var holder: ApplicationHolderImpl

        init {
            jobId = window.setTimeout({
                holder = ApplicationHolderImpl(previousInjector, document.body!!, allowDefaultDarkMode, modules)
                holder.run()
            })
        }

        override fun shutdown() {
            when {
                ::holder.isInitialized -> holder.shutdown()
                else                   -> window.clearTimeout(jobId)
            }
        }
    }

    companion object {
        operator fun invoke(previousInjector: DirectDI, allowDefaultDarkMode: Boolean = false, modules: List<Module> = emptyList()): Application {
            return when (val body = document.body) {
                // This is the case when the Javascript is loaded in the document header
                null -> AsyncAppWrapper(previousInjector,       allowDefaultDarkMode, modules)
                else -> invoke         (previousInjector, body, allowDefaultDarkMode, modules)
            }
        }

        operator fun invoke(previousInjector    : DirectDI,
                            root                : HTMLElement,
                            allowDefaultDarkMode: Boolean      = false,
                            modules             : List<Module> = emptyList()): Application {
            return ApplicationHolderImpl(previousInjector, root, allowDefaultDarkMode, modules).apply { run() }
        }
    }
}