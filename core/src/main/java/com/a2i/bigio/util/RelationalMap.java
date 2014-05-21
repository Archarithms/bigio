/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package com.a2i.bigio.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a Relational Map that stores relations between objects.
 *
 * @author Andy Trimble
 * @param <R>
 */
public class RelationalMap<R extends Relation> {
    
    /** The set of classes in the relation. */
    private List<Class<?>> classes = Collections.emptyList();

    /** The mapping storage container. */
    private final Map<Integer, Map<Object, List<R>>> mapping
            = new ConcurrentHashMap<>();

    /** The set of contained relations. */
    private final List<R> relationList = new ArrayList<>();

    /** The set of query indices in case the relation has indistinct classes. */
    private int[] indices = null;

    /** A flat specifying if the relation has distinct classes. */
    private boolean distinct = true;
   
    /**
     * Add a relation to the map.
     * 
     * @param relations a set of relations
     */
    public void add(R ... relations) {
        if(relations.length == 0)
            return;
        
        if(classes.isEmpty()) { // this is the first addition
            classes = new ArrayList<>(relations[0].getLength());
            for(int i = 0; i < relations[0].getLength(); ++i) {
                if(classes.contains(relations[0].getClass(i))) {
                    distinct = false;
                }
                
                classes.add(i, relations[0].getClass(i));
                mapping.put(i, new ConcurrentHashMap<Object, List<R>>());
            }
        }
        
        for(int k = 0; k < relations.length; ++k) {
            relationList.add(relations[k]);

            for(int i = 0; i < relations[k].getLength(); ++i) {
                if(mapping.get(i).get(relations[k].getItem(i)) == null) {
                    mapping.get(i).put(relations[k].getItem(i), new ArrayList<R>());
                }

                mapping.get(i).get(relations[k].getItem(i)).add(relations[k]);
            }
        }
    }

    /**
     * Adds a collection of relations.
     * 
     * @param relations a collection of relations.
     */
    public void add(Collection<R> relations) {
        for(R r : relations) {
            add(r);
        }
    }

    /**
     * Remove a relation from the map.
     * 
     * @param relations a set of relation.
     */
    public void remove(R ... relations) {
        for(int k = 0; k < relations.length; ++k) {
            R relation = relations[k];
            this.relationList.remove(relation);
            for(int i = 0; i < relation.getLength(); ++i) {
                for(R r : mapping.get(i).get(relation.getItem(i))) {
                    if(!r.getItem(i).equals(relation.getItem(i))) {
                        mapping.get(i).get(r.getItem(i)).remove(relation);
                    }
                }
                mapping.get(i).get(relation.getItem(i)).remove(relation);
            }
        }
    }

    /**
     * Remove a collection of relations.
     * 
     * @param relations a collection of relations.
     */
    public void remove(Collection<R> relations) {
        for(R r : relations) {
            remove(r);
        }
    }

    /**
     * Get all the data in the map as a list.
     * 
     * @return a list.
     */
    public List<R> asList() {
        List<R> ret = new ArrayList<>(relationList);
        return ret;
    }

    /**
     * Clear out the Relational Map.
     */
    public void clear() {
        classes = Collections.emptyList();
        mapping.clear();
    }

    /**
     * Sets the relation indices for the next query.
     * 
     * @param indices a set of indices into the relation.
     */
    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    /**
     * Get the set of relations that represents the union of the provided query
     * keys.  If the classes in the relation are distinct, then this class will
     * return the union of all relations that contain the specified keys.  If
     * the classes in the relation are not distinct, then it is necessary to
     * call <code>setIndices()</code> before executing a query.  If any of the
     * arguments are a <code>Range</code> object, then <code>setIndices()</code>
     * must be called prior to the query.
     * <br>
     * Passing in no arguments results in the entire set of relations being
     * returned.
     * 
     * @param keys the set of keys.
     * @return the set of all relations containing the union of the keys.
     * @see com.ng.mdeac.utilities.Range
     */
    public List<R> query(Object ... keys) {
        if(!distinct && indices == null) {
            throw new IllegalArgumentException("Classes not distinct.  Call setIndices() prior to searching.");
        }
        
        if(keys.length == 0) {
            return asList();
        }

        List<R> ret = new ArrayList<>(get(keys, 0, keys.length - 1));
        indices = null;

        return ret;
    }

    /**
     * Perform a query recursively on the specified key array.  This method 
     * is recursive on the begin parameter.  That is, this method calls
     * <code>get(keys, begin + 1, end)</code> until begin is equal to end.
     * For each recursive step, the intersection of the current result list
     * and the result of the query at the current index is tallied.
     * 
     * @param keys a set of keys.
     * @param begin the beginning index into the key array.
     * @param end the end index into the key array.
     * @return the collection of relations containing the union of the keys.
     */
    private List<R> get(Object[] keys, int begin, int end) {
        if(begin == end) {
            return get(keys[begin], indices == null ? -1 : indices[begin]);
        }

        List ret = get(keys[begin], indices == null ? -1 : indices[begin]);
        ret.retainAll(get(keys, begin + 1, end));
        
        return ret;
    }

    /**
     * Get the set of relations containing the specified key.
     * 
     * @param key a key.
     * @param index the index of the key.
     * @return the collection of keys containing the specified key.
     */
    private List<R> get(Object key, int index) {
        if(distinct && !(key instanceof Range)) {
            index = classes.indexOf(key.getClass());
        }

        Class sup = key.getClass().getSuperclass();
        while(index == -1 && sup != null) {
            index = classes.indexOf(sup);
            sup = sup.getSuperclass();
        }

        if(key instanceof Range && index == -1) {
            throw new IllegalArgumentException("setIndices() must be called prior to querying on a range.");
        }

        if(index < 0 || mapping.get(index) == null) {
            return Collections.emptyList();
        }

        if(mapping.get(index).get(key) == null && !(key instanceof Range)) {
            return Collections.emptyList();
        }
            
        if(key instanceof Range) {
            Range range = (Range)key;
            List ret = new ArrayList();
            for(Object number : mapping.get(index).keySet()) {
                if(number instanceof Number) {
                    if(range.contains((Number)number)) {
                        ret.addAll(mapping.get(index).get(number));
                    }
                }
            }
            return ret;
        } else {
            return new ArrayList(mapping.get(index).get(key));
        }
    }
}
