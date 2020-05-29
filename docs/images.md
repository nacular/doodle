# Rendering Images
------------------

Doodle allows you to load [`Image`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/image/Image.kt#L6)s
into your app for rendering using the [`ImageLoader`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/image/ImageLoader.kt#L3).

```kotlin
import kotlinx.coroutines.GlobalScope

class SimpleImageApp(display: Display, imageLoader: ImageLoader): Application {
    init {
        GlobalScope.launch {
            val image: Image? = imageLoader.load("some_image_url")
            
            // won't get here until load resolves
            image?.let {
                display.children += Photo(it)
            }
        }
    }
}
```

This app tries to load and render an image. Notice that [`ImageLoader.load`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/image/ImageLoader.kt#L11)
returns `Image?`, which is `null` when the image fails to load for some reason.

?> The `load` method is suspending, so it much be called from another `suspend` method or from a coroutine scope.

### You can draw images directly onto a Canvas

The method for doing this allows you to customize how the image drawing by specify the following.

  * **Source** rectangular in the image
  * **Destination** rectangle to draw into
  * **Opacity** to draw the image with
  * **Radius** for the image corners