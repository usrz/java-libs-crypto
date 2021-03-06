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

import java.io.IOException;

import org.testng.annotations.Test;
import org.usrz.libs.crypto.hash.Hash;
import org.usrz.libs.crypto.kdf.KDF.Function;
import org.usrz.libs.testing.AbstractTest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KDFSpecTest extends AbstractTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final void equalityTest(String[] json)
    throws IOException {
        /* Construct specs */
        final KDFSpec[] specs = new KDFSpec[json.length];
        for (int x = 0; x < json.length; x ++) {
            specs[x] = mapper.readValue(json[x], KDFSpec.class);
        }

        /* Test equality */
        for (int x = 0; x < json.length; x ++) {
            final KDFSpec spec = mapper.readValue(json[x], KDFSpec.class);
            assertNotSame(spec, specs[x], "Specs at " + x + " are same instance");
            assertEquals(spec, specs[x], "Specs at " + x + " are different");
            assertEquals(spec.hashCode(), specs[x].hashCode(), "Specs at " + x + " produced different hash codes");
        }

        /* Test inequality */
        for (int x = 0; x < json.length; x ++) {
            for (int y = x + 1 ; y < json.length; y ++) {
                assertNotEquals(specs[x], specs[y], "Spec " + x + " and " + y + " produced same result");
                assertNotEquals(specs[x].hashCode(), specs[y].hashCode(), "Spec " + x + " and " + y + " produced same hash code");
            }
        }
    }

    @Test
    public void testJSON_OPENSSL()
    throws IOException {
        final String json = "{\"function\": \"OPENSSL\", \"hash\": \"SHA1\", \"derivedKeyLength\": \"20\"}";
        final OpenSSLKDFSpec spec = (OpenSSLKDFSpec) mapper.readValue(json, KDFSpec.class);
        assertEquals(spec.getFunction(), Function.OPENSSL);
        assertEquals(spec.getHash(), Hash.SHA1);
        assertEquals(spec.getDerivedKeyLength(), 20);

        final String json2 = mapper.writeValueAsString(spec);
        final OpenSSLKDFSpec spec2 = (OpenSSLKDFSpec) mapper.readValue(json2, KDFSpec.class);
        assertEquals(spec2.getFunction(), Function.OPENSSL);
        assertEquals(spec2.getHash(), Hash.SHA1);
        assertEquals(spec2.getDerivedKeyLength(), 20);

        assertNotSame(spec2, spec);
        assertEquals(spec2, spec);
        assertEquals(spec2.hashCode(), spec.hashCode());
    }

    @Test
    public void testJSON_OPENSSL_Defaults()
    throws IOException {
        final String json = "{\"function\": \"OPENSSL\"}";
        final OpenSSLKDFSpec spec = (OpenSSLKDFSpec) mapper.readValue(json, KDFSpec.class);
        assertEquals(spec.getFunction(), Function.OPENSSL);
        assertEquals(spec.getHash(), Hash.MD5);
        assertEquals(spec.getDerivedKeyLength(), 16);
        assertEquals(mapper.writeValueAsString(spec), "{\"function\":\"OPENSSL\",\"hash\":\"MD5\",\"derivedKeyLength\":16}");
    }

    @Test
    public void testJSON_OPENSSL_Equality()
    throws IOException {
        /* Here we also mix cases for type and hash, just to check */
        equalityTest(new String[] { "{\"function\":\"OPENSSL\",\"hash\":\"md5\", \"derivedKeyLength\":10}"
                                  , "{\"function\":\"openssl\",\"hash\":\"MD5\", \"derivedKeyLength\":16}"
                                  , "{\"function\":\"OPENSSL\",\"hash\":\"SHA1\",\"derivedKeyLength\":16}"
                                  });
    }

    @Test
    public void testJSON_PBKDF2()
    throws IOException {
        final String json = "{\"function\": \"PBKDF2\", \"hash\": \"SHA512\", \"derivedKeyLength\": \"128\",\"iterations\":\"10000\"}";
        final PBKDF2Spec spec = (PBKDF2Spec) mapper.readValue(json, KDFSpec.class);
        assertEquals(spec.getFunction(), Function.PBKDF2);
        assertEquals(spec.getHash(), Hash.SHA512);
        assertEquals(spec.getDerivedKeyLength(), 128);
        assertEquals(spec.getIterations(), 10000);

        final String json2 = mapper.writeValueAsString(spec);
        final PBKDF2Spec spec2 = (PBKDF2Spec) mapper.readValue(json2, KDFSpec.class);
        assertEquals(spec2.getFunction(), Function.PBKDF2);
        assertEquals(spec2.getHash(), Hash.SHA512);
        assertEquals(spec2.getDerivedKeyLength(), 128);
        assertEquals(spec2.getIterations(), 10000);

        assertNotSame(spec2, spec);
        assertEquals(spec2, spec);
        assertEquals(spec2.hashCode(), spec.hashCode());
    }

    @Test
    public void testJSON_PBKDF2_Defaults()
    throws IOException {
        final String json = "{\"function\": \"PBKDF2\",\"iterations\":\"5000\"}";
        final PBKDF2Spec spec = (PBKDF2Spec) mapper.readValue(json, KDFSpec.class);
        assertEquals(spec.getFunction(), Function.PBKDF2);
        assertEquals(spec.getHash(), Hash.SHA1);
        assertEquals(spec.getDerivedKeyLength(), 20);
        assertEquals(mapper.writeValueAsString(spec), "{\"function\":\"PBKDF2\",\"hash\":\"SHA1\",\"derivedKeyLength\":20,\"iterations\":5000}");
    }

    @Test
    public void testJSON_PBKDF2_Equality()
    throws IOException {
        /* Here we also mix cases for type and hash, just to check */
        equalityTest(new String[] { "{\"function\":\"PBKDF2\",\"hash\":\"SHA1\",\"derivedKeyLength\":20,\"iterations\":5000}"
                                  , "{\"function\":\"pbkdf2\",\"hash\":\"MD5\", \"derivedKeyLength\":20,\"iterations\":5000}"
                                  , "{\"function\":\"PBKDF2\",\"hash\":\"sha1\",\"derivedKeyLength\":10,\"iterations\":5000}"
                                  , "{\"function\":\"pbkdf2\",\"hash\":\"SHA1\",\"derivedKeyLength\":20,\"iterations\":1000}"
                                  });
    }

    @Test
    public void testJSON_SCrypt()
    throws IOException {
        final String json = "{\"function\": \"SCRYPT\", \"hash\": \"SHA512\", \"derivedKeyLength\": \"128\",\"iterations\":\"16384\",\"blockSize\":\"8\",\"parallelization\":\"1\"}";
        final SCryptSpec spec = (SCryptSpec) mapper.readValue(json, KDFSpec.class);
        assertEquals(spec.getFunction(), Function.SCRYPT);
        assertEquals(spec.getHash(), Hash.SHA512);
        assertEquals(spec.getDerivedKeyLength(), 128);
        assertEquals(spec.getIterations(), 16384);
        assertEquals(spec.getBlockSize(), 8);
        assertEquals(spec.getParallelization(), 1);

        final String json2 = mapper.writeValueAsString(spec);
        final SCryptSpec spec2 = (SCryptSpec) mapper.readValue(json2, KDFSpec.class);
        assertEquals(spec2.getFunction(), Function.SCRYPT);
        assertEquals(spec2.getHash(), Hash.SHA512);
        assertEquals(spec2.getDerivedKeyLength(), 128);
        assertEquals(spec2.getIterations(), 16384);
        assertEquals(spec2.getBlockSize(), 8);
        assertEquals(spec2.getParallelization(), 1);

        assertNotSame(spec2, spec);
        assertEquals(spec2, spec);
        assertEquals(spec2.hashCode(), spec.hashCode());
    }

    @Test
    public void testJSON_SCrypt_Defaults()
    throws IOException {
        final String json = "{\"function\": \"SCRYPT\",\"iterations\":\"16384\"}";
        final SCryptSpec spec = (SCryptSpec) mapper.readValue(json, KDFSpec.class);
        assertEquals(spec.getFunction(), Function.SCRYPT);
        assertEquals(spec.getHash(), Hash.SHA256);
        assertEquals(spec.getDerivedKeyLength(), 32);
        assertEquals(mapper.writeValueAsString(spec), "{\"function\":\"SCRYPT\",\"hash\":\"SHA256\",\"derivedKeyLength\":32,\"iterations\":16384,\"blockSize\":8,\"parallelization\":1}");
    }

    @Test
    public void testJSON_SCrypt_Equality()
    throws IOException {
        /* Here we also mix cases for type and hash, just to check */
        equalityTest(new String[] { "{\"function\":\"SCRYPT\",\"hash\":\"SHA256\",\"derivedKeyLength\":32,\"iterations\":16384,\"blockSize\":8, \"parallelization\":1}"
                                  , "{\"function\":\"scrypt\",\"hash\":\"SHA512\",\"derivedKeyLength\":32,\"iterations\":16384,\"blockSize\":8, \"parallelization\":1}"
                                  , "{\"function\":\"SCRYPT\",\"hash\":\"sha256\",\"derivedKeyLength\":64,\"iterations\":16384,\"blockSize\":8, \"parallelization\":1}"
                                  , "{\"function\":\"scrypt\",\"hash\":\"SHA256\",\"derivedKeyLength\":32,\"iterations\":65536,\"blockSize\":8, \"parallelization\":1}"
                                  , "{\"function\":\"SCRYPT\",\"hash\":\"sha256\",\"derivedKeyLength\":32,\"iterations\":16384,\"blockSize\":16,\"parallelization\":1}"
                                  , "{\"function\":\"scrypt\",\"hash\":\"SHA256\",\"derivedKeyLength\":32,\"iterations\":16384,\"blockSize\":8, \"parallelization\":2}"
                                  });
    }
}
