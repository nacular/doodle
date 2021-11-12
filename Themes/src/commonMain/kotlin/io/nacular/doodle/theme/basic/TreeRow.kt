package io.nacular.doodle.theme.basic

import io.nacular.doodle.accessibility.TreeItemRole
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.tree.TreeLike
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.rounded
import io.nacular.doodle.layout.ConstraintLayout
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.HorizontalConstraint
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.MagnitudeConstraint
import io.nacular.doodle.layout.VerticalConstraint
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.utils.Path
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/7/19.
 */

public abstract class TreeRowIcon: View() {
    public abstract var expanded: Boolean
    public abstract var selected: Boolean
}

public class SimpleTreeRowIcon(private val color: Color = Black, private val selectedColor: Color = White): TreeRowIcon() {
    override var expanded: Boolean = false
        set (new) {
            field = new
            rerender()
        }

    override var selected: Boolean = false
        set (new) {
            field = new
            rerender()
        }

    override fun render(canvas: Canvas) {
        val transform = when {
            expanded -> Identity.rotate(Point(width / 2, height / 2), 90 * degrees)
            else     -> Identity
        }

        val centeredRect = bounds.atOrigin.inset(6.0)

        val path = ConvexPolygon(centeredRect.position,
                                 Point(centeredRect.right, centeredRect.y + centeredRect.height / 2),
                                 Point(centeredRect.x, centeredRect.bottom)).rounded(1.0)

        canvas.transform(transform) {
            path(path, ColorPaint(if (selected) selectedColor else color))
        }
    }
}

public class TreeRow<T>(
                 tree                 : TreeLike,
                 node                 : T,
     public  var path                 : Path<Int>,
     private var index                : Int,
     private val itemVisualizer       : ItemVisualizer<T, IndexedItem>,
     private val selectionColor       : Color? = Green,
     private val selectionBlurredColor: Color? = selectionColor,
     private val iconFactory          : () -> TreeRowIcon,
     private val role: TreeItemRole       = TreeItemRole()): View(role) {

    public var insetTop: Double = 1.0

    public var positioner: Constraints.() -> Unit = { left = parent.left; centerY = parent.centerY }
        set(new) {
            if (field == new) {
                return
            }

            field = new

            layout = constrainLayout(children[0])
        }

    private var icon    = null as TreeRowIcon?
    private var depth   = -1
    public  var content: View = itemVisualizer.invoke(node, context = SimpleIndexedItem(index, tree.selected(path)))
        private set(new) {
            if (field != new) {
                children.batch {
                    remove(field)
                    field = new
                    add(field)
                }
                depth = -1 // force layout
            }
        }

    private  val iconWidth   = 20.0
    private  var pointerOver = false

    private val treeFocusChanged = { _:View, _:Boolean, new:Boolean ->
        if (tree.selected(path)) {
            backgroundColor = if (new) selectionColor else selectionBlurredColor
        }
    }

    private lateinit var constraintLayout: ConstraintLayout

    init {
        focusable     = false
        children     += content
        styleChanged += { rerender() }
        pointerChanged += object: PointerListener {
            private var pressed   = false

            override fun entered(event: PointerEvent) {
                pointerOver = true
            }

            override fun exited(event: PointerEvent) {
                pointerOver = false
            }

            override fun pressed(event: PointerEvent) {
                pressed = true
            }

            override fun released(event: PointerEvent) {
                if (pointerOver && pressed) {
                    setOf(path).also {
                        tree.apply {
                            when {
                                Ctrl  in event.modifiers || Meta in event.modifiers -> toggleSelection(it)
                                Shift in event.modifiers && lastSelection != null -> {
                                    selectionAnchor?.let { rowFromPath(it) }?.let { anchor ->
                                        rowFromPath(path)?.let { current ->
                                            when {
                                                current < anchor  -> setSelection((current .. anchor ).reversed().toSet())
                                                anchor  < current -> setSelection((anchor  .. current).           toSet())
                                            }
                                        }
                                    }
                                }
                                else -> setSelection(it)
                            }
                        }
                    }
                }

                pressed = false
            }
        }

        update(tree, node, path, index)
    }

    private fun constrainLayout(view: View) = constrain(view) { content ->
        positioner(
            // Override the parent for content to confine it within a smaller region
            ConstraintWrapper(content) { parent ->
                object: ParentConstraintWrapper(parent) {
                    override val top    = VerticalConstraint  (this@TreeRow) { insetTop }
                    override val left   = HorizontalConstraint(this@TreeRow) { iconWidth * (1 + depth) }
                    override val width  = MagnitudeConstraint (this@TreeRow) { it.width - iconWidth * (1 + depth) }
                    override val height = MagnitudeConstraint (this@TreeRow) { it.height - insetTop }
                }
            }
        )
    }

    public fun update(tree: TreeLike, node: T, path: Path<Int>, index: Int) {
        this.path  = path
        this.index = index

        update(itemVisualizer.invoke(node, content, SimpleIndexedItem(index, tree.selected(path))), tree)
    }

    public fun update(content: View, tree: TreeLike) {
        this.content = content

        val newDepth = (path.depth - if (!tree.rootVisible) 1 else 0)

        if (newDepth != depth) {
            depth            = newDepth
            constraintLayout = constrainLayout(content)

            constrainIcon(icon)

            layout = constraintLayout
        }

        if (tree.isLeaf(this.path)) {
            icon?.let {
                this.children -= it
                constraintLayout.unconstrain(it)
            }
            icon = null
        } else  {
            icon = icon ?: iconFactory().apply {
                width    = iconWidth
                height   = width

                this@TreeRow.children += this

                pointerChanged += object: PointerListener {
                    private var pressed     = false
                    private var pointerOver = false

                    override fun entered(event: PointerEvent) {
                        pointerOver = true
                    }

                    override fun exited(event: PointerEvent) {
                        pointerOver = false
                    }

                    override fun pressed(event: PointerEvent) {
                        pressed     = true
                        pointerOver = true
                        event.consume()
                    }

                    override fun released(event: PointerEvent) {
                        if (pointerOver && pressed) {
                            when (tree.expanded(this@TreeRow.path)) {
                                true -> tree.collapse(this@TreeRow.path)
                                else -> tree.expand  (this@TreeRow.path)
                            }

                            event.consume()
                        }
                        pressed = false
                    }
                }

                constrainIcon(this)
            }

            icon?.apply {
                expanded = tree.expanded(path)
                selected = tree.selected(path)
            }
        }

        idealSize       = Size(children.map { it.width }.reduce { a, b -> a + b  }, children.map { it.height }.reduce { a, b -> max(a, b) })
        backgroundColor = when {
            tree.selected(path) -> {
                tree.focusChanged += treeFocusChanged

                if (tree.hasFocus) selectionColor else selectionBlurredColor
            }
            else                 -> {
                tree.focusChanged -= treeFocusChanged
                null
            }
        }

        role.index    = index
        role.depth    = depth
        role.treeSize = tree.numRows
        role.expanded = tree.expanded(path)
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = insetTop)), ColorPaint(it)) }
    }

    private fun constrainIcon(icon: TreeRowIcon?) {
        icon?.let {
            constraintLayout.constrain(it, content) { icon, content ->
                icon.right   = content.left
                icon.centerY = content.centerY
            }
        }
    }
}