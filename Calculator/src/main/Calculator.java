package main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Stack;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Calculator{
	
	private static final String PI_STRING = "3.141592653589793238462643383279502884197169399375105820974944592307816406286208998628034825342117067";
	
	private ScriptEngine engine;
	private ArrayList<String> tokens;
	
	public Calculator(){
		
		loadTokens();
		engine = new ScriptEngineManager().getEngineByName("JavaScript");
		
	}
	
	private String removeParens(String equation){
		
		boolean lastWasParen = false;
		boolean lastWasDigit = false;
		int parenCount = 0;
		char last = 0;
		String string = "";
		String newEquation = "";
		
		for( char current : equation.toCharArray() ){
			
			if( lastWasParen && ( current == '(' || (current >= '0' && current <= '9') ) )
				newEquation += 'x';
			
			if(parenCount == 0 && current == '('){
				
				if(lastWasDigit)
					newEquation += 'x';
				
				string = "";
				parenCount++;
				last = current;
				lastWasParen = false;
				continue;
				
			}
			
			if(current == '(')
				parenCount++;
			else if(current == ')')
				parenCount--;
			
			if(parenCount < 0)
				return "";
			else if(parenCount == 0){
				
				if(current == ')')
					newEquation += calculate(string);
				else if(current != '(')
					newEquation += current;
				else if(last >= '0' || last <= '9')
					newEquation += 'x';
				
			}else
				string += current;
			
			last = current;
			lastWasParen = current == ')';
			lastWasDigit = current >= '0' && current <= '9';
			
		}
		
		return newEquation;
		
	}
	
	private int getPrecedence(String token){
		
		if( token.equals("^") )
			return 2;
		else if( token.equals("*") )
			return 1;
		else if( token.equals("/") )
			return 1;
		else if( token.equals("+") )
			return 0;
		else if( token.equals("-") )
			return 0;
		else
			return -1;
		
	}
	
	private boolean applyOperation(Stack<String> operatorStack, Stack<String> valueStack, int internalPrecision){
		
		boolean leftValueNegative;
		String operator;
		String rightValue;
		String leftValue;
		BigDecimal answer;
		
		operator = operatorStack.pop();
		
		if(valueStack.size() < 2)
			return false;
		
		rightValue = valueStack.pop();
		leftValue = valueStack.pop();
		
		if( operator.equals("^") && leftValue.startsWith("-") ){
			
			leftValue = leftValue.substring(1);
			leftValueNegative = true;
			
		}else
			leftValueNegative = false;
		
		leftValue = leftValue.replace('_', '-').replace("--", "");
		rightValue = rightValue.replace('_', '-').replace("--", "");
		
		if( operator.equals("^") ){
			
			if( rightValue.matches("^\\d+(\\.\\d+)?$") ){
				
				answer = pow(leftValue, rightValue);
				
				if(answer == null)
					return false;
				
				valueStack.push( answer.toPlainString() );
				
			}else if( rightValue.matches("^-\\d+(\\.\\d+)?$") ){
				
				answer = pow( leftValue, rightValue.substring( 1, rightValue.length() ) );
				
				if(answer == null)
					return false;
				
				try{
					
					valueStack.push( new BigDecimal("1").divide(answer, internalPrecision,
																BigDecimal.ROUND_HALF_UP).toPlainString() );
					
				}catch(ArithmeticException e){
					
					return false;
					
				}
				
			}else
				return false;
			
		}else if( operator.equals("*") )
			valueStack.push( new BigDecimal(leftValue).multiply( new BigDecimal(rightValue) ).toPlainString() );
		else if( operator.equals("/") ){
			
			try{
				
				valueStack.push( new BigDecimal(leftValue).divide(new BigDecimal(rightValue), internalPrecision,
																  BigDecimal.ROUND_HALF_UP).toPlainString() );
				
			}catch(ArithmeticException e){
				
				return false;
				
			}
			
		}else if( operator.equals("+") )
			valueStack.push( new BigDecimal(leftValue).add( new BigDecimal(rightValue) ).toPlainString() );
		else if( operator.equals("-") )
			valueStack.push( new BigDecimal(leftValue).subtract( new BigDecimal(rightValue) ).toPlainString() );
		
		if(leftValueNegative)
			valueStack.push( "-" + valueStack.pop() );
		
		valueStack.push( valueStack.pop().replace("--", "") );
		
		return true;
		
	}
	
	private ArrayList<String> formatTokens(ArrayList<String> tokens){
		
		int state = 0;
		String top;
		String token2;
		ArrayList<String> tokens2 = new ArrayList<String>();
		Stack<String> stack = new Stack<String>();
		
		for(String token : tokens){
			
			if( stack.isEmpty() )
				top = null;
			else
				top = stack.peek();
			
			if( token.matches("^[*/+^]$") )
				token2 = "*";
			else if( token.matches("^\\d+(\\.\\d+)?$") )
				token2 = "0";
			else
				token2 = token;
			
			if(state == 0){
				
				if( token2.equals("(") ){//|| token2.equals("log(") ){
					
					stack.push("(");
					state = 0;
					
				}else if( token2.equals("-") )
					state = 1;
				else if( token2.equals("0") )
					state = 2;
				else
					return null;
				
			}else if(state == 1){
				
				if( token2.equals("0") ){
					
					tokens2.remove(tokens2.size() - 1);
					token = "-" + token;
					state = 2;
					
				}else if( token2.equals("(") || token2.equals("log(") ){
					
					tokens2.remove(tokens2.size() - 1);
					token = "-" + token;
					stack.push(token);
					state = 0;
					
				}else
					return null;
				
			}else if(state == 2){
				
				if( token2.equals("*") || token2.equals("-") )
					state = 0;
				else if( token2.equals(")") ){
					
					if( top == null || ( !top.equals("(") && !top.equals("log(") &&
										 !top.equals("-(") && !top.equals("-log(") ) )
						return null;
					
					stack.pop();
					state = 2;
					
				}else
					return null;
				
			}
			
			tokens2.add(token);
			
		}
		
		if( state != 2 || !stack.isEmpty() )
			return null;
		
		return tokens2;
		
	}
	
	public BigDecimal calculate2(String expression, int answerPrecision, int internalPrecision){
		
		boolean topNegative;
		char current;
		int currentTokenPrecedence;
		String token = "";
		String top;
		ArrayList<String> tokens = new ArrayList<String>();
		Stack<String> valueStack;
		Stack<String> operatorStack;
		char[] expressionArray;
		
		expressionArray = expression.replaceAll(" ", "").replaceAll("x", "*").toCharArray();
		
		for(int i = 0; i < expressionArray.length; i++){
			
			current = expressionArray[i];
			
			if( current == 'I' && token.equals("P") ){
				
				tokens.add(PI_STRING);
				token = "";
				
			}else if( current == '(' && token.equals("log") ){
				
				tokens.add(token + current);
				token = "";
				
			}else if(current == '(' || current == ')' || current == '^' ||
					 current == '*' || current == '/' || current == '+' || current == '-'){
				
				if( !token.equals("") )
					tokens.add(token);
				
				tokens.add("" + current);
				token = "";
				
			}else{
				
				token += current;
				
				if(i == expressionArray.length - 1)
					tokens.add(token);
				
			}
			
		}
		
		tokens = formatTokens(tokens);
		
		if(tokens == null)
			return null;
		
		valueStack = new Stack<String>();
		operatorStack = new Stack<String>();
		
		for(String currentToken : tokens){
			
			if( currentToken.matches("^-?\\d+(\\.\\d+)?$") )
				valueStack.push(currentToken);
			else if( currentToken.equals("(") || currentToken.equals("log(") ||
					 currentToken.equals("-(") || currentToken.equals("-log(") )
				operatorStack.push(currentToken);
			else if( currentToken.equals(")") ){
				
//		         1 While the thing on top of the operator stack is not a 
//		           left parenthesis,
//		             1 Pop the operator from the operator stack.
//		             2 Pop the value stack twice, getting two operands.
//		             3 Apply the operator to the operands, in the correct order.
//		             4 Push the result onto the value stack.
//		         2 Pop the left parenthesis from the operator stack, and discard it.
				
				while( !operatorStack.peek().equals("(") && !operatorStack.peek().equals("log(") &&
					   !operatorStack.peek().equals("-(") && !operatorStack.peek().equals("-log(") ){
					
					if( !applyOperation(operatorStack, valueStack, internalPrecision) )
						return null;
					
				}
				
				top = operatorStack.pop();
				
				if( top.equals("-(") || top.equals("-log(") ){
					
					top = top.substring(1);
					topNegative = true;
					
				}else
					topNegative = false;
				
				if( top.equals("log(") )
					valueStack.push( log( valueStack.pop() ).toPlainString() );
				
				if( valueStack.peek().startsWith("-") )
					valueStack.push( "_" + valueStack.pop().substring(1) );
				
				if(topNegative)
					valueStack.push( "-" + valueStack.pop() );
				
			}else if( currentToken.matches("^[*/+-^]$") ){
				
//		         1 While the operator stack is not empty, and the top thing on the
//		           operator stack has the same or greater precedence as thisOp,
//		           1 Pop the operator from the operator stack.
//		           2 Pop the value stack twice, getting two operands.
//		           3 Apply the operator to the operands, in the correct order.
//		           4 Push the result onto the value stack.
//		         2 Push thisOp onto the operator stack.
				
				currentTokenPrecedence = getPrecedence(currentToken);
				
				while(!operatorStack.isEmpty() && getPrecedence( operatorStack.peek() ) >= currentTokenPrecedence){
					
					if( !applyOperation(operatorStack, valueStack, internalPrecision) )
						return null;
					
				}
				
				operatorStack.push(currentToken);
				
			}else
				return null;
			
		}
		
		while( !operatorStack.isEmpty() ){
			
			if( !applyOperation(operatorStack, valueStack, internalPrecision) )
				return null;
			
		}
		
		if(!operatorStack.isEmpty() || valueStack.size() != 1)
			return null;
		
		valueStack.push( valueStack.pop().replace('_', '-') );
		
		try{
			
			return new BigDecimal( valueStack.pop() ).divide(BigDecimal.ONE, answerPrecision,
															 BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
			
		}catch(NumberFormatException e){
			
			return null;
			
		}
		
//		1. While there are still tokens to be read in,
//		   1.1 Get the next token.
//		   1.2 If the token is:
//		       1.2.1 A number: push it onto the value stack.
//		       1.2.2 A variable: get its value, and push onto the value stack.
//		       1.2.3 A left parenthesis: push it onto the operator stack.
//		       1.2.4 A right parenthesis:
//		         1 While the thing on top of the operator stack is not a 
//		           left parenthesis,
//		             1 Pop the operator from the operator stack.
//		             2 Pop the value stack twice, getting two operands.
//		             3 Apply the operator to the operands, in the correct order.
//		             4 Push the result onto the value stack.
//		         2 Pop the left parenthesis from the operator stack, and discard it.
//		       1.2.5 An operator (call it thisOp):
//		         1 While the operator stack is not empty, and the top thing on the
//		           operator stack has the same or greater precedence as thisOp,
//		           1 Pop the operator from the operator stack.
//		           2 Pop the value stack twice, getting two operands.
//		           3 Apply the operator to the operands, in the correct order.
//		           4 Push the result onto the value stack.
//		         2 Push thisOp onto the operator stack.
//		2. While the operator stack is not empty,
//		    1 Pop the operator from the operator stack.
//		    2 Pop the value stack twice, getting two operands.
//		    3 Apply the operator to the operands, in the correct order.
//		    4 Push the result onto the value stack.
//		3. At this point the operator stack should be empty, and the value
//		   stack should have only one value in it, which is the final result.
		
	}
	
	public BigDecimal pow(String value, String exponent){
		
		int decimalIndex = exponent.indexOf('.');
		double doubleValue;
		String decimalPart;
		BigDecimal bigDecimal;
		
		value = value.replace('_', '-');
		exponent = exponent.replace('_', '-');
		
		if(decimalIndex == -1)
			return new BigDecimal(value).pow( Integer.parseInt(exponent) );
		else{
			
			bigDecimal = new BigDecimal(value);
			decimalPart = "0" + exponent.substring( decimalIndex, exponent.length() );
			doubleValue = bigDecimal.doubleValue();
			
			if(doubleValue < 0)
				return null;
			else{
				
				bigDecimal = new BigDecimal( Math.pow( doubleValue, Double.parseDouble(decimalPart) ) );
				return pow( value, exponent.substring(0, decimalIndex) ).multiply(bigDecimal);
				
			}
			
		}
		
	}
	
	public BigDecimal log(String value){
		
		return null;
		
	}
	
	public BigDecimal calculate(String string){
		
		boolean lastWasPow = false;
		String equation = "";
		String last = null;
		String parenStack = "";
		ArrayList<String> list = Parser.tokenize(removeParens(string), tokens);
		
		if(list == null)
			return null;
		
		for(String current : list){
			
			if(last == null){
				
				last = current;
				continue;
				
			}
			
			if(!lastWasPow){
				
				if( current.equals("^") ){
					
					equation += "Math.pow(" + last + ",";
					parenStack += ')';
					
				}else{
					
					if(parenStack.length() > 0){
						
						equation += last + parenStack;
						parenStack = "";
						
					}else
						equation += last;
					
				}
				
			}
			
			last = current;
			lastWasPow = current.equals("^");
			
		}
		
		if(last != null){
			
			if( !last.equals("^") )
				equation += last;
			
			if(parenStack.length() > 0)
				equation += parenStack;
			
		}
		
		equation = "(" + equation + ")+0.0";
		
		if( !equation.equals("") ){
			
			try{
				
				return new BigDecimal( engine.eval(equation).toString() );
				
			}catch(Exception e){
				
				return null;
				
			}
			
		}
		
		return null;
		
	}
	
	private void loadTokens(){
		
		String num = "\\-?[0-9]+(\\.[0-9]*)?(E\\-?[0-9]*)?";
		String inf = "[Infinity]";
		String eq = "\\=";
		String pl = "\\+";
		String min = "\\-";
		String ast = "\\*";
		String ex = "x";
		String sl = "\\/";
		String crt = "\\^";
		String lp = "\\(";
		String rp = "\\)";
		
		tokens = new ArrayList<String>();
		tokens.add(num);
		tokens.add(inf);
		tokens.add(eq);
		tokens.add(pl);
		tokens.add(min);
		tokens.add(ast);
		tokens.add(ex);
		tokens.add(sl);
		tokens.add(crt);
		tokens.add(lp);
		tokens.add(rp);
		
	}
	
}
