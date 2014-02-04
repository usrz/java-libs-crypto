/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.crypto.codecs;

import static org.usrz.libs.crypto.codecs.HexCodec.HEX;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class CharsetCodecTest extends AbstractTest {

    @Test
    public void test() {
        final String string = "\u6771\u4EAC";
        final byte[] bytes = HEX.decode("e69db1e4baac");
        assertEquals(new CharsetCodec().encode(bytes), string);
        assertEquals(new CharsetCodec().decode(string), bytes);
    }
}
