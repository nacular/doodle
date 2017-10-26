package com.nectar.doodle.dom

import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.get


inline fun HTMLElement.cloneNode() = cloneNode(true)

fun Node.childAt(index: Int): Node? {
    if( index in 0 until childNodes.length ) {
        return childNodes[index]
    }

    return null
}

inline val Node.parent get() = parentNode

inline val Node.numChildren get() = childNodes.length

fun Node.index(of: Node) = (0 until childNodes.length).firstOrNull { childNodes[it] == of } ?: -1

inline fun Node.add(child: Node) = appendChild(child)

inline fun Node.insert(element: Node, index: Int) = insertBefore(element, childAt(index))

inline fun Node.remove(element: Node) = removeChild(element)

fun Node.removeAll() {
    while(firstChild != null) {
        firstChild?.let { remove(it as HTMLElement) }
    }
}

inline val HTMLElement.top    get() = offsetTop.toDouble   ()
inline val HTMLElement.left   get() = offsetLeft.toDouble  ()
inline val HTMLElement.width  get() = offsetWidth.toDouble ()
inline val HTMLElement.height get() = offsetHeight.toDouble()

inline val Node.hasAutoOverflow get() = this is HTMLElement && style.overflowWrap != ""


//    public static final class Type extends JavaScriptObject
//    {
//        protected Type() {}
//
//        public static Type create( String aValue )
//        {
//            if( GWT.isScript() ) { return createScript( aValue ); }
//            else                 { return createHosted( aValue ); }
//        }
//
//        public static Type createScript( String aValue ) /*-{ return aValue; }-*/;
//        public static Type createHosted( String aValue ) /*-{ this[0] = [aValue]; return this[0]; }-*/;
//
//        public static final Type text     = create( "text"     );
//        public static final Type radio    = create( "radio"    );
//        public static final Type button   = create( "button"   );
//        public static final Type checkbox = create( "checkbox" );
//    }
