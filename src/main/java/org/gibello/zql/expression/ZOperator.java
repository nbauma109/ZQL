package org.gibello.zql.expression;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public enum ZOperator {

	AND("AND"), OR("OR"), NOT("NOT"), EQUALS("="), NOT_EQUALS("!="), EXCLUDING("<>"), DIEZ("#"), GREATER_THAN(">"), GREATER_THAN_OR_EQUALS(">="), LESS_THAN(
			"<"), LESS_THAN_OR_EQUALS("<="), BETWEEN("BETWEEN"), NOT_BETWEEN("NOT BETWEEN"), LIKE("LIKE"), NOT_LIKE("NOT LIKE"), IN("IN"), NOT_IN(
					"NOT IN"), IS_NULL("IS NULL"), IS_NOT_NULL("IS NOT NULL"), PLUS("+"), MINUS("-"), MULTIPLICATION("*"), SLASH("/"), POW("**"), COMMA(","), NONE(null);

	private static Map<String, ZOperator> symbolMap = new HashMap<String, ZOperator>();
	static {
		for (ZOperator value : values()) {
			symbolMap.put(value.symbol(), value);
		}
	}

	private String symbol;

	private ZOperator(String symbol) {
		this.symbol = symbol;
	}

	public String symbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}

	public static ZOperator toEnum(String op) throws SQLException {
		try {
			return symbolMap.get(op);
		} catch (Exception e) {
			throw new SQLException("Unknown operator " + op);
		}
	}

	public static ZOperator toEnumQuiet(String op) {
		try {
			return symbolMap.get(op);
		} catch (Exception e) {
			return NONE;
		}
	}
}
