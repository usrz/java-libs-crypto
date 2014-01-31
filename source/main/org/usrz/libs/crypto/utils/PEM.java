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

import static org.usrz.libs.crypto.codecs.CharsetCodec.UTF8;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import org.usrz.libs.crypto.pem.PEMEntry;
import org.usrz.libs.crypto.pem.PEMEntry.Type;
import org.usrz.libs.crypto.pem.PEMException;
import org.usrz.libs.crypto.pem.PEMRSAPrivateKeyEntry;
import org.usrz.libs.crypto.pem.PEMRSAPublicKeyEntry;
import org.usrz.libs.crypto.pem.PEMReader;
import org.usrz.libs.crypto.pem.PEMX509CertificateEntry;

/**
 * A simple utility file to read RSA keys and X509 certificates in PEM format
 * as generated by <a href="http://www.openssl.org/">OpenSSL</a>.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class PEM {

    private PEM() {
        throw new IllegalStateException("Do not construct");
    }

    /* ====================================================================== */
    /* X509 CERTIFICATES                                                      */
    /* ====================================================================== */

    /**
     * Load a (list of) X509 certificate(s) from the specified {@link File}.
     *
     * @throws GeneralSecurityException If the certificate could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final List<X509Certificate> loadCertificates(File file)
    throws GeneralSecurityException, PEMException, IOException {
        if (file == null) throw new NullPointerException("Null file");
        return loadCertificates(file.toURI().toURL());
    }

    /**
     * Load a (list of) X509 certificate(s) from the specified {@link URL}.
     *
     * @throws GeneralSecurityException If the certificate could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final List<X509Certificate> loadCertificates(URL url)
    throws GeneralSecurityException, PEMException, IOException {

        final List<PEMEntry<?>> entries = new PEMReader(url).read();
        final List<X509Certificate> certificates = new ArrayList<>();

        for (PEMEntry<?> entry: entries) {
            if (entry.getType() == Type.X509_CERTIFICATE) {
                certificates.add(((PEMX509CertificateEntry)entry).get());
            }
        }

        /* Check if we read any certificate */
        if (certificates.size() > 0) return certificates;
        throw new PEMException(url, "No certificates found");

    }

    /* ====================================================================== */
    /* PUBLIC KEYS                                                            */
    /* ====================================================================== */

    /**
     * Load a RSA public key from the specified {@link File}.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPublicKey loadPublicKey(File file)
    throws GeneralSecurityException, PEMException, IOException {
        if (file == null) throw new NullPointerException("Null file");
        return loadPublicKey(file.toURI().toURL());
    }

    /**
     * Load a RSA public key from the specified {@link File}.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPublicKey loadPublicKey(URL url)
    throws GeneralSecurityException, PEMException, IOException {

        final PEMEntry<?> entry = new PEMReader(url).read().get(0);

        if (entry.getType() == Type.RSA_PUBLIC_KEY) {
            return ((PEMRSAPublicKeyEntry)entry).get();
        }

        /* Check if we read any certificate */
        throw new PEMException(url, "Invalid type " + entry.getType() + " found");
    }

    /* ====================================================================== */
    /* PRIVATE KEYS                                                           */
    /* ====================================================================== */

    /**
     * Load a RSA private key from the specified {@link File}.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPrivateCrtKey loadPrivateKey(File file)
    throws GeneralSecurityException, PEMException, IOException {
        return loadPrivateKey(file, (byte[]) null);
    }

    /**
     * Load a RSA private key from the specified {@link URL}.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPrivateCrtKey loadPrivateKey(URL url)
    throws GeneralSecurityException, PEMException, IOException {
        return loadPrivateKey(url, (byte[]) null);
    }

    /**
     * Load a RSA private key from the specified {@link File} using the
     * specified password (in UTF-8) to decrypt it.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPrivateCrtKey loadPrivateKey(File file, String password)
    throws GeneralSecurityException, PEMException, IOException {
        return loadPrivateKey(file.toURI().toURL(), password);
    }

    /**
     * Load a RSA private key from the specified {@link URL} using the
     * specified password (in UTF-8) to decrypt it.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPrivateCrtKey loadPrivateKey(URL url, String password)
    throws GeneralSecurityException, PEMException, IOException {
        return loadPrivateKey(url, password == null? null: password.getBytes(UTF8));
    }

    /**
     * Load a RSA private key from the specified {@link File} using the
     * specified password to decrypt it.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPrivateCrtKey loadPrivateKey(File file, byte[] password)
    throws GeneralSecurityException, PEMException, IOException {
        return loadPrivateKey(file.toURI().toURL(), password);
    }

    /**
     * Load a RSA private key from the specified {@link URL} using the
     * specified password to decrypt it.
     *
     * @throws GeneralSecurityException If the key could not be loaded.
     * @throws PEMException If the PEM format was somehow broken.
     * @throws IOException If an I/O error occurred.
     */
    public static final RSAPrivateCrtKey loadPrivateKey(URL url, byte[] password)
    throws GeneralSecurityException, PEMException, IOException {
        final PEMEntry<?> entry = new PEMReader(url).read().get(0);

        if (entry.getType() == Type.RSA_PRIVATE_KEY) {
            return ((PEMRSAPrivateKeyEntry)entry).get(password);
        }

        /* Check if we read any certificate */
        throw new PEMException(url, "Invalid type " + entry.getType() + " found");

    }
}
