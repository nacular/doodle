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

/**
 * Created by Nicholas Eddy on 1/30/20.
 */
class ApplicationView(htmlFactory: HtmlFactory, private val builder: (ApplicationView, HTMLElement) -> ApplicationHolder): View() {

    private val root = htmlFactory.create<HTMLElement>().apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
    }

    private var application = null as ApplicationHolder?

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

    companion object {
//        inline operator fun <reified T: Application> invoke(
//                htmlFactory         : HtmlFactory,
//                allowDefaultDarkMode: Boolean     = false,
//                modules             : Set<Module> = emptySet(),
//                noinline creator    : NoArgSimpleBindingKodein<*>.() -> T) = ApplicationView(htmlFactory) { root -> application(root, allowDefaultDarkMode, modules, creator) }

        inline operator fun <reified T: Application> invoke(
                htmlFactory         : HtmlFactory,
                allowDefaultDarkMode: Boolean     = false,
                modules             : Set<Module> = emptySet(),
                noinline creator    : NoArgSimpleBindingKodein<*>.() -> T) = ApplicationView(htmlFactory) { view, root -> nestedApplication(view, root, allowDefaultDarkMode, modules, creator) }
    }
}