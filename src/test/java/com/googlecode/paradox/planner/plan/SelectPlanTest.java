package com.googlecode.paradox.planner.plan;

import com.googlecode.paradox.Driver;
import com.googlecode.paradox.ParadoxConnection;
import com.googlecode.paradox.data.TableData;
import com.googlecode.paradox.metadata.ParadoxTable;
import com.googlecode.paradox.planner.nodes.PlanTableNode;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link SelectPlan} class.
 *
 * @author Leonardo Alves da Costa
 * @version 1.0
 * @since 1.3
 */
public class SelectPlanTest {
    
    /**
     * The connection string used in this tests.
     */
    private static final String CONNECTION_STRING = "jdbc:paradox:target/test-classes/";
    
    /**
     * The database connection.
     */
    private ParadoxConnection conn;
    
    /**
     * Register the database driver.
     *
     * @throws Exception
     *             in case of failures.
     */
    @BeforeClass
    public static void initClass() throws Exception {
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
        this.conn = (ParadoxConnection) DriverManager.getConnection(SelectPlanTest.CONNECTION_STRING + "db");
    }
    
    /**
     * Test for ambiguous column table alias.
     *
     * @throws SQLException
     *             if has errors.
     */
    @Test(expected = SQLException.class)
    public void testAmbiguousColumn() throws SQLException {
        final SelectPlan plan = new SelectPlan(this.conn);
        
        PlanTableNode tableNode = new PlanTableNode();
        tableNode.setAlias("test");
        
        final List<ParadoxTable> tables = TableData.listTables(this.conn, "areacodes");
        tableNode.setTable(tables.get(0));
        plan.addTable(tableNode);
        
        tableNode = new PlanTableNode();
        tableNode.setAlias("test2");
        tableNode.setTable(tables.get(0));
        plan.addTable(tableNode);
        
        plan.addColumn("ac");
        Assert.assertEquals("Invalid column size.", 1, plan.getColumns().size());
    }
    
    /**
     * Test for column value with table alias.
     *
     * @throws SQLException
     *             if has errors.
     */
    @Test
    public void testColumnWithTableAlias() throws SQLException {
        final SelectPlan plan = new SelectPlan(this.conn);
        
        final PlanTableNode tableNode = new PlanTableNode();
        tableNode.setAlias("test");
        
        final List<ParadoxTable> tables = TableData.listTables(this.conn, "areacodes");
        tableNode.setTable(tables.get(0));
        plan.addTable(tableNode);
        
        plan.addColumn("test.ac");
        Assert.assertEquals("Invalid column size.", 1, plan.getColumns().size());
    }
    
    /**
     * Test for invalid column value.
     *
     * @throws SQLException
     *             if there are no errors.
     */
    @Test(expected = SQLException.class)
    public void testInvalidColumn() throws SQLException {
        final SelectPlan plan = new SelectPlan(this.conn);
        plan.addColumn("invalid");
    }
    
    /**
     * Test for invalid table alias.
     *
     * @throws SQLException
     *             if has errors.
     */
    @Test(expected = SQLException.class)
    public void testInvalidTableAlias() throws SQLException {
        final SelectPlan plan = new SelectPlan(this.conn);
        
        final PlanTableNode tableNode = new PlanTableNode();
        tableNode.setAlias("test");
        
        final List<ParadoxTable> tables = TableData.listTables(this.conn, "areacodes");
        tableNode.setTable(tables.get(0));
        plan.addTable(tableNode);
        
        plan.addColumn("test2.ac");
    }
    
    /**
     * Test for invalid table value.
     *
     * @throws SQLException
     *             if has errors.
     */
    @Test(expected = SQLException.class)
    public void testInvalidTableValue() throws SQLException {
        final SelectPlan plan = new SelectPlan(this.conn);
        
        final PlanTableNode tableNode = new PlanTableNode();
        tableNode.setAlias("test");
        tableNode.setTable(null);
        plan.addTable(tableNode);
        
        plan.addColumn("test.ac");
    }
}
