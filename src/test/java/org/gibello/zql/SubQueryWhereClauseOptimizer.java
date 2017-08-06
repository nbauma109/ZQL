package org.gibello.zql;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
			List<ZFromItem> fromItems = toZFromItems(from);
			ByteArrayInputStream whereBytes = new ByteArrayInputStream(where.getBytes());
			ZqlParser whereZqlParser = new ZqlParser(whereBytes);
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

	private List<ZFromItem> toZFromItems(String from) {
		List<ZFromItem> fromItems = new ArrayList<ZFromItem>();
		List<String> tokens = Arrays.asList(from.split("\\s*,\\s*"));
		for (String token : tokens) {
			fromItems.add(new ZFromItem(token));
		}
		return fromItems;
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
