package com.nectar.doodle.core;


public interface Property<T>
{
    void setValue( T aValue );

    T getValue();
}
