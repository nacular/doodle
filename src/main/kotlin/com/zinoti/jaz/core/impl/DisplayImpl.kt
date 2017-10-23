package com.zinoti.jaz.core.impl

import com.zinoti.jaz.containers.Padding
import com.zinoti.jaz.core.Container
import com.zinoti.jaz.core.Display
import com.zinoti.jaz.core.Gizmo
import com.zinoti.jaz.core.Layout
import com.zinoti.jaz.dom.create
import com.zinoti.jaz.dom.height
import com.zinoti.jaz.dom.insert
import com.zinoti.jaz.dom.setBackgroundColor
import com.zinoti.jaz.dom.setBackgroundImage
import com.zinoti.jaz.dom.setHeightPercent
import com.zinoti.jaz.dom.setWidthPercent
import com.zinoti.jaz.dom.width
import com.zinoti.jaz.drawing.Brush
import com.zinoti.jaz.drawing.SolidBrush
import com.zinoti.jaz.drawing.TextureBrush
import com.zinoti.jaz.geometry.Size
import com.zinoti.jaz.system.Cursor
import com.zinoti.jaz.utils.PropertyObservers
import com.zinoti.jaz.utils.PropertyObserversImpl
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.max


class DisplayImpl(private val rootElement: HTMLElement): Display {
    private fun onResize(event: Event? = null) {
        root.minimumSize.let {
            root.size = Size.create(max(rootElement.width, it.width), max(rootElement.height, it.height))
        }
    }

    private val root         = Container()
    private val canvasElement: HTMLElement = create("b") as HTMLElement
//        private val sStyles = GWT.create(GraphicsStyles::class.java)
//        private val sCanvas = GraphicsService.locator().getGraphicsDevice().create(canvasElement).getCanvas()


    init {
//            sStyles.init()

        rootElement.onresize = this::onResize

        onResize()

        root.isFocusCycleRoot = true

        canvasElement.style.setWidthPercent (100.0)
        canvasElement.style.setHeightPercent(100.0)

        root.boundsChange + { gizmo, old, new ->
            if (old.size != new.size) {
                (sizeChange as PropertyObserversImpl<Display, Size>).forEach {
                    it(this, old.size, new.size)
                }
            }
        }
    }


    override var cursor: Cursor
        get(   ) = root.cursor ?: Cursor.DEFAULT
        set(new) { root.cursor = new }

    val width  get() = size.width
    val height get() = size.height

    override val size = root.size

    override var padding: Padding
        get(   ) = root.padding
        set(new) { root.padding = new }

    override var minimumSize: Size
        get(   ) = root.minimumSize
        set(new) { root.minimumSize = new }

    override var layout: Layout?
        get(   ) = root.layout
        set(new) { root.layout = new }

    override val children = root.children

    override val sizeChange: PropertyObservers<Display, Size> = PropertyObserversImpl(mutableSetOf())

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
//                documentBody.remove(canvasElement)

                rootElement.style.setBackgroundColor(brush.color)
            }
            is TextureBrush -> {
//                documentBody.remove(canvasElement)

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

    operator fun contains(aGizmo: Gizmo) = root.contains(aGizmo)

    override fun iterator() = root.iterator()
}