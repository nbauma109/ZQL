package org.gibello.zql;
import org.gibello.zql.ParseException;

public interface WhereClauseOptimizer {
	void optimizeQuery() throws ParseException;
}
