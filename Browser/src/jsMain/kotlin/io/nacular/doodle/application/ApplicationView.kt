package io.nacular.doodle.application

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.setHeight
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeCanvas
import io.nacular.doodle.focus.NativeFocusManager
import org.kodein.di.DI.Module
import org.kodein.di.bindProvider
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

/**
 * Created by Nicholas Eddy on 1/30/20.
 */
public class ApplicationViewFactory private constructor(public val htmlFactory: HtmlFactory, public val nativeFocusManager: NativeFocusManager?) {
    public inline operator fun <reified T: Application> invoke(
            allowDefaultDarkMode: Boolean      = false,
            modules             : List<Module> = emptyList(),
            noinline creator    : NoArgBindingDI<*>.() -> T
    ): View = ApplicationView(htmlFactory, nativeFocusManager) { view, root -> nestedApplication(view, root, allowDefaultDarkMode, modules, creator) }

    public companion object {
        public val AppViewModule: Module = Module(allowSilentOverride = true, name = "ApplicationView") {
            bindProvider { ApplicationViewFactory(instance(), instanceOrNull()) }
        }
    }
}

public class ApplicationView(htmlFactory: HtmlFactory, private val nativeFocusManager: NativeFocusManager?, private val builder: (ApplicationView, HTMLElement) -> Application): View() {

    private val root = htmlFactory.create<HTMLElement>().apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
        tabIndex = 0 // FIXME: Move this functionality into custom KeyInputStrategy?
    }

    private var application   = null as Application?
    private var initialRender = false

    init {
        boundsChanged += { _,old,new ->
            if (old.size != new.size) {
                if (new.width != old.width) {
                    root.style.setWidth(new.width)
                }

                if (new.height != old.height) {
                    root.style.setHeight(new.height)
                }

                // Send resize message to nested app since onresize won't be called automatically
                // in most browsers
                root.onresize?.let {
                    it(Event("onresize"))
                }
            }
        }

        focusChanged += { _,_,new ->
            when (new) {
                true -> if(nativeFocusManager?.hasFocusOwner == false) root.focus()
                else -> root.blur ()
            }
        }
    }

    override fun addedToDisplay() {
        super.addedToDisplay()

        initialRender = true
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        application?.shutdown()
        application = null
    }

    override fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(root))

            if (initialRender) {
                application = builder(this, root)
                initialRender = false
            }
        }
    }
}