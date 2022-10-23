package com.nextlabs.cc.common.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests for HostnameUtil.
 *
 * @author Sachindra Dasun
 */
public class HostnameUtilTest {

    @Test
    public void getHostname() throws IOException {
        String hostname = HostnameUtil.getHostname();
        System.out.println(hostname);
        assertNotNull(hostname);
    }

}