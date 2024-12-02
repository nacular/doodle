@file:OptIn(ExperimentalSkikoApi::class)

package io.nacular.doodle.core.impl

import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult
import io.nacular.doodle.core.View
import io.nacular.doodle.core.width
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.skia.skia33
import io.nacular.doodle.skia.skia44
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.theme.native.toDoodle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
import org.jetbrains.skia.Canvas as SkiaCanvas


/**
 * Created by Nicholas Eddy on 5/14/21.
 */
// FIXME: Move common parts to common code
internal class DisplayImpl(
    private  val appScope      : CoroutineScope,
    private  val uiDispatcher  : CoroutineContext,
    private  val defaultFont   : Font,
    private  val fontCollection: FontCollection,
    internal val device        : GraphicsDevice<RealGraphicsSurface>,
                 targetWindow  : JFrame,
                 skiaLayers    : () -> SkiaLayer
): DisplaySkiko, SkikoRenderDelegate {
    override var insets = Insets.None

    override var layout: Layout? by Delegates.observable(null) { _, _, _ ->
        relayout()
    }

    override val panel: JPanel get() = skiaLayer

    private val skiaLayer = skiaLayers().apply {
        renderDelegate     = this@DisplayImpl
        canvas.isFocusable = false // FIXME: This is currently needed b/c the canvas steals focus from native controls. Need to fix.
    }

    override val popups get() = popUps

    private val popUps by lazy { mutableListOf<View>() }

    override val children by lazy { ObservableList<View>().apply {
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
                                filterIndexed { i, view -> i != index && view == item }.forEach { remove(it); --index }
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
    } }

    private inner class ChildObserversImpl: SetPool<ChildObserver<Display>>() {
        operator fun invoke(differences: Differences<View>) = forEach { it(this@DisplayImpl, differences) }
    }

    override val childrenChanged: Pool<ChildObserver<Display>> by lazy { ChildObserversImpl() }

    override val sizeChanged  : PropertyObservers<Display, Size>    by lazy { PropertyObserversImpl<Display, Size>(this) }
    override val cursorChanged: PropertyObservers<Display, Cursor?> by lazy { PropertyObserversImpl<Display, Cursor?>(this) }
    override var size                                               by observable(Empty, sizeChanged as PropertyObserversImpl<Display, Size>); private set

    override val indexInParent get() = 0

    override val locationOnScreen get() = panel.locationOnScreen.toDoodle()

    override var cursor: Cursor?                                    by observable(null, cursorChanged as PropertyObserversImpl<Display, Cursor?>)

    override var focusTraversalPolicy: FocusTraversalPolicy? = null

    override val contentDirectionChanged: ChangeObservers<Display> by lazy { ChangeObserversImpl(this) }

    override var mirrorWhenRightLeft = true; set(new) {
        if (field == new) return

        field = new

        notifyMirroringChanged()
    }

    override val mirroringChanged: ChangeObservers<Display> by lazy { ChangeObserversImpl(this) }

    override var contentDirection: ContentDirection = LeftRight; set(new) {
        if (field == new) return

        field = new

        contentDirectionChanged()
    }

    private var renderJob: Job? = null
    private var shutDown        = false

    override fun syncSize() {
        size = Size(panel.width, panel.height)
    }

    override fun paintNeeded() {
        if ((renderJob == null || renderJob?.isActive != true) && !shutDown){
            renderJob = appScope.launch(uiDispatcher) {
                skiaLayer.needRedraw()
            }
        }
    }

    private fun contentDirectionChanged() {
        (contentDirectionChanged as ChangeObserversImpl)()
        notifyMirroringChanged()
    }

    private fun notifyMirroringChanged() {
        (mirroringChanged as ChangeObserversImpl)()
        paintNeeded()
    }

    private var fillCanvas: CanvasImpl? = null

    private var fill: Paint? by Delegates.observable(null) { _,_,_ ->
        paintNeeded()
    }

    override var transform: AffineTransform = Identity; set (new) {
        field = new

        paintNeeded()
    }

    override fun onRender(canvas: SkiaCanvas, width: Int, height: Int, nanoTime: Long) {
        canvas.save()

        panel.graphicsConfiguration.defaultTransform.scaleX.toFloat().let {
            canvas.scale(it, it)
        }

        when {
            resolvedTransform.is3d -> canvas.concat(resolvedTransform.skia44())
            else                   -> canvas.concat(resolvedTransform.skia33())
        }

        fill?.let {
            fillCanvas = fillCanvas?.also { it.skiaCanvas = canvas } ?: CanvasImpl(canvas, defaultFont, fontCollection)

            fillCanvas!!.apply {
                size = this@DisplayImpl.size
                rect(Rectangle(width, height), it)
            }
        }

        children.forEach {
            device[it].onRender(canvas)
        }

        popUps.forEach {
            device[it].onRender(canvas)
        }

        canvas.restore()
    }

    init {
        skiaLayer.apply {
            attachTo(targetWindow.contentPane)
            addComponentListener(object: ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    syncSize()
                }
            })
        }

        syncSize()
    }

    override fun fill(fill: Paint) {
        this.fill = fill
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

    private var layingOut = false

    override fun toAbsolute(point: Point) = resolvedTransform(point).as2d()

    override fun fromAbsolute(point: Point) = resolvedTransform.inverse?.invoke(point)?.as2d() ?: point

    override fun relayout() {
        if (!layingOut) {
            layingOut = true

            super.relayout()

            layingOut = false

            paintNeeded()
        }
    }

    override fun shutdown() {
        shutDown = true

        children.clear()
        popUps.clear()

        fill = null
    }

    override fun repaint() {
        fill?.let {
            paintNeeded()
        }
    }

    override fun showPopup(view: View) {
        children -= view
        popUps   += view
    }

    override fun hidePopup(view: View) {
        popUps -= view
    }

    private val resolvedTransform get() = when {
        mirrored -> transform.flipHorizontally(at = width / 2)
        else     -> transform
    }
}