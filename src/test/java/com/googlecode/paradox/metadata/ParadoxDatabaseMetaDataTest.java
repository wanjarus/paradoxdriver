package com.googlecode.paradox.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.paradox.Driver;
import com.googlecode.paradox.ParadoxResultSet;
import com.googlecode.paradox.integration.MainTest;

/**
 * Unit test for {@link ParadoxDatabaseMetaData} class.
 *
 * @author Leonardo Alves da Costa
 * @version 1.0
 * @since 1.3
 */
public class ParadoxDatabaseMetaDataTest {
    /**
     * The connection string used in this tests.
     */
    public static final String CONNECTION_STRING = "jdbc:paradox:target/test-classes/";

    /**
     * The database connection.
     */
    private Connection conn;

    /**
     * Register the database driver.
     *
     * @throws Exception
     *         in case of failures.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        Class.forName(Driver.class.getName());
    }

    /**
     * Close the test conneciton.
     *
     * @throws Exception
     *         in case of failures.
     */
    @After
    public void closeConnection() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * Connect to the test database.
     *
     * @throws Exception
     *         in case of failures.
     */
    @Before
    public void connect() throws Exception {
        conn = DriverManager.getConnection(MainTest.CONNECTION_STRING + "db");
    }

    /**
     * Test for attributes.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testAttributes() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getAttributes("db", null, null, null)) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for commit failure closes results.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testAutocommitFailureClosesResult() throws SQLException {
        Assert.assertFalse(conn.getMetaData().autoCommitFailureClosesAllResultSets());
    }

    /**
     * Test for database definition causes transaction commit.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testDatabaseDefinitionsCausesCommit() throws SQLException {
        Assert.assertTrue(conn.getMetaData().dataDefinitionCausesTransactionCommit());
    }

    /**
     * Test for columns.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testColumns() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns("db", "%", "%", "%")) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for columns without column pattern.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testColumnsWithoutColumnPattern() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns("db", "%", "%", null)) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for columns with invalid pattern.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testColumnsInvaidPattern() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns("db", "%", "%", "invalid_column")) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for connection.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testConnection() throws SQLException {
        Assert.assertSame("Testing for connection.", conn, conn.getMetaData().getConnection());
    }

    /**
     * Test for deletes autodetectes.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testDeleteAutoDetects() throws SQLException {
        Assert.assertFalse(conn.getMetaData().deletesAreDetected(0));
    }

    /**
     * Test for imported keys.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testImportedKeys() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getImportedKeys("db", null, "teste.db")) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for JDBC version.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testJDBCVersion() throws SQLException {
        final DatabaseMetaData meta = conn.getMetaData();
        meta.getJDBCMajorVersion();
    }

    /**
     * Test for max rows include blobs.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testMaxRowsIncludesBlob() throws SQLException {
        Assert.assertTrue(conn.getMetaData().doesMaxRowSizeIncludeBlobs());
    }

    /**
     * Test for procedures callable.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testProcedureCallable() throws SQLException {
        Assert.assertFalse(conn.getMetaData().allProceduresAreCallable());
    }

    /**
     * Test for procedure columns.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testProcedureColumns() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getProcedureColumns("db", null, "*", "*")) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for procedures.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testProcedures() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getProcedures("db", null, "*")) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for schemas.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testSchemas() throws SQLException {
        try (ResultSet rs = conn.getMetaData().getSchemas()) {
            Assert.assertTrue(rs instanceof ParadoxResultSet);
        }
    }

    /**
     * Test for the catalog metadata.
     *
     * @throws Exception
     *         in case of failures.
     */
    @Test
    public void testCatalog() throws Exception {
        final DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getCatalogs()) {
            if (rs.next()) {
                Assert.assertEquals("db", rs.getString("TABLE_CAT"));
            } else {
                Assert.fail("No catalog selected.");
            }
        }
    }

    /**
     * Test for table selectable.
     *
     * @throws SQLException
     *         in case of errors.
     */
    @Test
    public void testTableSelectable() throws SQLException {
        Assert.assertTrue(conn.getMetaData().allTablesAreSelectable());
    }
}
