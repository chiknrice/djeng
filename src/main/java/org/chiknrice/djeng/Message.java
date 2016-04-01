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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.chiknrice.djeng.MessageElement.Section;

/**
 * A class that uses hierarchical model to represent the message elements in a byte stream. Message elements are
 * organized into a tree-like structure. This class would be the root element which is represented as a collection of
 * sub elements. The sub elements can either be a value element (leaf) or a composite element (branch). Each
 * branch/composite element is again a collection of sub elements.
 * <p>
 * The underlying collection implement is a {@see CompositeMap} which is a TreeMap that restricts keys and values to
 * String and MessageElement<?>.  Keys corresponds to the indexes defined in the configuration xml.  Getting, setting
 * and removing an element can be done with index path, setting would require a non-null value.  The index path is a
 * pattern of indexes separated by a dot (.).  Valid indexes are restricted by the config schema to alpha-numeric
 * characters plus the hyphen (-).  An example index path would be h1.a.b-1 or 63.2.1 (DE5 in DE2 in DE63 in an ISO8583
 * message).  The accessor/mutator methods are all thread-safe.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class Message {

    private static final Pattern INDEX_PATH_PATTERN = Pattern.compile("[a-z,A-F,0-9,-]+(\\.[a-z,A-F,0-9,-]+)*");

    private final CompositeMap elements;
    final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    ByteBuffer messageBuffer;

    /**
     * The only publicly accessible constructor which accepts an mti.
     *
     * @param mti
     */
    public Message(int mti) {
        if (mti < 0) {
            throw new IllegalArgumentException("MTI cannot be negative");
        }
        // TODO implement setting mti
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

    private <T> T getElement(String indexPath, boolean raw) {
        Matcher m = INDEX_PATH_PATTERN.matcher(indexPath);
        if (!m.matches()) {
            throw new IllegalArgumentException(format("%s is not a valid index expression", indexPath));
        } else {
            try {
                rwLock.readLock().lock();
                String[] indexes = indexPath.split("\\.");

                if (indexes.length > 1) {
                    CompositeMap currentCompositeMap = elements;
                    for (int i = 0; i < indexes.length; i++) {
                        if (currentCompositeMap != null) {
                            if (i == indexes.length - 1) {
                                return (T) getSubElement(currentCompositeMap, indexes[i], raw);
                            } else {
                                MessageElement subElement = currentCompositeMap.get(indexes[i]);
                                if (subElement != null && subElement.getValue() instanceof CompositeMap) {
                                    currentCompositeMap = (CompositeMap) subElement.getValue();
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    return null;
                } else {
                    return (T) getSubElement(elements, indexes[0], raw);
                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    /**
     * Returns the value if the sub element exists and if it is not a CompositeMap.
     *
     * @param compositeMap
     * @param index
     * @param raw
     * @return
     */
    private Object getSubElement(CompositeMap compositeMap, String index, boolean raw) {
        MessageElement<?> messageElement = compositeMap.get(index);
        if (messageElement == null || messageElement.getValue() instanceof CompositeMap) {
            return null;
        } else if (raw) {
            if (messageBuffer == null) {
                throw new IllegalStateException("Message doesn't have a backing raw data");
            } else {
                byte[] bytes = new byte[messageElement.getLimit() - messageElement.getPos()];
                messageBuffer.rewind().position(messageElement.getPos());
                messageBuffer.get(bytes);
                return ByteUtil.encodeHex(bytes);
            }
        } else {
            return messageElement.getValue();
        }
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
                resetRawState();
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
                            MessageElement subElement = new MessageElement(value);
                            compositeMapStack.pop().put(key, subElement);
                        }
                    } else {
                        MessageElement subElement = compositeMapStack.peek().get(key);
                        if (subElement != null && subElement.getValue() instanceof CompositeMap) {
                            compositeMapStack.push((CompositeMap) subElement.getValue());
                        } else {
                            subElement = new MessageElement<>(new CompositeMap());
                            MessageElement<?> replaced = compositeMapStack.peek().put(key, subElement);
                            if (replaced != null) {
                                // TODO log replaced?
                            }
                            compositeMapStack.push((CompositeMap) subElement.getValue());
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
            rwLock.readLock().unlock();
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
            // TODO
            return super.toString();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    void resetRawState() {
        if (messageBuffer != null) {
            messageBuffer = null;
            for (MessageElement<?> subElement : elements.values()) {
                subElement.clearMarkers();
            }
        }
    }

    /**
     * Recursive method to get all message elements except for composite elements.
     *
     * @param compositeMap
     * @param parentMap
     */
    private void findElements(CompositeMap compositeMap, Map<Section, MessageElement<?>> parentMap) {
        for (Entry<String, MessageElement<?>> entry : compositeMap.entrySet()) {
            MessageElement<?> element = entry.getValue();
            Object value = element.getValue();
            if (value instanceof CompositeMap) {
                Map<Section, MessageElement<?>> tmpMap = new HashMap<>();
                findElements((CompositeMap) value, tmpMap);
                for (Section section : tmpMap.keySet()) {
                    section.index = entry.getKey().concat(".").concat(section.index);
                }
                parentMap.putAll(tmpMap);
            } else {
                Section section = new Section();
                section.index = entry.getKey();
                section.pos = element.getPos();
                section.limit = element.getLimit();
                parentMap.put(section, element);
            }
        }
    }

    /**
     * Recursive method to get all message element values except for composite elements.
     *
     * @param compositeMap
     * @param parentMap
     */
    private void findElementValues(CompositeMap compositeMap, Map<String, Object> parentMap) {
        for (Entry<String, MessageElement<?>> entry : compositeMap.entrySet()) {
            MessageElement<?> element = entry.getValue();
            Object value = element.getValue();
            if (value instanceof CompositeMap) {
                Map<String, Object> tmpMap = new HashMap<>();
                findElementValues((CompositeMap) value, tmpMap);
                for (Entry<String, Object> tmpEntry : tmpMap.entrySet()) {
                    parentMap.put(entry.getKey().concat(".").concat(tmpEntry.getKey()), tmpEntry.getValue());
                }
            } else {
                parentMap.put(entry.getKey(), entry.getValue().getValue());
            }
        }
    }

    /**
     * Internal method used for logging messages during encoding/decoding.
     *
     * @return
     */
    Map<Section, MessageElement<?>> getElementsInternal() {
        Map<MessageElement.Section, MessageElement<?>> elementMap = new TreeMap<>();
        try {
            rwLock.readLock().lock();
            findElements(this.elements, elementMap);
            return elementMap;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Public API

    /**
     * Method to allow for getting all element values with key being the index path.
     *
     * @return
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
     * Traverses the message element hierarchy to get the value located at the indexPath. TODO: get list of elements
     * using * wildcard?
     *
     * @param indexPath
     * @return the value or null if the element doesn't exist or is a composite element
     * @throws IllegalArgumentException if the indexPath pattern is not valid
     */
    public <T> T getElement(String indexPath) {
        return getElement(indexPath, false);
    }

    /**
     * Same as {@see #getElement} except that the value is returned as raw hex value.  This would include any data
     * related to the element like length prefix bytes.
     *
     * @param indexPath
     * @return
     * @throws IllegalStateException if the underlying byte[] that represents the message doesn't exist.  This could be
     *                               due to the message not being encoded yet or if the decoded message was modified via
     *                               set/remove element methods.
     */
    public byte[] getRawElement(String indexPath) {
        return getElement(indexPath, true);
    }

    /**
     * Sets the value of a element at the position expressed by indexPath.  This method adds the constraint of non null
     * values to {@link #setOrRemoveElement} to distinguish from the intent of setting or removing values.  This method
     * invalidates the underlying byte[] if it exists.
     *
     * @param indexPath
     * @param value
     * @throws IllegalArgumentException if the value is null
     */
    public void setElement(String indexPath, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Message element value cannot be null");
        }
        setOrRemoveElement(indexPath, value);
    }

    /**
     * Removes the element at the position indicated by indexPath.  This method invalidates the underlying byte[] if it
     * exists.
     *
     * @param indexPath
     */
    public void removeElement(String indexPath) {
        setOrRemoveElement(indexPath, null);
    }

}
