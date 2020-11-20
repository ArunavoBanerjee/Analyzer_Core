package analyzer.Engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class BooleanParser {
	static public String valid_expr_substr = "";
	Stack<Boolean> operand = new Stack<Boolean>();
	Stack<String> operator = new Stack<String>();
	
	public boolean checkExprValidity(List<String> checkexpr) {
		Stack<String> brackets = new Stack<String>();
		Set<String> operatorList = new HashSet<String>() {{ add("and"); add("or"); add("not");}};
		String token = "";
		boolean opFlag = false;
		for (int i = 0; i < checkexpr.size(); i++) {
			token += checkexpr.get(i) + " ";
			if(checkexpr.get(i).matches("\\{|\\}|\\[|\\]")) {
				valid_expr_substr = token.trim() + ". Only parnthesis (single brackets) are allowed in expression.";
				return false;
			}
			else if(checkexpr.get(i).equals("not"))
				continue;
			else if (checkexpr.get(i).matches("\\(")) {
				if(!opFlag)
					brackets.push(token);
				else {
					valid_expr_substr = token.trim() + "\nOperator Expected.";
					return false;
				}
					
			}
			else if (checkexpr.get(i).matches("\\)")) {
				if(opFlag) {
				if(!brackets.empty())
					brackets.pop();
				else {
					valid_expr_substr = token.trim() + "\nUnclosed Parenthesis.";
					return false;
				}
				} else {
					valid_expr_substr = token.trim() + "\nOperand Expected.";
					return false;
				}
					
			}
			else if (opFlag ^ operatorList.contains(checkexpr.get(i))) {
					valid_expr_substr = token.trim() + "\nConsequitive Types.";
					return false;
				}
			else
				opFlag = !opFlag;
		}
		if(!opFlag) {
			valid_expr_substr = token + "\nOperand Expected.";
			return opFlag;
		}
		else if(!brackets.empty()) {
			valid_expr_substr = brackets.pop() + "\nUnclosed Parenthesis.";
			brackets.empty();
			return false;
		} else
			return opFlag;
	}
	public boolean evalExpr(List<String> filter_expression) {
		//System.out.println(operand);
		for (int i = 0; i < filter_expression.size(); i++) {
			String token = filter_expression.get(i);
			if (token.matches("(true)|(false)")) {
				operand.push(Boolean.parseBoolean(token));
			} else if (token.matches("(and)|(or)|(not)")) {
				if (operator.isEmpty())
					operator.push(token);
				else {
					while (!operator.isEmpty() && checkPrecedence(operator.peek()) < checkPrecedence(token))
						operand.push(eval(operator.pop()));
					operator.push(token);
				}
			} else if (token.equals("("))
				operator.push(token);
			else if (token.equals(")")) {
				while (!operator.peek().equals("("))
					operand.push(eval(operator.pop()));
				operator.pop();
			}
		}
		while (!operator.isEmpty())
			operand.push(eval(operator.pop()));
		return operand.pop();

	}

	int checkPrecedence(String token) {
		switch (token) {
		case "and":
			return 2;
		case "or":
			return 3;
		case "not":
			return 1;
		default:
			return 4;
		}
	}

	boolean eval(String operator) {
		boolean op1;
		boolean op2;
		switch (operator) {
		case "and":
			op1 = operand.pop();
			op2 = operand.pop();
			return (op1 && op2);
		case "or":
			op1 = operand.pop();
			op2 = operand.pop();
			return (op1 || op2);
		case "not":
			op1 = operand.pop();
			return (!op1);
		default:
			return false;
		}
	}
}
