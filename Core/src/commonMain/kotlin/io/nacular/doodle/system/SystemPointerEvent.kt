package io.nacular.doodle.system

import io.nacular.doodle.core.Internal
import io.nacular.doodle.geometry.Point

public class SystemPointerEvent @Internal constructor(
        @Internal public val id               : Int,
                  public val type             : Type,
                  public val location         : Point,
                  public val buttons          : Set<Button>,
                  public val clickCount       : Int,
                             modifiers        : Set<Modifier>,
                  public val nativeScrollPanel: Boolean = false): SystemInputEvent(modifiers) {

    @Internal
    public constructor(
            id               : Int,
            type             : Type,
            location         : Point,
            button           : Button,
            clickCount       : Int,
            modifiers        : Set<Modifier>,
            nativeScrollPanel: Boolean = false): this(id, type, location, setOf(button), clickCount, modifiers, nativeScrollPanel)

    public enum class Type { Up, Down, Move, Exit, Drag, Click, Enter }

    public enum class Button { Button1, Button2, Button3 }
}