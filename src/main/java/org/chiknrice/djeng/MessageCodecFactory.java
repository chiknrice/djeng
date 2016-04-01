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

/**
 * A factory for creating {@see MessageCodec} implementation based on a supplied xml configuration.  A
 * MessageCodecFactory is reusable and would have consistent custom schemas and custom type mappers configured.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public interface MessageCodecFactory {

    /**
     * The argument is a path to the configXml.  The underlying implementation expects this config to exist in the
     * classpath.
     *
     * @param configXml
     * @return
     */
    MessageCodec build(String configXml);

    /**
     * The codec factory accepts an inputstream to the actual config xml, in this case, it is expected that the caller
     * would be closing the resource after the method returns.
     *
     * @param configXml
     * @return
     */
    MessageCodec build(InputStream configXml);

}
