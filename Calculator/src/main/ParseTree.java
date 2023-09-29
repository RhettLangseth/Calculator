package main;

public class ParseTree{
	
	public static final int TYPE_INT = 0;
	public static final int TYPE_FLOAT = 1;
	public static final int TYPE_PAREN = 2;
	public static final int TYPE_PLUS = 3;
	public static final int TYPE_MINUS = 4;
	public static final int TYPE_MULTIPLY = 5;
	public static final int TYPE_DIVIDE = 6;
	
	public int type;
	public float data;
	public ParseTree lTree;
	public ParseTree rTree;
	public ParseTree parent;
	
	public ParseTree(){}
	
}
