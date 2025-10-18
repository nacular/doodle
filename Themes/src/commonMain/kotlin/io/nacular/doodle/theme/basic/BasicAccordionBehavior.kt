package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.Accordion
import io.nacular.doodle.controls.AccordionBehavior
import io.nacular.doodle.controls.AccordionItem
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.theme.simpleButtonRenderer
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.layout.ListLayout
import io.nacular.doodle.layout.WidthSource.Parent
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.withSizeInsets
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.NoOpCompletable
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.addOrAppend
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.lerp
import io.nacular.doodle.utils.observable

/**
 * Represents an expandable section within an [Accordion] managed by a [BasicAccordionBehavior].
 */
public abstract class Section<T>: View() {
    /** The section's index within the [Accordion] */
    public abstract var index   : Int

    /** Whether the section is expanded or not */
    public abstract var expanded: Boolean

    /** Notified of changes to the sections expansion progress */
    public abstract val expansionProgressChanged: Pool<(Float) -> Unit>
}

public open class BasicSection<T>(
    private  val accordion          : Accordion<T>,
    override var index              : Int,
    private  val sectionColor       : Color,
    private  val disabledColorMapper: ColorMapper,
    private  val expandedColorMapper: ColorMapper,
    private  var hoverColorMapper   : ColorMapper,
    private  val animateExpansion   : (Boolean, block: (progress: Float) -> Unit) -> Completable = { _, block -> NoOpCompletable.also { block(1f) } },
): Section<T>() {

    private var expansionProgress by observable(0f) { _,new ->
        if (new == 0f) children[1].visible = false

        toggleIcon.expansion = new

        (expansionProgressChanged as SetPool<(Float) -> Unit>).forEach { it(new) }
    }
    private var animation: Completable? by autoCanceling(null)

    private val toggleIcon = AnimatableTreeRowIcon().apply { suggestSize(20.0, 20.0) }

    private val toggleButton = object: ToggleButton() {
        init {
            children += accordion.sectionVisualizer(accordion[index]!!, null, AccordionItem(index, index in accordion.expandedItems))
            children += toggleIcon

            layout = constrain(children[0], toggleIcon) { item, icon ->
                withSizeInsets(width = toggleIcon.width + 2 + 2) { cellAlignment(item) }

                icon.size    eq icon.idealSize
                icon.right   eq parent.right - 2
                icon.centerY eq parent.centerY
            }.also { it.exceptionThrown += { _,e -> e.printStackTrace()} }

            selectedChanged += { _,_,new ->
                when {
                    new  -> accordion.expand  (index)
                    else -> accordion.collapse(index)
                }
            }

            behavior = simpleButtonRenderer { _, canvas ->
                val model     = model
                var fillColor = if (model.selected || model.pressed && model.armed) expandedColorMapper(sectionColor) else sectionColor

                when {
                    !enabled          -> fillColor = disabledColorMapper(fillColor)
                    model.pointerOver -> fillColor = hoverColorMapper(fillColor)
                }

                canvas.rect(bounds.atOrigin, fillColor.paint)
            }
        }
    }

    override val expansionProgressChanged: Pool<(Float) -> Unit> = SetPool()

    override var expanded: Boolean by observable(false) { _,new ->
        toggleButton.selected = new

        if (new) children[1].visible = true

        animation = animateExpansion(expanded) {
            expansionProgress = if (expanded) lerp(0f, 1f, it) else lerp(1f, 0f, it)

            suggestHeight(idealSize.height)
        }
    }

    public var cellAlignment: (ConstraintDslContext.(Bounds) -> Unit) = {
        it.width      lessEq    parent.width strength Strong

        it.left       eq        0
        it.size       eq        it.idealSize
        it.width      greaterEq 0
        it.centerY    eq        parent.centerY
        parent.height eq        it.height + 4
    }

    init {
        children += toggleButton.apply { selected = index in accordion.expandedItems }
        children += accordion.visualizer(accordion[index]!!, null, AccordionItem(index, index in accordion.expandedItems)).apply { visible = toggleButton.selected }
        layout    = constrainLayout(toggleButton, children[1])
    }

    private fun constrainLayout(header: View, body: View) = constrain(header, body) { h, b ->
        h.top    eq 0
        h.left   eq 0
        h.width  eq parent.width
        h.height eq h.idealHeight

        b.top    eq h.bottom
        b.left   eq h.left
        b.width  eq h.width

        b.height eq lerp(0.0, b.idealHeight, expansionProgress)

//        when {
//            expanded -> b.height eq b.idealHeight
//            else     -> b.height eq 0
//        }

        parent.bottom eq b.bottom
    }
}

/**
 * Used by [BasicAccordionBehavior] to generate new [Section]s when needed for an [Accordion].
 */
public interface SectionProducer<T> {
    /**
     * Generates a [Section] for the given item within an [Accordion].
     *
     * @param accordion to generate section for
     * @param item within [accordion]
     * @param index of [item]
     */
    public operator fun invoke(accordion: Accordion<T>, item: T, index: Int): Section<T>
}

public open class BasicSectionProducer<T>(
    protected val sectionColor       : Color       = Color(0xdee1e6u),
    protected val disabledColorMapper: ColorMapper = { it.lighter(    ) },
    protected val expandedColorMapper: ColorMapper = { it.darker (0.1f) },
    protected val hoverColorMapper   : ColorMapper = { it.darker (0.1f) },
    protected val animateExpansion   : (Boolean, block: (progress: Float) -> Unit) -> Completable = { _, block -> NoOpCompletable.also { block(1f) } }
): SectionProducer<T> {
    override fun invoke(accordion: Accordion<T>, item: T, index: Int): BasicSection<T> = BasicSection(
        accordion,
        index,
        sectionColor,
        disabledColorMapper,
        expandedColorMapper,
        hoverColorMapper,
        animateExpansion
    )
}

public open class BasicAccordionBehavior<T>(
    private val sectionProducer: SectionProducer<T>,
    private val backgroundColor: Color = Color(0xdee1e6u),
): AccordionBehavior<T>() {

    override fun install(view: Accordion<T>) {
        val listener = { _: Float -> view.suggestHeight(view.idealSize.height) }

        view.apply {
            view.forEachIndexed { index, item ->
                children.add(sectionProducer(view, item, index).apply {
                    expansionProgressChanged += listener
                })
            }

            layout = ListLayout(widthSource = Parent)
        }
    }

    override fun uninstall(view: Accordion<T>) {
        view.apply {
            children.clear()
            layout = null
        }
    }

    override fun expansionChanged(accordion: Accordion<T>, index: Int, expanded: Boolean) {
        (accordion.children.getOrNull(index) as? Section<T>)?.expanded = expanded
    }

    override fun itemsChanged(accordion: Accordion<T>, differences: Differences<T>) {
        var index = 0

        differences.computeMoves().forEach {
            when (it) {
                is Delete -> {
                    it.items.forEach { item ->
                        when (val destination = it.destination(of = item)) {
                            null -> accordion.children.removeAt(index)
                            else -> accordion.children.move(accordion.children[index], destination)
                        }
                    }
                }
                is Insert -> {
                    it.items.forEach { item ->
                        if (it.origin(of = item) == null) {
                            accordion.children.addOrAppend(index, accordion.visualizer(item, null, AccordionItem(index, index in accordion.expandedItems)))
                            ++index
                        }
                    }
                }
                else      -> { index += it.items.size }
            }
        }
    }

    override fun render(view: Accordion<T>, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, backgroundColor.paint)
    }
}