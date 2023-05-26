package io.nacular.doodle.controls.popupmenu

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time

/**
 * Specifies [Behavior] used to manage a [Menu]'s look/feel.
 */
public abstract class MenuBehavior: Behavior<Menu> {
    /**
     * Context for an item in a [Menu].
     */
    public interface ItemInfo {
        /** The item's text */
        public val text: String

        /** The item's font */
        public val font: Font?

        /** Whether the item is enabled */
        public val enabled: Boolean

        /** Whether the item is selected */
        public val selected: Boolean

        /**
         * Whether the item is mirrored
         *
         * @see View.mirrored
         */
        public val mirrored: Boolean
    }

    /**
     * Context for submenu within a [Menu].
     */
    public interface SubMenuInfo: ItemInfo {
        /**
         * `true` if the submenu has items to show
         */
        public val hasChildren: Boolean
    }

    /**
     * Configuration for an [ActionItemInfo].
     */
    public interface ActionItemInfo: ItemInfo {
        /**
         * The item's icon if any
         */
        public val icon: Icon<ActionItemInfo>?
    }

    /**
     * Configuration for an [ItemInfo].
     */
    public interface ItemConfig<T: ItemInfo> {
        /**
         * Preferred size for the item.
         *
         * @param item to specify a size for
         */
        public fun preferredSize(item: T): Size

        /**
         * Render the item
         *
         * @param item to render
         * @param canvas to render onto
         */
        public fun render(item: T, canvas: Canvas)
    }

    /**
     * Configuration for [SubMenuInfo]
     */
    public interface SubMenuConfig: ItemConfig<SubMenuInfo> {
        /**
         * Time to delay showing a sub-menu popup
         */
        public val showDelay: Measure<Time> get() = zeroMillis
    }

    /**
     * Configuration for a separator within a [Menu].
     */
    public interface SeparatorConfig {
        /**
         * Preferred size for separators.
         */
        public fun preferredSize(): Size

        /**
         * Render a separator
         *
         * @param canvas to render onto
         */
        public fun render(canvas: Canvas)
    }

    /**
     * Provides a configuration for action items in a [Menu].
     */
    public abstract fun actionConfig(menu: Menu): ItemConfig<ActionItemInfo>

    /**
     * Provides a configuration for prompt items in a [Menu].
     */
    public abstract fun promptConfig(menu: Menu): ItemConfig<ActionItemInfo>

    /**
     * Provides a configuration for submenu items in a [Menu].
     */
    public abstract fun subMenuConfig(menu: Menu): SubMenuConfig

    /**
     * Provides a configuration for separators in a [Menu].
     */
    public abstract fun separatorConfig(menu: Menu): SeparatorConfig

    /**
     * Allows implementors to specify a [Menu]'s insets.
     */
    public var Menu.insets: Insets get() = this.insets_; set(value) { this.insets_ = value }

    /**
     * Allows implementors to check whether any of the [Menu]'s items have an icon.
     */
    public val Menu.anyItemWithIcon: Boolean get() = this.anyItemWithIcon_
}