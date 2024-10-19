package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.popupmenu.Menu
import io.nacular.doodle.controls.popupmenu.MenuBehavior
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.Stroke.LineCap
import io.nacular.doodle.drawing.Stroke.LineJoint
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.height
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.width
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.inset
import io.nacular.doodle.geometry.path
import io.nacular.doodle.layout.Insets
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times

/**
 * Controls the look/feel of [Menu]s and their subcomponents.
 *
 * @param textMetrics used to measure text dimensions
 * @param pathMetrics used to measure [Path][io.nacular.doodle.geometry.Path] dimensions
 * @param config      that changes how the behavior works
 */
public class BasicMenuBehavior(
    private val textMetrics: TextMetrics,
    private val pathMetrics: PathMetrics,
    private val config     : Config = Config(),
): MenuBehavior() {
    /**
     * Configuration for [BasicMenuBehavior].
     */
    public data class Config(
        val menuInset               : Double        =  5.0,
        val menuRadius              : Double        =  6.0,
        val menuFillPaint           : Paint         = White.paint,
        val itemTextPaint           : Paint         = Black.paint,
        val itemIconWidth           : Double        = 20.0,
        val itemSelectedRadius      : Double        =  4.0,
        val itemIconTextSpacing     : Double        =  6.0,
        val itemHighlightPaint      : Paint         = Color(0xD2DEFAu).paint,
        val itemVerticalPadding     : Double        =  4.0,
        val itemHorizontalPadding   : Double        = 14.0,
        val itemTextSelectedPaint   : Paint         = itemTextPaint,
        val itemDisabledTextPaint   : Paint         = Color(0xA9ADBCu).paint,
        val subMenuIconPaint        : Paint         = Color(0x818593u).paint,
        val subMenuIconSelectedPaint: Paint         = subMenuIconPaint,
        val subMenuIconDisabledPaint: Paint         = itemDisabledTextPaint,
        val subMenuIconTextSpacing  : Double        = itemHorizontalPadding,
        val separatorPaint          : Paint         = (Black opacity 0.07f).paint,
        val separatorHeight         : Double        = 11.0,
        val subMenuIconPath         : Path          = path("M1 1L5 5L1 9")!!,
        val subMenuShowDelay        : Measure<Time> = 100 * milliseconds
    )

    private open inner class BaseItemConfig<T: ItemInfo>(adjustForIcon: Boolean): ItemConfig<T> {
        private val iconAdjustment = if (adjustForIcon) config.itemIconWidth + config.itemIconTextSpacing else 0.0

        private fun textSize(item: T) = textMetrics.size(item.text, item.font)

        override fun preferredSize(item: T): Size {
            val textSize = textSize(item)
            val height   = textSize.height + 2 * config.itemVerticalPadding

            return Size(textSize.width + 2 * config.itemHorizontalPadding + iconAdjustment, height)
        }

        override fun render(item: T, canvas: Canvas) {
            val textSize   = textSize(item)
            val textOffset = config.itemHorizontalPadding + iconAdjustment

            if (item.selected) {
                canvas.rect(
                    Rectangle(size = canvas.size).inset(left = config.menuInset, right = config.menuInset),
                    radius = config.itemSelectedRadius,
                    fill   = config.itemHighlightPaint
                )
            }

            val (transform, at) = when {
                item.mirrored -> Identity.flipHorizontally(canvas.width / 2) to Point(canvas.width - textSize.width - textOffset, (canvas.size.height - textSize.height) / 2)
                else          -> Identity to Point(textOffset, (canvas.size.height - textSize.height) / 2)
            }

            canvas.transform(transform) {
                text(
                    item.text,
                    at   = at,
                    fill = when {
                        !item.enabled -> config.itemDisabledTextPaint
                        item.selected -> config.itemTextSelectedPaint
                        else          -> config.itemTextPaint
                    },
                    font = item.font
                )
            }
        }
    }

    private open inner class ActionItemConfig(adjustForIcon: Boolean): BaseItemConfig<ActionItemInfo>(adjustForIcon) {
        override fun render(item: ActionItemInfo, canvas: Canvas) {
            super.render(item, canvas)

            item.icon?.let {
                val iconSize  = it.size(item)
                var xScale    = config.itemIconWidth / iconSize.width
                var yScale    = iconSize.height      / iconSize.width * xScale
                val maxHeight = canvas.height - 2 * config.itemVerticalPadding

                if (iconSize.height * yScale > maxHeight) {
                    yScale = maxHeight      / iconSize.height
                    xScale = iconSize.width / iconSize.height * yScale
                }

                val at = Point(
                    config.itemHorizontalPadding + (config.itemIconWidth - iconSize.width  * xScale) / 2,
                    config.itemVerticalPadding   + (maxHeight            - iconSize.height * yScale) / 2
                )

                canvas.scale(around = at, xScale, yScale){
                    it.render(item, this, at)
                }
            }
        }
    }

    private inner class PromptMenuItemConfig(adjustForIcon: Boolean): ActionItemConfig(adjustForIcon) {
        override fun preferredSize(item: ActionItemInfo): Size {
            return super.preferredSize(object: ActionItemInfo by item {
                override val text: String get() = itemText(item)
            })
        }

        override fun render(item: ActionItemInfo, canvas: Canvas) {
            super.render(object: ActionItemInfo by item {
                override val text: String get() = itemText(item)
            }, canvas)
        }

        private fun itemText(item: ItemInfo) = "${item.text}..."
    }

    private inner class SubMenuItemConfig(adjustForIcon: Boolean): BaseItemConfig<SubMenuInfo>(adjustForIcon), SubMenuConfig {
        private  val iconSize                     = pathMetrics.size(config.subMenuIconPath)
        override val showDelay              get() = config.subMenuShowDelay
        override val displayInset           get() = 5.0
        override val parentVerticalOffset   get() = MENU_INSETS.top
        override val parentHorizontalOffset get() = 2.0

        override fun preferredSize(item: SubMenuInfo): Size = super.preferredSize(item).run {
            Size(width + iconSize.width + config.subMenuIconTextSpacing, height)
        }

        override fun render(item: SubMenuInfo, canvas: Canvas) {
            super.render(item, canvas)

            if (item.hasChildren) {
                val iconPaint = when {
                    item.selected -> config.subMenuIconSelectedPaint
                    !item.enabled -> config.subMenuIconDisabledPaint
                    else          -> config.subMenuIconPaint
                }

                canvas.translate(Point(canvas.size.width - iconSize.width - config.itemHorizontalPadding - 2.0, (canvas.size.height - iconSize.height) / 2)) {
                    path(
                        config.subMenuIconPath,
                        stroke = Stroke(iconPaint, 1.5, lineJoint = LineJoint.Round, lineCap = LineCap.Round)
                    )
                }
            }
        }
    }

    private val noIconActionConfig  by lazy { ActionItemConfig    (adjustForIcon = false) }
    private val iconActionConfig    by lazy { ActionItemConfig    (adjustForIcon = true ) }
    private val noIconPromptConfig  by lazy { PromptMenuItemConfig(adjustForIcon = false) }
    private val iconPromptConfig    by lazy { PromptMenuItemConfig(adjustForIcon = true ) }
    private val noIconSubMenuConfig by lazy { SubMenuItemConfig   (adjustForIcon = false) }
    private val iconSubMenuConfig   by lazy { SubMenuItemConfig   (adjustForIcon = true ) }
    private val separatorConfig     =  object: SeparatorConfig {
        override fun preferredSize(): Size = Size(0.0, config.separatorHeight)

        override fun render(canvas: Canvas) {
            val middle = canvas.size.height / 2

            canvas.line(
                Point(                    config.itemHorizontalPadding, middle),
                Point(canvas.size.width - config.itemHorizontalPadding, middle),
                Stroke(config.separatorPaint)
            )
        }
    }

    override fun actionConfig   (menu: Menu): ItemConfig<ActionItemInfo> = if (menu.anyItemWithIcon) iconActionConfig  else noIconActionConfig
    override fun promptConfig   (menu: Menu): ItemConfig<ActionItemInfo> = if (menu.anyItemWithIcon) iconPromptConfig  else noIconPromptConfig
    override fun subMenuConfig  (menu: Menu): SubMenuConfig              = if (menu.anyItemWithIcon) iconSubMenuConfig else noIconSubMenuConfig
    override fun separatorConfig(menu: Menu): SeparatorConfig            = separatorConfig

    override fun render(view: Menu, canvas: Canvas) {
        canvas.shadow(MENU_TIGHT_SHADOW) {
            canvas.shadow(MENU_DROP_SHADOW) {
                rect(view.bounds.atOrigin, radius = config.menuRadius, fill = config.menuFillPaint)
            }
        }
    }

    override fun install(view: Menu) {
        super.install(view)

        view.insets = MENU_INSETS
    }

    private companion object {
        private val MENU_INSETS       = Insets(top = 5.0, bottom = 5.0)
        private val MENU_DROP_SHADOW  = OuterShadow(color = Black opacity 0.25f, blurRadius = 10.0, vertical = 3.0)
        private val MENU_TIGHT_SHADOW = OuterShadow(color = Black opacity 0.50f, blurRadius =  0.5                )
    }
}