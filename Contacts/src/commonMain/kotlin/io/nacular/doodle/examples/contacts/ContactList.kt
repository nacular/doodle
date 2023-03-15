package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.table.CellInfo
import io.nacular.doodle.controls.table.CellVisualizer
import io.nacular.doodle.controls.table.ColumnSizePolicy
import io.nacular.doodle.controls.table.ColumnSizePolicy.Column
import io.nacular.doodle.controls.table.DynamicTable
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.blackOrWhiteContrast
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.text.invoke
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Manages/displays the main Contact list.
 *
 * @param assets used to style various parts of the app
 * @param modals to show pop-ups
 * @param appScope for running coroutines
 * @param contacts data model
 * @param navigator for transitioning between various app views
 * @param textMetrics for measuring text
 * @param pathMetrics for measuring paths
 */
class ContactList(
    assets      : AppConfig,
    modals      : Modals,
    appScope    : CoroutineScope,
    contacts    : MutableListModel<Contact>,
    navigator   : Navigator,
    textMetrics : TextMetrics,
    pathMetrics : PathMetrics,
    uiDispatcher: CoroutineDispatcher,
): DynamicTable<Contact, MutableListModel<Contact>>(contacts, SingleItemSelectionModel(), block = {
    // Specifies alignment for the table's cells
    val alignment: ConstraintDslContext.(Bounds) -> Unit = {
        it.left    eq INSET
        it.centerY eq parent.centerY
    }

    // Used to render the name column
    val nameVisualizer = object: CellVisualizer<Contact, String> {
        override fun invoke(item: String, previous: View?, context: CellInfo<Contact, String>) = when (previous) {
            is NameCell -> previous.also { it.update(item) }
            else        -> NameCell(textMetrics, item)
        }
    }

    // Used to render the edit/delete column
    val toolsVisualizer = object: CellVisualizer<Contact, Unit> {
        override fun invoke(item: Unit, previous: View?, context: CellInfo<Contact, Unit>) = when (previous) {
            is ToolCell -> previous.also { it.update(context.selected) }
            else        -> ToolCell(assets, pathMetrics, context.selected).apply {
                onDelete = {
                    appScope.launch(uiDispatcher) {
                        if (modals.confirmDelete(assets, context.item).show()) {
                            contacts.remove(context.item)
                            // show toast
                        }
                    }
                }
                onEdit = {
                    navigator.showContactEdit(context.item)
                }
            }
        }
    }

    column(Label("Name"        ), { name        }, nameVisualizer   ) { cellAlignment = alignment; headerAlignment = alignment                        }
    column(Label("Phone Number"), { phoneNumber }, TextVisualizer() ) { cellAlignment = alignment; headerAlignment = alignment                        }
    column(null,                                   toolsVisualizer  ) { cellAlignment = fill(Insets(top = 20.0, bottom = 20.0, right = 20.0), Strong) }
}) {
    init {
        font = assets.small

        // Controls how the table's columns resize
        columnSizePolicy = object: ColumnSizePolicy {
            override fun layout(tableWidth: Double, columns: List<Column>, startIndex: Int): Double {
                columns[2].width = if (tableWidth > 672.0 - 2 * INSET) 100.0 else 0.0 // FIXME: factor out hard-coded width
                columns[0].width = tableWidth / 2
                columns[1].width = tableWidth - columns[0].width - columns[2].width

                return tableWidth
            }

            override fun changeColumnWidth(tableWidth: Double, columns: List<Column>, index: Int, to: Double) {
                // no-op
            }
        }

        behavior      = ContactListBehavior(assets, navigator)
        acceptsThemes = false
    }
}

/**
 * Renders the Avatar and name for the name column
 */
private class NameCell(private val textMetrics: TextMetrics, value: String): View() {
    init {
        update(value)
    }

    fun update(value: String) {
        if (children.isEmpty()) {
            children += Label("${value.first()}").apply {
                size          = Size(36)
                fitText       = emptySet()
                acceptsThemes = false
            }

            children += Label(value)

            layout = constrain(children[0], children[1]) { icon, name ->
                icon.centerY eq parent.centerY
                name.left    eq icon.right + INSET
                name.centerY eq icon.centerY
            }.then {
                size = Size(children[1].bounds.right, children[0].height)
            }
        }
        (children[0] as Label).apply {
            val circleColor = value.toColor()
            styledText      = blackOrWhiteContrast(circleColor)("${value.first()}")
            behavior        = object: CommonLabelBehavior(textMetrics) {
                override fun render(view: Label, canvas: Canvas) {
                    canvas.circle(Circle(radius = min(width, height) / 2, center = Point(width / 2, height / 2)), fill = circleColor.paint)
                    super.render(view, canvas)
                }
            }
        }

        (children[1] as Label).text = value
    }
}

/**
 * Renders edit/delete buttons within a row when it is selected
 */
private class ToolCell(private val assets: AppConfig, private val pathMetrics: PathMetrics, private var selected: Boolean): View() {
    init {
        update(selected)
    }

    private fun createButton(path: String) = PathIconButton(pathData = path, pathMetrics = pathMetrics).apply {
        size            = Size(24)
        foregroundColor = assets.tool
        pointerChanged  += object: PointerListener {
            override fun entered(event: PointerEvent) { foregroundColor = assets.toolHighlight }
            override fun exited (event: PointerEvent) { foregroundColor = assets.tool          }
        }
    }

    var onEdit  : (() -> Unit)? = null
    var onDelete: (() -> Unit)? = null

    // Called whenever the cell needs to be updated
    fun update(selected: Boolean) {
        if (selected == this.selected) return

        this.selected = selected

        when {
            selected -> {
                children += createButton(assets.editIcon ).apply { fired += { onEdit?.invoke  () } }
                children += createButton(assets.trashIcon).apply { fired += { onDelete?.invoke() } }

                layout = constrain(children[0], children[1]) { edit, delete ->
                    delete.centerY eq parent.centerY
                    delete.right   eq parent.right
                    edit.centerY   eq delete.centerY
                    edit.right     eq delete.left - 4
                }
            }
            else     -> {
                layout = null
                children.clear()
            }
        }
    }
}