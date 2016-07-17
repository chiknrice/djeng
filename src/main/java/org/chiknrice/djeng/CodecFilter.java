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
public abstract class CodecFilter<T, W> implements Codec<T> {

    @Override
    public final void encode(ByteBuffer buffer, MessageElement<T> element) {
        if (chain == null) {
            throw new RuntimeException("Missing codec chain");
        }
        encode(buffer, element, chain);
    }

    protected abstract void encode(ByteBuffer buffer, MessageElement<T> element, Codec<W> chain);

    @Override
    public final MessageElement<T> decode(ByteBuffer buffer) {
        if (chain == null) {
            throw new RuntimeException("Missing codec chain");
        }
        MessageElement<T> element = decode(buffer, chain);
        return element;
    }

    protected abstract MessageElement<T> decode(ByteBuffer buffer, Codec<W> chain);

    @Override
    public final <A> A getAttribute(Attribute attribute) {
        //noinspection unchecked
        return (A) chain.getAttribute(attribute);
    }

    @Override
    public final void setAttribute(Attribute attribute, Object value) {
        chain.setAttribute(attribute, value);
    }

    private Codec<W> chain;

    void setChain(Codec<W> codec) {
        this.chain = codec;
    }

    public Codec<W> getChain() {
        return chain;
    }

    /**
     * TODO
     * @param chain TODO
     * @param type TODO
     * @param <D> TODO
     * @return the delegate or {@code null} if it doesn't exist
     */
    protected <D> D getDelegate(Codec chain, Class<D> type) {
        while (true) {
            if (type.isAssignableFrom(chain.getClass())) {
                return type.cast(chain);
            }
            if (chain instanceof CodecFilter) {
                chain = ((CodecFilter) chain).getChain();
            } else {
                return null;
            }
        }
    }
}
