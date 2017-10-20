package com.zinoti.jaz.core;


public interface Property<T>
{
    void setValue( T aValue );

    T getValue();
}
