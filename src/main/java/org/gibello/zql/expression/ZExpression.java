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

package org.gibello.zql.expression;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.gibello.zql.query.ZQuery;
import org.gibello.zql.utils.ZCommonConstants;
import org.gibello.zql.utils.ZUtils;

/**
 * ZExpression: an SQL Expression An SQL expression is an operator and one or
 * more operands Example: a AND b AND c -> operator = AND, operands = (a, b, c).
 *
 * @author Pierre-Yves Gibello
 * @author Bogdan Mariesan, Romania
 */
public class ZExpression implements ZExp {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The operator.
	 */
	private String operator = null;

	/**
	 * The list of operands.
	 */
	private List<ZExp> operands = null;

	/**
	 * Create an SQL Expression given the operator.
	 *
	 * @param operator
	 *            The operator
	 */
	public ZExpression(final String operator) {
		this.operator = operator;
	}

	/**
	 * Create an SQL Expression given the operator and 1st operand.
	 *
	 * @param operator
	 *            The operator
	 * @param firstOperand
	 *            The 1st operand
	 */
	public ZExpression(final String operator, final ZExp firstOperand) {
		this.operator = operator;
		this.addOperand(firstOperand);
	}

	/**
	 * Create an SQL Expression given the operator, 1st and 2nd operands.
	 *
	 * @param operator
	 *            The operator
	 * @param secondOperand
	 *            The 1st operand
	 * @param thirdOperand
	 *            The 2nd operand
	 */
	public ZExpression(final String operator, final ZExp secondOperand, final ZExp thirdOperand) {
		this.operator = operator;
		this.addOperand(secondOperand);
		this.addOperand(thirdOperand);
	}

	/**
	 * Sets this expression's operator.
	 *
	 * @return the operator.
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * Get this expression's operator.
	 *
	 * @return the operator.
	 */
	public String getOperator() {
		return this.operator;
	}

	/**
	 * Get this expression's operands.
	 *
	 * @return the operands (as a Vector of ZExp objects).
	 */
	public List<ZExp> getOperands() {
		return this.operands;
	}

	/**
	 * Set the operands list.
	 *
	 * @param operands
	 *            A vector that contains all operands (ZExp objects).
	 */
	public void setOperands(final List<ZExp> operands) {
		this.operands = operands;
	}

	/**
	 * Add an operand to the current expression.
	 *
	 * @param operand
	 *            The operand to add.
	 */
	public void addOperand(final ZExp operand) {
		if (this.operands == null) {
			this.operands = new Vector<ZExp>();
		}
		this.operands.add(operand);
	}

	/**
	 * Get an operand according to its index (position).
	 *
	 * @param pos
	 *            The operand index, starting at 0.
	 * @return The operand at the specified index, null if out of bounds.
	 */
	public ZExp getOperand(final int pos) {
		return this.operands.get(pos);
	}

	/**
	 * Get the number of operands.
	 *
	 * @return The number of operands
	 */
	public int nbOperands() {
		return this.operands.size();
	}

	/**
	 * String form of the current expression (reverse polish notation). Example:
	 * a > 1 AND b = 2 -> (AND (> a 1) (= b 2))
	 *
	 * @return The current expression in reverse polish notation (a String)
	 */
	public String toReversePolish() {
		final StringBuilder buf = new StringBuilder(ZCommonConstants.LEFT_BRACKET);
		buf.append(this.operator);
		for (int i = 0; i < this.nbOperands(); i++) {
			final ZExp opr = this.getOperand(i);
			if (opr instanceof ZExpression) {
				// Warning recursive call
				buf.append(ZCommonConstants.EMPTY_STRING).append(((ZExpression) opr).toReversePolish());
			} else if (opr instanceof ZQuery) {
				buf.append(ZCommonConstants.EMPTY_STRING + ZCommonConstants.LEFT_BRACKET).append(opr.toString()).append(ZCommonConstants.RIGHT_BRACKET);
			} else {
				buf.append(ZCommonConstants.EMPTY_STRING).append(opr.toString());
			}
		}
		buf.append(ZCommonConstants.RIGHT_BRACKET);
		return buf.toString();
	}

	@Override
	public String toString() {

		if (this.operator.equals("?")) {
			// For prepared columns ("?")
			return this.operator;
		}

		if (ZUtils.isCustomFunction(this.operator) >= 0) {
			return this.formatFunction();
		}

		final StringBuilder buf = new StringBuilder();
		if (needPar(this.operator)) {
			buf.append(ZCommonConstants.LEFT_BRACKET);
		}

		ZExp operand;
		switch (this.nbOperands()) {

		case 1:
			operand = this.getOperand(0);
			if (operand instanceof ZConstant) {
				// Operator may be an aggregate function (MAX, SUM...)
				if (ZUtils.isAggregate(this.operator)) {
					buf.append(this.operator).append(ZCommonConstants.LEFT_BRACKET).append(operand.toString()).append(ZCommonConstants.RIGHT_BRACKET);
				} else if (this.operator.equals(ZOperator.IS_NULL.symbol()) || this.operator.equals(ZOperator.IS_NOT_NULL.symbol())) {
					buf.append(operand.toString()).append(ZCommonConstants.EMPTY_STRING).append(this.operator);
				}
				// "," = list of values, here just one single value
				else if (this.operator.equals(ZCommonConstants.COMMA)) {
					buf.append(operand.toString());
				} else {
					buf.append(this.operator).append(ZCommonConstants.EMPTY_STRING).append(operand.toString());
				}
			} else if (operand instanceof ZQuery) {
				buf.append(this.operator).append(ZCommonConstants.EMPTY_STRING).append(ZCommonConstants.LEFT_BRACKET).append(operand.toString())
						.append(ZCommonConstants.RIGHT_BRACKET);
			} else {
				switch (ZOperator.toEnumQuiet(operator)) {
				case IS_NULL:
				case IS_NOT_NULL:
					buf.append(operand.toString()).append(ZCommonConstants.EMPTY_STRING).append(this.operator);
					break;
				// "," = list of values, here just one single value
				case COMMA:
					buf.append(operand.toString());
					break;
				default:
					buf.append(this.operator).append(ZCommonConstants.EMPTY_STRING).append(operand.toString());
					break;
				}
			}
			break;

		case ZCommonConstants.MAGIC_NUMBER_3:
			if (this.operator.toUpperCase().endsWith("BETWEEN")) {
				buf.append(this.getOperand(0).toString()).append(ZCommonConstants.EMPTY_STRING).append(this.operator).append(ZCommonConstants.EMPTY_STRING)
						.append(this.getOperand(1).toString()).append(" AND ").append(this.getOperand(ZCommonConstants.MAGIC_NUMBER_2).toString());
				break;
			}

		default:

			boolean inOperator = false;
			if (this.operator.equals("IN") || this.operator.equals("NOT IN")) {
				inOperator = true;
			}

			final int nb = this.nbOperands();
			for (int i = 0; i < nb; i++) {

				if (inOperator && (i == 1)) {
					buf.append(ZCommonConstants.EMPTY_STRING).append(this.operator).append(" (");
				}

				operand = this.getOperand(i);
				if ((operand instanceof ZQuery) && !inOperator) {
					buf.append(ZCommonConstants.LEFT_BRACKET).append(operand.toString()).append(ZCommonConstants.RIGHT_BRACKET);
				} else {
					buf.append(operand.toString());
				}
				if (i < (nb - 1)) {
					if (this.operator.equals(ZCommonConstants.COMMA) || (inOperator && (i > 0))) {
						buf.append(", ");
					} else if (!inOperator) {
						buf.append(ZCommonConstants.EMPTY_STRING).append(this.operator).append(ZCommonConstants.EMPTY_STRING);
					}
				}
			}
			if (inOperator) {
				buf.append(ZCommonConstants.RIGHT_BRACKET);
			}
			break;
		}

		if (this.needPar(this.operator)) {
			buf.append(ZCommonConstants.RIGHT_BRACKET);
		}

		return buf.toString();
	}

	/**
	 * Checks for special operators.
	 *
	 * @param op
	 *            the operator.
	 * @return the result of the check.
	 */
	private boolean needPar(final String op) {
		final String tmp = op.toUpperCase();
		return !(tmp.equals("ANY") || tmp.equals("ALL") || tmp.equals("UNION") || ZUtils.isAggregate(tmp));
	}

	/**
	 * Formatting function.
	 *
	 * @return the formatted string.
	 */
	private String formatFunction() {
		final StringBuilder b = new StringBuilder(this.operator + ZCommonConstants.LEFT_BRACKET);
		final int nb = this.nbOperands();
		for (int i = 0; i < nb; i++) {
			b.append(this.getOperand(i).toString()).append(i < (nb - 1) ? ZCommonConstants.COMMA : "");
		}
		b.append(ZCommonConstants.RIGHT_BRACKET);
		return b.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}

		ZExpression that = (ZExpression) o;

		return new EqualsBuilder().append(getOperator(), that.getOperator()).append(getOperands(), that.getOperands()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getOperator()).append(getOperands()).toHashCode();
	}
}
