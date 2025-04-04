package io.nacular.doodle.application

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.WindowGroup
import io.nacular.doodle.core.WindowGroupImpl
import io.nacular.doodle.core.WindowImpl
import io.nacular.doodle.core.impl.DisplayFactory
import io.nacular.doodle.core.impl.DisplayFactoryImpl
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.deviceinput.KeyboardFocusManager
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.DesktopRenderManagerImpl
import io.nacular.doodle.drawing.impl.RealGraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.TextMetricsImpl
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Strand
import io.nacular.doodle.scheduler.impl.AnimationSchedulerImpl
import io.nacular.doodle.scheduler.impl.SchedulerImpl
import io.nacular.doodle.scheduler.impl.StrandImpl
import io.nacular.doodle.system.impl.DesktopPointerInputManagers
import io.nacular.doodle.theme.InternalThemeManager
import io.nacular.doodle.theme.Scene
import io.nacular.doodle.time.Timer
import io.nacular.doodle.time.impl.TimerImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.datetime.Clock
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontSlant.UPRIGHT
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.PathMeasure
import org.jetbrains.skia.paragraph.FontCollection
import org.kodein.di.Copy.All
import org.kodein.di.DI.Companion.direct
import org.kodein.di.DI.Module
import org.kodein.di.DirectDI
import org.kodein.di.bind
import org.kodein.di.bindFactory
import org.kodein.di.bindInstance
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.provider
import org.kodein.di.singleton
import java.awt.Toolkit
import javax.swing.UIManager

/**
 * Created by Nicholas Eddy on 5/14/21.
 */
public inline fun <reified T: Application> application(
        modules             : List<Module> = emptyList(),
        noinline creator    : NoArgBindingDI<*>.() -> T): Application = createApplication(direct {
    bind<Application> { singleton(creator = creator) }
}, modules)

/** @suppress */
@Internal
public fun createApplication(
        injector            : DirectDI,
        modules             : List<Module>
): Application = ApplicationHolderImpl(injector, modules = modules)

private open class ApplicationHolderImpl protected constructor(
    previousInjector    : DirectDI,
    modules             : List<Module> = emptyList()
): Application {

    private inner class ShutdownHook: Thread() {
        override fun run() {
            appScope.launch(Dispatchers.Swing) {
                shutdown()
            }
        }
    }

    init {
        // Better match Mac OS appearance
        System.setProperty("apple.awt.application.appearance", "system")

        // Set Skiko properties before any Skiko components are initialized
        System.setProperty("skiko.rendering.laf.global",        "true" )
        System.setProperty("skiko.rendering.useScreenMenuBar",  "true" )
        System.setProperty("skiko.linux.autodpi",               "true" )
//        System.setProperty("skiko.vsync.enabled",               "false")
//        System.setProperty("skiko.fps.enabled",                 "true" )

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        Runtime.getRuntime().addShutdownHook(ShutdownHook())
    }

    private val defaultFontName = UIManager.getDefaults().getFont("defaultFont")?.fontName ?: "Courier"

    private val appScope       = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val fontManager    = FontMgr.default
    private val defaultFont    = Font(fontManager.matchFamilyStyle(defaultFontName, FontStyle(300, 5, UPRIGHT)), 13f)
    private val fontCollection = FontCollection().apply {
        setDefaultFontManager(FontMgr.default)
    }

    private var injector = direct {
        extend(previousInjector, copy = All)

        bindInstance                      { appScope          }
        bindInstance                      { Clock.System      }
        bindInstance<CoroutineDispatcher> { Dispatchers.Swing }

        bindInstance                      { defaultFont    }
        bindInstance                      { fontManager    }
        bindInstance                      { fontCollection }
        bindFactory<Unit, PathMeasure>    { PathMeasure()  }

        bind<Timer>             () with singleton { TimerImpl             (instance()                        ) }
        bind<Scene>             () with singleton { MultiDisplayScene     (provider()                        ) }
        bind<Strand>            () with singleton { StrandImpl            (instance(), instance()            ) }
        bind<Scheduler>         () with singleton { SchedulerImpl         (instance(), instance()            ) }
        bind<TextMetrics>       () with singleton { TextMetricsImpl       (instance(), instance()            ) }
        bind<DisplayFactory>    () with singleton { DisplayFactoryImpl() }
        bind<AnimationScheduler>() with singleton { AnimationSchedulerImpl(instance(), instance(), instance()) }

        bind<WindowGroup>() with singleton {
            WindowGroupImpl { undecorated ->
                WindowImpl(
                    appScope             = instance(),
                    defaultFont          = instance(),
                    uiDispatcher         = instance(),
                    fontCollection       = instance(),
                    graphicsDevices      = { parent ->
                        RealGraphicsDevice(RealGraphicsSurfaceFactory(
                            parent          = parent,
                            defaultFont     = instance(),
                            fontCollection  = instance(),
                        ))
                    },
                    renderManagers       = { display ->
                        DesktopRenderManagerImpl(display, instance(), instanceOrNull(), instanceOrNull())
                    },
                    undecorated          = undecorated,
                    size                 = Toolkit.getDefaultToolkit().screenSize.run { Size(width, height) },
                    accessibilityManager = { instanceOrNull() },
                    displays             = instance(),
                )
            }
        }

        bindSingleton { instance<WindowGroup>().main.display }

        bindProvider<WindowGroupImpl> { instance<WindowGroup>() as WindowGroupImpl }

        // TODO: Can this be handled better?
        bindSingleton { instance<AnimationScheduler>() as AnimationSchedulerImpl }
        bindSingleton { instance<TextMetrics>       () as TextMetricsImpl        }

        modules.forEach {
            import(it, allowOverride = true)
        }
    }

    private var isShutdown  = false
    private var application = null as Application?

    protected fun run() {
        injector.instanceOrNull<DesktopPointerInputManagers>()
        injector.instanceOrNull<KeyboardFocusManager>       ()
        injector.instanceOrNull<DragManager>                ()
        injector.instanceOrNull<WindowGroupImpl>()?.also {
            injector.instanceOrNull<InternalThemeManager>()

            it.start()
        }

        application = injector.instance()
    }

    override fun shutdown() {
        if (isShutdown) {
            return
        }

        application?.shutdown()

        injector.instanceOrNull<WindowGroupImpl>            ()?.shutdown()
        (injector.instance<Scheduler>                       () as? SchedulerImpl)?.shutdown()
        injector.instance<AnimationSchedulerImpl>           ().shutdown()
        injector.instanceOrNull<DragManager>                ()?.shutdown()
        injector.instanceOrNull<DesktopPointerInputManagers>()?.shutdown()
        injector.instanceOrNull<KeyboardFocusManager>       ()?.shutdown()

        injector = direct {}

        isShutdown = true
    }

    companion object {
        operator fun invoke(previousInjector: DirectDI, modules: List<Module> = emptyList()) = ApplicationHolderImpl(
            previousInjector,
            modules
        ).apply {
            appScope.launch(Dispatchers.Swing) {
                run()
            }
        }
    }
}