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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This base codec just provides the convenience of handling attribute storage for all codecs
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class BaseCodec<T> implements Codec<T> {

    private final Map<Attribute, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public final <A> A getAttribute(Attribute attribute) {
        return (A) attributes.get(attribute);
    }

    @Override
    public final void setAttribute(Attribute attribute, Object value) {
        attributes.put(attribute, value);
    }

}
