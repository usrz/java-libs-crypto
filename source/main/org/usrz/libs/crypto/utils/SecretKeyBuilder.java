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
package org.usrz.libs.crypto.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Build a {@link SecretKey}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class SecretKeyBuilder {

    private String algorithm = "AES";
    private int keySize = 128;

    /**
     * Create a new {@link SecretKeyBuilder} using the default <em>AES</em>
     * algorithm and producing keys of 128 bits.
     */
    public SecretKeyBuilder() {
        /* Nothing to do, initialized to sensible defaults */
    }

    /**
     * Create a new {@link SecretKeyBuilder} with the specified algorithm
     * and producing keys of 256 bits.
     */
    public SecretKeyBuilder(String algorithm) {
        algorithm(algorithm);
    }

    /**
     * Generate and return a new {@link SecretKey}.
     */
    public SecretKey build() {
        final KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmh \"" + algorithm + "\" not supported", exception);
        }
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    /**
     * Specify the name of the algorithm used by this {@link SecretKeyBuilder}.
     */
    public SecretKeyBuilder algorithm(String algorithm) {
        if (algorithm == null) throw new NullPointerException("Null algorithm");
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Specify the size (in bits) of the keys generated by this
     * {@link SecretKeyBuilder}.
     */
    public SecretKeyBuilder keySize(int keySize) {
        if (keySize < 0) throw new IllegalArgumentException("Invalid key size " + keySize);
        this.keySize = keySize;
        return this;
    }
}
