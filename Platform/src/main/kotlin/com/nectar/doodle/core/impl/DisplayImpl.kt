package com.nectar.doodle.core.impl

import com.nectar.doodle.core.Container
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.height
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.setBackgroundColor
import com.nectar.doodle.dom.setBackgroundImage
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.dom.width
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.SolidBrush
import com.nectar.doodle.drawing.TextureBrush
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.max


class DisplayImpl(private val htmlFactory: HtmlFactory, private val rootElement: HTMLElement): Display {
    private fun onResize(event: Event? = null) {
        root.minimumSize.let {
            root.size = Size(max(rootElement.width, it.width), max(rootElement.height, it.height))
        }
    }

    private val root         = Container()
    private val canvasElement: HTMLElement = htmlFactory.create("b")
//        private val sStyles = GWT.create(GraphicsStyles::class.java)
//        private val sCanvas = GraphicsService.locator().getGraphicsDevice().create(canvasElement).getCanvas()

    init {
//            sStyles.init()

        rootElement.onresize = this::onResize

        onResize()

        root.isFocusCycleRoot = true

        canvasElement.style.setWidthPercent (100.0)
        canvasElement.style.setHeightPercent(100.0)

        root.boundsChange += { gizmo, old, new ->
            if (old.size != new.size) {
                (sizeChange as PropertyObserversImpl<Gizmo, Size>).forEach {
                    it(gizmo, old.size, new.size)
                }
            }
        }
    }


    override var cursor: Cursor?
        get(   ) = root.cursor
        set(new) { root.cursor = new }

    val width  get() = size.width
    val height get() = size.height

    override val size = root.size

    override var insets: Insets
        get(   ) = root.insets
        set(new) { root.insets = new }

    override var minimumSize: Size
        get(   ) = root.minimumSize
        set(new) { root.minimumSize = new }

    override var layout: Layout?
        get(   ) = root.layout
        set(new) { root.layout = new }

    override val children get() = root.children

    override val sizeChange: PropertyObservers<Gizmo, Size> = PropertyObserversImpl(mutableSetOf())

//    val childrenByZIndex: List<Gizmo>
//        get() = ROOT_CONTAINER.getChildrenByZIndex()

//    var focusTraversalPolicy: FocusTraversalPolicy
//        get() = ROOT_CONTAINER.getFocusTraversalPolicy()
//        set(aPolicy) {
//            ROOT_CONTAINER.setFocusTraversalPolicy(aPolicy)
//        }

    override fun fill(brush: Brush) {
        when (brush) {
            is SolidBrush   -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundColor(brush.color)
            }
            is TextureBrush -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundImage(brush.image)
            }
            else            -> {
                rootElement.insert(canvasElement, 0)

                return // FIXME
//                sCanvas.drawRect(root.bounds, brush)
            }
        }
    }

    override fun isAncestor(gizmo: Gizmo) = root.isAncestor(gizmo)

    override fun child(at: Point): Gizmo? = root.child(at)

    operator fun contains(aGizmo: Gizmo) = root.contains(aGizmo)

    override fun iterator() = root.iterator()
}