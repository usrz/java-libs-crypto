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

import static org.usrz.libs.crypto.vault.NoOpVault.NO_OP_VAULT;
import static org.usrz.libs.utils.codecs.HexCodec.HEX;

import java.security.SecureRandom;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.Password;
import org.usrz.libs.crypto.kdf.BasicKDFManager;
import org.usrz.libs.crypto.kdf.KDF;
import org.usrz.libs.crypto.kdf.KDFSpec;
import org.usrz.libs.crypto.kdf.KDFSpecBuilder;
import org.usrz.libs.crypto.vault.Vault.Type;
import org.usrz.libs.utils.Check;
import org.usrz.libs.utils.codecs.Codec;
import org.usrz.libs.utils.codecs.CodecManager;

public class VaultBuilder {

    public static final String TYPE = "type";
    public static final String CODEC = "codec";
    public static final String KDF = "kdf";

    private final Type type;
    private KDF kdf;
    private Codec codec = HEX;
    private Password password;
    private SecureRandom random;

    public VaultBuilder(Type type) {
        this.type = Check.notNull(type, "Null type");
    }

    public VaultBuilder(Configurations configurations) {
        Check.notNull(configurations, "Null configurations");
        final String type = configurations.getString(TYPE, "AES").toUpperCase();
        try {
            this.type = Type.valueOf(type);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown vault type " + type, exception);
        }
        withConfigurations(configurations);
    }

    public Vault build() {
        if (type == Type.NONE) return NO_OP_VAULT;

        if (type != Type.AES) throw new IllegalArgumentException("Unsupported vault type " + type);
        final Password password = Check.notNull(this.password, "Null password");
        try {
            return new AESVault(random == null ? new SecureRandom() : random, codec,
                                Check.notNull(kdf, "KDF not specified"), password);
        } finally {
            password.close();
        }
    }

    public VaultBuilder withPassword(Password password) {
        this.password = Check.notNull(password);
        return this;
    }

    public VaultBuilder withSecureRandom(SecureRandom random) {
        this.random = Check.notNull(random, "Null random");
        return this;
    }

    public VaultBuilder withKDF(KDF kdf) {
        this.kdf = Check.notNull(kdf, "Null KDF");
        return this;
    }

    public VaultBuilder withKDFSpec(KDFSpec kdfSpec) {
        return withKDF(new BasicKDFManager().getKDF(kdfSpec));
    }

    public VaultBuilder withCodec(Codec codec) {
        this.codec = Check.notNull(codec, "Null codec");
        return this;
    }

    public VaultBuilder withCodecSpec(String codecSpec) {
        return withCodec(CodecManager.getCodec(codecSpec));
    }

    public VaultBuilder withConfigurations(Configurations configurations) {

        final String codec = configurations.getString(CODEC, null);
        if (codec != null) withCodecSpec(codec);

        final Configurations kdf = configurations.strip(KDF);
        if (!kdf.isEmpty()) withKDFSpec(new KDFSpecBuilder(kdf).build());

        return this;
    }
}
