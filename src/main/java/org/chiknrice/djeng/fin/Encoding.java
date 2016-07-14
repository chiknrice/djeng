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
package org.chiknrice.djeng.fin;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
enum Encoding {

    CHAR,
    BINARY,// when dealing with numeric types, the value is two's complement
    BCD,
    BCD_F, // e.g. 23 3F (for odd digits BCD is left justified and right padded with F - only used for as2805 messages)
    C_BCD, // e.g. C0 01 23 45 (first nibble is C/D, rest is BCD)
    CC_BCD // e.g. 43 01 23 45 (first byte is a C/D char, the rest is BCD)

}
