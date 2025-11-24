package com.example.lab_4_oop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class Storage<T> implements Iterable<T> {
    private final List<T> storage = new ArrayList<>();

    public void add(T item) {
        if (!storage.contains(item)) { storage.add(item);}
    }

    public void remove(T item) { storage.remove(item);}

    public void clear() { storage.clear(); }

    public int size() { return storage.size(); }

    public T get(int index) {return storage.get(index);}

    @Override
    public Iterator<T> iterator() { return storage.iterator(); }
}