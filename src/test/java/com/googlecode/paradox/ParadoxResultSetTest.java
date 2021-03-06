/*
 * ParadoxResultSetTest.java 07/21/2016 Copyright (C) 2016 Leonardo Alves da Costa This program is free software: you
 * can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.googlecode.paradox;

import com.googlecode.paradox.data.table.value.FieldValue;
import com.googlecode.paradox.results.Column;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link ParadoxResultSet} class.
 *
 * @author Leonardo Alves da Costa
 * @version 1.0
 * @since 1.3
 */
public class ParadoxResultSetTest {
    /**
     * The connection string used in this tests.
     */
    private static final String CONNECTION_STRING = "jdbc:paradox:target/test-classes/";

    /**
     * The database connection.
     */
    private Connection conn;

    /**
     * Register the database driver.
     *
     * @throws Exception
     *             in case of failures.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        Class.forName(Driver.class.getName());
    }

    /**
     * Close the test connection.
     *
     * @throws Exception
     *             in case of failures.
     */
    @After
    public void closeConnection() throws Exception {
        if (this.conn != null) {
            this.conn.close();
        }
    }

    /**
     * Connect to the test database.
     *
     * @throws Exception
     *             in case of failures.
     */
    @Before
    public void connect() throws Exception {
        this.conn = DriverManager.getConnection(ParadoxResultSetTest.CONNECTION_STRING + "db");
    }

    /**
     * Test for {@link ParadoxResultSet#absolute(int)} method with empty values.
     *
     * @throws SQLException
     *             in case of errors.
     */
    @Test
    public void testAbsoluteEmpty() throws SQLException {
        final List<Column> columns = new ArrayList<>();
        final List<List<FieldValue>> values = new ArrayList<>();
        final ParadoxStatement stmt = new ParadoxStatement((ParadoxConnection) this.conn);
        try (final ParadoxResultSet rs = new ParadoxResultSet((ParadoxConnection) this.conn, stmt, values, columns)) {
            Assert.assertTrue("Invalid absolute value.", rs.absolute(0));
        }
    }

    /**
     * Test for {@link ParadoxResultSet#absolute(int)} method with high row
     * number.
     *
     * @throws SQLException
     *             in case of errors.
     */
    @Test
    public void testAbsoluteInvalidRow() throws SQLException {
        final List<Column> columns = new ArrayList<>();
        final List<List<FieldValue>> values = new ArrayList<>();
        final ParadoxStatement stmt = new ParadoxStatement((ParadoxConnection) this.conn);
        try (final ParadoxResultSet rs = new ParadoxResultSet((ParadoxConnection) this.conn, stmt, values, columns)) {
            Assert.assertFalse("Invalid absolute value.", rs.absolute(1));
        }
    }

    /**
     * Test for {@link ParadoxResultSet#absolute(int)} method with low row
     * number.
     *
     * @throws SQLException
     *             in case of errors.
     */
    @Test
    public void testAbsoluteLowRowValue() throws SQLException {
        final List<Column> columns = new ArrayList<>();
        final List<List<FieldValue>> values = new ArrayList<>();
        final ParadoxStatement stmt = new ParadoxStatement((ParadoxConnection) this.conn);
        try (final ParadoxResultSet rs = new ParadoxResultSet((ParadoxConnection) this.conn, stmt, values, columns)) {
            Assert.assertFalse("Invalid absolute value.", rs.absolute(-1));
        }
    }

    /**
     * Test for {@link ParadoxResultSet#absolute(int)} method with negative row
     * value.
     *
     * @throws SQLException
     *             in case of errors.
     */
    @Test
    public void testAbsoluteNegativeRowValue() throws SQLException {
        final List<Column> columns = new ArrayList<>();
        columns.add(new Column());
        final List<List<FieldValue>> values = new ArrayList<>();
        values.add(Collections.singletonList(new FieldValue("Test", Types.VARCHAR)));
        final ParadoxStatement stmt = new ParadoxStatement((ParadoxConnection) this.conn);
        try (final ParadoxResultSet rs = new ParadoxResultSet((ParadoxConnection) this.conn, stmt, values, columns)) {
            Assert.assertTrue("Invalid absolute value.", rs.absolute(-1));
        }
    }

    /**
     * Test for {@link ParadoxResultSet#afterLast()} method.
     *
     * @throws SQLException
     *             in case of errors.
     */
    @Test
    public void testAfterLast() throws SQLException {
        final List<Column> columns = new ArrayList<>();
        columns.add(new Column());
        final List<List<FieldValue>> values = new ArrayList<>();
        values.add(Collections.singletonList(new FieldValue("Test", Types.VARCHAR)));
        final ParadoxStatement stmt = new ParadoxStatement((ParadoxConnection) this.conn);
        try (final ParadoxResultSet rs = new ParadoxResultSet((ParadoxConnection) this.conn, stmt, values, columns)) {
            rs.afterLast();
            Assert.assertTrue("Testing for invalid position.", rs.isAfterLast());
        }
    }

    /**
     * Test for first result.
     *
     * @throws Exception
     *             in case of failures.
     */
    @Test
    public void testFirstResult() throws Exception {
        try (Statement stmt = this.conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT AC as 'ACode', State, CITIES FROM AREACODES")) {
            Assert.assertTrue("No first row", rs.next());
            final String firstValue = rs.getString("ac");
            Assert.assertTrue("No first row", rs.next());
            Assert.assertNotEquals("Rows with same value.", firstValue, rs.getString("ac"));
            Assert.assertTrue("Not in first row.", rs.first());
            Assert.assertEquals("Rows with different values.", firstValue, rs.getString("ac"));
        }
    }

    /**
     * Test for first result.
     *
     * @throws Exception
     *             in case of failures.
     */
    @Test
    public void testNoFirstResult() throws Exception {
        final ParadoxConnection paradoxConnection = (ParadoxConnection) this.conn;
        try (ParadoxResultSet rs = new ParadoxResultSet(paradoxConnection, new ParadoxStatement(paradoxConnection),
                Collections.<List<FieldValue>> emptyList(), Collections.<Column> emptyList())) {
            
            Assert.assertFalse("There is one first row", rs.next());
            Assert.assertFalse("There is one first row", rs.first());
        }
    }

    /**
     * Test for {@link ResultSet} execution.
     *
     * @throws Exception
     *             in case of failures.
     */
    @Test
    public void testResultSet() throws Exception {
        try (Statement stmt = this.conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT AC as 'ACode', State, CITIES FROM AREACODES")) {
            Assert.assertTrue("No First row", rs.next());
            Assert.assertEquals("Testing for column 'AC'.", "201", rs.getString("ac"));
            Assert.assertEquals("Testing for column 'State'.", "NJ", rs.getString("State"));
            Assert.assertEquals("Testing for column 'Cities'.", "Hackensack, Jersey City (201/551 overlay)",
                    rs.getString("Cities"));
        }
    }
}
