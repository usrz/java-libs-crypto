package com.usrz.libs.crypto.codecs;

import java.util.Arrays;

public class HexCodec extends AbstractCodec {

    private static final char[] ALPHABET_UPPER = "0123456789ABCDEF".toCharArray();
    private static final char[] ALPHABET_LOWER = "0123456789abcdef".toCharArray();
    private static final int[] VALUES = new int[128];

    static {
        /* Reverse the HEX alphabet when loading the class */
        Arrays.fill(VALUES, -1);
        for (char[] alphabet: new char[][] {ALPHABET_UPPER, ALPHABET_LOWER})
            for (int x = 0; x < alphabet.length; x++) VALUES[alphabet[x]] = x;
    }

    /* ====================================================================== */

    private final char[] alphabet;

    /* ====================================================================== */

    /**
     * Create a new {@link HexCodec} using the default upper-case alphabet.
     */
    public HexCodec() {
        alphabet = ALPHABET_UPPER;
    }

    /**
     * Create a new {@link HexCodec} using the specified case (upper or lower).
     *
     * @param upperCase If <b>true</b> use the upper-case alphabet, if
     *                  <b>false</b> use the lower-case one.
     */
    public HexCodec(final boolean upperCase) {
        alphabet = upperCase ? ALPHABET_UPPER : ALPHABET_LOWER;
    }

    @Override
    public String encode(final byte[] data, final int offset, final int length) {

        /* Quick bailout */
        if (length == 0) return EMPTY_STRING;

        /* Build up a string (back-to-front, faster) */
        final char[] result = new char[length * 2];
        int resultpos = result.length;
        for (int pos = offset + length - 1; pos >= offset; pos--) {
            final int current = data[pos];
            result[--resultpos] = alphabet[(current     ) & 0x0F];
            result[--resultpos] = alphabet[(current >> 4) & 0x0F];
        }

        /* All done, easy! */
        return new String(result);
    }

    @Override
    public byte[] decode(final String data)
    throws IllegalArgumentException {

        /* Quick bailouts */
        if (data.length() == 0) return EMPTY_ARRAY;
        if (data.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid data length");
        }

        /* Create the byte array to be returned */
        final byte[] result = new byte[data.length() / 2];

        /* Get the data chracters */
        final char[] array = data.toCharArray();

        /* Process the string, one byte at a time */
        try {
            for (int x = 0; x < result.length; x++) {

                /* Where are our high and low bits? */
                final int hipos = x * 2;
                final int lopos = hipos + 1;

                /* Analyse the value for high and low bits */
                final int hi = VALUES[array[hipos]];
                final int lo = VALUES[array[lopos]];
                if ((hi < 0) || (lo < 0)) {
                    throw new IllegalArgumentException("Invalid character in input");
                }

                /* All is safe, we can store this byte */
                result[x] = (byte) ((hi << 4) | lo);
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new IllegalArgumentException("Invalid character in input", exception);
        }

        /* All done */
        return result;
    }
}
