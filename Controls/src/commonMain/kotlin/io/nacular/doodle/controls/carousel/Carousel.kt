package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.SimpleIndexedItem
import io.nacular.doodle.controls.carousel.CarouselBehavior.Presenter
import io.nacular.doodle.controls.carousel.CarouselBehavior.Presenter.Position
import io.nacular.doodle.controls.carousel.CarouselBehavior.Transitioner
import io.nacular.doodle.core.Camera
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.geometry.times
import io.nacular.doodle.utils.Cancelable
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Pausable
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.lerp
import io.nacular.doodle.utils.observable
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Provides context about a [Carousel]'s current state to [ItemVisualizer]s.
 *
 * @property numItems              within the Carousel
 * @property targetItem            item the carousel is moving towards
 * @property nearestItem           item currently closest to the "selected" position
 * @property progressToNextItem    how close the Carousel is to transitioning from [nearestItem] to `currentIndex + 1`
 * @property progressToTargetItem  how close the carousel is to [targetItem]
 * @property previousSelectedItem  the carousel's previously selected index
 */
public interface CarouselItem: IndexedItem {
    public val numItems            : Int
    public val targetItem          : Int
    public val nearestItem         : Int
    public val progressToNextItem  : Float
    public val progressToTargetItem: Float
    public val previousSelectedItem: Int
}

/**
 * A visual component that renders and cycles through a list of items of type [T] using a [CarouselBehavior]. Items are
 * obtained via the [model] and presented such that a single item is "selected" at any time. Large ("infinite") lists of
 * items are supported efficiently, since Carousel recycles the Views generated to render its items.
 *
 * Carousel relies heavily on the [Presenter] provided by its behavior for the positioning and visual treatment of
 * its underlying data. Different StageManagers can radically change the way a Carousel looks and functions.
 *
 * @param model that holds the data for this Carousel
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 */
public open class Carousel<T, M: ListModel<T>>(
    private val model: M,
    private val itemVisualizer: ItemVisualizer<T, CarouselItem>
): View() {

    /**
     * An item bound to some data (and a [View]) within a [Carousel] that will be displayed. [Presenter]s
     * create these during [Presenter.present] to decide what is shown within a [Carousel].
     *
     * @property cache    that stateless Presenters can be used to store data across invocations
     * @property clipPath used to clip the item
     */
    public class PresentedItem internal constructor(
        internal val previous            : Int?,
        internal val view                : View,
        internal val index               : Int,
        internal val boundsAlreadyChanged: Boolean,
        public   var cache               : Any?,
        public   var clipPath            : ClipPath? = null
    ) {
        private val initialBounds    = view.prospectiveBounds
        private val initialTransform = view.transform

        internal val boundsChanged    get() = initialBounds    != view.prospectiveBounds
        internal val transformChanged get() = initialTransform != view.transform

        public var x        : Double          get() = view.prospectiveBounds.x;      set(value) { view.x      = value } //by view::x
        public var y        : Double          get() = view.prospectiveBounds.y;      set(value) { view.y      = value } //by view::y
        public var size     : Size            get() = view.prospectiveBounds.size;   set(value) { view.size   = value } //by view::size
        public var width    : Double          get() = view.prospectiveBounds.width;  set(value) { view.width  = value } //by view::width
        public var height   : Double          get() = view.prospectiveBounds.height; set(value) { view.height = value } //by view::height
        public var bounds   : Rectangle       get() = view.prospectiveBounds;        set(value) { view.bounds = value } //by view::bounds
        public var zOrder   : Int             by view::zOrder
        public var camera   : Camera          by view::camera
        public var opacity  : Float           by view::opacity
        public var position : Point           get() = view.prospectiveBounds.position; set(value) { view.position = value } //by view::position
        public var transform: AffineTransform by view::transform

        /**
         * Indicates that the item has been changed in some way that might require
         * the Presenter to refresh any internal state.
         */
        public val itemChanged: Boolean = previous != index || boundsAlreadyChanged

        public fun rerenderNow() { view.rerenderNow() }
    }

    /**
     * Data structure defining where the [previous] and [next] items are located relative to their
     * [Carousel]'s origin. This data is provided to [Transitioner]s at the end of a Carousel's manual movement,
     * so they can decide which item to stop at.
     *
     * @property previous location of the closest previous item
     * @property next location of the closest next item
     */
    public class ItemMarkers internal constructor(public val previous: Vector2D, public val next: Vector2D) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ItemMarkers

            if (previous != other.previous) return false
            return next == other.next
        }

        override fun hashCode(): Int {
            var result = previous.hashCode()
            result = 31 * result + next.hashCode()
            return result
        }
    }

    private inner class PositionImpl(index: Int): Position(index) {
        override val previous: Position? get() = previousIndex(index)?.let { PositionImpl(it) }
        override val next    : Position? get() = nextIndex    (index)?.let { PositionImpl(it) }

        override fun compareTo(other: Position) = compareValues(index, other.index)
    }

    private data class StagedItemState(
        val bounds   : Rectangle,
        val opacity  : Float,
        val camera   : Camera,
        val transform: AffineTransform,
        val zOrder   : Int
    )

    private class CarouselItemImpl(
                     index               : Int,
                     selected            : Boolean,
        override val numItems            : Int,
        override val targetItem          : Int,
        override val nearestItem         : Int,
        override val progressToNextItem  : Float,
        override val progressToTargetItem: Float,
        override val previousSelectedItem: Int,
    ): SimpleIndexedItem(index, selected), CarouselItem

    private class ClipContainer: Container() {
        var clipPath: ClipPath? get() = childrenClipPath; set(new) {
            if (new != childrenClipPath) {
                childrenClipPath = new
                rerenderNow()
            }
        }

        init {
            focusable = false
        }

        override fun contains(point: Point) = clipPath?.let { point in it } ?: super.contains(point)
    }

    private data class DataChild(
        val view         : View,
        val clipPath     : ClipPath?,
        val index        : Int,
        val previousIndex: Int?,
        val cache        : Any?,
        var used         : Boolean = false,
        var boundsChanged: Boolean = false
    )

    @Suppress("PrivatePropertyName")
    private val nearestItemChanged_ by lazy { PropertyObserversImpl<Carousel<T, M>, Int>(this) }

    /**
     * Notifies of changes to the Carousel's [nearestItem].
     */
    public val nearestItemChanged: PropertyObservers<Carousel<T, M>, Int> = nearestItemChanged_

    @Suppress("PrivatePropertyName")
    private val progressChanged_ by lazy { ChangeObserversImpl(this) }

    /**
     * Notifies of changes to the Carousel's progress to its target. Listeners
     * can then check [nearestItem], [progressToTargetItem] and [progressToNextItem] to
     * figure out exactly how the Carousel is positioned.
     */
    public val progressChanged: ChangeObservers<Carousel<T, M>> = progressChanged_

    /**
     * Determines whether the Carousel will wrap its values at the start and end.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public var wrapAtEnds: Boolean by observable(false) { _, _ -> update() }

    /**
     * The item being selected by the Carousel, even if that selection is in progress.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public val targetItem: Int; get() = targetVirtualSelection.mod(numItems)

    /**
     * The previous item selected by the Carousel.
     *
     * @see targetItem
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public val previousSelectedItem: Int; get() = previousVirtualSelection.mod(numItems)

    /**
     * The index the Carousel is closest to displaying in the "selected" slot at this moment.
     * This value changes as the Carousel animates to a new [targetItem].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public val nearestItem: Int get() = floor(trueVirtualIndex).toInt()

    /**
     * Where the Carousel is between [nearestItem] and the next index as it travels
     * to [targetItem]. This value is always within `[0-1]`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public var progressToNextItem: Float = 0f; private set

    /**
     * Where the Carousel is between [previousSelectedItem] and [targetItem] as it travels
     * to the latter. This value is always within `[0-1]`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public var progressToTargetItem: Float = 1f

    /**
     * Number of items in the Carousel
     */
    public val numItems: Int get() = model.size

    /**
     * [CarouselBehavior] that governs the way this Carousel behaves.
     */
    public var behavior: CarouselBehavior<T>? by behavior { _,new ->
        dataChildren.forEach {
            cleanupItem(it)
        }

        dataChildren.clear()

        children.clear()
        supplementaryChildren = emptyList()

        presenter    = new?.presenter
        transitioner = new?.transitioner

        update()
    }

    private var presenter   : Presenter<T>?    = null
    private var transitioner: Transitioner<T>? = null

    private var animation       : Pausable?   by autoCanceling()
    private var moveEndAnimation: Cancelable? by autoCanceling()

    private val dataChildren             = mutableListOf<DataChild>()
    private var changedViews             = mutableSetOf<View>()
    private val viewToIndexMap           = mutableMapOf<View, Int>()
    private var supplementaryChildren    = listOf<View>()
    private val dataChildrenInitialState = mutableMapOf<View, StagedItemState>()

    private val boundsListener: (View, Rectangle, Rectangle) -> Unit = { view,_,_ ->
        if (!ignoreChildBoundsChanges) {
            // FIXME: Make this more efficient
            dataChildren.firstOrNull { it.view == view }?.let {
                it.boundsChanged = true
            }

            update()
        }
    }

    private var trueVirtualIndex        : Float = 0f
    private var targetVirtualSelection  : Int   = 0
    private var previousVirtualSelection: Int   = 0

    private var moveOffset                   = Origin
    private var ignoreCleanup                = false
    private var firstManualMove              = true
    private var nextFrameOffset              = moveOffset
    private var moveEndAnimating             = false
    private var neverMovedForward            = true
    private var offsetWithinFrame            = Origin
    private var previousFrameOffset          = moveOffset
    private var informedBehaviorOfMove       = false
    private var ignoreChildBoundsChanges     = false
    private var offsetWithinFrameAtMoveStart = Origin
    private var currentFrameOffsetDuringMove = nearestItem

    private var manuallyMoving               = false; set(new) {
        if (new) {
            firstManualMove              = true
            moveEndAnimating             = false
            neverMovedForward            = true
            previousFrameOffset          = Origin
            informedBehaviorOfMove       = false
            previousVirtualSelection     = floor(trueVirtualIndex).toInt()
            offsetWithinFrameAtMoveStart = offsetWithinFrame
            currentFrameOffsetDuringMove = nearestItem

            ignoreCleanup = true
            animation?.cancel()
            moveEndAnimation?.cancel()
            ignoreCleanup = false
        }

        field = new
    }

    private val cleanUpSkipCompletable = { _: Completable ->
        targetVirtualSelection   = index(offset = 0, stopAtEndsIfCannotWrap = true) ?: 0
        previousVirtualSelection = targetVirtualSelection
    }

    private val cleanUpSkip = {
        targetVirtualSelection   = index(offset = 0, stopAtEndsIfCannotWrap = true) ?: 0
        previousVirtualSelection = targetVirtualSelection
    }

    init {
        // NOTE
        // +-----------------------------------------------------------------------------------------------------------+
        // There are places where rerenderNow is used to avoid issues where children get out of sync visually. This is
        // unavoidable b/c items can be recycled and used for a different index while still visible due to the fact that
        // call-order is used to decide which item maps to which index in the Presentation. This fact is made trickier
        // because things like transform changes are immediate, while rendering and bounds changes are deferred.
        // The forced re-renders simplify Presenter implementations since they have fewer edge-cases to address.

        boundsChanged += { _, old, new ->
            if (old.size != new.size) {
                update()
            }
        }

        childrenChanged += { _,_ ->
            children.forEach { it.rerenderNow() }
        }

        update()
    }

    /**
     * Gets the value at the given [index].
     */
    public operator fun get(index: Int): Result<T> = model[index]

    /**
     * Moves to the next item if one exists.
     */
    public fun next(): Unit = skip(1)

    /**
     * Moves to the previous item if one exists.
     */
    public fun previous(): Unit = skip(-1)

    /**
     * Skips forward/backward by [amount] to select the item there.
     *
     * @param amount to skip
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun skip(amount: Int) {
        if (amount == 0) return

        stopManualMove()

        index(offset = amount, stopAtEndsIfCannotWrap = true)?.let {
            if (targetVirtualSelection != it) {
                // manually cancel animation so cleanUpSkip is called before calculating new targetVirtualSelection
                animation?.cancel()

                val oldSelection       = targetVirtualSelection
                targetVirtualSelection = if (wrapAtEnds) oldSelection + amount else it

                animation = transitioner?.transition(this, previousVirtualSelection, targetVirtualSelection) { progress ->
                    progressToTargetItem = progress
                    update()
                }?.apply {
                    completed += cleanUpSkipCompletable
                    canceled  += cleanUpSkipCompletable
                }

                if (animation == null) {
                    cleanUpSkip()
                }
            }
        }
    }

    /**
     * Initiates manual movement of the Carousel. This will interrupt any other movement and get
     * the Carousel ready for subsequent calls to [moveManually].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun startManualMove() {
        if (model.isEmpty) return

        manuallyMoving = true
    }

    /**
     * Manually moves the Carousel's contents to some [to]. This will stop any current transition and update
     * the selected item as the values change.
     */
    public fun moveManually(to: Vector2D) {
        if (!manuallyMoving || to.magnitude() == 0.0) return

        moveOffset = -to + offsetWithinFrameAtMoveStart

        when {
            !informedBehaviorOfMove -> {
                informedBehaviorOfMove = true
                transitioner?.moveStarted(this, moveOffset)
            }
            !moveEndAnimating -> transitioner?.moveUpdated(this, moveOffset)
        }

        presenter?.let { presenter ->
            var frameAdjust = 0

            loop@ while (true) {
                ignoreChildBoundsChanges = true

                offsetWithinFrame = moveOffset - previousFrameOffset

                // TODO: Cache this and avoid calling pathToNext if insufficient movement has happened since the last call
                val toNextFrame   = presenter.distanceToNext(this, PositionImpl(nearestItem), offsetWithinFrame) { position ->
                    model[position.index].getOrNull()?.let {
                        val dataChild = getItem(position.index, it, progressToTargetItem)
                        PresentedItem(
                            dataChild.previousIndex,
                            index                = position.index,
                            view                 = dataChild.view,
                            cache                = dataChild.cache,
                            boundsAlreadyChanged = dataChild.boundsChanged,
                            clipPath = null
                        )
                    }
                }

                dataChildren.forEach { it.used = false }

                ignoreChildBoundsChanges = false

                val nextFrameDirection = toNextFrame.direction.normalize().as2d()
                val nextFrameVector    = toNextFrame.magnitude * nextFrameDirection

                val pushFrame = {
                    when {
                        frameAdjust < 0 -> previousFrameOffset -= nextFrameVector
                        else            -> previousFrameOffset += nextFrameVector
                    }
                }

                val progressToNextFrame = (offsetWithinFrame * nextFrameDirection).let {
                    when {
                        it < 0.0 && frameAdjust < 0 -> {
                            pushFrame()
                            toNextFrame.magnitude + it
                        }
                        else -> it
                    }
                }

                if (firstManualMove && progressToNextFrame == 0.0 || toNextFrame.magnitude == 0.0 && progressToNextFrame >= 0.0) {
                    progressToTargetItem  = 1f
                    offsetWithinFrame = Origin
                    update()
                    break@loop
                }

                val fractionToNextFrame = when {
                    toNextFrame.magnitude > 0 -> progressToNextFrame / toNextFrame.magnitude
                    else                      -> -1.0
                }

                nextFrameOffset = previousFrameOffset + nextFrameVector

                frameAdjust = when {
                    fractionToNextFrame < 0 -> -1
                    fractionToNextFrame > 1 ->  1
                    else                    ->  0
                }

                val moveForward = frameAdjust == 1 || fractionToNextFrame >= 1

                if (firstManualMove || neverMovedForward) {
                    firstManualMove = false

                    val delta = when {
                        fractionToNextFrame > 0 -> 1
                        else                    -> 0
                    }

                    index(nearestItem, delta, false)?.let {
                        targetVirtualSelection = nearestItem + delta
                    }

                    neverMovedForward = !wrapAtEnds &&
                            targetVirtualSelection == previousVirtualSelection &&
                            targetVirtualSelection == 0
                }

                val adjustFrame = { offset: Int ->
                    val oldTarget = targetVirtualSelection

                    targetVirtualSelection += offset

                    if (!wrapAtEnds) {
                        targetVirtualSelection = max(0, min(numItems - 1, targetVirtualSelection))
                    }

                    var result = true

                    if (oldTarget == targetVirtualSelection) {
                        progressToTargetItem  = 1f
                        offsetWithinFrame = Origin
                        update()
                        result = false
                    } else {
                        progressToTargetItem = 0f
                    }

                    result
                }

                when  {
                    moveForward -> {
                        if (!adjustFrame(if (previousVirtualSelection - targetVirtualSelection == 1) 2 else 1)) {
                            break
                        }

                        neverMovedForward = false

                        trueVirtualIndex = index(relativeTo = nearestItem, offset = 1, stopAtEndsIfCannotWrap = true)?.toFloat() ?: break

                        ++currentFrameOffsetDuringMove

                        pushFrame()
                    }
                    frameAdjust == -1 -> {
                        if (!adjustFrame(if (targetVirtualSelection - previousVirtualSelection == 1) -2 else -1)) {
                            break
                        }

                        trueVirtualIndex = index(relativeTo = nearestItem, offset = -1, stopAtEndsIfCannotWrap = true)?.toFloat() ?: break

                        --currentFrameOffsetDuringMove
                    }
                    else -> {
                        neverMovedForward = false

                        progressToTargetItem = when (targetVirtualSelection) {
                            previousSelectedItem -> 1f
                            else              -> abs(
                                (currentFrameOffsetDuringMove - previousSelectedItem + fractionToNextFrame) /
                                (targetVirtualSelection - previousSelectedItem)
                            ).toFloat()
                        }

                        update()
                        break@loop
                    }
                }
            }
        }
    }

    /**
     * @see moveManually
     */
    public fun moveManually(x: Double, y: Double): Unit = moveManually(Vector2D(x, y))

    /**
     * Finishes an ongoing manual move and indicates that the Carousel should return to a
     * state where it has selected an item.
     *
     * @see startManualMove
     * @see moveManually
     */
    public fun completeManualMove() {
        if (!manuallyMoving || moveEndAnimating) return

        moveEndAnimating = true
        moveEndAnimation = transitioner?.moveEnded(
            this,
            moveOffset,
            ItemMarkers(previousFrameOffset, nextFrameOffset)
        ) { offset ->
            moveManually(-offset + offsetWithinFrameAtMoveStart)
            ItemMarkers(previousFrameOffset, nextFrameOffset)
        }?.apply {
            completed += { cleanUpMove() }
            canceled  += { cleanUpMove() }
        }

        if (moveEndAnimation == null) {
            cleanUpMove()
        }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    internal fun update() {
        if (model.isEmpty) return

        ignoreChildBoundsChanges = true

        continuousIndex(
            previousVirtualSelection.toFloat(),
            targetVirtualSelection,
            progressToTargetItem,
            true
        )?.let { trueIndex ->
            val oldNearest     = nearestItem
            trueVirtualIndex   = trueIndex
            progressToNextItem = trueIndex - nearestItem

            progressChanged_.forEach { it(this) }

            val newNearest = nearestItem

            if (oldNearest != newNearest) {
                nearestItemChanged_.forEach { it(this, oldNearest, newNearest) }
            }

            presenter?.let { p ->
                val stage = p.present(
                    this,
                    PositionImpl(nearestItem),
                    progressToNextItem,
                    supplementaryChildren
                ) { position ->
                    model[position.index].getOrNull()?.let {
                        val dataChild = getItem(position.index, it, progressToTargetItem)
                        PresentedItem(
                            dataChild.previousIndex,
                            index                = position.index,
                            view                 = dataChild.view,
                            cache                = dataChild.cache,
                            boundsAlreadyChanged = dataChild.boundsChanged,
                            clipPath = null
                        )
                    }
                }

                dataChildren.forEach { it.used = false }

                var anyBoundsChanged    = false
                var anyTransformChanged = false

                stage.items.forEachIndexed { _,item ->
                    anyBoundsChanged    = anyBoundsChanged    || item.boundsChanged
                    anyTransformChanged = anyTransformChanged || item.transformChanged

                    val foundItemIndex = dataChildren.indexOfFirst { !it.used && it.index == item.index }

                    when {
                        foundItemIndex >= 0 -> {
                            val dataChild = dataChildren[foundItemIndex]

                            if (item.view != dataChild.view || item.clipPath != dataChild.clipPath) {
                                dataChildren[foundItemIndex] = DataChild(
                                    previousIndex = dataChild.index,
                                    view          = item.view,
                                    clipPath      = item.clipPath,
                                    index         = item.index,
                                    cache         = item.cache
                                ).apply { used = true }

                                if (item.view != dataChild.view) {
                                    dataChild.view.boundsChanged -= boundsListener
                                    item.view.boundsChanged      += boundsListener
                                }
                            } else {
                                dataChild.used = true
                            }
                        }
                        else -> {
                            dataChildren.add(DataChild(
                                previousIndex = null,
                                view          = item.view,
                                clipPath      = item.clipPath,
                                index         = item.index,
                                cache         = item.cache
                            ).apply { used = true })
                            dataChildrenInitialState[item.view] = item.state()
                            item.view.boundsChanged += boundsListener
                        }
                    }
                }

                val it = dataChildren.iterator()

                while (it.hasNext()) {
                    val dataChild = it.next()

                    if (!dataChild.used) {
                        cleanupItem(dataChild)
                        it.remove()
                    }

                    else dataChild.used = false
                }

                supplementaryChildren = stage.supplementalViews

                children.batch {
                    val finalDataChildren = dataChildren.mapIndexed { index, dataChild ->
                        when (dataChild.clipPath) {
                            null -> dataChild.view
                            else -> when (val current = this.getOrNull(index)) {
                                is ClipContainer -> current.apply {
                                    clipPath = dataChild.clipPath
                                    if (dataChild.view.parent != this) {
                                        when {
                                            children.isNotEmpty() -> children[0] = dataChild.view
                                            else -> children += dataChild.view
                                        }

                                        changedViews += dataChild.view
                                    }
                                }
                                else -> ClipContainer().apply {
                                    clipPath      = dataChild.clipPath
                                    children     += dataChild.view
                                    changedViews += dataChild.view
                                }
                            }.apply {
                                bounds = Rectangle(size = this@Carousel.size)
                            }
                        }
                    }

                    clear()
                    addAll(finalDataChildren)
                    addAll(supplementaryChildren)
                }

                if (anyBoundsChanged && anyTransformChanged) {
                    relayout   ()
                    rerenderNow()
                }

                changedViews.forEach { it.rerenderNow() }
                changedViews.clear()
            }
        }

        ignoreChildBoundsChanges = false
    }

    private fun nextIndex    (after : Int): Int? = index(after,  offset =  1, false)
    private fun previousIndex(before: Int): Int? = index(before, offset = -1, false)

    private fun stopManualMove() {
        moveOffset          = Origin
        manuallyMoving      = false
        moveEndAnimating    = false
        moveEndAnimation    = null
        offsetWithinFrame   = Origin
        previousFrameOffset = Origin
    }

    private fun cleanUpMove() {
        if (ignoreCleanup) return

        stopManualMove()
        progressToNextItem         = 0f
        progressToTargetItem       = 1f
        targetVirtualSelection = round(trueVirtualIndex).toInt()

        update()

        previousVirtualSelection = targetVirtualSelection
    }

    private fun cleanupItem(item: DataChild) {
        val view = item.view

        changedViews       -= view
        viewToIndexMap     -= view
        view.boundsChanged -= boundsListener

        dataChildrenInitialState.remove(view)?.apply {
            view.camera    = camera
            view.bounds    = bounds
            view.zOrder    = zOrder
            view.opacity   = opacity
            view.transform = transform
        }
    }

    private fun getItem(itemIndex: Int, value: T, progress: Float): DataChild {
        val recycledChild = dataChildren.firstOrNull {
            !it.used && it.index == itemIndex
        }?.also {
            val oldIndex = viewToIndexMap[it.view]
            viewToIndexMap[it.view] = itemIndex

            if (oldIndex != null && oldIndex != itemIndex) {
                changedViews += it.view
            }

            it.used = true
        }

        return DataChild(
            previousIndex = recycledChild?.index,
            view          = itemVisualizer(
                value,
                recycledChild?.view,
                CarouselItemImpl(
                    index            = itemIndex,
                    numItems         = numItems,
                    selected         = itemIndex == targetItem,
                    targetItem      = targetItem,
                    nearestItem     = nearestItem,
                    previousSelectedItem    = previousSelectedItem,
                    progressToNextItem   = progressToNextItem,
                    progressToTargetItem = progress,
                )
            ),
            clipPath = recycledChild?.clipPath,
            index    = itemIndex,
            cache    = recycledChild?.cache
        )
    }

    private fun index(relativeTo: Int = targetVirtualSelection, offset: Int, stopAtEndsIfCannotWrap: Boolean): Int? {
        val result = relativeTo + offset

        return when {
            wrapAtEnds                       -> result.mod(numItems)
            stopAtEndsIfCannotWrap           -> max(0, min(numItems - 1, result))
            result in 0 until numItems -> result
            else                             -> null
        }
    }

    private fun continuousIndex(from: Float, to: Int, progress: Float, stopAtEndsIfCannotWrap: Boolean): Float? = when (progress) {
        0f   -> from
        else -> {
            when {
                wrapAtEnds -> lerp(from, to.toFloat(), progress).mod(numItems.toFloat())
                else       -> {
                    val nearestIndex = floor(from).toInt()

                    index(nearestIndex, to - nearestIndex, stopAtEndsIfCannotWrap)?.let {
                        max(0f, min((numItems - 1).toFloat(), lerp(from, it.toFloat(), progress)))
                    }
                }
            }
        }
    }

    private fun PresentedItem.state() = StagedItemState(
        bounds    = bounds,
        opacity   = opacity,
        camera    = camera,
        transform = transform,
        zOrder    = zOrder,
    )
}