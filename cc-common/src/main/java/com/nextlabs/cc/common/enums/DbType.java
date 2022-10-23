package com.nextlabs.cc.common.enums;

/**
 * Database types supported by Control Center.
 *
 * @author Sachindra Dasun
 */
public enum DbType {
    DB2, SQL_SERVER, ORACLE, POSTGRESQL;

    public static DbType fromJdbcUrl(String url) {
        url = url.toUpperCase();
        if (url.contains("ORACLE:")) {
            return ORACLE;
        } else if (url.contains("POSTGRESQL:")) {
            return POSTGRESQL;
        } else if (url.contains("SQLSERVER:")) {
            return SQL_SERVER;
        } else if (url.contains("DB2:")) {
            return DB2;
        }
        throw new IllegalArgumentException("Unsupported database type");
    }

    public String getDriver() {
        if (this == DbType.ORACLE) {
            return "oracle.jdbc.driver.OracleDriver";
        } else if (this == DbType.POSTGRESQL) {
            return "org.postgresql.Driver";
        } else if (this == DbType.SQL_SERVER) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (this == DbType.DB2) {
            return "com.ibm.db2.jcc.DB2Driver";
        }
        throw new IllegalArgumentException("Unsupported database type");
    }

    public String getHibernate2Dialect() {
        if (this == DbType.ORACLE) {
            return "net.sf.hibernate.dialect.Oracle9Dialect";
        } else if (this == DbType.POSTGRESQL) {
            return "net.sf.hibernate.dialect.PostgreSQLDialect";
        } else if (this == DbType.SQL_SERVER) {
            return "com.bluejungle.framework.datastore.hibernate.dialect.SqlServer2000Dialect";
        } else if (this == DbType.DB2) {
            return "net.sf.hibernate.dialect.DB2Dialect";
        }
        throw new IllegalArgumentException("Unsupported database type");
    }

    public String getHibernateDialect() {
        if (this == DbType.ORACLE) {
            return "org.hibernate.dialect.Oracle10gDialect";
        } else if (this == DbType.POSTGRESQL) {
            return "org.hibernate.dialect.PostgreSQL9Dialect";
        } else if (this == DbType.SQL_SERVER) {
            return "org.hibernate.dialect.SQLServerDialect";
        } else if (this == DbType.DB2) {
            return "org.hibernate.dialect.DB2Dialect";
        }
        throw new IllegalArgumentException("Unsupported database type");
    }

}
