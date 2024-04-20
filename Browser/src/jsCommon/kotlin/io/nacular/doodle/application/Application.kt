package io.nacular.doodle.application

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.FontSerializerImpl
import io.nacular.doodle.accessibility.AccessibilityManagerImpl
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.core.impl.DisplayImpl
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.deviceinput.KeyboardFocusManager
import io.nacular.doodle.deviceinput.PointerInputManager
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.dom.FocusEvent
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.MutationObserver
import io.nacular.doodle.dom.MutationObserverConfig
import io.nacular.doodle.dom.Node
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.dom.SystemStylerImpl
import io.nacular.doodle.dom.defaultFontSize
import io.nacular.doodle.dom.document
import io.nacular.doodle.dom.get
import io.nacular.doodle.dom.impl.ElementRulerImpl
import io.nacular.doodle.dom.impl.HtmlFactoryImpl
import io.nacular.doodle.dom.impl.SvgFactoryImpl
import io.nacular.doodle.dom.jsArrayOf
import io.nacular.doodle.dom.startMonitoringSize
import io.nacular.doodle.dom.startObserve
import io.nacular.doodle.dom.stopMonitoringSize
import io.nacular.doodle.dom.toJsString
import io.nacular.doodle.dom.window
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
import io.nacular.doodle.drawing.impl.TextVerticalAligner
import io.nacular.doodle.drawing.impl.TextVerticalAlignerImpl
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
import io.nacular.doodle.system.impl.PointerLocationResolver
import io.nacular.doodle.system.impl.PointerLocationResolverImpl
import io.nacular.doodle.theme.Scene
import io.nacular.doodle.theme.SingleDisplayScene
import io.nacular.doodle.time.Timer
import io.nacular.doodle.time.impl.PerformanceTimer
import io.nacular.doodle.utils.IdGenerator
import io.nacular.doodle.utils.SimpleIdGenerator
import io.nacular.doodle.utils.observable
import org.kodein.di.Copy
import org.kodein.di.DI
import org.kodein.di.DI.Module
import org.kodein.di.DirectDI
import org.kodein.di.bind
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.singleton
import kotlin.random.Random

/**
 * Launches a top-level application (full-screen) with the given [modules]. The set of modules configure which types are
 * available for injection to the app. Modules also control some features like Pointer and Keyboard
 * support.
 *
 * @param allowDefaultDarkMode controls whether the Browser tries to provide dark vs light styles
 * @param modules to use for the application
 * @param creator block that constructs the application
 */
public inline fun <reified T: Application> application(
                 allowDefaultDarkMode: Boolean     = false,
                 modules             : List<Module> = emptyList(),
        noinline creator             : NoArgBindingDI<*>.() -> T): Application = createApplication(DI.direct {
    bind<Application> { singleton(creator = creator) }
}, allowDefaultDarkMode, modules)

/**
 * Launches an application nested within [root] with the given [modules]. The set of modules configure which types are
 * available for injection to the app. Modules also control some features like Pointer and Keyboard
 * support.
 *
 * @param root element where the application will be hosted
 * @param allowDefaultDarkMode controls whether the Browser tries to provide dark vs light styles
 * @param modules to use for the application
 * @param creator block that constructs the application
 */
/** @suppress */
@Internal
public inline fun <reified T: Application> application(
             root                : Any,
             allowDefaultDarkMode: Boolean      = false,
             modules             : List<Module> = emptyList(),
    noinline creator             : NoArgBindingDI<*>.() -> T): Application = createApplication(DI.direct {
    bind<Application> { singleton(creator = creator) }
}, root, allowDefaultDarkMode, modules)

/** @suppress */
@Internal
public inline fun <reified T: Application> nestedApplication(
             view                : ApplicationView,
             root                : Any,
             allowDefaultDarkMode: Boolean      = false,
             modules             : List<Module> = emptyList(),
    noinline creator             : NoArgBindingDI<*>.() -> T): Application = createNestedApplication(view, DI.direct {
    bind<Application> { singleton(creator = creator) }
}, root, allowDefaultDarkMode, modules)

/** @suppress */
@Internal
public fun createApplication(
        injector            : DirectDI,
        allowDefaultDarkMode: Boolean,
        modules             : List<Module>): Application = ApplicationHolderImpl(injector, allowDefaultDarkMode = allowDefaultDarkMode, modules = modules)

/** @suppress */
@Internal
public fun createApplication(
    injector            : DirectDI,
    root                : Any,
    allowDefaultDarkMode: Boolean,
    modules             : List<Module>): Application = ApplicationHolderImpl(injector, root as HTMLElement, allowDefaultDarkMode, modules)

/** @suppress */
@Internal
public fun createNestedApplication(
    view                : ApplicationView,
    injector            : DirectDI,
    root                : Any,
    allowDefaultDarkMode: Boolean,
    modules             : List<Module>): Application = NestedApplicationHolder(view, injector, root as HTMLElement, allowDefaultDarkMode, modules)

private class NestedApplicationHolder(
    view                : ApplicationView,
    previousInjector    : DirectDI,
    root                : HTMLElement,
    allowDefaultDarkMode: Boolean = false,
    modules             : List<Module> = emptyList()): ApplicationHolderImpl(previousInjector, root, allowDefaultDarkMode, modules, isNested = true) {

    init {
        (injector.instanceOrNull<PointerLocationResolver>() as? PointerLocationResolverImpl)?.also { it.owner = view } // TODO: Find better way to handle this

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

    private var focusManager        : FocusManager? = null

    private fun targetOutsideApp(target: EventTarget?) = target == null || (target is Node && !root.contains(target))

    private val onblur = { event: Event ->
        when {
            // For some reason an Event is given for this when registered to document.body,
            // so treat that case a window focus loss by default.
            event is FocusEvent -> if (targetOutsideApp(event.relatedTarget)) (focusManager as? FocusManagerImpl)?.enabled = false
            else                ->  (focusManager as? FocusManagerImpl)?.enabled = false
        }
    }

    private val onfocus = { event: Event ->
        when {
            // For some reason an Event is given for this when registered to document.body,
            // so treat that case a window focus gain by default.
            event is FocusEvent -> if (targetOutsideApp(event.relatedTarget)) (focusManager as? FocusManagerImpl)?.enabled = true
            else                -> (focusManager as? FocusManagerImpl)?.enabled = true
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
            else          -> root.id.takeIf { it.isNotBlank() } ?: "${Random.nextInt()}"
        }

        val userAgent = window.navigator.userAgent
        val isSafari  = "Safari" in userAgent && "Chrome" !in userAgent

        bind<Timer>                                      () with singleton { PerformanceTimer          (window.performance                                                    ) }
        bind<Scene>                                      () with singleton { SingleDisplayScene        (instance()                                                            ) }
        bind<Strand>                                     () with singleton { StrandImpl                (instance(), instance()                                                ) }
        bind<Display>                                    () with singleton { DisplayImpl               (instance(), instance(), root                                          ) }
        bind<Scheduler>                                  () with singleton { SchedulerImpl             (window, instance()                                                    ) }
        bind<SvgFactory>                                 () with singleton { SvgFactoryImpl            (root, document                                                        ) }
        bind<IdGenerator>                                () with singleton { SimpleIdGenerator         (idPrefix                                                              ) }
        bind<HtmlFactory>                                () with singleton { HtmlFactoryImpl           (root, document                                                        ) }
        bind<TextFactory>                                () with singleton { TextFactoryImpl           (instance()                                                            ) }
        bind<TextMetrics>                                () with singleton { TextMetricsImpl           (instance(), instance(), instance(), instance(), cacheLength = 1000    ) }
        bind<ElementRuler>                               () with singleton { ElementRulerImpl          (instance()                                                            ) }
        bind<SystemStyler>                               () with singleton { SystemStylerImpl          (instance(), instance(), document, isNested, allowDefaultDarkMode      ) }
        bind<CanvasFactory>                              () with singleton { CanvasFactoryImpl         (instance(), instance(), instance(), instance(), instance(), instance(), isSafari  ) }
        bind<RenderManager>                              () with singleton { RenderManagerImpl         (instance(), instance(), instanceOrNull(), instanceOrNull(), instance()) }
        bind<FontSerializer>                             () with singleton { FontSerializerImpl        (instance()                                                            ) }
        bind<AnimationScheduler>                         () with singleton { AnimationSchedulerImpl    (window                                                                ) } // FIXME: Provide fallback in case not supported
        bind<GraphicsDevice<RealGraphicsSurface>>        () with singleton { RealGraphicsDevice        (instance()                                                            ) }
        bind<GraphicsSurfaceFactory<RealGraphicsSurface>>() with singleton { RealGraphicsSurfaceFactory(instance(), instance(), instance()                                    ) }
        bind<TextVerticalAligner>                        () with singleton { TextVerticalAlignerImpl   (defaultFontSize, instance(), instance(), cacheLength = 100) }

        // TODO: Can this be handled better?
        bind<DisplayImpl>     () with singleton { instance<Display>     () as DisplayImpl      }
        bind<InternalDisplay> () with singleton { instance<DisplayImpl> ()                     }
        bind<TextFactoryImpl> () with singleton { instance<TextFactory> () as TextFactoryImpl  }
        bind<ElementRulerImpl>() with singleton { instance<ElementRuler>() as ElementRulerImpl }

        importAll(modules, allowOverride = true)
    }

    private var initTask    = null as Task?
    private var isShutdown  = false
    private var application = null as Application?

    private fun onPageHide() {
        shutdown()
    }

    private var mutations: MutationObserver? = null

    private var focusListener: ((FocusManager, View?, View?) -> Unit)? = null

    protected fun run() {
        window.addEventListener("pagehide", ::onPageHide)

        root.parentNode?.let { parent ->
            mutations = MutationObserver { mutations ->
                (0 until mutations.length).forEach { mutationIndex ->
                    mutations[mutationIndex]?.let { record ->
                        if (record.attributeName == DIR) {
                            injector.instance<DisplayImpl>().updateContentDirection()
                        }

                        (0 until record.removedNodes.length).forEach {
                            if (record.removedNodes[it] == root) {
                                shutdown()
                            }
                        }
                    }
                }

            }.apply {
                startObserve(parent, MutationObserverConfig(
                    childList = true
                ))

                startObserve(root, MutationObserverConfig(
                    attributeFilter = jsArrayOf(DIR.toJsString())
                ))
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

        window.removeEventListener("unload", ::onPageHide)

        mutations?.disconnect()

        initTask?.cancel()

        if (!isNested) {
            injector.instance<SystemStyler>().shutdown()
        }

        (injector.instance<Scheduler> () as? SchedulerImpl)?.shutdown()
        injector.instance<DisplayImpl>().shutdown()
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

    private class AsyncAppWrapper(previousInjector: DirectDI, allowDefaultDarkMode: Boolean = false, modules: List<Module> = emptyList()):
        Application {
        private  var jobId : Int? by observable(null) {old,_ ->
            old?.let { window.clearTimeout(it) }
        }

        lateinit var holder: ApplicationHolderImpl

        init {
            start(previousInjector, allowDefaultDarkMode, modules)
        }

        private fun start(previousInjector: DirectDI, allowDefaultDarkMode: Boolean, modules: List<Module>) {
            when {
                document.body != null -> {
                    holder = ApplicationHolderImpl(previousInjector, document.body!!, allowDefaultDarkMode, modules)
                    holder.run()
                }
                else                  -> jobId = window.setTimeout({ start(previousInjector, allowDefaultDarkMode, modules) }, 0)
            }
        }

        override fun shutdown() {
            when {
                ::holder.isInitialized -> holder.shutdown()
                else                   -> jobId?.let { window.clearTimeout(it) }
            }
        }
    }

    companion object {
        private const val DIR = "dir"

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