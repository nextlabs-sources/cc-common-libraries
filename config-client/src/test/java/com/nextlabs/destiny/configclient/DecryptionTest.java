package com.nextlabs.destiny.configclient;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.bluejungle.framework.crypt.ReversibleEncryptor;

/**
 * Tests for decryption.
 *
 * @author Sachindra Dasun
 */
public class DecryptionTest {

    @Test
    public void testDecrypt() {
        String encrypted = "s64cea50ebddd2ae09d76cfe950455f0df98dcb0233f7e676906fe984fb16bbf3";
        String decrypted = new ReversibleEncryptor().decrypt(encrypted);
        System.out.println();
        assertNotNull(decrypted);
    }

}
