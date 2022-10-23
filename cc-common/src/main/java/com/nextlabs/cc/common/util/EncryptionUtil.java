package com.nextlabs.cc.common.util;

import org.apache.commons.lang3.StringUtils;

import com.bluejungle.framework.crypt.ReversibleEncryptor;

/**
 * Utility for handling encryption/decryption.
 *
 * @author Sachindra Dasun
 */
public class EncryptionUtil {

    public static final String CIPHER_VALUE_FORMAT = "{cipher}%s";
    public static final String CIPHER_VALUE_PREFIX = "{cipher}";
    private static final ReversibleEncryptor REVERSIBLE_ENCRYPTOR = new ReversibleEncryptor();

    private EncryptionUtil() {
    }

    public static String encrypt(String text) {
        return String.format(CIPHER_VALUE_FORMAT, REVERSIBLE_ENCRYPTOR.encrypt(text));
    }

    public static String decrypt(String encryptedText) {
        return REVERSIBLE_ENCRYPTOR.decrypt(encryptedText);
    }

    public static String decryptIfEncrypted(String text) {
        if (StringUtils.isNotEmpty(text) && text.startsWith(CIPHER_VALUE_PREFIX)) {
            return REVERSIBLE_ENCRYPTOR.decrypt(text);
        }
        return text;
    }

}
