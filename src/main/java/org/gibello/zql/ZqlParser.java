/*
 * This file is part of Zql.
 *
 * Zql is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Zql is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Zql.  If not, see http://www.gnu.org/licenses.
 */

package org.gibello.zql;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.gibello.zql.expression.ZExp;
import org.gibello.zql.statement.ZStatement;
import org.gibello.zql.utils.ZCommonConstants;
import org.gibello.zql.utils.ZUtils;

/**
 * ZqlParser: an SQL parser.
 *
 * @author Pierre-Yves Gibello
 * @author Bogdan Mariesan, Romania
 */
public class ZqlParser {

	/**
	 * The parser.
	 */
	private ZqlJJParser parser = null;

	/**
	 * Create a new parser to parse SQL statements from a given input stream.
	 *
	 * @param in
	 *            The InputStream from which SQL statements will be read.
	 * @throws IOException
	 */
	public ZqlParser(final InputStream in) throws IOException {
		this.initParser(in);
	}

	/**
	 * Create a new parser to parse SQL statements from a given input string.
	 *
	 * @param in
	 *            The String from which SQL statements will be read.
	 * @throws IOException
	 */
	public ZqlParser(final String in) throws IOException {
		this.initParser(in);
	}

	/**
	 * Create a new parser: before use, call initParser(InputStream) to specify
	 * an input stream for the parsing.
	 */
	public ZqlParser() {

	}

	/**
	 * Initialize (or re-initialize) the input stream for the parser.
	 *
	 * @param in
	 *            the input stream.
	 * @throws IOException
	 */
	public void initParser(final InputStream in) throws IOException {
		if (this.parser == null) {
			this.parser = new ZqlJJParser(new StreamProvider(in));
		} else {
			this.parser.ReInit(new StreamProvider(in));
		}
	}

	/**
	 * Initialize (or re-initialize) the input string for the parser.
	 *
	 * @param in
	 *            the input string.
	 * @throws IOException
	 */
	public void initParser(final String in) throws IOException {
		if (this.parser == null) {
			this.parser = new ZqlJJParser(new StringProvider(in));
		} else {
			this.parser.ReInit(new StringProvider(in));
		}
	}

	/**
	 * Adds a custom function string.
	 *
	 * @param fct
	 *            the function names.
	 * @param nparm
	 *            the function params.
	 */
	public void addCustomFunction(final String fct, final int nparm) {
		ZUtils.addCustomFunction(fct, nparm);
	}

	/**
	 * Parse an SQL Statement from the parser's input stream.
	 *
	 * @return An SQL statement, or null if there's no more statement.
	 * @throws ParseException
	 *             the parse exception.
	 */
	public ZStatement readStatement() throws ParseException {
		if (this.parser == null) {
			throw new ParseException(ZCommonConstants.PARSE_EXCEPTION);
		}
		return this.parser.SQLStatement();
	}

	/**
	 * Parse a set of SQL Statements from the parser's input stream (all the
	 * available statements are parsed and returned).
	 *
	 * @return A vector of ZStatement objects (SQL statements).
	 * @throws ParseException
	 *             the parse exception.
	 */
	public List<ZStatement> readStatements() throws ParseException {
		if (this.parser == null) {
			throw new ParseException(ZCommonConstants.PARSE_EXCEPTION);
		}
		return this.parser.SQLStatements();
	}

	/**
	 * Parse an SQL Expression (like the WHERE clause of an SQL query).
	 *
	 * @return An SQL expression.
	 * @throws ParseException
	 *             the parse exception.
	 */
	public ZExp readExpression() throws ParseException {
		if (this.parser == null) {
			throw new ParseException(ZCommonConstants.PARSE_EXCEPTION);
		}
		return this.parser.SQLExpression();
	}

}
