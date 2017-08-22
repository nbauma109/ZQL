package org.gibello.zql;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gibello.zql.alias.ZFromItem;
import org.gibello.zql.expression.ZExp;
import org.gibello.zql.expression.ZExpression;
import org.gibello.zql.query.ZQuery;

public class SubQueryWhereClauseOptimizer implements WhereClauseOptimizer {

	private String from;
	private String where;

	public SubQueryWhereClauseOptimizer(String from, String where) {
		this.from = from;
		this.where = where;
	}

	@Override
	public void optimizeQuery() {
		try {
			where = replaceDateAndTimestampPatterns(where);
			List<ZFromItem> fromItems = toZFromItems(from);
			ZqlParser whereZqlParser = new ZqlParser(where);
			ZExp parsedWhere = whereZqlParser.readExpression();
			if (parsedWhere instanceof ZExpression) {
				ZExpression whereExp = (ZExpression) parsedWhere;
				List<?> whereOps = whereExp.getOperands();
				for (Object whereElemObj : whereOps) {
					if (whereElemObj instanceof ZExpression) {
						ZExpression whereElem = (ZExpression) whereElemObj;
						if (whereElem.getOperator().equals("OR")) {
							List<?> orOps = whereElem.getOperands();
							for (Object orElemObj : orOps) {
								if (orElemObj instanceof ZExpression) {
									ZExpression orElem = (ZExpression) orElemObj;
									if (orElem.getOperator().equals("IN") && (orElem.nbOperands() == 2)) {
										if (orElem.getOperand(1) instanceof ZQuery) {
											ZQuery subQuery = (ZQuery) orElem.getOperand(1);
											if (subQuery.getWhere() instanceof ZExpression) {
												ZExpression subExpr = (ZExpression) subQuery.getWhere();
												if (fromItems.containsAll(subQuery.getFrom())) {
													subExpr.getOperands().removeAll(whereOps);
													orElem.setOperator(subExpr.getOperator());
													orElem.setOperands(subExpr.getOperands());
												} else {
													subExpr.getOperands().removeAll(whereOps);
													subQuery.getFrom().removeAll(fromItems);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			where = parsedWhere.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String replaceDateAndTimestampPatterns(String where) {
		return where.replaceAll("\\{(d|ts)([^}]*)\\}","$2");
	}

	private List<ZFromItem> toZFromItems(String from) {
		List<ZFromItem> fromItems = new ArrayList<ZFromItem>();
		String[] tokens = from.split("\\s*,\\s*");
		for (String token : tokens) {
			fromItems.add(toZFromItem(token));
		}
		return fromItems;
	}

	private ZFromItem toZFromItem(String fromElement) {
		String[] tokens = fromElement.split("\\s+");
		ZFromItem zFromItem = new ZFromItem(tokens[0]);
		if (tokens.length > 1) {
			zFromItem.setAlias(tokens[1]);
		}
		return zFromItem;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public static void main(String[] args) throws IOException, ParseException {
		String from = FileUtils.readFileToString(new File("src/test/resources/sqlFrom.txt"), Charset.defaultCharset());
		String where = FileUtils.readFileToString(new File("src/test/resources/sqlWhere.txt"), Charset.defaultCharset());
		SubQueryWhereClauseOptimizer opt = new SubQueryWhereClauseOptimizer(from, where);
		opt.optimizeQuery();
		System.out.println(opt.getWhere());
	}

}
