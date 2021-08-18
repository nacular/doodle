package io.nacular.doodle.core.impl

import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
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
import io.nacular.doodle.skia.skia
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.skija.Font
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
import org.jetbrains.skija.Canvas as SkiaCanvas


/**
 * Created by Nicholas Eddy on 5/14/21.
 */
// FIXME: Move common parts to common code
internal class DisplayImpl(
        private val appScope      : CoroutineScope,
        private val uiDispatcher  : CoroutineContext,
        private val window        : SkiaWindow,
        private val defaultFont   : Font,
        private val fontCollection: FontCollection,
        private val device        : GraphicsDevice<RealGraphicsSurface>
): InternalDisplay {
    override var insets = Insets.None

    override var layout: Layout? by Delegates.observable(null) { _, _, _ ->
        relayout()
    }

    override val children by lazy { ObservableList<View>().apply {
        changed += { _, removed, added, moved ->
            added.forEach { item ->
                filterIndexed { index, view -> index != item.key && view == item.value }.forEach { remove(it) }
            }

            (childrenChanged as ChildObserversImpl).invoke(removed, added, moved)
        }
    } }

    private inner class ChildObserversImpl(mutableSet: MutableSet<ChildObserver<Display>> = mutableSetOf()): SetPool<ChildObserver<Display>>(mutableSet) {
        operator fun invoke(removed: Map<Int, View>, added: Map<Int, View>, moved: Map<Int, Pair<Int, View>>) = delegate.forEach { it(this@DisplayImpl, removed, added, moved) }
    }

    override val childrenChanged: Pool<ChildObserver<Display>> by lazy { ChildObserversImpl() }

    override val sizeChanged  : PropertyObservers<Display, Size>    by lazy { PropertyObserversImpl<Display, Size>(this) }
    override val cursorChanged: PropertyObservers<Display, Cursor?> by lazy { PropertyObserversImpl<Display, Cursor?>(this) }
    override var size                                               by observable(Empty, sizeChanged as PropertyObserversImpl<Display, Size>)
        private set

    override var cursor: Cursor?                                    by observable(null, cursorChanged as PropertyObserversImpl<Display, Cursor?>)

    override var focusTraversalPolicy: FocusTraversalPolicy? = null

    override val contentDirectionChanged: Pool<ChangeObserver<Display>> by lazy { ChangeObserversImpl(this) }

    override var mirrorWhenRightLeft = true
        set(new) {
            if (field == new) return

            field = new

            notifyMirroringChanged()
        }

    override val mirroringChanged: Pool<ChangeObserver<Display>> by lazy { ChangeObserversImpl(this) }

    override var contentDirection: ContentDirection = ContentDirection.LeftRight
        set(new) {
            if (field == new) return

            field = new

            contentDirectionChanged()
        }

    private var renderJob: Job? = null

    private fun requestRender() {
        if (renderJob == null || renderJob?.isActive != true) {
            renderJob = appScope.launch(uiDispatcher) {
                window.layer.needRedraw()
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

    override var transform = Identity
        set (new) {
            field = new

            requestRender()
        }

    init {
        window.layer.renderer = object: SkiaRenderer {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun onRender(skiaCanvas: SkiaCanvas, width: Int, height: Int, nanoTime: Long) {
                skiaCanvas.save     ()
                skiaCanvas.scale    (window.layer.contentScale, window.layer.contentScale)
                skiaCanvas.setMatrix(skiaCanvas.localToDeviceAsMatrix33.makeConcat(resolvedTransform.skia()))

                fill?.let {
                    CanvasImpl(skiaCanvas, defaultFont, fontCollection).apply {
                        size = this@DisplayImpl.size
                        rect(Rectangle(width, height), it)
                    }
                }

                children.forEach {
                    device[it].onRender(skiaCanvas)
                }

                skiaCanvas.restore()
            }
        }

        window.addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                size = Size(window.contentPane.width, window.contentPane.height)
                window.layer.setSize(window.contentPane.width, window.contentPane.height)
            }
        })

        window.preferredSize = Dimension(800, 600)
        window.pack()
        window.isVisible = true
        window.layout    = null

        size = Size(window.contentPane.width, window.contentPane.height)
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

    override fun child(at: Point): View? = (resolvedTransform.inverse?.invoke(at) ?: at).let { point ->
        when (val result = layout?.child(positionableWrapper, point)) {
            null, LookupResult.Ignored -> {
                var child = null as View?
                var topZOrder = 0

                children.reversed().forEach {
                    if (it.visible && point in it && (child == null || it.zOrder > topZOrder)) {
                        child = it
                        topZOrder = it.zOrder
                    }
                }

                child
            }
            is LookupResult.Found -> result.child as? View
            is LookupResult.Empty -> null
        }
    }

    override fun iterator() = children.iterator()

    private var layingOut = false

    override fun toAbsolute(point: Point) = resolvedTransform(point)

    override fun fromAbsolute(point: Point) = resolvedTransform.inverse?.invoke(point) ?: point

    override fun relayout() {
        if (!layingOut) {
            layingOut = true

            layout?.layout(positionableWrapper)

            layingOut= false

            requestRender()
        }
    }

    fun shutdown() {
        children.clear()

        fill = null
    }

    override fun repaint() {
        fill?.let {
            requestRender()
        }
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