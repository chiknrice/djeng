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
package org.chiknrice.djeng.specs;

import org.chiknrice.concordion.SetVarCommandExtension;
import org.chiknrice.djeng.MessageCodec;
import org.chiknrice.djeng.MessageCodecFactory;
import org.chiknrice.djeng.MessageCodecFactoryBuilder;
import org.chiknrice.djeng.fin.FinancialAttributes;
import org.concordion.api.FullOGNL;
import org.concordion.api.extension.Extensions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@RunWith(ConcordionRunner.class)
@FullOGNL
@Extensions({SetVarCommandExtension.class, StyleExtension.class})
public abstract class BaseFixture {

    static MessageCodecFactory MESSAGE_CODEC_FACTORY;

    @BeforeClass
    public static void init() {
        MESSAGE_CODEC_FACTORY = new MessageCodecFactoryBuilder().withSchema("djeng-financial.xsd").withTypeMapper(FinancialAttributes.class).build();
    }

    MessageCodec messageCodec;

    public String buildCodecWithConfig(String configuration) {
        try {
            messageCodec = MESSAGE_CODEC_FACTORY.build(new ByteArrayInputStream(configuration.trim().getBytes(StandardCharsets.UTF_8)));
            return "successful";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
