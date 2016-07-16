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

import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
class XmlConfig implements Closeable {

    static final String CORE_SCHEMA_FILE = "djeng.xsd";
    static final String NAMESPACE = "http://www.chiknrice.org/djeng";

    final Document document;
    final List<AttributeTypeMapper> customTypeMappers;

    final List<InputStream> inputStreams;

    XmlConfig(InputStream configSource, List<String> customSchemas, List<AttributeTypeMapper> customTypeMappers) {
        try {
            this.customTypeMappers = customTypeMappers;
            Source[] schemaSources = new Source[customSchemas.size() + 1];
            inputStreams = new ArrayList<>();
            inputStreams.add(Thread.currentThread().getContextClassLoader().getResourceAsStream(CORE_SCHEMA_FILE));
            for (String customSchema : customSchemas) {
                inputStreams.add(Thread.currentThread().getContextClassLoader().getResourceAsStream(customSchema));
            }
            for (int i = 0; i < inputStreams.size(); i++) {
                schemaSources[i] = new StreamSource(inputStreams.get(i));
            }

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(schemaSources);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setSchema(schema);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(configSource);
            document.getDocumentElement().normalize();
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        for (InputStream inputStream : inputStreams) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    enum ElementName {
        CODECS,
        ELEMENT_CODEC,
        COMPOSITE_CODEC,
        FILTER_CODEC,
        FILTER,
        MESSAGE_ELEMENTS,
        ELEMENT,
        COMPOSITE;

        private final String name;

        ElementName() {
            String name = this.toString().toLowerCase();
            name = name.replace('_', '-');
            this.name = name;
        }

        public String asString() {
            return name;
        }

        public static ElementName asEnum(String elementName) {
            return ElementName.valueOf(elementName.toUpperCase().replace('-', '_'));
        }

    }

    XmlElement getElement(ElementName elementName) {
        List<XmlElement> elements = getElements(elementName);
        if (elements.size() > 1) {
            throw new RuntimeException(String.format("Got %d %s elements", elements.size(), elementName.asString()));
        }
        if (elements.size() > 0) {
            return elements.get(0);
        } else {
            return null;
        }
    }

    List<XmlElement> getElements(ElementName elementName) {
        NodeList nodeList = document.getElementsByTagNameNS(NAMESPACE, elementName.asString());
        int listSize = nodeList.getLength();
        List<XmlElement> elements = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            Node nodeItem = nodeList.item(i);
            if (nodeItem instanceof Element) {
                elements.add(new XmlElement((Element) nodeItem));
            }
        }
        return elements;
    }

    class XmlElement {

        final Element element;

        XmlElement(Element element) {
            this.element = element;
        }

        List<XmlElement> getChildren() {
            NodeList childNodes = element.getChildNodes();
            List<XmlElement> children = new ArrayList<>();

            int length = childNodes.getLength();

            for (int i = 0; i < length; i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element && NAMESPACE.equals(node.getNamespaceURI())) {
                    children.add(new XmlElement((Element) node));
                }

            }
            return children;
        }

        ElementName getName() {
            return ElementName.asEnum(element.getTagName());
        }

        Map<Attribute, Object> getAttributes() {
            Map<Attribute, Object> attributes = new HashMap<>();
            NamedNodeMap xmlAttributes = element.getAttributes();
            int length = xmlAttributes.getLength();
            for (int i = 0; i < length; i++) {
                Attr xmlAttribute = (Attr) xmlAttributes.item(i);
                Attribute key = new Attribute(xmlAttribute.getLocalName(), xmlAttribute.getNamespaceURI() != null ? xmlAttribute.getNamespaceURI() : element.getNamespaceURI());
                attributes.put(key, CoreAttributes.mapType(key, xmlAttribute.getValue(), customTypeMappers));
            }
            return attributes;
        }

        <T> T getAttribute(Attribute attribute) {
            T value = getOptionalAttribute(attribute);
            if (value != null) {
                return value;
            } else {
                throw new RuntimeException(String.format("Missing mandatory attribute %s in element %s", attribute, element.getTagName()));
            }
        }

        <T> T getOptionalAttribute(Attribute attribute) {
            return getOptionalAttribute(attribute, null);
        }

        <T> T getOptionalAttribute(Attribute attribute, String defaultValue) {
            String stringValue;
            if (element.getNamespaceURI().equals(attribute.getNamespace())) {
                stringValue = element.getAttribute(attribute.getName());
            } else {
                stringValue = element.getAttributeNS(attribute.getNamespace(), attribute.getName());
            }
            if (stringValue == null || stringValue.trim().length() == 0) {
                stringValue = defaultValue;
            }
            if (stringValue == null || stringValue.trim().length() == 0) {
                return null;
            } else {
                return (T) CoreAttributes.mapType(attribute, stringValue, customTypeMappers);
            }
        }

        @Override
        public int hashCode() {
            return element.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof XmlElement) {
                XmlElement other = (XmlElement) obj;
                return element.equals(other.element);
            }
            return false;
        }
    }

}
