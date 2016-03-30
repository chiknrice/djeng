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
import org.concordion.api.FullOGNL;
import org.concordion.api.extension.Extensions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@RunWith(ConcordionRunner.class)
@FullOGNL
@Extensions({SetVarCommandExtension.class})
public abstract class BaseFixture {

    MessageCodec messageCodec;

    public String buildCodecWithConfig(String configuration) {
        try {
            messageCodec = MessageCodecFactory
                    .build(new ByteArrayInputStream(configuration.trim().getBytes(StandardCharsets.UTF_8)),
                            Thread.currentThread().getContextClassLoader().getResourceAsStream("djeng-financial.xsd"));
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
