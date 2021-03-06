/*
 * SQLParser.java 03/12/2009 Copyright (C) 2009 Leonardo Alves da Costa This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.googlecode.paradox.parser;

import com.googlecode.paradox.parser.nodes.FieldNode;
import com.googlecode.paradox.parser.nodes.JoinNode;
import com.googlecode.paradox.parser.nodes.JoinType;
import com.googlecode.paradox.parser.nodes.SQLNode;
import com.googlecode.paradox.parser.nodes.SelectNode;
import com.googlecode.paradox.parser.nodes.StatementNode;
import com.googlecode.paradox.parser.nodes.TableNode;
import com.googlecode.paradox.parser.nodes.comparisons.BetweenNode;
import com.googlecode.paradox.parser.nodes.comparisons.EqualsNode;
import com.googlecode.paradox.parser.nodes.comparisons.GreaterThanNode;
import com.googlecode.paradox.parser.nodes.comparisons.LessThanNode;
import com.googlecode.paradox.parser.nodes.comparisons.NotEqualsNode;
import com.googlecode.paradox.parser.nodes.conditional.ANDNode;
import com.googlecode.paradox.parser.nodes.conditional.ExistsNode;
import com.googlecode.paradox.parser.nodes.conditional.NOTNode;
import com.googlecode.paradox.parser.nodes.conditional.ORNode;
import com.googlecode.paradox.parser.nodes.conditional.XORNode;
import com.googlecode.paradox.parser.nodes.values.AsteriskNode;
import com.googlecode.paradox.parser.nodes.values.CharacterNode;
import com.googlecode.paradox.parser.nodes.values.NumericNode;
import com.googlecode.paradox.utils.Constants;
import com.googlecode.paradox.utils.SQLStates;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a SQL statement.
 *
 * @author Leonardo Alves da Costa
 * @version 1.2
 * @since 1.0
 */
public final class SQLParser {
    
    /**
     * The scanner used to read tokens.
     */
    private final Scanner scanner;
    
    /**
     * The SQL to parse.
     */
    private final String sql;
    
    /**
     * The current token.
     */
    private Token token;
    
    /**
     * Creates a new instance.
     *
     * @param sql
     *            the SQL to parse.
     * @throws SQLException
     *             in case of parse errors.
     */
    public SQLParser(final String sql) throws SQLException {
        this.sql = sql;
        this.scanner = new Scanner(sql);
    }
    
    /**
     * Parses the SQL statement.
     *
     * @return a list of statements.
     * @throws SQLException
     *             in case of parse errors.
     */
    public List<StatementNode> parse() throws SQLException {
        if (!this.scanner.hasNext()) {
            throw new SQLException(this.sql, SQLStates.INVALID_SQL.getValue());
        }
        this.token = this.scanner.nextToken();
        
        final ArrayList<StatementNode> statementList = new ArrayList<>();
        switch (this.token.getType()) {
            case SELECT:
                statementList.add(this.parseSelect());
                break;
            case SEMI:
                if (!statementList.isEmpty()) {
                    break;
                }
            default:
                throw new SQLFeatureNotSupportedException(Constants.ERROR_UNSUPPORTED_OPERATION,
                        SQLStates.INVALID_SQL.getValue());
        }
        return statementList;
    }
    
    /**
     * Test for expected tokens.
     *
     * @param rparens
     *            the tokens to validate.
     * @throws SQLException
     *             in case of unexpected tokens.
     */
    private void expect(final TokenType... rparens) throws SQLException {
        boolean found = false;
        for (final TokenType rparen : rparens) {
            if (this.token.getType() == rparen) {
                // Expected do not happen
                found = true;
                break;
            }
        }
        if (!found) {
            throw new SQLException(String.format("Unexpected error in SQL syntax (%s)", this.token.getValue()),
                    SQLStates.INVALID_SQL.getValue());
        }
        if (this.scanner.hasNext()) {
            this.token = this.scanner.nextToken();
        } else {
            this.token = null;
        }
    }
    
    /**
     * Test for a token.
     *
     * @param rparen
     *            the token to test.
     * @param message
     *            message in case of invalid token.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void expect(final TokenType rparen, final String message) throws SQLException {
        if (this.token.getType() != rparen) {
            throw new SQLException(message, SQLStates.INVALID_SQL.getValue());
        }
        if (this.scanner.hasNext()) {
            this.token = this.scanner.nextToken();
        } else {
            this.token = null;
        }
    }
    
    /**
     * Parse the asterisk token.
     *
     * @param select
     *            the select node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void parseAsterisk(final SelectNode select) throws SQLException {
        select.addField(new AsteriskNode());
        this.expect(TokenType.ASTERISK);
    }
    
    /**
     * Parses between token.
     *
     * @param field
     *            the between field.
     * @return the between node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private BetweenNode parseBetween(final FieldNode field) throws SQLException {
        this.expect(TokenType.BETWEEN);
        final FieldNode left = this.parseField();
        this.expect(TokenType.AND, "AND expected.");
        final FieldNode right = this.parseField();
        return new BetweenNode(field, left, right);
    }
    
    /**
     * Parse the character token.
     *
     * @param select
     *            the select node.
     * @param fieldName
     *            the field name.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void parseCharacter(final SelectNode select, final String fieldName) throws SQLException {
        String fieldAlias = fieldName;
        this.expect(TokenType.CHARACTER);
        // Field alias (with AS identifier)
        if (this.token.getType() == TokenType.AS) {
            this.expect(TokenType.AS);
            fieldAlias = this.token.getValue();
            this.expect(TokenType.IDENTIFIER);
        } else if (this.token.getType() == TokenType.IDENTIFIER) {
            // Field alias (without AS identifier)
            fieldAlias = this.token.getValue();
            this.expect(TokenType.IDENTIFIER);
        }
        select.addField(new CharacterNode(fieldName, fieldAlias));
    }
    
    /**
     * Parses the conditional statements.
     *
     * @return the node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private SQLNode parseCondition() throws SQLException {
        if (this.token.getType() == TokenType.NOT) {
            return new NOTNode(this.parseCondition());
        } else if (this.token.isOperator()) {
            return this.parseOperators();
        } else if (this.token.getType() == TokenType.LPAREN) {
            this.expect(TokenType.RPAREN, "Right parenthesis expected");
        } else if (this.token.getType() == TokenType.EXISTS) {
            return this.parseExists();
        } else {
            return this.parseFieldNode();
        }
        return null;
    }
    
    /**
     * Parses the conditional listing.
     *
     * @return a list of nodes.
     * @throws SQLException
     *             in case of parse errors.
     */
    private ArrayList<SQLNode> parseConditionList() throws SQLException {
        final ArrayList<SQLNode> conditions = new ArrayList<>();
        
        while (this.scanner.hasNext()) {
            if (this.token.isConditionBreak()) {
                break;
            }
            conditions.add(this.parseCondition());
        }
        return conditions;
    }
    
    /**
     * Parses the equals tokens.
     *
     * @param field
     *            the left field token.
     * @return the equals node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private EqualsNode parseEquals(final FieldNode field) throws SQLException {
        this.expect(TokenType.EQUALS);
        final FieldNode value = this.parseField();
        return new EqualsNode(field, value);
    }
    
    /**
     * Parses the exists token.
     *
     * @return the exists node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private ExistsNode parseExists() throws SQLException {
        this.expect(TokenType.EXISTS);
        this.expect(TokenType.LPAREN, "Left parenthesis expected.");
        final SelectNode select = this.parseSelect();
        this.expect(TokenType.RPAREN, "Left parenthesis expected.");
        return new ExistsNode(select);
    }
    
    /**
     * Parses the table join fields.
     *
     * @return the field node.
     * @throws SQLException
     *             in case of errors.
     */
    private FieldNode parseField() throws SQLException {
        String tableName = null;
        String fieldName = this.token.getValue();
        
        this.expect(TokenType.IDENTIFIER, TokenType.NUMERIC, TokenType.CHARACTER);
        
        // If it has a Table Name
        if (this.scanner.hasNext() && (this.token.getType() == TokenType.PERIOD)) {
            this.expect(TokenType.PERIOD);
            tableName = fieldName;
            fieldName = this.token.getValue();
            this.expect(TokenType.IDENTIFIER);
        }
        return new FieldNode(tableName, fieldName, fieldName);
    }
    
    /**
     * Parses the field node.
     *
     * @return the field node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private SQLNode parseFieldNode() throws SQLException {
        final FieldNode firstField = this.parseField();
        SQLNode node;
        
        switch (this.token.getType()) {
            case BETWEEN:
                node = this.parseBetween(firstField);
                break;
            case EQUALS:
                node = this.parseEquals(firstField);
                break;
            case NOTEQUALS:
                node = this.parseNotEquals(firstField);
                break;
            case NOTEQUALS2:
                node = this.parseNotEqualsVariant(firstField);
                break;
            case LESS:
                node = this.parseLess(firstField);
                break;
            case MORE:
                node = this.parseMore(firstField);
                break;
            default:
                throw new SQLException("Invalid operator.", SQLStates.INVALID_SQL.getValue());
        }
        return node;
    }
    
    /**
     * Parse the field list in SELECT statement.
     *
     * @param select
     *            the select node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void parseFields(final SelectNode select) throws SQLException {
        boolean firstField = true;
        while (this.scanner.hasNext()) {
            if (this.token.getType() == TokenType.DISTINCT) {
                throw new SQLException("Invalid statement.");
            }
            
            if (this.token.getType() != TokenType.FROM) {
                // Field Name
                if (!firstField) {
                    this.expect(TokenType.COMMA, "Missing comma.");
                }
                final String tableName = null;
                final String fieldName = this.token.getValue();
                
                if (this.token.getType() == TokenType.CHARACTER) {
                    this.parseCharacter(select, fieldName);
                } else if (this.token.getType() == TokenType.NUMERIC) {
                    this.parseNumeric(select, fieldName);
                } else if (this.token.getType() == TokenType.ASTERISK) {
                    this.parseAsterisk(select);
                } else {
                    this.parseIdentifier(select, tableName, fieldName);
                }
                firstField = false;
            } else {
                break;
            }
        }
    }
    
    /**
     * Parse table field names from WHERE.
     *
     * @param oldAlias
     *            the old alias name.
     * @return the new alias.
     * @throws SQLException
     *             in case of errors.
     */
    private String parseFields(final String oldAlias) throws SQLException {
        String tableAlias = oldAlias;
        if (this.scanner.hasNext()) {
            this.expect(TokenType.IDENTIFIER);
            if ((this.token.getType() == TokenType.IDENTIFIER) || (this.token.getType() == TokenType.AS)) {
                // Field alias (with AS identifier)
                if (this.token.getType() == TokenType.AS) {
                    this.expect(TokenType.AS);
                    tableAlias = this.token.getValue();
                    this.expect(TokenType.IDENTIFIER);
                } else if (this.token.getType() == TokenType.IDENTIFIER) {
                    // Field alias (without AS identifier)
                    tableAlias = this.token.getValue();
                    this.expect(TokenType.IDENTIFIER);
                }
            }
        }
        return tableAlias;
    }
    
    /**
     * Parse the FROM keyword.
     *
     * @param select
     *            the select node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void parseFrom(final SelectNode select) throws SQLException {
        this.expect(TokenType.FROM);
        boolean firstField = true;
        do {
            if (this.token.getType() == TokenType.WHERE) {
                break;
            }
            if (!firstField) {
                this.expect(TokenType.COMMA, "Missing comma.");
            }
            if (this.token.getType() == TokenType.IDENTIFIER) {
                this.parseJoinTable(select);
                firstField = false;
            }
        } while (this.scanner.hasNext());
        
        if (this.scanner.hasNext() && (this.token.getType() == TokenType.WHERE)) {
            this.expect(TokenType.WHERE);
            select.setConditions(this.parseConditionList());
        }
    }
    
    /**
     * Parse the identifier token associated with a field.
     *
     * @param select
     *            the select node.
     * @param tableName
     *            the table name.
     * @param fieldName
     *            the field name.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void parseIdentifier(final SelectNode select, final String tableName, final String fieldName)
            throws SQLException {
        String fieldAlias = fieldName;
        String newTableName = tableName;
        String newFieldName = fieldName;
        this.expect(TokenType.IDENTIFIER);
        
        if ((this.token.getType() == TokenType.IDENTIFIER) || (this.token.getType() == TokenType.AS)
                || (this.token.getType() == TokenType.PERIOD)) {
            // If it has a Table Name
            if (this.token.getType() == TokenType.PERIOD) {
                this.expect(TokenType.PERIOD);
                newTableName = fieldName;
                fieldAlias = fieldName;
                newFieldName = this.token.getValue();
                this.expect(TokenType.IDENTIFIER);
            }
            // Field alias (with AS identifier)
            if (this.token.getType() == TokenType.AS) {
                this.expect(TokenType.AS);
                fieldAlias = this.token.getValue();
                // may be: select field as name
                // select field as "Name"
                // select field as 'Name'
                this.expect(TokenType.CHARACTER, TokenType.IDENTIFIER);
            } else if (this.token.getType() == TokenType.IDENTIFIER) {
                // Field alias (without AS identifier)
                fieldAlias = this.token.getValue();
                this.expect(TokenType.IDENTIFIER);
            }
        }
        select.addField(new FieldNode(newTableName, newFieldName, fieldAlias));
    }
    
    /**
     * Parses the join tokens.
     *
     * @param table
     *            the table.
     * @throws SQLException
     *             in case of errors.
     */
    private void parseJoin(final TableNode table) throws SQLException {
        while (this.scanner.hasNext() && (this.token.getType() != TokenType.COMMA)
                && (this.token.getType() != TokenType.WHERE)) {
            final JoinNode join = new JoinNode();
            
            // Inner join
            if (this.token.getType() == TokenType.LEFT) {
                join.setType(JoinType.LEFT);
                this.expect(TokenType.LEFT);
            } else if (this.token.getType() == TokenType.RIGHT) {
                join.setType(JoinType.RIGHT);
                this.expect(TokenType.RIGHT);
            }
            if (this.token.getType() == TokenType.INNER) {
                this.expect(TokenType.INNER);
            } else if (this.token.getType() == TokenType.OUTER) {
                this.expect(TokenType.OUTER);
            }
            this.expect(TokenType.JOIN);
            join.setTableName(this.token.getValue());
            join.setAlias(this.token.getValue());
            this.expect(TokenType.IDENTIFIER);
            if (this.token.getType() == TokenType.AS) {
                this.expect(TokenType.AS);
                join.setAlias(this.token.getValue());
                this.expect(TokenType.IDENTIFIER);
            } else if (this.token.getType() != TokenType.ON) {
                join.setAlias(this.token.getValue());
                this.expect(TokenType.IDENTIFIER);
            }
            this.expect(TokenType.ON);
            join.setConditions(this.parseConditionList());
            table.addJoin(join);
        }
    }
    
    /**
     * Parse the tables name after a from keyword.
     *
     * @param select
     *            the select node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void parseJoinTable(final SelectNode select) throws SQLException {
        final String tableName = this.token.getValue();
        String tableAlias = tableName;
        
        tableAlias = this.parseFields(tableAlias);
        
        final TableNode table = new TableNode(tableName, tableAlias);
        this.parseJoin(table);
        
        select.addTable(table);
    }
    
    /**
     * Parses less token.
     *
     * @param field
     *            the left token field.
     * @return the less token.
     * @throws SQLException
     *             in case of parse errors.
     */
    private LessThanNode parseLess(final FieldNode field) throws SQLException {
        this.expect(TokenType.LESS);
        final FieldNode value = this.parseField();
        return new LessThanNode(field, value);
    }
    
    /**
     * Parses more token.
     *
     * @param firstField
     *            the left more token field.
     * @return the grater than node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private GreaterThanNode parseMore(final FieldNode firstField) throws SQLException {
        this.expect(TokenType.MORE);
        final FieldNode value = this.parseField();
        return new GreaterThanNode(firstField, value);
    }
    
    /**
     * Parses a not equals token.
     *
     * @param firstField
     *            the left not equals field.
     * @return the not equals node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private NotEqualsNode parseNotEquals(final FieldNode firstField) throws SQLException {
        this.expect(TokenType.NOTEQUALS);
        final FieldNode value = this.parseField();
        return new NotEqualsNode(firstField, value);
    }
    
    /**
     * Parses a not equals token variant (2).
     *
     * @param firstField
     *            the left not equals field.
     * @return the not equals node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private NotEqualsNode parseNotEqualsVariant(final FieldNode firstField) throws SQLException {
        this.expect(TokenType.NOTEQUALS2);
        final FieldNode value = this.parseField();
        return new NotEqualsNode(firstField, value);
    }
    
    /**
     * Parse the numeric token.
     *
     * @param select
     *            the select node.
     * @param fieldName
     *            the field name.
     * @throws SQLException
     *             in case of parse errors.
     */
    private void parseNumeric(final SelectNode select, final String fieldName) throws SQLException {
        String fieldAlias = fieldName;
        
        this.expect(TokenType.NUMERIC);
        // Field alias (with AS identifier)
        if (this.token.getType() == TokenType.AS) {
            this.expect(TokenType.AS);
            fieldAlias = this.token.getValue();
            this.expect(TokenType.IDENTIFIER);
        } else if (this.token.getType() == TokenType.IDENTIFIER) {
            // Field alias (without AS identifier)
            fieldAlias = this.token.getValue();
            this.expect(TokenType.IDENTIFIER);
        }
        select.addField(new NumericNode(fieldName, fieldAlias));
    }
    
    /**
     * Parses the operators token.
     *
     * @return the conditional operator node.
     * @throws SQLException
     *             in case or errors.
     */
    private SQLNode parseOperators() throws SQLException {
        switch (this.token.getType()) {
            case AND:
                this.expect(TokenType.AND);
                return new ANDNode(null);
            case OR:
                this.expect(TokenType.OR);
                return new ORNode(null);
            case XOR:
                this.expect(TokenType.XOR);
                return new XORNode(null);
            default:
                throw new SQLException("Invalid operator location.", SQLStates.INVALID_SQL.getValue());
        }
    }
    
    /**
     * Parse a Select Statement.
     *
     * @return a select statement node.
     * @throws SQLException
     *             in case of parse errors.
     */
    private SelectNode parseSelect() throws SQLException {
        final SelectNode select = new SelectNode();
        this.expect(TokenType.SELECT);
        
        // Allowed only in the beginning of Select Statement
        if (this.token.getType() == TokenType.DISTINCT) {
            select.setDistinct(true);
            this.expect(TokenType.DISTINCT);
        }
        
        // Field loop
        this.parseFields(select);
        
        if (this.token.getType() == TokenType.FROM) {
            this.parseFrom(select);
        } else {
            throw new SQLException("FROM expected.", SQLStates.INVALID_SQL.getValue());
        }
        return select;
    }
}
