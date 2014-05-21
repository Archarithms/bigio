/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.util;

/**
 * This interface represents an ordered relation between objects.  Note that it
 * is not necessary for all objects in a relation to have a distinct type.  If
 * there are multiple object types, then the <code>setIndices()</code> method
 * must be called before any Polymorphic Map query.
 *
 * @author Andy Trimble
 */
public interface Relation {

    /**
     * Get the number of objects in this relation.
     * 
     * @return the number of objects.
     */
    public int getLength();

    /**
     * Get the type of the object at the specified index.
     * 
     * @param itemNum the object index.
     * @return the type of the object at the index.
     */
    public Class<?> getClass(int itemNum);

    /**
     * Get the object at the specified index.
     * 
     * @param itemNum the object index.
     * @return the object at the index.
     */
    public Object getItem(int itemNum);
}
