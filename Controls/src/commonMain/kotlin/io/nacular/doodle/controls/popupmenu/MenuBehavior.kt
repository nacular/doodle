package io.nacular.doodle.controls.popupmenu

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets

/**
 * Specifies [Behavior] used to manage a [Menu]'s look/feel.
 */
public abstract class MenuBehavior: Behavior<Menu> {
    /**
     * Context for an item in a [Menu].
     */
    public interface ItemInfo {
        public val text    : String
        public val font    : Font?
        public val enabled : Boolean
        public val selected: Boolean
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
    public abstract fun actionConfig(): ItemConfig<ItemInfo>

    /**
     * Provides a configuration for promt items in a [Menu].
     */
    public abstract fun promptConfig(): ItemConfig<ItemInfo>

    /**
     * Provides a configuration for submenu items in a [Menu].
     */
    public abstract fun subMenuConfig(): ItemConfig<SubMenuInfo>

    /**
     * Provides a configuration for separators in a [Menu].
     */
    public abstract fun separatorConfig(): SeparatorConfig

    /**
     * Allows implementors to specify a [Menu]'s insets.
     */
    public var Menu.insets: Insets get() = this.insets_; set(value) { this.insets_ = value }
}