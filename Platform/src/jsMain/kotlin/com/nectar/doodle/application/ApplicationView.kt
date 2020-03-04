package com.nectar.doodle.application

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.CanvasImpl
import org.kodein.di.Kodein.Module
import org.kodein.di.bindings.NoArgSimpleBindingKodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider

/**
 * Created by Nicholas Eddy on 1/30/20.
 */
class ApplicationView(htmlFactory: HtmlFactory, private val builder: (ApplicationView, HTMLElement) -> Application): View() {

    private val root = htmlFactory.create<HTMLElement>().apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
    }

    private var application = null as Application?

    override fun addedToDisplay() {
        super.addedToDisplay()

        application = builder(this, root)
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        application?.shutdown()
        application = null
    }

    override fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {
            canvas.addData(listOf(root))
        }
    }
}

class ApplicationViewFactory private constructor(val htmlFactory: HtmlFactory) {
    inline operator fun <reified T: Application> invoke(
            allowDefaultDarkMode: Boolean     = false,
            modules             : Set<Module> = emptySet(),
            noinline creator    : NoArgSimpleBindingKodein<*>.() -> T) = ApplicationView(htmlFactory) { view, root -> nestedApplication(view, root, allowDefaultDarkMode, modules, creator) }

    companion object {
        val appViewModule = Module(allowSilentOverride = true, name = "ApplicationView") {
            bind<ApplicationViewFactory>() with provider { ApplicationViewFactory(instance()) }
        }
    }
}