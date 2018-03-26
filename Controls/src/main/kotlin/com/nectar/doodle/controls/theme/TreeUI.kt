package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.tree.Path
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.isEven

/**
 * Created by Nicholas Eddy on 3/23/18.
 */

interface ItemUIGenerator<T> {
    operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Gizmo
}

interface ItemPositioner<T> {
    operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Rectangle
}

interface TreeUI<T>: Renderer<Tree<T>> {
    val positioner : ItemPositioner<T>
    val uiGenerator: ItemUIGenerator<T>
}

private class BasicPositioner<T>(private val height: Double): ItemPositioner<T> {
    override fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Rectangle {
        return Rectangle(0.0, index * height, tree.width, height)
    }
}

class LabelItemUIGenerator<T>(private val labelFactory: LabelFactory): ItemUIGenerator<T> {
    override fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, selected: Boolean, hasFocus: Boolean, expanded: Boolean): Gizmo {
        val iconWidth = 20.0

        val icon = when (!tree.isLeaf(path)) {
            true -> labelFactory("+").apply {
                fitText             = false
                horizontalAlignment = Center
            }
            false -> null
        }

        val label = labelFactory(node.toString()).apply {
            fitText             = false
            horizontalAlignment = Left
        }

        return object: Box() {
            override fun render(canvas: Canvas) {
                backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorBrush(it)) }
            }
        }.apply<Box> {
            styleChanged += { rerender() }

            children += label
            icon?.let { children += it }

            val layout = constrain(label) { label ->
                label.top    = label.parent.top
                label.left   = label.parent.left + { iconWidth * (1 + (path.depth - if (!tree.rootVisible) 1 else 0)) }
                label.right  = label.parent.right
                label.bottom = label.parent.bottom
            }

            icon?.let {
                children += it

                it.width = iconWidth

                layout.constrain(it, label) { icon, label ->
                    icon.top    = label.top
                    icon.right  = label.left
                    icon.bottom = label.bottom
                }
            }

            this.layout = layout

            var background = if (selected) Color.green else Color.lightgray

            background = when {
                index.isEven -> background.lighter()
                else         -> background
            }

            backgroundColor = background

            mouseChanged += object: MouseListener {
                private var pressed   = false
                private var mouseOver = false

                override fun mouseEntered(event: MouseEvent) {
                    mouseOver = true
                    backgroundColor = backgroundColor?.lighter(0.25f)
                }

                override fun mouseExited(event: MouseEvent) {
                    mouseOver = false
                    backgroundColor = background
                }

                override fun mousePressed(event: MouseEvent) {
                    pressed = true
                    mouseOver = true
                }

                override fun mouseReleased(event: MouseEvent) {
                    if (mouseOver && pressed) {
                        if (!tree.isLeaf(path)) {
                            when (tree.expanded(path)) {
                                true -> tree.collapse(path)
                                else -> tree.expand  (path)
                            }
                        }
//                        setOf(index).also {
//                            tree.apply {
//                                when {
//                                    selected(path)          -> removeSelection(it)
//                                    Ctrl in event.modifiers ||
//                                    Meta in event.modifiers -> addSelection   (it)
//                                    else                    -> setSelection   (it)
//                                }
//                            }
//                        }
                    }
                    pressed = false
                }
            }
        }
    }
}

class AbstractTreeUI<T>(labelFactory: LabelFactory): TreeUI<T> {
    override val positioner: ItemPositioner<T> = BasicPositioner(20.0)

    override val uiGenerator: ItemUIGenerator<T> = LabelItemUIGenerator(labelFactory)

    override fun render(gizmo: Tree<T>, canvas: Canvas) {
        canvas.rect(gizmo.bounds.atOrigin, Pen(red), ColorBrush(white))
    }

    override fun install(gizmo: Tree<T>) {
        gizmo.insets = Insets(2.0)
    }
}

//abstract class AbstractTreeUI<T>(
//        protected val rowHeight: Int,
//        protected val rowFactory: FontFactory): TreeUI<T> {//protected constructor(): AbstractListContainerUI<Tree<T>>(), TreeUI, Model.Listener, Tree<T>.ExpansionListener, SelectionModel.Listener, Listener, PropertyListener {
//
//    protected val nodeUIGenerator: NodeUIGenerator
//        get() = DefaultNodeUIGenerator()
//
//    protected val displayRectPadding: Double
//        get() = (0 * sRowHeight).toDouble()
//
//    private var tree          : Tree<T>? = null
//    private val pathRowMap   : MutableMap<Path<T>, Int>
//    private val pathComparator: Comparator<Path<*>>
//
////    private var mIsEditing: Boolean = false
////    private var mEditingPath: Path<T>? = null
//
//    fun render(canvas: Canvas, tree: Tree<T>) {
//        canvas.rect(tree.bounds.atOrigin, ColorBrush(tree.backgroundColor ?: Color.white))
//    }
//
//    private val selectionChanged: SetObserver<SelectionModel<Path<T>>, Path<T>> = { _,removed,added ->
//
//    }
//
//    private val onExpanded: ExpansionObserver<T> = { tree,paths ->
//        nodesExpanded(tree, paths)
//    }
//
//    private val onCollapsed: ExpansionObserver<T> = { _,paths ->
//
//    }
//
//    override fun install(gizmo: Tree<T>) {
//        super.install(gizmo)
//
//        tree = gizmo.apply {
////            model.addListener(this)
//            selectionModel.onChanged += selectionChanged
//            onExpanded               += this@AbstractTreeUI.onExpanded
//            onCollapsed              += this@AbstractTreeUI.onCollapsed
//        }
//
////        Service.locator().getUIManager().getInstalledTheme().installProperty(SimpleProperty<NodeUIGenerator>(tree.getNodeUIGenerator()), nodeUIGenerator)
//
//        updateNodes()
//    }
//
//    override fun uninstall(gizmo: Tree<T>) {
//        super.uninstall(gizmo)
//
//        gizmo.apply {
//            //            model.addListener(this)
//            selectionModel.onChanged -= selectionChanged
//            onExpanded               -= this@AbstractTreeUI.onExpanded
//            onCollapsed              -= this@AbstractTreeUI.onCollapsed
//        }
//    }
//
////    fun startEditing(tree: Tree<T>, aPath: Path<T>) {
////        startEditing(tree, aPath, null)
////    }
////
////    fun startEditing(tree: Tree<T>, aPath: Path<T>, aEvent: InputEvent?) {
////        if (!mIsEditing || !mEditingPath.equals(aPath)) {
////            val aNodeEditor = mTree.getNodeEditorGenerator()
////
////            if (aNodeEditor != null) {
////                if (isEditing(mTree) && !stopEditing(mTree)) {
////                    return
////                }
////
////                if (aNodeEditor.isEditable(aEvent)) {
////                    mIsEditing = true
////                    mEditingPath = aPath
////
////                    val aIndex = mTree.getRowFromPath(aPath)
////                    val aGizmo = mTree.get(aIndex)
////
////                    val aRow = mRowFactory.createEditorRow(mTree, aPath)
////
////                    aRow.setOwner(this)
////
////                    aRow.setHeight(rowHeight)
////
////                    mTree.queueContainerEvents()
////
////                    mTree.remove(aGizmo)
////                    mTree.insert(aRow, aIndex)
////
////                    mTree.dispatchQueuedContainerEvents()
////
////                    aNodeEditor.addListener(this)
////                }
////            }
////        }
////    }
////
////    fun isEditing(tree: Tree<T>): Boolean {
////        return mIsEditing
////    }
////
////    fun stopEditing(tree: Tree<T>): Boolean {
////        val aNodeEditor = mTree.getNodeEditorGenerator()
////
////        return if (aNodeEditor != null) {
////            aNodeEditor.stopEditing()
////        } else false
////
////    }
////
////    fun cancelEditing(tree: Tree<T>): Boolean {
////        val aNodeEditor = mTree.getNodeEditorGenerator()
////
////        return if (aNodeEditor != null) {
////            aNodeEditor.cancelEditing()
////        } else false
////
////    }
//
////    fun rootChanged(aEvent: Model.Event) {
////        mTree.removeAll()
////
////        mPathRowMap.clear()
////
////        updateNodes()
////    }
////
////    fun nodeRemoved(aEvent: Model.Event) {
////        val aPath = aEvent.getPath()
////        val aParentPath = aPath.getParent()
////        var aParentPathIndex: Int? = mPathRowMap[aParentPath]
////
////        aParentPathIndex = if (aParentPathIndex == null) -1 else aParentPathIndex
////
////        if (mTree.isPathExpanded(aParentPath)) {
////            val aSet = HashSet<Path<Any>>()
////
////            aSet.add(aPath)
////
////            nodesCollapsed(Tree<T>.ExpansionEvent(mTree, aSet))
////        }
////
////        val aRow = mTree.get(mPathRowMap[aPath])
////
////        if (aRow != null) {
////            removeRow(aRow)
////        }
////
////        if (aParentPathIndex >= 0 && (aEvent.getSource() as Model).getNumChildren(aParentPath.getBottomItem()) === 0) {
////            (mTree.get(aParentPathIndex) as TreeRow).updateButton()
////        }
////    }
////
////    fun nodeInserted(aEvent: Model.Event) {
////        val aPath = aEvent.getPath()
////        val aIndex = aEvent.getIndex()
////        val aParentPath = aPath.getParent()
////        var aParentPathIndex: Int? = mPathRowMap[aParentPath]
////
////        aParentPathIndex = if (aParentPathIndex == null) -1 else aParentPathIndex
////
////        if (mTree.isPathExpanded(aParentPath)) {
////            var aRowIndex = aParentPathIndex + 1
////
////            if (aIndex > 0) {
////                val aSiblingPath = aParentPath.getDecendant((aEvent.getSource() as Model).child(aParentPath.getBottomItem(), aIndex - 1))
////
////                aRowIndex = mPathRowMap[aSiblingPath] + mTree.getNumRowsBelowPath(aSiblingPath) + 1
////            }
////
////            addRow(aRowIndex)
////        }
////
////        if (aIndex == 0 && aParentPathIndex >= 0) {
////            (mTree.get(aParentPathIndex) as TreeRow).updateButton()
////        }
////    }
////
////    fun nodeDataChanged(aEvent: Model.Event) {
////        mTree.queueContainerEvents()
////
////        mTree.remove(mTree.get(aEvent.getIndex()))
////
////        addRow(aEvent.getIndex())
////
////        mTree.dispatchQueuedContainerEvents()
////    }
////
////    fun nodesRestructured(aEvent: Model.Event) {
////        // TODO: IMPLEMENT
////    }
//
//    fun nodesExpanded(tree: Tree<T>, paths: Set<Path<T>>) {
//        // Filter descendants since they will be covered
//        // when their parents are added
//
//        val keep     = mutableSetOf(*paths.toTypedArray())
//        val iterator = keep.iterator()
//
//        while (iterator.hasNext()) {
//            val child = iterator.next()
//
//            for (aPath in keep) {
//                if (aPath.isAncestor(child)) {
//                    iterator.remove()
//                    break
//                }
//            }
//        }
//
//        // Sort to ensure that paths are ordered by index (increasing), since
//        // algorithm below assumes this
//
//        val sorted = keep.sortedBy { pathRowMap[it] }
//
//        tree.queueContainerEvents()
//
//        val forceRender = tree.size <= getVisibleIndexEnd()
//
//        for (path in keep) {
//            val rowIndex      = tree.rowFromPath(path)
//            val localRowIndex = pathRowMap[path]
//
//            if (localRowIndex != null && localRowIndex >= 0) {
//                val numNewRows = tree.rowsBelow(path)
//
//                for (i in 0 until numNewRows) {
//                    tree.pathFromRow(rowIndex + i + 1)?.let { addRow(it, localRowIndex + i + 1) }
//                }
//
//                (tree.get(localRowIndex) as TreeRow).updateExpansionState()
//            }
//        }
//
//        // Work-around to force render context resize and prevent clipping
//        // of the newly added items.  This is important since rerenderNow is being
//        // used when display rect change event arrives (which happens before context is enlarged)
//
//        if (forceRender) {
//            tree.rerenderNow()
//        }
//
//        tree.dispatchQueuedContainerEvents()
//    }
//
//    fun nodesCollapsed(tree: Tree<T>, paths: Set<Path<T>>) {
//
//        val remove      = HashSet<Gizmo>()
//        val numChildren = tree.numRows
//
//        tree.queueContainerEvents()
//
//        for (path in paths) {
//            val localRowIndex = pathRowMap[path]
//
//            if (localRowIndex != null && localRowIndex >= 0) {
//                for (i in localRowIndex + 1 until numChildren) {
//                    val aRow = tree.get(i)
//                    val aRowPath = (aRow as TreeRow).path
//
//                    if (path.isAncestor(aRowPath)) {
//                        remove.add(aRow)
//                    } else {
//                        break
//                    }
//                }
//
//                (tree.get(localRowIndex) as TreeRow).updateExpansionState()
//            }
//        }
//
//        for (aGizmo in remove) {
//            removeRow(aGizmo)
//        }
//
//        tree.dispatchQueuedContainerEvents()
//    }
//
////    fun selectionChanged(aEvent: SelectionModel.Event) {
////        val aSelectedPaths = aEvent.getSelectedPaths()
////        val aDeselectedPaths = aEvent.getDeselectedPaths()
////
////        for (aPath in aDeselectedPaths) {
////            val aRowIndex = mPathRowMap[aPath]
////
////            try {
////                val aRow = mTree.get(aRowIndex)
////
////                if (aRow != null) {
////                    (aRow as TreeRow).setSelected(false)
////                }
////            } catch (aException: IndexOutOfBoundsException) {
////            }
////
////        }
////
////        for (aPath in aSelectedPaths) {
////            if (aPath != null) {
////                val aRowIndex = mPathRowMap[aPath]
////
////                try {
////                    val aRow = mTree.get(aRowIndex) as TreeRow
////
////                    aRow?.setSelected(true)
////                } catch (aException: IndexOutOfBoundsException) {
////                }
////
////            }
////        }
////    }
//
////    fun propertyChanged(aEvent: PropertyEvent) {
////        val aProperty = aEvent.getProperty()
////
////        if (aProperty === Tree<T>.ROOT_VISIBILITY) {
////            val aRow = mTree.get(0)
////
////            if (mTree.isRootVisible()) {
////                addRow(0)
////            } else {
////                removeRow(aRow)
////            }
////        } else if (aProperty === Tree<T>.MODEL) {
////            val aOldModel = aEvent.getOldValue() as Model
////            val aNewModel = aEvent.getNewValue() as Model
////
////            if (aOldModel != null) {
////                aOldModel.removeListener(this)
////            }
////            if (aNewModel != null) {
////                aNewModel.addListener(this)
////            }
////        } else if (aProperty === Tree<T>.SELECTION_MODEL) {
////            val aOldModel = aEvent.getOldValue() as SelectionModel
////            val aNewModel = aEvent.getNewValue() as SelectionModel
////
////            if (aOldModel != null) {
////                aOldModel.removeListener(this)
////            }
////            if (aNewModel != null) {
////                aNewModel.addListener(this)
////            }
////        }
////    }
//
////    fun editingStopped(aEvent: ChangeEvent) {
////        val aEditor = aEvent.getSource() as ItemEditor
////
////        aEditor.removeListener(this)
////
////        val aIndex = mTree.getRowFromPath(mEditingPath)
////
////        mTree.queueContainerEvents()
////
////        val aValue = aEditor.getValue()
////
////        mTree.getModel().removeListener(this)
////        mTree.getModel().updateValue(mEditingPath.getBottomItem(), aValue)
////        mTree.getModel().addListener(this)
////
////        mTree.remove(mTree.get(aIndex))
////        addRow(aIndex)
////
////        mTree.dispatchQueuedContainerEvents()
////
////        mIsEditing = false
////    }
////
////    fun editingCancelled(aEvent: ChangeEvent) {
////        (aEvent.getSource() as ItemEditor).removeListener(this)
////
////        val aIndex = mTree.getRowFromPath(mEditingPath)
////
////        mTree.queueContainerEvents()
////
////        mTree.remove(mTree.get(aIndex))
////        addRow(aIndex)
////
////        mTree.dispatchQueuedContainerEvents()
////
////        mIsEditing = false
////    }
//
//    protected fun firstRowAfterPosition(y: Double): Int {
//        return (y / rowHeight).toInt()
//    }
//
//    protected fun lastRowBeforePosition(y: Double): Int {
//        return ceil(y / rowHeight).toInt()
//    }
//
//    private fun updateRows(aStartRow: Int) {
//        var i = aStartRow
//        var aValue = i
//        while (i < tree.numRows) {
//            val aRow = tree.get(i) as TreeRow
//
//            pathRowMap[aRow.path] = aValue++
//            ++i
//        }
//    }
//
//    private fun addRow(index: Int) {
//        tree?.pathFromRow(index)?.let { addRow(it, index) }
//    }
//
//    private fun addRow(aPath: Path<T>, aIndex: Int) {
//        val aRow = rowFactory.createRow(tree, aPath)
//
//        aRow.setHeight(rowHeight)
//
//        super.addRow(tree, aRow, aIndex)
//
//        pathRowMap[aPath] = aIndex
//
//        aRow.setOwner(this)
//
//        updateRows(aIndex + 1)
//    }
//
//    private fun removeRow(aRow: Gizmo) {
//        val paths = mutableSetOf<Path<T>>()
//
//        val path = (aRow as TreeRow).path
//
//        paths.add(path)
//
//        tree?.removeSelection(paths)
//
//        super.removeRow(tree, aRow)
//
//        val index = pathRowMap[path]
//
//        pathRowMap.remove(path)
//
//        updateRows(index ?: 0)
//    }
//
//    private fun updateNodes() {
////        tree.queueContainerEvents()
//
//        (0 until (tree?.numRows ?: 0)).forEach { i -> addRow(i) }
//
////        tree.dispatchQueuedContainerEvents()
//    }
//
//    interface TreeRow<T> {
//        val path: Path<T>
//        fun setOwner(aOwner: TreeUI<T>)
//        fun setSelected(aSelected: Boolean)
//        fun updateButton()
//        fun updateRenderer()
//        fun updateExpansionState()
//    }
//
//    interface RowFactory<T: Gizmo> where T: TreeRow {
//        fun createRow(tree: Tree<T>?, aPath: Path<T>): T
//        fun createEditorRow(tree: Tree<T>, aPath: Path<T>): T
//    }
//
//    private inner class PathComparator : Comparator<Path<*>> {
//        override fun compare(aFirst: Path<*>, aSecond: Path<*>): Int {
//            val aFirstIndex = pathRowMap[aFirst]
//            val aSecondIndex = pathRowMap[aSecond]
//
//            return aFirstIndex - aSecondIndex
//        }
//    }
//
//    init {
//        pathRowMap = HashMap()
//        pathComparator = PathComparator()
//
//        setRowFactory(null)
//    }
//}