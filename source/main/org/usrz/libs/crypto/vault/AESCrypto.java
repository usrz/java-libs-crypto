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
package org.usrz.libs.crypto.vault;

import static org.usrz.libs.utils.Check.notNull;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.usrz.libs.configurations.Password;
import org.usrz.libs.crypto.kdf.KDF;
import org.usrz.libs.crypto.utils.CryptoUtils;
import org.usrz.libs.utils.Check;

public class AESCrypto implements Crypto {

    private final KDF kdf;
    private final AESCryptoSpec spec;
    private final SecureRandom random;
    private final byte[] password;
    private volatile boolean destroyed = false;
    private final Object lock = new Object();

    public AESCrypto(KDF kdf, Password password) {
        this(new SecureRandom(), kdf, password);
    }

    public AESCrypto(SecureRandom random, KDF kdf, Password password) {
        this.kdf = Check.notNull(kdf, "Null KDF");
        this.random = random == null ? new SecureRandom() : random;
        this.password = CryptoUtils.safeEncode(password.get(), false);
        spec = new AESCryptoSpec(kdf.getKDFSpec());
    }

    @Override
    public CryptoSpec getSpec() {
        return spec;
    }

    @Override
    public void close() {
        if (! destroyed) {
            synchronized (lock) {
                CryptoUtils.destroyArray(password);
                destroyed = true;
            }
        }
    }

    @Override
    public boolean isDestroyed() {
        synchronized (lock) {
            return destroyed;
        }
    }

    @Override
    public boolean canEncrypt() {
        return ! isDestroyed();
    }

    @Override
    public boolean canDecrypt() {
        return ! isDestroyed();
    }

    @Override
    public byte[] encrypt(byte[] data)
    throws GeneralSecurityException {
        if (isDestroyed()) throw new IllegalStateException("Vault destroyed");
        notNull(data, "Null data to encrypt");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        /* We use the Initialization Vector from AES as the salt for our KDF */
        final byte[] iv = CryptoUtils.randomBytes(cipher.getBlockSize());
        final byte[] key = kdf.deriveKey(password, iv);
        byte[] encrypted = null;

        try {
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            final SecretKey secretKey = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec, random);
            encrypted = cipher.doFinal(data);

            final byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return result;
        } finally {
            CryptoUtils.destroyArray(encrypted);
            CryptoUtils.destroyArray(key);
            CryptoUtils.destroyArray(iv);
        }
    }

    @Override
    public byte[] decrypt(byte[] data)
    throws GeneralSecurityException {
        if (isDestroyed()) throw new IllegalStateException("Vault destroyed");
        notNull(data, "No data to decrypt");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        /* We use the Initialization Vector from AES as the salt for our KDF */
        final byte[] iv = new byte[cipher.getBlockSize()];
        System.arraycopy(data, 0, iv, 0, iv.length);
        final byte[] key = kdf.deriveKey(password, iv);

        try {
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            final SecretKey secretKey = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec, random);
            return cipher.doFinal(data, iv.length, data.length - iv.length);
        } finally {
            CryptoUtils.destroyArray(key);
            CryptoUtils.destroyArray(iv);
        }
    }

}
