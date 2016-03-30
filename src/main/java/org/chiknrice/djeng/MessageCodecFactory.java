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

import java.io.InputStream;
import java.util.*;

import static java.lang.String.format;
import static org.chiknrice.djeng.CoreAttributes.*;
import static org.chiknrice.djeng.XmlConfig.ElementName.*;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class MessageCodecFactory {

    public static MessageCodec build(String configXml, String... customSchema) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream[] customSchemaStreams = new InputStream[customSchema.length];
        for (int i = 0; i < customSchema.length; i++) {
            customSchemaStreams[i] = classLoader.getResourceAsStream(customSchema[i]);
        }
        return build(classLoader.getResourceAsStream(configXml), customSchemaStreams);
    }

    public static MessageCodec build(InputStream xmlInputStream, InputStream... customSchema) {
        return new MessageCodecParser(xmlInputStream, customSchema).build();
    }

    private static class MessageCodecParser {

        XmlConfig xmlConfig;
        Map<String, XmlConfig.XmlElement> codecMap;

        MessageCodecParser(InputStream inputStream, InputStream... customSchema) {
            this.xmlConfig = new XmlConfig(inputStream, customSchema);
        }

        MessageCodec build() {
            try {
                codecMap = buildCodecMap();
                final XmlConfig.XmlElement element = xmlConfig.getElement(MESSAGE_ELEMENTS);
                Codec<?> codec = buildCodec(element);
                configureComposite(element, codec);
                return new MessageCodec((CompositeCodec) codec);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        List<String> elementIndexList = new LinkedList<>();
        Stack<String> indexStack = new Stack<>();

        Map<String, XmlConfig.XmlElement> buildCodecMap() {
            Map<String, XmlConfig.XmlElement> codecMap = new HashMap<>();
            XmlConfig.XmlElement codecsElement = xmlConfig.getElement(CODECS);
            for (XmlConfig.XmlElement codecElement : codecsElement.getChildren()) {
                String id = codecElement.getAttribute(ID);
                Class<?> codecClass = codecElement.getAttribute(CLASS);
                switch (codecElement.getName()) {
                    case ELEMENT_CODEC:
                        if (CompositeCodec.class.isAssignableFrom(codecClass)) {
                            throw new RuntimeException(format("%s subclass (%s) cannot be used on %s", CompositeCodec.class.getSimpleName(), codecClass.getName(), codecElement.getName().asString()));
                        }
                        break;
                    case COMPOSITE_CODEC:
                        if (!CompositeCodec.class.isAssignableFrom(codecClass)) {
                            throw new RuntimeException(format("Invalid %s implementation (%s)", CompositeCodec.class.getSimpleName(), codecClass.getName()));
                        }
                        break;
                    case FILTER_CODEC:
                        if (!CodecFilter.class.isAssignableFrom(codecClass)) {
                            throw new RuntimeException(format("Invalid %s implementation (%s)", CodecFilter.class.getSimpleName(), codecClass.getName()));
                        }
                        break;
                    default:
                        throw new RuntimeException("Unexpected element " + codecElement.getName().asString());
                }
                codecMap.put(id, codecElement);
            }
            return codecMap;
        }

        void configureComposite(XmlConfig.XmlElement compositeConfig, Codec<?> codec) throws Exception {
            Map<String, Codec<?>> subElementCodecMap = new LinkedHashMap<>();
            setAttributes(compositeConfig, codec);
            codec.setAttribute(SUB_ELEMENT_CODECS_MAP, subElementCodecMap);
            for (XmlConfig.XmlElement subElementConfig : compositeConfig.getChildren()) {
                Codec<?> subElementCodec = buildCodec(subElementConfig);
                String index = subElementConfig.getAttribute(INDEX);
                indexStack.push(index);
                switch (subElementConfig.getName()) {
                    case ELEMENT:
                        setAttributes(subElementConfig, subElementCodec);
                        elementIndexList.add(getCurrentIndex());
                        break;
                    case COMPOSITE:
                    case MESSAGE_ELEMENTS:
                        configureComposite(subElementConfig, subElementCodec);
                        break;
                    default:
                        throw new RuntimeException("Unexpected element " + subElementConfig.getName().asString());
                }
                subElementCodecMap.put(index, subElementCodec);
                indexStack.pop();
            }
        }

        String getCurrentIndex() {
            StringBuilder sb = new StringBuilder(indexStack.firstElement());
            for (int i = 1; i < indexStack.size(); i++) {
                sb.append(".");
                sb.append(indexStack.elementAt(i));
            }
            return sb.toString();
        }

        Codec<?> buildCodec(XmlConfig.XmlElement elementConfig) {
            String elementCodec = elementConfig.getAttribute(CODEC);
            XmlConfig.XmlElement codecConfig = codecMap.get(elementCodec);
            Codec codec = buildObject(codecConfig.<Class<?>>getAttribute(CLASS));
            setAttributes(codecConfig, codec);
            String filter;
            while ((filter = codecConfig.getOptionalAttribute(FILTER)) != null) {
                codecConfig = codecMap.get(filter);
                CodecFilter<?> codecFilter = buildObject(codecConfig.<Class<?>>getAttribute(CLASS));
                codecFilter.setChain(codec);
                setAttributes(codecConfig, codec);
                codec = codecFilter;
            }
            return codec;
        }

        <T> T buildObject(Class<?> clazz) {
            if (clazz != null) {
                try {
                    return (T) clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            } else {
                return null;
            }
        }

        void setAttributes(XmlConfig.XmlElement element, Codec<?> codec) {
            for (Map.Entry<Attribute, Object> attributeEntry : element.getAttributes().entrySet()) {
                Attribute key = attributeEntry.getKey();
                Object value = attributeEntry.getValue();
                if (CoreAttributes.CLASS.equals(key)) {
                    continue;
                }
                codec.setAttribute(key, value);
            }
        }


    }
}
