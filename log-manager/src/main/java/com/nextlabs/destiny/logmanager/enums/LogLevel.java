package com.nextlabs.destiny.logmanager.enums;


import java.util.logging.Level;

/**
 * This enum is used to map common log levels to Java Util Log levels.
 *
 * @author Sachindra Dasun
 */
public enum LogLevel {

    ALL(java.util.logging.Level.ALL, org.apache.logging.log4j.Level.ALL),
    TRACE(java.util.logging.Level.FINEST, org.apache.logging.log4j.Level.TRACE),
    DEBUG(java.util.logging.Level.FINE, org.apache.logging.log4j.Level.DEBUG),
    INFO(java.util.logging.Level.INFO, org.apache.logging.log4j.Level.INFO),
    WARN(java.util.logging.Level.WARNING, org.apache.logging.log4j.Level.WARN),
    ERROR(java.util.logging.Level.SEVERE, org.apache.logging.log4j.Level.ERROR),
    OFF(java.util.logging.Level.OFF, org.apache.logging.log4j.Level.OFF);

    private java.util.logging.Level julLevel;
    private org.apache.logging.log4j.Level log4jLevel;

    LogLevel(java.util.logging.Level julLevel, org.apache.logging.log4j.Level log4jLevel) {
        this.julLevel = julLevel;
        this.log4jLevel = log4jLevel;
    }

    public static LogLevel fromLog4jLevel(org.apache.logging.log4j.Level level) {
        switch (level.toString()) {
            case "FATAL":
            case "ERROR":
                return LogLevel.ERROR;
            case "WARN":
                return LogLevel.WARN;
            case "INFO":
                return LogLevel.INFO;
            case "DEBUG":
                return LogLevel.DEBUG;
            case "TRACE":
                return LogLevel.TRACE;
            case "ALL":
                return LogLevel.ALL;
            default:
                return LogLevel.OFF;
        }
    }

    public Level getJulLevel() {
        return julLevel;
    }

    public org.apache.logging.log4j.Level getLog4jLevel() {
        return log4jLevel;
    }
}
