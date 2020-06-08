package io.nacular.doodle.application

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeCanvas
import org.kodein.di.Kodein.Module
import org.kodein.di.bindings.NoArgSimpleBindingKodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider

/**
 * Created by Nicholas Eddy on 1/30/20.
 */
class ApplicationViewFactory private constructor(val htmlFactory: HtmlFactory) {
    inline operator fun <reified T: Application> invoke(
            allowDefaultDarkMode: Boolean      = false,
            modules             : List<Module> = emptyList(),
            noinline creator    : NoArgSimpleBindingKodein<*>.() -> T): View = ApplicationView(htmlFactory) { view, root -> nestedApplication(view, root, allowDefaultDarkMode, modules, creator) }

    companion object {
        val AppViewModule = Module(allowSilentOverride = true, name = "ApplicationView") {
            bind<ApplicationViewFactory>() with provider { ApplicationViewFactory(instance()) }
        }
    }
}

class ApplicationView(htmlFactory: HtmlFactory, private val builder: (ApplicationView, HTMLElement) -> Application): View() {

    private val root = htmlFactory.create<HTMLElement>().apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
        tabIndex = 0 // FIXME: Move this functionality into custom KeyInputStrategy?
    }

    private var application = null as Application?
    private var firstRender = false

    init {
        boundsChanged += { _,old,new ->
            if (old.size != new.size) {
                // Send resize message to nested app since onresize won't be called automatically
                // in most browsers
                root.onresize?.let {
                    it(Event("onresize"))
                }
            }
        }

        focusChanged += { _,_,new ->
            when (new) {
                true -> root.focus()
                else -> root.blur ()
            }
        }
    }

    override fun addedToDisplay() {
        super.addedToDisplay()

        firstRender = true
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        application?.shutdown()
        application = null
    }

    override fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(root))

            if (firstRender) {
                application = builder(this, root)
                firstRender = false
            }
        }
    }
}