package io.nacular.doodle.core.impl

import io.nacular.doodle.application.CustomSkikoView
import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult.Empty
import io.nacular.doodle.core.LookupResult.Found
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.height
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
import org.jetbrains.skiko.SkiaLayer
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
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
    internal val skiaLayer     : SkiaLayer,
    private  val defaultFont   : Font,
    private  val fontCollection: FontCollection,
    internal val device        : GraphicsDevice<RealGraphicsSurface>,
): InternalDisplay {
    override var insets = Insets.None

    override var layout: Layout? by Delegates.observable(null) { _, _, _ ->
        relayout()
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
    override var size                                               by observable(Empty, sizeChanged as PropertyObserversImpl<Display, Size>)
        private set

    override var cursor: Cursor?                                    by observable(null, cursorChanged as PropertyObserversImpl<Display, Cursor?>)

    override var focusTraversalPolicy: FocusTraversalPolicy? = null

    override val contentDirectionChanged: ChangeObservers<Display> by lazy { ChangeObserversImpl(this) }

    override var mirrorWhenRightLeft = true
        set(new) {
            if (field == new) return

            field = new

            notifyMirroringChanged()
        }

    override val mirroringChanged: ChangeObservers<Display> by lazy { ChangeObserversImpl(this) }

    override var contentDirection: ContentDirection = LeftRight
        set(new) {
            if (field == new) return

            field = new

            contentDirectionChanged()
        }

    private var renderJob: Job? = null

    private fun requestRender() {
        if (renderJob == null || renderJob?.isActive != true) {
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
        requestRender()
    }

    private val positionableWrapper = PositionableWrapper()
    private var fill: Paint? by Delegates.observable(null) { _, _, _ ->
        requestRender()
    }

    override var transform: AffineTransform = Identity
        set (new) {
            field = new

            requestRender()
        }

    private fun onRender(skiaCanvas: SkiaCanvas, width: Int, height: Int, @Suppress("UNUSED_PARAMETER") nano: Long) {
        skiaCanvas.save ()
        skiaCanvas.scale(skiaLayer.contentScale, skiaLayer.contentScale)

        when {
            resolvedTransform.is3d -> skiaCanvas.concat(resolvedTransform.skia44())
            else                   -> skiaCanvas.concat(resolvedTransform.skia33())
        }

        fill?.let {
            CanvasImpl(skiaCanvas, defaultFont, fontCollection).apply {
                size = this@DisplayImpl.size
                rect(Rectangle(width, height), it)
            }
        }

        children.forEach {
            device[it].onRender(skiaCanvas)
        }

        popUps.forEach {
            device[it].onRender(skiaCanvas)
        }

        skiaCanvas.restore()
    }

    init {
        skiaLayer.addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                size = Size(skiaLayer.width, skiaLayer.height)
            }
        })

        (skiaLayer.skikoView as CustomSkikoView).onRender = this::onRender

        size = Size(skiaLayer.width, skiaLayer.height)
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
        when (val result = layout?.child(positionableWrapper, point)) {
            null, Ignored -> child_(point) { true }
            is Found      -> result.child as? View
            is Empty      -> null
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

            layout?.layout(positionableWrapper)

            layingOut = false

            requestRender()
        }
    }

    fun shutdown() {
        children.clear()
        popUps.clear()

        fill = null
    }

    override fun repaint() {
        fill?.let {
            requestRender()
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

    private inner class PositionableWrapper: PositionableContainer {
        override val size        get() = this@DisplayImpl.size
        override val width       get() = this@DisplayImpl.width
        override val height      get() = this@DisplayImpl.height
        override var idealSize   get() = null as Size?;           set(_) {}
        override var minimumSize get() = Empty;                   set(_) {}

        override val insets      get() = this@DisplayImpl.insets
        override val children    get() = this@DisplayImpl.children
    }
}