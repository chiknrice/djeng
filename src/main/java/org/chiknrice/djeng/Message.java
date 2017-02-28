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

    private static final Pattern INDEX_PATH_PATTERN = Pattern.compile("[^\\s.]+(\\.[^\\s.]+)*");

    private final CompositeMap elements;

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

    private void validateIndexPath(String indexPath) {
        Matcher m = INDEX_PATH_PATTERN.matcher(indexPath);
        if (!m.matches()) {
            throw new IllegalArgumentException(format("%s is not a valid index path", indexPath));
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
        validateIndexPath(indexPath);

        String[] indexes = indexPath.split("\\.");

        Stack<CompositeMap> compositeMapStack = new Stack<>();
        compositeMapStack.push(elements);

        for (int i = 0; i < indexes.length; i++) {
            String key = indexes[i];
            // if at target index
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
    }

    /**
     * Access to the composite data elements.  Used only within the framework during encoding.
     *
     * @return the elements.
     */
    CompositeMap getCompositeMap() {
        return elements;
    }

    private Map<String, Object> getCompositeElement(CompositeMap compositeMap) {
        Map<String, Object> resultingMap = new HashMap<>();
        flattenCompositeMap(compositeMap, resultingMap);
        return resultingMap;
    }

    /**
     * Recursive method to get all message element values except for composite elements.
     *
     * @param compositeMap TODO
     * @param targetMap    TODO
     */
    private void flattenCompositeMap(CompositeMap compositeMap, Map<String, Object> targetMap) {
        for (Entry<String, Object> entry : compositeMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof CompositeMap) {
                Map<String, Object> subMap = new HashMap<>();
                flattenCompositeMap((CompositeMap) value, subMap);
                for (Entry<String, Object> tmpEntry : subMap.entrySet()) {
                    targetMap.put(entry.getKey().concat(".").concat(tmpEntry.getKey()), tmpEntry.getValue());
                }
            } else {
                targetMap.put(entry.getKey(), value);
            }
        }
    }

    //
    // Public API starts
    //

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
        return "TODO Message.toString()";
    }

    /**
     * Gets all the elements as a flat map.
     *
     * @return TODO
     */
    public Map<String, Object> getElements() {
        return getCompositeElement(this.elements);
    }

    /**
     * Gets the value located at the indexPath.  If the value is a {@code CompositeMap} it would be flatten to a map of
     * value having a relative index path.
     *
     * @param indexPath TODO
     * @param <T>       TODO
     * @return the value or {@code null} if the element doesn't exist
     * @throws IllegalArgumentException if the indexPath pattern is not valid
     */
    public <T> T getElement(String indexPath) {
        validateIndexPath(indexPath);

        String[] indexes = indexPath.split("\\.");

        CompositeMap currentCompositeMap = elements;
        Object element = null;
        for (int i = 0; i < indexes.length; i++) {
            element = currentCompositeMap.get(indexes[i]);
            // if at target index
            if (i == indexes.length - 1) {
                // if value at target index is composite map, flatten it
                if (element instanceof CompositeMap) {
                    element = getCompositeElement((CompositeMap) element);
                }
                break;
            } else {
                // expect a composite
                if (element instanceof CompositeMap) {
                    currentCompositeMap = (CompositeMap) element;
                } else {
                    element = null;
                    break;
                }
            }
        }
        return (T) element;
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
