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

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class CodecFilter<T, W> extends Codec<T> {

    @Override
    public final void encode(ByteBuffer buffer, T element) {
        if (chain == null) {
            throw new RuntimeException("Missing codec chain");
        }
        encode(buffer, element, chain);
    }

    protected abstract void encode(ByteBuffer buffer, T element, Codec<W> chain);

    @Override
    public final T decode(ByteBuffer buffer) {
        if (chain == null) {
            throw new RuntimeException("Missing codec chain");
        }
        return decode(buffer, chain);
    }

    protected abstract T decode(ByteBuffer buffer, Codec<W> chain);

    @Override
    public final <A> A getAttribute(Attribute attribute) {
        //noinspection unchecked
        return (A) chain.getAttribute(attribute);
    }

    Codec<W> chain;

    /**
     * TODO
     * @param type TODO
     * @param <D> TODO
     * @return the delegate or {@code null} if it doesn't exist
     */
    protected <D> D getDelegate(Class<D> type) {
        Codec chain = this.chain;
        while (true) {
            if (type.isAssignableFrom(chain.getClass())) {
                return type.cast(chain);
            }
            if (chain instanceof CodecFilter) {
                chain = ((CodecFilter) chain).chain;
            } else {
                return null;
            }
        }
    }
}
