package io.nacular.doodle.examples.contacts

import io.nacular.doodle.animation.NumericAnimationPlan
import io.nacular.doodle.animation.transition.easeInOutCubic
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 3/22/22.
 */
interface AppConfig {
    val logo       : Image
    val create     : Image
    val blankAvatar: Image

    val small      : Font
    val medium     : Font
    val large      : Font
    val xLarge     : Font
    val smallBold  : Font

    val tool            : Color  get() = toolHighlight.opacity(0.3f)
    val search          : Color  get() = Color(0x5F6368u)
    val shadow          : Color  get() = Black.opacity(0.1f)
    val header          : Color  get() = search
    val outline         : Color  get() = Color(0xe5e7ebu)
    val placeHolder     : Color  get() = Color(0x9c999bu)
    val background      : Color  get() = White
    val phoneNumber     : Color  get() = Black
    val listHighlight   : Color  get() = Color(0xf5f5f5u)
    val toolHighlight   : Color  get() = Black
    val searchSelected  : Color  get() = Color(0xF1F3F4u)
    val phoneNumberLink : Color  get() = Color(0x3B82F6u)
    val createButtonText: Color  get() = Black
    val deleteBackground: Color  get() = Color(0xDD4235u)
    val buttonBackground: Color  get() = Color(0x1A73E8u)
    val buttonForeground: Color  get() = White

    val backIcon        : String get() = "M18.6903 8.177H4.4975l6.5417-6.5417L9.3452 0 0 9.3452l9.3452 9.3452 1.6352-1.6353-6.4829-6.5417H18.6903v-2.3363z"
    val nameIcon        : String get() = "M8 1.9a2.1 2.1 0 1 1 0 4.2 2.1 2.1 0 0 1 0-4.2m0 9c2.97 0 6.1 1.46 6.1 2.1v1.1H1.9V13c0-.64 3.13-2.1 6.1-2.1M8 0C5.79 0 4 1.79 4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 9c-2.67 0-8 1.34-8 4v3h16v-3c0-2.66-5.33-4-8-4z"
    val editIcon        : String get() = "M0 18h3.75L14.81 6.94l-3.75-3.75L0 14.25V18zm2-2.92 9.06-9.06.92.92L2.92 16H2v-.92zM15.37.29a.996.996 0 0 0 -1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83a.996.996 0 0 0 0 -1.41l-2.34-2.34z"
    val phoneIcon       : String get() = "M3.54 2c.06.89.21 1.76.45 2.59l-1.2 1.2c-.41-1.2-.67-2.47-.76-3.79h1.51m9.86 12.02c.85.24 1.72.39 2.6.45v1.49c-1.32-.09-2.59-.35-3.8-.75l1.2-1.19M4.5 0H1C.45 0 0 .45 0 1c0 9.39 7.61 17 17 17 .55 0 1-.45 1-1v-3.49c0-.55-.45-1-1-1-1.24 0-2.45-.2-3.57-.57a.84.84 0 0 0-.31-.05c-.26 0-.51.1-.71.29l-2.2 2.2a15.149 15.149 0 0 1-6.59-6.59l2.2-2.2c.28-.28.36-.67.25-1.02A11.36 11.36 0 0 1 5.5 1c0-.55-.45-1-1-1z"
    val trashIcon       : String get() = "M1 16c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V4H1v12zM14 1h-3.5l-1-1h-5l-1 1H0v2h14V1z"
    val searchIcon      : String get() = "M12.5 11h-.79l-.28-.27A6.471 6.471 0 0 0 13 6.5 6.5 6.5 0 1 0 6.5 13c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L17.49 16l-4.99-5Zm-6 0C4.01 11 2 8.99 2 6.5S4.01 2 6.5 2 11 4.01 11 6.5 8.99 11 6.5 11Z"
    val deleteIcon      : String get() = "M14 1.41 12.59 0 7 5.59 1.41 0 0 1.41 5.59 7 0 12.59 1.41 14 7 8.41 12.59 14 14 12.59 8.41 7Z"

    val slowTransition  : NumericAnimationPlan<Float, Double> get() = tweenFloat(easeInOutCubic, 250 * milliseconds)
    val fastTransition  : NumericAnimationPlan<Float, Double> get() = tweenFloat(easeInOutCubic, 100 * milliseconds)

    val createButtonLargeSize: Size get() = Size(186, 45)
    val createButtonSmallSize: Size get() = Size(68)
}

class AppConfigImpl private constructor(
    override val small      : Font,
    override val medium     : Font,
    override val large      : Font,
    override val xLarge     : Font,
    override val smallBold  : Font,
    override val logo       : Image,
    override val create     : Image,
    override val blankAvatar: Image,
): AppConfig {

    companion object {
        suspend operator fun invoke(fonts: FontLoader, images: ImageLoader): AppConfig {
            val large = fonts("dmsans.ttf") {
                size     = 20
                weight   = 100
                families = listOf("DM Sans")
            }!!

            val small = fonts(large) { size = 16 }!!

            return AppConfigImpl(
                logo        = images.load("logo.png"  )!!,
                create      = images.load("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACQAAAAkCAYAAADhAJiYAAAAAXNSR0IArs4c6QAAAL5JREFUWEdj/P///3+GQQQYRx1EIDZGQ4hQch1+IfTaxQzF06J7ThEKBLzyFIfQqIMIhf9oCI2GEKV12WgaGjRp6M9eNkJuAcu/bzeAq/trwM8QLbSBKH17q7ixqsNZUpPqIFIcA3IJTR30WpORIVdyH1EhA1NEMwfd2mBIsmPICiFivWu6MgRF6enwNcRqJS0NEWvqqIMIhdRoCI2GEKW1/WgaGnJpiJCDSZWnuF9GqoWE1I86aDSECIUAIXkAXtfmleN2uj8AAAAASUVORK5CYII=")!!,
                blankAvatar = images.load("create.png")!!,
                small       = fonts(large) { size = 16 }!!,
                large       = large,
                medium      = fonts(large) { size = 18 }!!,
                xLarge      = fonts(large) { size = 30 }!!,
                smallBold   = fonts("dmsans-500.ttf") {
                    size     = small.size
                    families = listOf(small.family)
                    weight   = 500
                }!!,
            )
        }
    }
}