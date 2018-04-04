package com.nectar.doodle.controls.spinner

import com.nectar.doodle.controls.theme.SpinnerUI
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl


interface Model<T> {
    fun next    ()
    fun previous()

    val value      : T
    val hasNext    : Boolean
    val hasPrevious: Boolean
    val changed  : ChangeObservers<Model<T>>
}

interface MutableModel<T>: Model<T> {
    override var value: T
}

open class Spinner<T, M: Model<T>>(model: M): Gizmo() {

    fun next    () = model.next()
    fun previous() = model.previous()

    var model = model
        set(new) {
            field.changed -= modelChanged
            field = new
            field.changed += modelChanged
        }

    open val value       get() = model.value
         val hasNext     get() = model.hasNext
         val hasPrevious get() = model.hasPrevious

    var renderer: SpinnerUI<T, M>? = null
        set(new) {
            children.clear()

            field = new?.also {
                it.components(this).also {
                    children.addAll(it.components)
                    layout = it.layout()
                }
            }
        }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    @Suppress("PrivatePropertyName")
    private val changed_ = ChangeObserversImpl(this)

    val changed: ChangeObservers<Spinner<T, M>> = changed_

    private val modelChanged: (Model<T>) -> Unit = {
        changed_()
    }

    init {
        this.model.changed += modelChanged
    }

    companion object {
        operator fun invoke(progression: IntProgression) = Spinner(IntModel(progression))
        operator fun <T> invoke(values: List<T>) = Spinner(ListModel(values))
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
//        Gizmo getGizmo( Spinner aSpinner, Object aObject )
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
//        @Override public Gizmo getGizmo( Spinner aSpinner, Object aObject )
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
        operator fun <T> invoke(values: MutableList<T>) = MutableSpinner(MutableListModel(values))
    }
}