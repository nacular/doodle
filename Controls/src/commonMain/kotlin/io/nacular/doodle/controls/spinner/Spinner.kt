package io.nacular.doodle.controls.spinner

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl


interface Model<T> {
    fun next    ()
    fun previous()

    val value      : T
    val hasNext    : Boolean
    val hasPrevious: Boolean
    val changed    : ChangeObservers<Model<T>>
}

interface MutableModel<T>: Model<T> {
    override var value: T
}

/**
 * Provides presentation and behavior customization for [Spinner].
 */
abstract class SpinnerBehavior<T, M: Model<T>>: Behavior<Spinner<T, M>> {
    val Spinner<T, M>.children get() = this._children
    var Spinner<T, M>.insets   get() = this._insets; set(new) { _insets = new }
    var Spinner<T, M>.layout   get() = this._layout; set(new) { _layout = new }

    /**
     * Called whenever the Spinner's selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [Spinner.changed].
     *
     * @param spinner with change
     */
    abstract fun changed(spinner: Spinner<T, M>)
}

@Suppress("PropertyName")
open class Spinner<T, M: Model<T>>(val model: M, val itemVisualizer: ItemVisualizer<T, Any>? = null): View() {

    fun next    () = model.next    ()
    fun previous() = model.previous()

    open val value       get() = model.value
         val hasNext     get() = model.hasNext
         val hasPrevious get() = model.hasPrevious

    var behavior: SpinnerBehavior<T, M>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    // Expose container APIs for behavior
    internal val _children get() = children
    internal var _insets   get() = insets; set(new) { insets = new }
    internal var _layout   get() = layout; set(new) { layout = new }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

    val changed: ChangeObservers<Spinner<T, M>> = changed_

    private val modelChanged: (Model<T>) -> Unit = {
        changed_()
    }

    init {
        this.model.changed += modelChanged
    }

    companion object {
        operator fun invoke(progression: IntProgression) = Spinner(IntModel (progression))
        operator fun <T> invoke(values: List<T>)         = Spinner(ListModel(values     ))
    }
}

class MutableSpinner<T>(model: MutableModel<T>): Spinner<T, MutableModel<T>>(model) {
    override var value: T
        get(   ) = super.value
        set(new) { model.value = new }


//     private val mEditorGenerator = new DefaultEditorGenerator()

//    public void setEditorGenerator( final EditorGenerator aEditorGenerator )
//    {
//        setProperty( new AbstractNamedProperty<EditorGenerator>( EDITOR_GENERATOR )
//                     {
//                         @Override public EditorGenerator getValue()
//                         {
//                             return mEditorGenerator
//                         }
//
//                         @Override public void setValue( EditorGenerator aValue )
//                         {
//                             if( aValue == null ) { mEditorGenerator = new DefaultEditorGenerator(); }
//                             else                 { mEditorGenerator = aValue;                       }
//                         }
//                     },
//                     aEditorGenerator )
//    }
//
//    public EditorGenerator getEditorGenerator() { return mEditorGenerator; }
//
//    public interface EditorGenerator extends ItemEditor
//    {
//        View getView( Spinner aSpinner, Object aObject )
//    }
//
//    private static class DefaultEditorGenerator implements EditorGenerator
//    {
//        public DefaultEditorGenerator()
//        {
//            mLabel = new Label()
//
//            mLabel.setTextHorizontalAlignment( Location.RIGHT )
//        }
//
//        @Override public View getView( Spinner aSpinner, Object aObject )
//        {
//            mLabel.setText( aObject.toString() )
//
//            return mLabel
//        }
//
//        @Override public Object  getValue        (                    ) { return mLabel.getText(); }
//        @Override public Boolean isEditable      ( Event aEvent       ) { return false;            }
//        @Override public Boolean stopEditing     (                    ) { return false;            }
//        @Override public Boolean cancelEditing   (                    ) { return false;            }
//        @Override public Boolean shouldSelectItem( Event aEvent       ) { return false;            }
//        @Override public void    addListener     ( Listener aListener ) {                          }
//        @Override public void    removeListener  ( Listener aListener ) {                          }
//
//
//        private Label mLabel
//    }

    companion object {
        operator fun <T> invoke(values: MutableList<T> = mutableListOf()) = MutableSpinner(MutableListModel(values))
    }
}