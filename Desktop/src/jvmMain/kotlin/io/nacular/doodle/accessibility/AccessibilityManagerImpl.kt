package io.nacular.doodle.accessibility

import io.nacular.doodle.controls.ProgressBar
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.controls.panels.TabbedPanel
import io.nacular.doodle.controls.range.RangeValueSlider
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.range.ValueSlider
import io.nacular.doodle.controls.spinbutton.SpinButton
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.core.View
import io.nacular.doodle.core.impl.DisplaySkiko
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.theme.native.toAwt
import io.nacular.doodle.theme.native.toDoodle
import io.nacular.doodle.utils.BoxOrientation.Bottom
import io.nacular.doodle.utils.BoxOrientation.Left
import io.nacular.doodle.utils.BoxOrientation.Right
import io.nacular.doodle.utils.BoxOrientation.Top
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Insert
import org.jetbrains.skia.BreakIterator
import org.jetbrains.skia.BreakIterator.Companion.DONE
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.event.FocusListener
import javax.accessibility.Accessible
import javax.accessibility.AccessibleAction
import javax.accessibility.AccessibleAction.CLICK
import javax.accessibility.AccessibleAction.DECREMENT
import javax.accessibility.AccessibleAction.INCREMENT
import javax.accessibility.AccessibleComponent
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleContext.ACCESSIBLE_STATE_PROPERTY
import javax.accessibility.AccessibleRole
import javax.accessibility.AccessibleRole.CHECK_BOX
import javax.accessibility.AccessibleRole.HYPERLINK
import javax.accessibility.AccessibleRole.LABEL
import javax.accessibility.AccessibleRole.LIST
import javax.accessibility.AccessibleRole.LIST_ITEM
import javax.accessibility.AccessibleRole.PAGE_TAB
import javax.accessibility.AccessibleRole.PAGE_TAB_LIST
import javax.accessibility.AccessibleRole.PANEL
import javax.accessibility.AccessibleRole.PROGRESS_BAR
import javax.accessibility.AccessibleRole.PUSH_BUTTON
import javax.accessibility.AccessibleRole.RADIO_BUTTON
import javax.accessibility.AccessibleRole.SLIDER
import javax.accessibility.AccessibleRole.SPIN_BOX
import javax.accessibility.AccessibleRole.TEXT
import javax.accessibility.AccessibleRole.TOGGLE_BUTTON
import javax.accessibility.AccessibleRole.TREE
import javax.accessibility.AccessibleSelection
import javax.accessibility.AccessibleState
import javax.accessibility.AccessibleState.CHECKED
import javax.accessibility.AccessibleState.ENABLED
import javax.accessibility.AccessibleState.FOCUSABLE
import javax.accessibility.AccessibleState.FOCUSED
import javax.accessibility.AccessibleState.HORIZONTAL
import javax.accessibility.AccessibleState.INDETERMINATE
import javax.accessibility.AccessibleState.SELECTED
import javax.accessibility.AccessibleState.SHOWING
import javax.accessibility.AccessibleState.VERTICAL
import javax.accessibility.AccessibleState.VISIBLE
import javax.accessibility.AccessibleStateSet
import javax.accessibility.AccessibleText
import javax.accessibility.AccessibleText.CHARACTER
import javax.accessibility.AccessibleText.SENTENCE
import javax.accessibility.AccessibleText.WORD
import javax.accessibility.AccessibleValue
import javax.swing.text.AttributeSet
import java.awt.Color as AwtColor
import java.awt.Cursor as AwtCursor
import java.awt.Point as AwtPoint
import java.awt.Rectangle as AwtRectangle

/**
 * Created by Nicholas Eddy on 5/30/24.
 */
internal class AccessibilityManagerImpl(
    private val focusManager: FocusManager,
    private val viewFinder: ViewFinder
): AccessibilityManagerSkiko() {

    override fun roleUpdated(view: View) {
        cache.entries.filter { it.key.second == view }.forEach { (_, accessible) ->
            accessible?.accessibleContext?.firePropertyChange(ACCESSIBLE_STATE_PROPERTY, false, true)
        }
    }

    override fun accessibilityContext(display: DisplaySkiko): AccessibleContext = object: AccessibleContext(), AccessibleComponent {
        // TODO: Handle popups

        init {
            display.childrenChanged += { _,diffs ->
                diffs.computeMoves()

                diffs.forEach { diff ->
                    when (diff) {
                        is Insert<View> -> diff.items.forEach {
                            firePropertyChange(ACCESSIBLE_CHILD_PROPERTY, null, getAccessible(display, it))
                        }
                        is Delete<View> -> diff.items.forEach {
                            cache[display to it]?.let { accessible ->
                                firePropertyChange(ACCESSIBLE_CHILD_PROPERTY, accessible, null)
                            }
                        }
                        else         -> {}
                    }
                }
            }
        }

        override fun getAccessibleRole() = PANEL

        override fun getAccessibleStateSet() = AccessibleStateSet().apply {
            add(ENABLED)
            add(VISIBLE)
            add(SHOWING)
        }

        override fun getAccessibleComponent() = this

        override fun getAccessibleIndexInParent() = display.indexInParent

        override fun getAccessibleChildrenCount() = display.children.size

        override fun getAccessibleChild(i: Int) = display.children.getOrNull(i)?.let {
            getAccessible(display, of = it)
        }

        /**
         * Gets the Accessible descendant at this point. This is different from the docs say (get only children) because
         * AWT resolves the right Component at a point first, then digs into the accessibility hierarchy for it. That means
         * it will resolve the top-level Skiko component only, and then this would only return top-level children if it did
         * not do a full descent.
         */
        override fun getAccessibleAt(p: AwtPoint) = viewFinder.find(display, p.toDoodle()) { true }?.let {
            getAccessible(display, it) { view ->
                view.accessibilityRole != null || view is Label
            }
        }

        override fun getLocale() = null

        override fun isEnabled() = true

        override fun isVisible() = true

        override fun isShowing() = true

        override fun contains(p: AwtPoint?) = p in bounds

        override fun getLocationOnScreen() = display.locationOnScreen.toAwt()

        override fun getLocation() = Origin.toAwt()

        override fun getBounds() = Rectangle(display.size).toAwt()

        override fun getSize() = display.size.toAwt()

        override fun isFocusTraversable() = false

        override fun getBackground(): Color {
            TODO("Not yet implemented")
        }

        override fun setBackground(c: Color?) {
            TODO("Not yet implemented")
        }

        override fun getForeground(): Color {
            TODO("Not yet implemented")
        }

        override fun setForeground(c: Color?) {
            TODO("Not yet implemented")
        }

        override fun getCursor(): Cursor {
            TODO("Not yet implemented")
        }

        override fun setCursor(cursor: Cursor?) {
            TODO("Not yet implemented")
        }

        override fun getFont(): Font {
            TODO("Not yet implemented")
        }

        override fun setFont(f: Font?) {
            TODO("Not yet implemented")
        }

        override fun getFontMetrics(f: Font?): FontMetrics {
            TODO("Not yet implemented")
        }

        override fun setEnabled(b: Boolean) {
            TODO("Not yet implemented")
        }

        override fun setVisible(b: Boolean) {
            TODO("Not yet implemented")
        }

        override fun setLocation(p: java.awt.Point?) {
            TODO("Not yet implemented")
        }

        override fun setBounds(r: AwtRectangle?) {
            TODO("Not yet implemented")
        }

        override fun setSize(d: Dimension?) {
            TODO("Not yet implemented")
        }

        override fun requestFocus() {
            TODO("Not yet implemented")
        }

        override fun addFocusListener(l: FocusListener?) {
            TODO("Not yet implemented")
        }

        override fun removeFocusListener(l: FocusListener?) {
            TODO("Not yet implemented")
        }
    }

    private class AccessibleTextImpl(private val text: String): AccessibleText {
        private fun partToBreakIterator(part: Int): BreakIterator {
            val iter = when (part) {
                SENTENCE   -> BreakIterator.makeSentenceInstance ()
                WORD       -> BreakIterator.makeWordInstance     ()
                CHARACTER  -> BreakIterator.makeCharacterInstance()
                else       -> throw IllegalArgumentException     ()
            }
            iter.setText(text)
            return iter
        }

        override fun getIndexAtPoint(p: java.awt.Point?): Int {
            TODO("Not yet implemented")
        }

        override fun getCharacterBounds(i: Int): java.awt.Rectangle {
            TODO("Not yet implemented")
        }

        override fun getCharCount() = text.length

        // FIXME
        override fun getCaretPosition() = -1

        override fun getAtIndex(part: Int, index: Int): String {
            return when (val end = partToBreakIterator(part).following(index)) {
                DONE -> ""
                else -> text.subSequence(index, end).toString()
            }
        }

        override fun getAfterIndex(part: Int, index: Int): String {
            val iterator = partToBreakIterator(part)
            var start = index

            do {
                start = iterator.following(start)
                if (start == DONE) return ""
            } while (text[start] == ' ' || text[start] == '\n')

            val end = when (val end = iterator.next()) {
                DONE -> iterator.last()
                else -> end
            }

            return text.subSequence(start, end).toString()
        }

        override fun getBeforeIndex(part: Int, index: Int): String {
            return when (val start = partToBreakIterator(part).preceding(index)) {
                DONE -> ""
                else -> text.subSequence(start, index).toString()
            }
        }

        override fun getCharacterAttribute(i: Int): AttributeSet {
            TODO("Not yet implemented")
        }

        override fun getSelectionStart(): Int {
            TODO("Not yet implemented")
        }

        override fun getSelectionEnd(): Int {
            TODO("Not yet implemented")
        }

        // FIXME
        override fun getSelectedText() = ""
    }

    private open inner class AccessibleViewContext(private val display: DisplaySkiko, private val view: View): AccessibleContext(), AccessibleComponent {
        private var value: AccessibleValue? = null

        private fun booleanStateChange(state: AccessibleState) = { _:View, old: Boolean, new: Boolean -> fireStateChanged(old, new, state) }

        private fun fireStateChanged(old: Boolean, new: Boolean, state: AccessibleState) {
            firePropertyChange(
                ACCESSIBLE_STATE_PROPERTY,
                if (old) state else null,
                if (new) state else null
            )
        }

        private fun fireValueChanged(old: Number, new: Number) {
            firePropertyChange(ACCESSIBLE_VALUE_PROPERTY, old, new)
        }

        init {
            // TODO: Consider whether these can be done via the sync* notifications on AccessibilityManagerImpl instead. That would remove the need for additional listeners
            view.focusChanged        += booleanStateChange(FOCUSED  )
            view.enabledChanged      += booleanStateChange(ENABLED  )
            view.visibilityChanged   += booleanStateChange(VISIBLE  )
            view.focusabilityChanged += booleanStateChange(FOCUSABLE)
            view.boundsChanged       += { _,old,new ->
                firePropertyChange(ACCESSIBLE_COMPONENT_BOUNDS_CHANGED, old.toAwt(), new.toAwt())
            }

            view.childrenChanged += { _,diffs ->
                diffs.computeMoves()

                diffs.forEach { diff ->
                    when (diff) {
                        is Insert<View> -> diff.items.forEach {
                            firePropertyChange(ACCESSIBLE_CHILD_PROPERTY, null, getAccessible(display, it))
                        }
                        is Delete<View> -> diff.items.forEach {
                            cache[display to it]?.let { accessible ->
                                firePropertyChange(ACCESSIBLE_CHILD_PROPERTY, accessible, null)
                            }
                        }
                        else         -> {}
                    }
                }
            }

            when (view) {
                is ProgressBar    -> view.changed            += { _,o,n -> fireValueChanged(o, n) }
                is ToggleButton   -> view.selectedChanged    += { _,o,n -> fireValueChanged(if (o) 1 else 0, if (n) 1 else 0) }
                is ValueSlider<*> -> view.model.valueChanged += { _,o,n -> fireValueChanged(value?.currentAccessibleValue ?: 0, view.fraction) }
            }
        }

        override fun getAccessibleRole(): AccessibleRole = when (view.accessibilityRole) {
            is TabRole          -> PAGE_TAB
            is LinkRole         -> HYPERLINK
            is ListRole         -> LIST
            is TreeRole         -> TREE
            is RadioRole        -> RADIO_BUTTON
//            is SwitchRole       -> object: AccessibleRole("switch") {}
            is SliderRole       -> SLIDER
            is TextBoxRole      -> TEXT
            is TabListRole      -> PAGE_TAB_LIST
            is ListItemRole     -> LIST_ITEM
            is CheckBoxRole     -> CHECK_BOX
            is ProgressBarRole  -> PROGRESS_BAR
            is ToggleButtonRole -> TOGGLE_BUTTON
            is ButtonRole       -> PUSH_BUTTON
            is SpinButtonRole   -> SPIN_BOX
//          is ImageRole        -> ICON
            else                -> when (view) {
                is Label -> LABEL
                else     -> PANEL
            }
        }

        override fun getAccessibleComponent() = this

        override fun getAccessibleStateSet() = AccessibleStateSet().apply {
            if (view.enabled  ) add(ENABLED  )
            if (view.visible  ) add(VISIBLE  )
            if (view.hasFocus ) add(FOCUSED  )
            if (isShowing()   ) add(SHOWING  )
            if (view.focusable) add(FOCUSABLE)

            when (val role = view.accessibilityRole) {
                is ToggleButtonRole -> if (role.pressed) add(CHECKED)
                is SliderRole       -> when (role.orientation) {
                    Horizontal -> add(HORIZONTAL)
                    Vertical   -> add(VERTICAL  )
                    else       -> {}
                }
                is ListItemRole     -> if (role.selected) add(SELECTED)
                is TreeItemRole     -> if (role.selected) add(SELECTED)
                else -> {}
            }

            var orientation: Orientation? = null

            when (view) {
                is CheckBox       -> if (view.indeterminate) add(INDETERMINATE)
                is Slider<*>      -> orientation = view.orientation
                is SplitPanel     -> orientation = view.orientation
                is TabbedPanel<*> -> orientation = when (view.orientation) {
                    Top, Bottom -> Horizontal
                    Left, Right -> Vertical
                }
            }

            when (orientation) {
                Horizontal -> add(HORIZONTAL)
                Vertical   -> add(VERTICAL  )
                else       -> {}
            }
        }

        override fun getAccessibleValue(): AccessibleValue? = when (view) {
            is ToggleButton   -> object: AccessibleValue {
                override fun getCurrentAccessibleValue() = when {
                    view.selected -> 1
                    else          -> 0
                }

                override fun setCurrentAccessibleValue(n: Number?): Boolean {
                    view.selected = when (n) {
                        1 -> true
                        else -> false
                    }

                    return true
                }

                override fun getMinimumAccessibleValue() = 0
                override fun getMaximumAccessibleValue() = 1
            }

            is ProgressBar    -> object: AccessibleValue {
                override fun getCurrentAccessibleValue() = view.progress
                override fun setCurrentAccessibleValue(n: Number?): Boolean {
                    TODO("Not yet implemented")
                }

                override fun getMinimumAccessibleValue() = 0f
                override fun getMaximumAccessibleValue() = 1f
            }

            is ValueSlider<*> -> object: AccessibleValue {
                override fun getCurrentAccessibleValue() = view.fraction
                override fun setCurrentAccessibleValue(n: Number?): Boolean {
                    TODO("Not yet implemented")
                }

                override fun getMinimumAccessibleValue() = 0f
                override fun getMaximumAccessibleValue() = 1f
            }

            else -> null
        }.also { value = it }

        override fun getAccessibleName() = view.accessibilityLabel ?: when (view) {
            is Label  -> view.text
            is Button -> view.text
            else      -> null
        }

        override fun getAccessibleDescription() = view.accessibilityDescriptionProvider?.accessibilityLabel

        override fun getAccessibleText() = when (view) {
            is Label     -> AccessibleTextImpl(view.text)
            is Button    -> AccessibleTextImpl(view.text)
            is TextField -> AccessibleTextImpl(view.text)
            else         -> null
        }

        override fun getAccessibleIndexInParent() = view.indexInParent

        override fun getAccessibleChildrenCount() = view.numChildren

        private var _accessibleAction: AccessibleAction? = null

        override fun getAccessibleAction(): AccessibleAction? {
            val actions = mutableListOf<Pair<String, () -> Unit>>()

            when (view) {
                is Button         -> actions += CLICK to { view.click() }
                is ValueSlider<*> -> actions += listOf(
                    INCREMENT to { view.increment() },
                    DECREMENT to { view.decrement() }
                )
                is SpinButton<*,*>   -> actions += listOf(
                    INCREMENT to { view.next    () },
                    DECREMENT to { view.previous() }
                )
                is RangeValueSlider<*> -> actions += listOf(
                    "increment start" to { view.incrementStart() },
                    "increment end"   to { view.incrementEnd  () },
                    "decrement start" to { view.decrementStart() },
                    "decrement end"   to { view.decrementEnd  () }
                )
            }

            if (actions.isEmpty()) return null

            return object: AccessibleAction {
                override fun getAccessibleActionCount      (      ) = actions.size
                override fun getAccessibleActionDescription(i: Int) = actions[i].first
                override fun doAccessibleAction            (i: Int): Boolean {
                    actions[i].second()
                    return true
                }
            }.also { _accessibleAction = it }
        }

        override fun getAccessibleChild(i: Int) = view.child(i)?.let {
            getAccessible(display, of = it) { true }
        }

        override fun getLocale() = null

        override fun getBackground() = view.backgroundColor?.toAwt()
        override fun setBackground(c: AwtColor?) { view.backgroundColor = c?.toDoodle() }

        override fun getForeground() = view.foregroundColor?.toAwt()
        override fun setForeground(c: AwtColor?) { view.foregroundColor = c?.toDoodle() }

        override fun isEnabled() = view.enabled
        override fun setEnabled(b: Boolean) { view.enabled = b }

        override fun isVisible() = view.visible
        override fun setVisible(b: Boolean) { view.visible = b }

        override fun isShowing(): Boolean {
            var v = view as View?

            while (v != null) {
                if (!v.visible) return false

                v = v.parent
            }

            return true
        }

        override fun contains(p: AwtPoint) = p.toDoodle() in view.bounds

        override fun getLocationOnScreen(): AwtPoint {
            val points = view.bounds.atOrigin.points.map { view.toAbsolute(it) }

            return (display.locationOnScreen + ConvexPolygon(points[0], points[1], points[2], points[3]).boundingRectangle.position).toAwt()
        }

        override fun getLocation(): AwtPoint = bounds.location

        override fun setLocation(p: AwtPoint?) {
            p?.toDoodle()?.let { view.position = it }
        }

        override fun getBounds() = view.boundingBox.toAwt()

        override fun setBounds(r: AwtRectangle) {
            view.bounds = r.toDoodle()
        }

        override fun getSize() = view.size.toAwt()

        override fun setSize(d: Dimension) {
            view.size = d.toDoodle()
        }

        /**
         * Gets the Accessible descendant at this point. This is different from the docs say (get only children) because
         * AWT resolves the right Component at a point first, then digs into the accessibility hierarchy for it. That means
         * it will resolve the top-level Skiko component only, and then this would only return top-level children if it did
         * not do a full descent.
         */
        override fun getAccessibleAt(p: AwtPoint) = getAccessible(display, viewFinder.find(within = view, at = p.toDoodle())) {
            it.accessibilityRole != null || it is Label
        }

        override fun isFocusTraversable() = view.focusable

        override fun requestFocus() {
            focusManager.requestFocus(view)
        }

        override fun getCursor(): AwtCursor {
            TODO("Not yet implemented")
        }

        override fun setCursor(cursor: AwtCursor?) {
            TODO("Not yet implemented")
        }

        override fun getFont(): Font {
            TODO("Not yet implemented")
        }

        override fun setFont(f: Font?) {
            TODO("Not yet implemented")
        }

        override fun getFontMetrics(f: Font?): FontMetrics {
            TODO("Not yet implemented")
        }

        override fun addFocusListener(l: FocusListener?) {
            TODO("Not yet implemented")
        }

        override fun removeFocusListener(l: FocusListener?) {
            TODO("Not yet implemented")
        }
    }

    private inner class AccessibleListContext(display: DisplaySkiko, private val list: List<*, *>): AccessibleViewContext(display, list), AccessibleSelection {
        init {
            list.selectionChanged += { _,_,_ ->
                firePropertyChange(ACCESSIBLE_SELECTION_PROPERTY, false, true)
            }
        }

        override fun getAccessibleSelection(): AccessibleSelection = this

        override fun getAccessibleSelectionCount() = list.selection.size

        override fun getAccessibleSelection(i: Int) = when {
            list.selected(i) -> getAccessibleChild(i)
            else             -> null
        }

        override fun isAccessibleChildSelected(i: Int) = list.selected(i)

        override fun addAccessibleSelection(i: Int) {
            list.addSelection(setOf(i))
        }

        override fun removeAccessibleSelection(i: Int) {
            list.removeSelection(setOf(i))
        }

        override fun clearAccessibleSelection() {
            list.clearSelection()
        }

        override fun selectAllAccessibleSelection() {
            list.selectAll()
        }
    }

    private inner class AccessibleTreeContext(display: DisplaySkiko, private val tree: Tree<*, *>): AccessibleViewContext(display, tree), AccessibleSelection {
        init {
            tree.selectionChanged += { _, _, _ ->
                firePropertyChange(ACCESSIBLE_SELECTION_PROPERTY, false, true)
            }
        }

        override fun getAccessibleSelection(): AccessibleSelection = this

        override fun getAccessibleSelectionCount() = tree.selection.size

        override fun getAccessibleSelection(i: Int) = when {
            tree.selected(i) -> getAccessibleChild(i)
            else             -> null
        }

        override fun isAccessibleChildSelected(i: Int) = tree.selected(i)

        override fun addAccessibleSelection(i: Int) {
            tree.addSelection(setOf(i))
        }

        override fun removeAccessibleSelection(i: Int) {
            tree.removeSelection(setOf(i))
        }

        override fun clearAccessibleSelection() {
            tree.clearSelection()
        }

        override fun selectAllAccessibleSelection() {
            tree.selectAll()
        }
    }

    private val cache = mutableMapOf<Pair<DisplaySkiko, View>, Accessible?>()

    private lateinit var displayChanged: (View, Boolean, Boolean) -> Unit

    init {
        displayChanged = { view: View, _: Boolean, new: Boolean ->
            if (!new) {
                cache.entries.removeIf { it.key.second == view }
                view.displayChange -= displayChanged
            }
        }
    }

    private class CustomAccessible(private val context: AccessibleContext): Accessible {
        override fun getAccessibleContext() = context
    }

    private fun getAccessible(
        display  : DisplaySkiko,
        of       : View,
        predicate: (View) -> Boolean = { true }
    ): Accessible? = cache.getOrPut(display to of) {
        when {
            predicate(of) -> {
                of.displayChange += displayChanged

                CustomAccessible(when (of) {
                    is List<*,*> -> AccessibleListContext(display, of)
                    is Tree<*,*> -> AccessibleTreeContext(display, of)
                    else         -> AccessibleViewContext(display, of)
                })
            }
            else -> when (val p = of.parent) {
                null -> null
                else -> getAccessible(display, p, predicate)
            }
        }
    }
}