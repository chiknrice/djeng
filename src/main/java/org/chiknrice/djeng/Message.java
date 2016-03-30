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
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.chiknrice.djeng.MessageElement.*;

/**
 * The main class which represents the structure of a message. Message elements are structured as a CompositeMap which a
 * TreeMap that restricts keys and values to String, MessageElement<?>.  Keys corresponds to the indexes defined in the
 * message config xml.  Elements can be composite which can further have sub elements.  Adding, getting, and removing an
 * element can be done with index path and optionally a value.  The index path is a pattern of indexes separated by a
 * dot (.).  Valid indexes are restricted by the config schema to alpha-numeric characters plus the hyphen (-).  An
 * example index path would be h1.a.b-1 or 63.2.1 (DE5 in DE2 in DE63 in an ISO8583 message).
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class Message {

    private static final Pattern INDEX_PATH_PATTERN = Pattern.compile("[a-z,A-F,0-9,-]+(\\.[a-z,A-F,0-9,-]+)*");

    private final CompositeMap elements;
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

    /**
     * Returns the value if the sub element exists and if it is not a CompositeMap.
     *
     * @param compositeMap
     * @param index
     * @return
     */
    private Object getSubElement(CompositeMap compositeMap, String index) {
        MessageElement<?> messageElement = compositeMap.get(index);
        return messageElement == null || messageElement.getValue() instanceof CompositeMap ? null : messageElement.getValue();
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
        return elements.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Message) {
            Message other = (Message) obj;
            if (elements.equals(other.elements)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        // TODO
        return super.toString();
    }

    void clearMarkers() {
        for (MessageElement<?> subElement : elements.values()) {
            subElement.clearMarkers();
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
                TreeMap<Section, MessageElement<?>> tmpMap = new TreeMap<>();
                findElements((CompositeMap) value, tmpMap);
                for (Section section : tmpMap.keySet()) {
                    section.index = entry.getKey().concat(".").concat(section.index);
                }
                parentMap.putAll(tmpMap);
            } else {
                /*if (raw) {
                    int pos = element.getPos();
                    int limit = element.getLimit();
                    byte[] bytes = new byte[limit - pos];
                    messageBuffer.position(pos);
                    messageBuffer.get(bytes);
                    value = ByteUtil.encodeHex(bytes);
                }*/
                Section section = new Section();
                section.index = entry.getKey();
                section.pos = element.getPos();
                section.limit = element.getLimit();
                parentMap.put(section, element);
            }
        }
    }

    /**
     * Same as getElement values but ordering is not defined by how it was decoded or encoded.
     *
     * @param compositeMap
     * @param parentMap
     */
    private void findElementValues(CompositeMap compositeMap, Map<String, Object> parentMap) {
        for (Entry<String, MessageElement<?>> entry : compositeMap.entrySet()) {
            MessageElement<?> element = entry.getValue();
            Object value = element.getValue();
            if (value instanceof CompositeMap) {
                Map<String, Object> tmpMap = new LinkedHashMap<>();
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
        findElements(this.elements, elementMap);
        return elementMap;
    }

    // Public API

    /**
     * Method to allow for getting all element values with key being the index path.
     *
     * @return
     */
    public Map<String, Object> getElements() {
        Map<String, Object> elements = new LinkedHashMap<>();
        findElementValues(this.elements, elements);
        return elements;
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
        Matcher m = INDEX_PATH_PATTERN.matcher(indexPath);
        if (!m.matches()) {
            throw new IllegalArgumentException(format("%s is not a valid index expression", indexPath));
        } else {
            String[] indexes = indexPath.split("\\.");

            if (indexes.length > 1) {
                CompositeMap currentCompositeMap = elements;
                for (int i = 0; i < indexes.length; i++) {
                    if (currentCompositeMap != null) {
                        if (i == indexes.length - 1) {
                            return (T) getSubElement(currentCompositeMap, indexes[i]);
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
                return (T) getSubElement(elements, indexes[0]);
            }
        }
    }

    /**
     * Sets the value of a element at the position expressed by indexPath.  This method adds the constraint of non null
     * values to {@link #setOrRemoveElement} to distinguish from the intent of setting or removing values.
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
     * Removes the element at the position indicated by indexPath.
     *
     * @param indexPath
     */
    public void removeElement(String indexPath) {
        setOrRemoveElement(indexPath, null);
    }

}
