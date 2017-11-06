package com.nectar.doodle.application

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.erased.bind
import com.github.salomonbrys.kodein.erased.instance
import com.github.salomonbrys.kodein.erased.singleton
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.impl.DisplayImpl
import com.nectar.doodle.deviceinput.MouseInputManager
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.HtmlFactoryImpl
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
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.impl.SchedulerImpl
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.impl.MouseInputServiceImpl
import com.nectar.doodle.system.impl.MouseInputServiceStrategy
import com.nectar.doodle.system.impl.MouseInputServiceStrategyWebkit
import com.nectar.doodle.ui.UIManager
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
abstract class Application {
    abstract fun run(display: Display)

    val kodein = Kodein {
        bind<UIManager>                () with instance  ( DummyUIManager     )
        bind<SystemStyler>             () with instance  ( SystemStylerImpl() )

        bind<Display>                  () with singleton { DisplayImpl                    (instance(), document.body!!                   ) }
        bind<Scheduler>                () with singleton { SchedulerImpl                  (                                              ) }
        bind<HtmlFactory>              () with singleton { HtmlFactoryImpl                (                                              ) }
        bind<TextFactory>              () with singleton { TextFactoryImpl(instance()) }
        bind<CanvasFactory>            () with singleton { CanvasFactoryImpl(instance(), instance()) }
        bind<RenderManager>            () with singleton { RenderManagerImpl(instance(), instance(), instance(), instance()) }
        bind<GraphicsDevice<*>>        () with singleton { RealGraphicsDevice(instance()) }
        bind<MouseInputService>        () with singleton { MouseInputServiceImpl          (instance()                                    ) }
        bind<MouseInputManager>        () with singleton { MouseInputManager              (instance(), instance()                        ) }
        bind<GraphicsSurfaceFactory<*>>() with singleton { RealGraphicsSurfaceFactory     (instance(), instance()                        ) }
        bind<MouseInputServiceStrategy>() with singleton { MouseInputServiceStrategyWebkit(instance()                                    ) }
    }

    init {
        kodein.instance<SystemStyler>     ()
        kodein.instance<RenderManager>    ()
        kodein.instance<MouseInputManager>()

        run(kodein.instance())
    }
}

private object DummyUIManager: UIManager {
    override val installedTheme get() = TODO("not implemented")
    override val availableThemes get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun installUI(gizmo: Gizmo, aResponse: UIManager.UIResponse) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override fun revalidateUI(gizmo: Gizmo) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun installTheme(theme: UIManager.ThemeInfo) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override fun <T> getDefaultValue(aName: String) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
