package com.nextlabs.cc.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Tests for EncryptionUtil.
 *
 * @author Sachindra Dasun
 */
public class EncryptionUtilTest {

    @Test
    public void decryptIfEncrypted() {
        String text = "Test";
        String encryptedText = EncryptionUtil.encrypt(text);
        String decryptedText = EncryptionUtil.decryptIfEncrypted(encryptedText);
        System.out.println(decryptedText);
        assertEquals(text, decryptedText);
    }

    @Test
    public void encrypt() {
        String text = "123@DefaultPassword";
        String encryptedText = EncryptionUtil.encrypt(text);
        System.out.println(encryptedText);
        assertNotNull(encryptedText);
    }

    @Test
    public void decrypt() {
        String text = "Test";
        String encryptedText = "{cipher}sede9b2e1c36644937e3dd47ee2a0c2d7955aacecb41434a43ce2946078162ab7c18d540a71281ddb635701cdfc90a698";
        String decryptedText = EncryptionUtil.decrypt(encryptedText);
        System.out.println(decryptedText);
        assertEquals(text, decryptedText);
    }

}