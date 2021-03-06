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
package org.usrz.libs.crypto.kdf;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.crypto.hash.Hash;
import org.usrz.libs.crypto.kdf.KDF.Function;
import org.usrz.libs.utils.Check;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * A simple <em>builder</em> constructing {@link KDFSpec} instances.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@JsonPOJOBuilder
public class KDFSpecBuilder {

    /** The key defining the <em>function</em> of this KDF. */
    public static final String FUNCTION = "function";
    /** The key defining the <em>hash function</em> of this KDF. */
    public static final String HASH_FUNCTION = "hashFunction";
    /** The key defining the <em>derived key length</em> of this KDF. */
    public static final String DERIVED_KEY_LENGTH = "derivedKeyLength";
    /** The key defining the <em>number of iterations</em> of this KDF. */
    public static final String ITERATIONS = "iterations";
    /** The key defining the <em>block size</em> of this KDF. */
    public static final String BLOCK_SIZE = "blockSize";
    /** The key defining the <em>parallelization</em> of this KDF. */
    public static final String PARALLELIZATION = "parallelization";

    /* ====================================================================== */

    private final Function function;
    private Hash hash = null;
    private int derivedKeyLength = -1;
    private int iterations = -1;
    private int blockSize = -1;
    private int parallelization = -1;

    /* ====================================================================== */

    /**
     * Create a new {@link KDFSpecBuilder} given the KDF's {@link Function}.
     *
     * <p>This method is also used by Jackson to create instances from JSON.</p>
     */
    @JsonCreator
    public KDFSpecBuilder(@JsonProperty(FUNCTION) String function) {
        Check.notNull(function, "Null function");
        try {
            this.function = Function.valueOf(function.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid function \"" + function + "\"", exception);
        }
    }

    /**
     * Create a new {@link KDFSpecBuilder} given the KDF's {@link Function}.
     */
    public KDFSpecBuilder(Function function) {
        this.function = Check.notNull(function, "Null function");
    }

    /**
     * Create a new {@link KDFSpecBuilder} from a {@link Configurations}
     * containing properties keyed by the static {@link String} fields outlined
     * in this class.
     */
    public KDFSpecBuilder(Configurations configurations) {
        Check.notNull(configurations, "Null configurations");
        final String function = configurations.requireString(FUNCTION);
        try {
            this.function = Function.valueOf(function.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid function \"" + function + "\"", exception);
        }
        withConfigurations(configurations);
    }

    /* ====================================================================== */

    /**
     * Build a {@link KDFSpec} instance according to the collected parameters.
     */
    public KDFSpec build() {
        switch (function) {
            case OPENSSL: return new OpenSSLKDFSpec(hash, derivedKeyLength);
            case PBKDF2:  return new PBKDF2Spec(hash, derivedKeyLength, iterations);
            case SCRYPT:  return new SCryptSpec(hash, derivedKeyLength, iterations, blockSize, parallelization);
            default: throw new IllegalStateException("Unsupported KDF function " + function);
        }
    }

    /* ====================================================================== */

    /**
     * Specify the {@link Hash} used by the {@link KDF}.
     */
    @JsonIgnore
    public KDFSpecBuilder withHash(Hash hash) {
        this.hash = hash;
        return this;
    }

    /**
     * Specify the {@link Hash} used by the {@link KDF}.
     *
     * <p>This method is also used by Jackson to create instances from JSON.</p>
     */
    @JsonProperty("hash")
    public KDFSpecBuilder withHash(String hash) {
        try {
            this.hash = Hash.valueOf(hash.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid hash \"" + hash + "\"", exception);
        }
        return this;
    }

    /**
     * Specify the <em>derived key length</em> produced by the {@link KDF}.
     */
    public KDFSpecBuilder withDerivedKeyLength(int derivedKeyLength) {
        this.derivedKeyLength = derivedKeyLength;
        return this;
    }

    /**
     * Specify the number of iterations used by {@link PBKDF2} and
     * {@link SCrypt} (also known as <em>CPU/memory cost</em> parameter).
     */
    public KDFSpecBuilder withIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    /**
     * Specify the block size used by {@link SCrypt}.
     */
    public KDFSpecBuilder withBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    /**
     * Specify the parallelization factor used by {@link SCrypt}.
     */
    public KDFSpecBuilder withParallelization(int parallelization) {
        this.parallelization = parallelization;
        return this;
    }

    /**
     * Read properties out of a {@link Configurations} instance.
     */
    @JsonIgnore
    public KDFSpecBuilder withConfigurations(Configurations configurations) {
        Check.notNull(configurations, "Null configurations");

        final String hash = configurations.getString(HASH_FUNCTION, null);
        if (hash != null) this.withHash(hash);

        derivedKeyLength = configurations.get(DERIVED_KEY_LENGTH, derivedKeyLength);
        iterations       = configurations.get(ITERATIONS,         iterations);
        blockSize        = configurations.get(BLOCK_SIZE,         blockSize);
        parallelization  = configurations.get(PARALLELIZATION,    parallelization);
        return this;
    }

}
