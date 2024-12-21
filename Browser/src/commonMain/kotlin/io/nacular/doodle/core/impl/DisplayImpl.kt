package io.nacular.doodle.core.impl

import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.ContentDirection.RightLeft
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult
import io.nacular.doodle.core.View
import io.nacular.doodle.core.width
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.addIfNotPresent
import io.nacular.doodle.dom.clear
import io.nacular.doodle.dom.height
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setBackgroundImage
import io.nacular.doodle.dom.setBackgroundSize
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOpacity
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.dom.width
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.ImagePaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.observable

internal class DisplayImpl(htmlFactory: HtmlFactory, canvasFactory: CanvasFactory, private val rootElement: HTMLElement): InternalDisplay {

    override var insets = None

    override var layout: Layout? by observable(null) { _,_ ->
        relayout()
    }

    override val popups get() = popUps

    private val popUps = mutableListOf<View>()

    override val children = ObservableList<View>().apply {
        changed += { _, differences ->
            var index = 0

            differences.forEach { diff ->
                when (diff) {
                    is Insert -> {
                        // items copy needed b/c diff.items might be a view into the children list,
                        // which could be modified when we remove duplicates below
                        diff.items.toMutableList().forEach { item ->
                            if (diff.origin(of = item) == null) {
                                // Avoid duplicating View
                                repeat(filterIndexed { i, view -> i != index && view == item }.size) { remove(item); --index }
                                popUps -= item
                            }

                            ++index
                        }
                    }
                    is Delete -> {}
                    else      -> { index += diff.items.size }
                }
            }

            (childrenChanged as ChildObserversImpl).invoke(differences)
        }
    }

    private inner class ChildObserversImpl: SetPool<ChildObserver<Display>>() {
        operator fun invoke(differences: Differences<View>) = forEach { it(this@DisplayImpl, differences) }
    }

    override val childrenChanged: Pool<ChildObserver<Display>> = ChildObserversImpl()

    override val sizeChanged  : PropertyObservers<Display, Size>    = PropertyObserversImpl<Display, Size>   (this)
    override val cursorChanged: PropertyObservers<Display, Cursor?> = PropertyObserversImpl<Display, Cursor?>(this)
    override var size                                               by observable(Empty, sizeChanged as PropertyObserversImpl<Display, Size>); private set

    override var cursor: Cursor?                                    by observable(null, cursorChanged as PropertyObserversImpl<Display, Cursor?>)

    override var focusTraversalPolicy: FocusTraversalPolicy? = null

    override val contentDirectionChanged: ChangeObservers<Display> = ChangeObserversImpl(this)

    override var mirrorWhenRightLeft = true; set(new) {
            if (field == new) return

            field = new

            notifyMirroringChanged()
        }

    override val mirroringChanged: ChangeObservers<Display> = ChangeObserversImpl(this)

    override var contentDirection: ContentDirection = LeftRight; set(new) {
            if (field == new) return

            field = new

            contentDirectionChanged()
        }

    private fun contentDirectionChanged() {
        (contentDirectionChanged as ChangeObserversImpl)()
        notifyMirroringChanged()
    }

    private fun notifyMirroringChanged() {
        updateTransform()

        (mirroringChanged as ChangeObserversImpl)()
    }

    private val canvasElement       = htmlFactory.create<HTMLElement>()
    private val canvas              = canvasFactory(canvasElement)
    private var fill                = null as Paint?

    val renderOffset: Int get() = canvasElement.parent?.let { 1 } ?: 0

    override var transform: AffineTransform = Identity; set (new) {
            field = new

            refreshAugmentedTransform()
        }

    private var augmentedTransform: AffineTransform = Identity; set (new) {
            field = new

            updateTransform()
        }

    private var ignoreNextContentDirection = false

    internal fun updateContentDirection(fromConstructor: Boolean = false) {
        if (ignoreNextContentDirection) {
            ignoreNextContentDirection = false
            return
        }

        contentDirection = when (rootElement.dir) {
            "rtl" -> RightLeft
            else  -> LeftRight
        }

        ignoreNextContentDirection = !fromConstructor

        // currently need to force left-to-right since we handle content direction
        // TODO: Need a better way to do this to avoid compromising browser/accessibility integrations
        if (rootElement.dir != "") {
            rootElement.dir = ""
        }
    }

    init {
        rootElement.onresize = ::onResize

        updateContentDirection(fromConstructor = true)
        onResize              ()

        canvasElement.style.setWidthPercent (100.0)
        canvasElement.style.setHeightPercent(100.0)
    }

    override fun fill(fill: Paint) {
        when (fill) {
            is ColorPaint -> {
                canvasElement.parentNode?.removeChild(canvasElement)

                rootElement.style.setBackgroundColor(
                    when {
                        fill.color.visible -> fill.color
                        else               -> null
                    }
                )
            }
            is ImagePaint  -> {
                when (fill.opacity) {
                    1.0f -> {
                        canvasElement.parentNode?.removeChild(canvasElement)
                        rootElement.style.setBackgroundSize (fill.size )
                        rootElement.style.setBackgroundImage(fill.image)
                    }
                    else -> {
                        rootElement.style.setBackgroundColor(null)
                        rootElement.addIfNotPresent(canvasElement, 0)

                        canvasElement.clear()
                        canvasElement.style.setOpacity        (fill.opacity)
                        canvasElement.style.setBackgroundSize (fill.size   )
                        canvasElement.style.setBackgroundImage(fill.image  )
                    }
                }
            }
            else         -> {
                rootElement.style.setBackgroundColor(null)

                this.fill = fill
                rootElement.addIfNotPresent(canvasElement, 0)

                repaint()
            }
        }
    }

    override infix fun ancestorOf(view: View): Boolean = if (children.isNotEmpty()) {
        var child: View? = view

        while (child?.parent != null) {
            child = child.parent
        }

        child in children
    } else false

    override fun child(at: Point): View? = fromAbsolute(at).let { point ->
        when (val result = layout?.item(children.asSequence().map { it.positionable }, point)) {
            null, LookupResult.Ignored -> child_(at = point) { true }
            is LookupResult.Found -> result.item.view
            is LookupResult.Empty -> null
        }
    }

    override fun child(at: Point, predicate: (View) -> Boolean): View? = child_(fromAbsolute(at), predicate)

    private fun child_(at: Point, predicate: (View) -> Boolean): View? {
        var child     = null as View?
        var topZOrder = 0

        popUps.asReversed().forEach {
            if (it.visible && at in it && (child == null) && predicate(it)) {
                child = it
            }
        }

        if (child == null) {
            children.asReversed().forEach {
                if (it.visible && at in it && (child == null || it.zOrder > topZOrder) && predicate(it)) {
                    child     = it
                    topZOrder = it.zOrder
                }
            }
        }

        return child
    }

    override fun iterator() = children.iterator()

    override fun toAbsolute(point: Point) = resolvedTransform(point).as2d()

    override fun fromAbsolute(point: Point) = resolvedTransform.inverse?.invoke(point)?.as2d() ?: point

    private var layingOut = false

    override fun relayout() {
        if (!layingOut) {
            layingOut = true

            super.relayout()

            layingOut= false
        }
    }

    fun shutdown() {
        children.clear()
        popUps.clear()

        fill = null

        rootElement.apply {
            style.setTransform(null)
            style.background = ""

            clear()
        }
    }

    private fun onResize(@Suppress("UNUSED_PARAMETER") event: Event? = null) {
        size = Size(rootElement.width, rootElement.height)

        refreshAugmentedTransform()
        repaint()
    }

    override fun repaint() {
        fill?.let {
            canvas.clear()
            canvas.rect (Rectangle(size = size), it)
            canvas.flush()
        }
    }

    override fun showPopup(view: View) {
        children -= view
        popUps   += view
    }

    override fun hidePopup(view: View) {
        popUps -= view
    }

    private fun refreshAugmentedTransform() {
        val point          = -Point(size.width / 2, size.height / 2)
        augmentedTransform = ((Identity translate point) * transform) translate -point
    }

    private val resolvedTransform get() = when {
        mirrored -> transform.flipHorizontally(at = width / 2)
        else     -> transform
    }

    private fun updateTransform() {
        // resolvedTransform isn't used b/c element transforms are centered already
        rootElement.style.setTransform(when {
            mirrored -> augmentedTransform.flipHorizontally()
            else     -> augmentedTransform
        })
    }
}