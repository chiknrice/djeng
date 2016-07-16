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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.String.format;
import static org.chiknrice.djeng.CoreAttributes.*;
import static org.chiknrice.djeng.XmlConfig.ElementName.CODECS;
import static org.chiknrice.djeng.XmlConfig.ElementName.MESSAGE_ELEMENTS;

/**
 * A {@code MessageCodecConfig} is the configuration required when creating a {@link MessageCodec}.  The configuration
 * requires at least a configuration xml and optional custom schemas and {@link AttributeTypeMapper}.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class MessageCodecConfig {

    /**
     * The argument is a path to the xmlConfig.  The underlying implementation expects this config to exist in the
     * classpath.
     *
     * @param xmlConfig TODO
     * @return TODO
     */
    public static MessageCodecConfigBuilder fromXml(String xmlConfig) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlConfig)) {
            byte[] buf = new byte[8192];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int read;
            while ((read = inputStream.read(buf)) != -1) {
                bos.write(buf, 0, read);
            }
            return fromXml(new ByteArrayInputStream(bos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Creates a config builder from an inputstream to the actual config xml, in this case, it is expected that the
     * caller would be closing the resource after the method returns.
     *
     * @param xmlConfig TODO
     * @return TODO
     */
    public static MessageCodecConfigBuilder fromXml(InputStream xmlConfig) {
        return new MessageCodecConfigBuilder(xmlConfig);
    }

    public static class MessageCodecConfigBuilder {

        private final InputStream xmlConfig;
        private final List<String> customSchemas = new ArrayList<>();
        private final List<AttributeTypeMapper> customTypeMappers = new ArrayList<>();
        private int encodeBufferSize = 0x7FFF;

        private MessageCodecConfigBuilder(InputStream xmlConfig) {
            this.xmlConfig = xmlConfig;
        }

        public MessageCodecConfigBuilder withEncodeBufferSize(int bufferSize) {
            this.encodeBufferSize = bufferSize;
            return this;
        }

        public MessageCodecConfigBuilder withSchemas(String... schemas) {
            for (String schema : schemas) {
                customSchemas.add(schema);
            }
            return this;
        }

        public MessageCodecConfigBuilder withAttributeMappers(Class<? extends AttributeTypeMapper>... typeMapperClasses) {
            for (Class<? extends AttributeTypeMapper> typeMapperClass : typeMapperClasses) {
                try {
                    AttributeTypeMapper mapper = typeMapperClass.newInstance();
                    customTypeMappers.add(mapper);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            return this;
        }

        public MessageCodecConfig build() {
            return new MessageCodecConfig(xmlConfig, customSchemas, customTypeMappers, encodeBufferSize);
        }
    }

    private final XmlConfig xmlConfig;
    private final Map<String, XmlConfig.XmlElement> codecMap;
    private final CompositeCodec rootCodec;
    private final int encodeBufferSize;

    private MessageCodecConfig(InputStream xmlConfigStream, List<String> customSchemas, List<AttributeTypeMapper> customTypeMappers, int encodeBufferSize) {
        // XmlConfig only closes the input streams that it creates, the xmlConfigStream is required to be closed by the caller if needed
        try (XmlConfig xmlConfig = new XmlConfig(xmlConfigStream, customSchemas, customTypeMappers)) {
            this.xmlConfig = xmlConfig;
            codecMap = buildCodecMap();
            final XmlConfig.XmlElement element = this.xmlConfig.getElement(MESSAGE_ELEMENTS);
            rootCodec = (CompositeCodec) buildCodec(element);
            configureComposite(element, rootCodec);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        this.encodeBufferSize = encodeBufferSize;
    }

    public CompositeCodec getRootCodec() {
        return rootCodec;
    }

    public int getEncodeBufferSize() {
        return encodeBufferSize;
    }

    private final List<String> elementIndexList = new LinkedList<>();
    private final Stack<String> indexStack = new Stack<>();

    private Map<String, XmlConfig.XmlElement> buildCodecMap() {
        Map<String, XmlConfig.XmlElement> codecMap = new HashMap<>();
        XmlConfig.XmlElement codecsElement = xmlConfig.getElement(CODECS);
        for (XmlConfig.XmlElement codecElement : codecsElement.getChildren()) {
            String id = codecElement.getAttribute(ID);
            Class<?> codecClass = codecElement.getAttribute(CLASS);
            switch (codecElement.getName()) {
                case ELEMENT_CODEC:
                    validateImplementation(ElementCodec.class, codecClass);
                    break;
                case COMPOSITE_CODEC:
                    validateImplementation(CompositeCodec.class, codecClass);
                    break;
                case FILTER_CODEC:
                    validateImplementation(CodecFilter.class, codecClass);
                    break;
                default:
                    throw new RuntimeException("Unexpected element " + codecElement.getName().asString());
            }
            codecMap.put(id, codecElement);
        }
        return codecMap;
    }

    private void validateImplementation(Class<?> expected, Class<?> actual) {
        if (!expected.isAssignableFrom(actual)) {
            throw new RuntimeException(format("%s is not a valid implementation of %s", actual.getName(), expected.getName()));
        }
    }

    private void configureComposite(XmlConfig.XmlElement compositeConfig, Codec<?> codec) {
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

    private String getCurrentIndex() {
        StringBuilder sb = new StringBuilder(indexStack.firstElement());
        for (int i = 1; i < indexStack.size(); i++) {
            sb.append(".");
            sb.append(indexStack.elementAt(i));
        }
        return sb.toString();
    }

    private Codec<?> buildCodec(XmlConfig.XmlElement elementConfig) {
        String elementCodec = elementConfig.getAttribute(CODEC);
        XmlConfig.XmlElement codecConfig = codecMap.get(elementCodec);
        Codec codec = buildObject(codecConfig.<Class<?>>getAttribute(CLASS));
        setAttributes(codecConfig, codec);
        List<XmlConfig.XmlElement> filters = codecConfig.getChildren() ;
        for (XmlConfig.XmlElement filter : filters) {
            codecConfig = codecMap.get(filter.getAttribute(CODEC));
            CodecFilter<?> codecFilter = buildObject(codecConfig.<Class<?>>getAttribute(CLASS));
            codecFilter.setChain(codec);
            setAttributes(codecConfig, codec);
            codec = codecFilter;
        }
        return codec;
    }

    private <T> T buildObject(Class<?> clazz) {
        if (clazz != null) {
            try {
                return (T) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + clazz.getSimpleName(), e);
            }
        } else {
            return null;
        }
    }

    private void setAttributes(XmlConfig.XmlElement element, Codec<?> codec) {
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
