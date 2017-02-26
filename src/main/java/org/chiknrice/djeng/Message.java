/*
 * Copyright (c) 2016 Ian Bondoc
 *
 * This file is part of Djeng
 *
 * Djeng is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * Djeng is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.chiknrice.djeng;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * The {@code Message} class represents a message sent to and from a byte stream.  Any message structure can be modeled
 * using a tree-like structure similar to JSON, XML, and YAML.  Similar to these hierarchical models, this class also
 * represents message elements using a collection of sub-elements which can either be a value element (leaf) or a
 * composite-element (branch).  Each branch/composite-element is also a collection of sub-elements.
 * <p/>
 * The collection of sub-elements is implemented using a {@link CompositeMap} which is a {@code HashMap} that restricts
 * keys and values to {@code String} and {@code Object}.  The key/index is the unique identifier of a sub-element within
 * that {@code CompositeMap}. This would usually correspond to the index of the element in the configuration xml but can
 * be set to a different value depending on the {@code Codec} implementation.  Valid indexes are restricted by the
 * configuration schema which does not permit spaces or the dot (.) character.
 * <p/>
 * Before encoding, messages can be built by setting sub-elements.  After decoding, sub-elements can be accessed from
 * the resulting message.  The message elements and sub-elements can be referenced using an index path.  The index path
 * is similar to XPATH which is a made up of element indexes separated by a dot (.). Similar to any path pattern it is a
 * way to navigate the hierarchical structure to reach a sub-element.  However, the message element restricts navigation
 * to leaf nodes only.  An example index path would be h1.a.b-1 or 63.2.1 (DE5 in DE2 in DE63 in an ISO8583 message).
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class Message {

    private static final Pattern INDEX_PATH_PATTERN = Pattern.compile("[^\\s.]+(\\.[^\\s]+)*");

    private final CompositeMap elements;
    // TODO: remove all locks - no need to be thread safe
    final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * The only publicly accessible constructor which creates an empty message.
     */
    public Message() {
        elements = new CompositeMap();
    }

    /**
     * A package only constructor which is only used by the framework during of decoding.
     *
     * @param elements
     */
    Message(CompositeMap elements) {
        this.elements = elements;
    }

    /**
     * Sets or removes the value of a msg element at the position expressed by indexPath.  Null value would remove the
     * element as well as empty composite elements resulting in the operation.
     *
     * @param indexPath the position where the value should be set.
     * @param value     the value of the data element.
     * @throws IllegalArgumentException if the indexPath is not in the form of a recursive index pattern
     */
    private void setOrRemoveElement(String indexPath, Object value) {
        Matcher m = INDEX_PATH_PATTERN.matcher(indexPath);
        if (!m.matches()) {
            throw new IllegalArgumentException(format("%s is not a valid index path", indexPath));
        } else {
            try {
                rwLock.writeLock().lock();
                String[] indexes = indexPath.split("\\.");

                Stack<CompositeMap> compositeMapStack = new Stack<>();
                compositeMapStack.push(elements);

                for (int i = 0; i < indexes.length; i++) {
                    String key = indexes[i];
                    if (i == (indexes.length - 1)) {
                        if (value == null) {
                            compositeMapStack.peek().remove(key);
                            // cleanup
                            while (compositeMapStack.size() > 0 && compositeMapStack.peek().size() == 0) {
                                compositeMapStack.pop();
                                compositeMapStack.peek().remove(indexes[--i]);
                            }
                            break;
                        } else {
                            Object subElement = value;
                            compositeMapStack.pop().put(key, subElement);
                        }
                    } else {
                        Object subElement = compositeMapStack.peek().get(key);
                        if (subElement instanceof CompositeMap) {
                            compositeMapStack.push((CompositeMap) subElement);
                        } else {
                            subElement = new CompositeMap();
                            Object replaced = compositeMapStack.peek().put(key, subElement);
                            if (replaced != null) {
                                // TODO log replaced?
                            }
                            compositeMapStack.push((CompositeMap) subElement);
                        }
                    }
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    /**
     * Access to the composite data elements.  Used only within the framework during encoding.
     *
     * @return the elements.
     */
    CompositeMap getCompositeMap() {
        return elements;
    }

    /**
     * Recursive method to get all message element values except for composite elements.
     *
     * @param compositeMap TODO
     * @param parentMap    TODO
     */
    private void findElementValues(CompositeMap compositeMap, Map<String, Object> parentMap) {
        for (Entry<String, Object> entry : compositeMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof CompositeMap) {
                Map<String, Object> tmpMap = new HashMap<>();
                findElementValues((CompositeMap) value, tmpMap);
                for (Entry<String, Object> tmpEntry : tmpMap.entrySet()) {
                    parentMap.put(entry.getKey().concat(".").concat(tmpEntry.getKey()), tmpEntry.getValue());
                }
            } else {
                parentMap.put(entry.getKey(), value);
            }
        }
    }

    //
    // Public API starts
    //

    @Override
    public int hashCode() {
        try {
            rwLock.readLock().lock();
            return elements.hashCode();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object obj) {
        try {
            rwLock.readLock().lock();
            if (obj != null && obj instanceof Message) {
                Message other = (Message) obj;
                if (elements.equals(other.elements)) {
                    return true;
                }
            }
            return false;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        try {
            rwLock.readLock().lock();
            return "TODO Message.toString()";
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Method to allow for getting all element values with key being the index path. TODO use the backing buffer if it
     * exists
     *
     * @return TODO
     */
    public Map<String, Object> getElements() {
        Map<String, Object> elements = new HashMap<>();
        try {
            rwLock.readLock().lock();
            findElementValues(this.elements, elements);
            return elements;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Gets the value located at the indexPath. TODO: get list of elements using * wildcard?
     *
     * @param indexPath TODO
     * @param <T>       TODO
     * @return the value or {@code null} if the element doesn't exist or if it's a composite element
     * @throws IllegalArgumentException if the indexPath pattern is not valid
     */
    public <T> T getElement(String indexPath) {
        Matcher m = INDEX_PATH_PATTERN.matcher(indexPath);
        if (!m.matches()) {
            throw new IllegalArgumentException(format("%s is not a valid index expression", indexPath));
        } else {
            try {
                rwLock.readLock().lock();
                String[] indexes = indexPath.split("\\.");

                CompositeMap currentCompositeMap = elements;
                for (int i = 0; i < indexes.length; i++) {
                    if (currentCompositeMap != null) {
                        Object element = currentCompositeMap.get(indexes[i]);
                        boolean isLeaf = i == indexes.length - 1;
                        if (element instanceof CompositeMap) {
                            if (isLeaf) {
                                // Don't return a composite
                                return null;
                            } else {
                                // If there's more indexes on path then continue
                                currentCompositeMap = (CompositeMap) element;
                            }
                        } else if (isLeaf) {
                            // Return the element
                            //noinspection unchecked
                            return (T) element;
                        } else {
                            // Else return nothing
                            break;
                        }
                    }
                }
                return null;
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    /**
     * Sets the value of an element at the position indicated by indexPath.  This method invalidates the underlying
     * byte[] if it exists.
     *
     * @param indexPath TODO
     * @param value     TODO
     * @throws IllegalArgumentException if the indexPath is not valid or if the value is {@code null}
     */
    public void setElement(String indexPath, Object value) {
        if (indexPath == null || value == null) {
            throw new IllegalArgumentException("Index path or value cannot be null");
        }
        setOrRemoveElement(indexPath, value);
    }

    /**
     * Removes the element at the position indicated by indexPath.  This method invalidates the underlying byte[] if it
     * exists.
     *
     * @param indexPath TODO
     */
    public void removeElement(String indexPath) {
        setOrRemoveElement(indexPath, null);
    }

    /**
     * TODO
     *
     * @param src TODO
     * @return TODO
     */
    public static Message createFrom(Message src) {
        Message m = new Message();
        for (Entry<String, Object> entry : src.getElements().entrySet()) {
            m.setElement(entry.getKey(), entry.getValue());
        }
        return m;
    }

    /**
     * TODO
     *
     * @param dst     TODO
     * @param indexes TODO
     */
    public void copyElementsTo(Message dst, String... indexes) {
        for (String index : indexes) {
            dst.setElement(index, this.getElement(index));
        }
    }
}
