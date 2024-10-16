package io.nacular.doodle.theme.basic

import io.nacular.doodle.accessibility.TreeItemRole
import io.nacular.doodle.controls.ExpandableItem
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
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.rounded
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.ConstraintLayout
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.withSizeInsets
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.theme.PaintMapper
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.observable
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 5/7/19.
 */

public abstract class TreeRowIcon: View() {
    public abstract var expanded: Boolean
    public abstract var selected: Boolean
}

public class SimpleTreeRowIcon(private val fill: Paint, private val selectedFill: Paint): TreeRowIcon() {
    public constructor(color: Color = Black, selectedColor: Color = White): this(color.paint, selectedColor.paint)

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

        val paint = (if (selected) selectedFill else fill).let { if (enabled) it else disabledPaintMapper(it) }

        canvas.transform(transform) {
            path(path, paint)
        }
    }

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    init {
        enabledChanged += { _,_,_ ->
            rerender()
        }
    }
}

public class TreeRow<T>(
                 tree                 : TreeLike,
                 node                 : T,
     public  var path                 : Path<Int>,
     private var index                : Int,
     private val itemVisualizer       : ItemVisualizer<T, ExpandableItem>,
     private val selectionColor       : Color? = Green,
     private val selectionBlurredColor: Color? = selectionColor,
     private val iconFactory          : () -> TreeRowIcon,
     private val role                 : TreeItemRole = TreeItemRole()): View(role) {

    private class SimpleExpandableItem(private val tree: TreeLike, private val path: Path<Int>, index: Int): ExpandableItem, SimpleIndexedItem(index, tree.selected(index)) {
        override val expanded: Boolean get() = tree.expanded(path)
    }

    public var insetTop: Double = 1.0

    // FIXME: Shouldn't need the explicit left setting here
    public var positioner: ConstraintDslContext.(Bounds) -> Unit by observable({
        it.left    eq 0
        it.width   eq it.preferredWidth
        it.centerY eq parent.centerY
    }) { _,_ ->
       updateLayout() //relayout()
    }

    private var icon    = null as TreeRowIcon?
    private var depth   = -1
    public  var content: View = itemVisualizer(node, context = SimpleExpandableItem(tree, path, index)); private set(new) {
        if (field != new) {
            children.batch {
                remove(field)
                field = new
                add(field)
            }
        }
    }

    private val iconWidth   = 20.0
    private var pointerOver = false

    private val treeFocusChanged = { _:View, _:Boolean, new:Boolean ->
        if (tree.selected(path)) {
            backgroundColor = if (new) selectionColor else selectionBlurredColor
        }
    }

    private lateinit var constraintLayout: ConstraintLayout

    private val indent get() = iconWidth * (depth + 1)

    private val iconConstraints: ConstraintDslContext.(Bounds) -> Unit = {
        it.width.preserve
        it.height.preserve

        it.right   eq indent
        it.centerY eq parent.centerY
    }

    private val contentConstraints: ConstraintDslContext.(Bounds) -> Unit = {
        withSizeInsets(width = { indent }, height = { insetTop }) {
            positioner(it.withOffset(top = { insetTop }, left = { indent }))
        }

        // FIXME: This doesn't really make much sense
        parent.width greaterEq it.right.readOnly
    }

    init {
        focusable       = false
        children       += content
        styleChanged   += { rerender() }
        pointerChanged += object: PointerListener {
            private var pressed = false

            override fun entered(event: PointerEvent) { pointerOver = true  }
            override fun exited (event: PointerEvent) { pointerOver = false }
            override fun pressed(event: PointerEvent) { pressed     = true  }

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

        updateLayout()
        update(tree, node, path, index)
    }

    private fun constrainContent(view: View) = constrain(view, contentConstraints)

    public fun update(tree: TreeLike, node: T, path: Path<Int>, index: Int): Unit = update(
        itemVisualizer(node, content, SimpleExpandableItem(tree, path, index)),
        tree,
        path,
        index
    )

    public fun update(content: View, tree: TreeLike): Unit = update(content, tree, path, index)

    private fun update(content: View, tree: TreeLike, path: Path<Int>, index: Int) {
        val oldDepth   = depth
        val oldContent = this.content

        this.path      = path
        this.index     = index
        this.depth     = path.depth - if (!tree.rootVisible) 1 else 0
        this.content   = content

        if (oldDepth != depth || oldContent != content) {
            if (oldContent != content) {
                constraintLayout.unconstrain(oldContent, contentConstraints)
                constraintLayout.constrain  (content,    contentConstraints)
            }

            relayout()
        }

        if (tree.isLeaf(path)) {
            icon?.let {
                children -= it
                constraintLayout.unconstrain(it, iconConstraints)
            }
            icon = null
        } else  {
            icon = icon ?: iconFactory().apply {
                width  = iconWidth
                height = iconWidth

                children += this

                pointerChanged += object: PointerListener {
                    private var pressed     = false
                    private var pointerOver = false

                    override fun entered(event: PointerEvent) { pointerOver = true  }
                    override fun exited (event: PointerEvent) { pointerOver = false }

                    override fun pressed(event: PointerEvent) {
                        pressed     = true
                        pointerOver = true
                        event.consume()
                    }

                    override fun released(event: PointerEvent) {
                        if (pointerOver && pressed) {
                            if (tree.expanded(this@TreeRow.path)) tree.collapse(this@TreeRow.path) else tree.expand(this@TreeRow.path)

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

//        idealSize       = Size(children.map { it.width }.reduce { a, b -> a + b  }, children.map { it.height }.reduce { a, b -> max(a, b) })
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
        role.selected = tree.selected(index)
    }

    override fun render(canvas: Canvas) {
        backgroundColor?.let { canvas.rect(bounds.atOrigin.inset(Insets(top = insetTop)), ColorPaint(it)) }
    }

    private fun updateLayout() {
        constraintLayout = constrainContent(content)

        constrainIcon(icon)

        layout = constraintLayout
    }

    private fun constrainIcon(icon: TreeRowIcon?) {
        icon?.let {
            constraintLayout.constrain(it, iconConstraints)
        }
    }
}