package main;

import java.util.ArrayList;

public class Parser{
	
	public Parser(){}
	
	public ParseTree parse(ArrayList<String> list){
		
		int parenCount = 0;
		ParseTree tree = new ParseTree();
		ParseTree newTree;
		
		for(String current : list){
			
			if( current.matches("[0-9]+(\\.[0-9]*)?(E[0-9]*)?") ){
				
				if(tree.parent == null){
					
					tree.data = Float.parseFloat(current);
					newTree = new ParseTree();
					tree.parent = newTree;
					tree = newTree;
					
				}else{
					
					newTree = new ParseTree();
					newTree.data = Float.parseFloat(current);
					newTree.parent = tree;
					tree.lTree = newTree;
					
				}
				
			}else if( current.equals("(") ){
				
				newTree = new ParseTree();
				newTree.parent = tree;
				tree.rTree = newTree;
				tree.type = ParseTree.TYPE_PAREN;
				tree = newTree;
				parenCount++;
				
			}else if( current.equals(")") ){
				
				if(tree.parent == null)
					return null;
				
				tree = tree.parent;
				parenCount--;
				
			}else if( current.equals("+") ){
				
				newTree = new ParseTree();
				newTree.parent = tree;
				tree.rTree = newTree;
				tree.type = ParseTree.TYPE_PLUS;
				tree = newTree;
				
			}else{
				
				
				
			}
			
		}
		
		if(parenCount != 0)
			return null;
		
		return tree;
		
	}
	
	public static ArrayList<String> tokenize(String string, ArrayList<String> tokens){
		
		char last = 0;
		int match = 0;
		int match2 = 0;
		String currentToken = "";
		ArrayList<String> list = new ArrayList<String>();
		
		for( char current : string.toCharArray() ){
			
			if(current == 'x')
				current = '*';
			
			if(last == 0){
				
				last = current;
				match2 = matches(currentToken + current, tokens);
				continue;
				
			}
			
			if(last == ' ' || last == '\t' || last == '\n'){
				
				last = current;//Fix this?
				currentToken = "";
				continue;
				
			}
			
			currentToken += last;
			match = matches(currentToken, tokens);
			match2 = matches(currentToken + current, tokens);
			
			if(match == 1 && match2 == 0){
				
				list.add(currentToken);
				currentToken = "";
				match2 = matches("" + current, tokens);
				
			}else if(match == 0 && match2 == 0)
				return null;
			
			last = current;
			
		}
		
		if(match2 == 1)
			list.add(currentToken + last);
		
		return list;
		
	}
	
	public static int matches(String string, ArrayList<String> tokens){
		
		for(String current : tokens){
			
			if( string.matches("^" + current) ){
				
				if( string.matches("^" + current + "$") )
					return 1;
				else
					return 2;
				
			}
			
		}
		
		return 0;
		
	}
	
	
	
}
