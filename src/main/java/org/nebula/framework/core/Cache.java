/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nebula.framework.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Not thread safe.
 */
public class Cache<T> implements Iterable<T> {

  private CacheLoader<T> loader;

  private T[] elements;

  //number of elements in the cache.
  private int size = 0;

  private int capacity;

  public Cache() {
    this(null, 16);
  }

  public Cache(CacheLoader<T> loader) {
    this(loader, 16);
  }

  public Cache(int capacity) {
    this(null, capacity);
  }

  public Cache(CacheLoader<T> loader, int capacity) {

    if (capacity <= 0) {
      throw new IllegalArgumentException("The capacity must be greater than 0");
    }

    this.loader = loader;
    this.elements = (T[]) new Object[capacity];
    this.capacity = capacity;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  public void add(T element) {
    if (element == null) {
      throw new IllegalArgumentException("The element can't be null.");
    }

    if (size >= capacity) {
      throw new IllegalStateException("The size " + size + " exceed the capacity.");
    }

    elements[size] = element;

    size++;
  }

  public T get(int index) {
    rangeCheck(index);

    while (index >= size) {
      if (!load()) {
        throw new IllegalArgumentException("The index is illegal.");
      }
    }

    return elements[index];
  }

  private void rangeCheck(int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }
  }

  private boolean load() {
    boolean loaded = false;
    if (loader != null) {
      for (T t : loader.load()) {
        add(t);
        loaded = true;
      }
    }
    return loaded;
  }


  public CacheIterator iterator() {
    return new CacheIterator();
  }

  public class CacheIterator implements Iterator<T> {

    private int i = 0;

    private T currentElement;

    public boolean hasNext() {
      if (i >= size) {
        load();
      }

      return i < size;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      return elements[i++];
    }

    public T current() {
      return elements[i];
    }

  }


}