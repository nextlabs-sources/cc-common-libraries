package com.nextlabs.destiny.configclient;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Servlet filter for clearing ThreadLocal values.
 *
 * @author Sachindra Dasun
 */
@WebFilter(filterName = "ConfigClientThreadLocalClearingFilter",
        urlPatterns = {"/*"})
public class ThreadLocalClearingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            ConfigClient.clear();
        }
    }

}
