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
import static org.chiknrice.djeng.CoreAttribute.*;
import static org.chiknrice.djeng.XmlConfig.ElementName.*;

/**
 * A {@code MessageCodecConfig} is the configuration required when creating a {@link MessageCodec}.  The configuration
 * requires at least a configuration xml and optional custom schemas and {@link Attribute}s.  The config can also be
 * built with an encode buffer size (defaults to 0xFFFF) and to enable debugging.
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
        private final List<Attribute> customAttributes = new ArrayList<>();
        private int encodeBufferSize = 0x7FFF;
        private boolean debugEnabled = false;

        private MessageCodecConfigBuilder(InputStream xmlConfig) {
            this.xmlConfig = xmlConfig;
        }

        public MessageCodecConfigBuilder withEncodeBufferSize(int bufferSize) {
            encodeBufferSize = bufferSize;
            return this;
        }

        public MessageCodecConfigBuilder withDebugEnabled() {
            debugEnabled = true;
            return this;
        }

        public MessageCodecConfigBuilder withSchemas(String... schemas) {
            for (String schema : schemas) {
                customSchemas.add(schema);
            }
            return this;
        }

        public MessageCodecConfigBuilder withCustomAttributes(Attribute[] attributes) {
            customAttributes.addAll(Arrays.asList(attributes));
            return this;
        }

        public MessageCodecConfig build() {
            return new MessageCodecConfig(xmlConfig, customSchemas, customAttributes, encodeBufferSize, debugEnabled);
        }
    }

    private final XmlConfig xmlConfig;
    private final Map<String, XmlConfig.XmlElement> codecConfigMap;
    private final Codec<CompositeMap> rootCodec;
    private final int encodeBufferSize;
    private final boolean debugEnabled;

    private MessageCodecConfig(InputStream xmlConfigStream, List<String> customSchemas, List<Attribute> customAttributes, int encodeBufferSize, boolean debugEnabled) {
        // XmlConfig only closes the input streams that it creates, the xmlConfigStream is required to be closed by the caller if needed
        try (XmlConfig xmlConfig = new XmlConfig(xmlConfigStream, customSchemas, customAttributes)) {
            this.xmlConfig = xmlConfig;
            codecConfigMap = buildCodecConfigMap();
            final XmlConfig.XmlElement messageElementsConfig = this.xmlConfig.getElement(MESSAGE_ELEMENTS);
            rootCodec = (Codec<CompositeMap>) buildCodec(messageElementsConfig);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        this.encodeBufferSize = encodeBufferSize;
        this.debugEnabled = debugEnabled;
    }

    public Codec<CompositeMap> getRootCodec() {
        return rootCodec;
    }

    public int getEncodeBufferSize() {
        return encodeBufferSize;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    private Map<String, XmlConfig.XmlElement> buildCodecConfigMap() throws Exception {
        Map<String, XmlConfig.XmlElement> codecConfigMap = new HashMap<>();
        XmlConfig.XmlElement codecsElement = xmlConfig.getElement(CODECS);
        for (XmlConfig.XmlElement codecElement : codecsElement.getChildren()) {
            String id = codecElement.getAttribute(ID);
            Class codecClass = codecElement.getAttribute(CLASS);
            switch (codecElement.getName()) {
                case CODEC_FILTER:
                    validateImplementation(CodecFilter.class, codecClass);
                    break;
                case ELEMENT_CODEC:
                    validateImplementation(ElementCodec.class, codecClass);
                    break;
                case COMPOSITE_CODEC:
                    validateImplementation(CompositeCodec.class, codecClass);
                    break;
                default:
                    throw new RuntimeException("Unexpected element " + codecElement.getName().asString());
            }
            codecConfigMap.put(id, codecElement);
        }
        return codecConfigMap;
    }

    private void validateImplementation(Class expected, Class actual) {
        if (!expected.isAssignableFrom(actual)) {
            throw new RuntimeException(format("%s is not a valid implementation of %s", actual.getName(), expected.getName()));
        }
    }

    private Map<String, Codec> buildSubElementCodecMap(XmlConfig.XmlElement compositeConfig) throws Exception {
        // LinkedHashMap ensures the ordering of the elements in the config is maintained
        Map<String, Codec> subElementCodecMap = new LinkedHashMap<>();
        for (XmlConfig.XmlElement subElementConfig : compositeConfig.getChildren()) {
            Codec subElementCodec = buildCodec(subElementConfig);
            String index = subElementConfig.getAttribute(INDEX);
            subElementCodecMap.put(index, subElementCodec);
        }
        return subElementCodecMap;
    }

    private Codec buildCodec(XmlConfig.XmlElement elementConfig) throws Exception {
        String codecRef = elementConfig.getAttribute(CODEC);
        XmlConfig.XmlElement codecConfig = codecConfigMap.get(codecRef);
        Codec codec = buildObject(codecConfig.<Class>getAttribute(CLASS));
        Codec baseCodec = codec;
        Map<Attribute, Object> codecAttributes = new HashMap<>();

        final XmlConfig.ElementName name = elementConfig.getName();
        if (MESSAGE_ELEMENTS.equals(name) || COMPOSITE.equals(name)) {
            Map<String, Codec> subElementCodecMap = buildSubElementCodecMap(elementConfig);
            codecAttributes.put(SUB_ELEMENT_CODECS_MAP, subElementCodecMap);
        }

        // Codec attributes first
        setAttributes(codecConfig, codecAttributes);
        List<XmlConfig.XmlElement> filters = codecConfig.getChildren();
        for (XmlConfig.XmlElement filter : filters) {
            XmlConfig.XmlElement globalFilterConfig = codecConfigMap.get(filter.getAttribute(CODEC));
            codec = wrap(codec, globalFilterConfig.<Class>getAttribute(CLASS));
            // Main filter attributes override codec attributes
            setAttributes(globalFilterConfig, codecAttributes);
            // Filter reference attributes override the main one
            setAttributes(filter, codecAttributes);
        }
        // Element attributes override both filter & codec attributes
        setAttributes(elementConfig, codecAttributes);

        // Attributes are immutable
        baseCodec.attributes = Collections.unmodifiableMap(codecAttributes);
        return codec;
    }

    private Codec wrap(Codec codec, Class filter) {
        CodecFilter codecFilter = buildObject(filter);
        codecFilter.chain = codec;
        return codecFilter;
    }

    private <T> T buildObject(Class clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getSimpleName(), e);
        }
    }

    private void setAttributes(XmlConfig.XmlElement element, Map<Attribute, Object> codecAttributes) throws Exception {
        for (Map.Entry<Attribute, Object> attributeEntry : element.getAttributes().entrySet()) {
            Attribute key = attributeEntry.getKey();
            Object value = attributeEntry.getValue();
            // Filters should not override id and class attributes
            if (CODEC_FILTER.equals(element.getName()) && (CoreAttribute.ID.equals(key) || CoreAttribute.CLASS.equals(key))) {
                continue;
            }
            codecAttributes.put(key, value);
        }
    }

}
