package com.nextlabs.cc.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * Utility to obtain hostname.
 *
 * @author Sachindra Dasun
 */
public class HostnameUtil {

    private HostnameUtil() {
    }

    public static String getHostname() throws IOException {
        String hostname = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DefaultExecutor defaultExecutor = new DefaultExecutor();
            defaultExecutor.setStreamHandler(new PumpStreamHandler(byteArrayOutputStream));
            defaultExecutor.execute(SystemUtils.IS_OS_WINDOWS ? new CommandLine("ipconfig").addArgument("/all") :
                    new CommandLine("hostname").addArgument("--fqdn"));
            hostname = SystemUtils.IS_OS_WINDOWS ? getHostnameFromWindowsCmdOutput(byteArrayOutputStream.toString()) :
                    byteArrayOutputStream.toString().trim();
        } catch (Exception ignored) {
            // Ignore exception
        }
        if (StringUtils.isEmpty(hostname)) {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        }
        return hostname;
    }

    private static String getHostnameFromWindowsCmdOutput(String output) {
        String hostname = null;
        String primaryDnsSuffix = null;
        for (String line : output.split("\\r?\\n")) {
            if (StringUtils.isEmpty(hostname) && line.contains("Host Name")) {
                hostname = line.split(":")[1].trim();
            } else if (StringUtils.isEmpty(primaryDnsSuffix) && line.contains("Primary Dns Suffix")) {
                primaryDnsSuffix = line.split(":")[1].trim();
            }
            if (StringUtils.isNotEmpty(hostname) && StringUtils.isNotEmpty(primaryDnsSuffix)) {
                return String.format("%s.%s", hostname, primaryDnsSuffix);
            }
        }
        return hostname;
    }

}
