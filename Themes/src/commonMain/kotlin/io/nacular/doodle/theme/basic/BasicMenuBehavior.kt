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
        val menuInset               : Double =  5.0,
        val menuRadius              : Double =  6.0,
        val menuFillPaint           : Paint  = White.paint,
        val itemTextPaint           : Paint  = Black.paint,
        val itemSelectedRadius      : Double =  4.0,
        val itemHighlightPaint      : Paint  = Color(0xD2DEFAu).paint,
        val itemVerticalPadding     : Double =  4.0,
        val itemHorizontalPadding   : Double = 14.0,
        val itemTextSelectedPaint   : Paint  = itemTextPaint,
        val itemDisabledTextPaint   : Paint  = Color(0xA9ADBCu).paint,
        val subMenuIconPaint        : Paint  = Color(0x818593u).paint,
        val subMenuIconSelectedPaint: Paint  = subMenuIconPaint,
        val separatorPaint          : Paint  = (Black opacity 0.07f).paint,
        val separatorHeight         : Double = 11.0,
        val subMenuIconPath         : Path   = path("M1 1L5 5L1 9")!!,
        val subMenuShowDelay        : Measure<Time> = 100 * milliseconds,
    )

    private open inner class BaseItemConfig<T: ItemInfo>: ItemConfig<T> {
        override fun preferredSize(item: T): Size {
            val textSize = textMetrics.size(item.text)
            val height   = textSize.height + 2 * config.itemVerticalPadding

            return Size(textSize.width + 2 * config.itemHorizontalPadding, height)
        }

        override fun render(item: T, canvas: Canvas) {
            val textSize = textMetrics.size(item.text)

            if (item.selected) {
                canvas.rect(
                    Rectangle(size = canvas.size).inset(left = config.menuInset, right = config.menuInset),
                    radius = config.itemSelectedRadius,
                    fill   = config.itemHighlightPaint
                )
            }

            val (transform, at) = when {
                item.mirrored -> Identity.flipHorizontally(canvas.width / 2) to Point(canvas.width - textSize.width - config.itemHorizontalPadding, (canvas.size.height - textSize.height) / 2)
                else          -> Identity to Point(config.itemHorizontalPadding, (canvas.size.height - textSize.height) / 2)
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

    private val actionConfig = object: BaseItemConfig<ItemInfo>() {}

    private val promptConfig = object: BaseItemConfig<ItemInfo>() {
        override fun preferredSize(item: ItemInfo): Size {
            return super.preferredSize(object: ItemInfo by item {
                override val text: String get() = itemText(item)
            })
        }

        override fun render(item: ItemInfo, canvas: Canvas) {
            super.render(object: ItemInfo by item {
                override val text: String get() = itemText(item)
            }, canvas)
        }

        private fun itemText(item: ItemInfo) = "${item.text}..."
    }

    private val subMenuConfig = object: BaseItemConfig<SubMenuInfo>(), SubMenuConfig {
        private val iconSize         = pathMetrics.size(config.subMenuIconPath)
        override val showDelay get() = config.subMenuShowDelay

        override fun render(item: SubMenuInfo, canvas: Canvas) {
            super.render(item, canvas)

            if (item.hasChildren) {
                val iconPaint = if (item.selected) config.subMenuIconSelectedPaint else config.subMenuIconPaint

                canvas.translate(Point(canvas.size.width - iconSize.width - 16, (canvas.size.height - iconSize.height) / 2)) {
                    path(
                        config.subMenuIconPath,
                        stroke = Stroke(iconPaint, 1.5, lineJoint = LineJoint.Round, lineCap = LineCap.Round)
                    )
                }
            }
        }
    }

    private val separatorConfig = object: SeparatorConfig {
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

    override fun actionConfig   (): ItemConfig<ItemInfo> = actionConfig
    override fun promptConfig   (): ItemConfig<ItemInfo> = promptConfig
    override fun subMenuConfig  (): SubMenuConfig        = subMenuConfig
    override fun separatorConfig(): SeparatorConfig      = separatorConfig

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