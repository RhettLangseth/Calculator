package main;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

public class GUI implements ActionListener, DocumentListener{
	
	private boolean updating;
	private Document inputDocument;
	private JTextArea inputArea;
	private JTextArea outputArea;
	private JScrollPane inputScrollPane;
	private JScrollPane outputScrollPane;
	private JFrame frame;
	private Calculator calculator;
	
	public GUI(){
		
		
	}
	
	public void start(){
		
		int screenWidth = 500;
		int screenHeight = 500;
		Font font = new Font("Consolas", Font.PLAIN, 20);
		DefaultCaret caret;
		Container contentPane;
		JSplitPane hSplitPane;
		JSplitPane vSplitPane;
		Container container;
		JButton button;
		String[] buttonStrings = {"NL", "CE", "C", "Del", "^", "PI", "7", "8", "9", "/", "", "4",
								  "5", "6", "x", "", "1", "2", "3", "-", "(", ")", "0", ".", "+"};
		
		updating = false;
		calculator = new Calculator();
		
		try{ UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() ); }catch(Exception e){}
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation( (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - screenWidth / 2, (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - screenHeight / 2 );
		frame.setSize(screenWidth, screenHeight);
		frame.setTitle("Calculator");
		
		contentPane = frame.getContentPane();
		
		inputArea = new JTextArea();
		inputArea.getDocument().addDocumentListener(this);
		inputArea.setFont(font);
		inputArea.getCaret().setBlinkRate(0);
		
		inputDocument = inputArea.getDocument();
		
		inputScrollPane = new JScrollPane(inputArea);
		inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		inputScrollPane.getVerticalScrollBar().setPreferredSize( new Dimension(0,0) );
		
		outputArea = new JTextArea();
		outputArea.getDocument().addDocumentListener(this);
		outputArea.setEditable(false);
		outputArea.setCursor( new Cursor(Cursor.TEXT_CURSOR) );
		outputArea.setFont(font);
		
		caret = (DefaultCaret)outputArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		outputScrollPane = new JScrollPane(outputArea);
		outputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScrollPane.getVerticalScrollBar().setModel( inputScrollPane.getVerticalScrollBar().getModel() );
		
		hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		hSplitPane.setLeftComponent(inputScrollPane);
		hSplitPane.setRightComponent(outputScrollPane);
		hSplitPane.setResizeWeight(0.5);
		
		container = new Container();
		container.setLayout( new GridLayout(5, 5) );
		
		for(String buttonText : buttonStrings){
			
			button = new JButton(buttonText);
			button.addActionListener(this);
			button.setFocusable(false);
			button.setFont(font);
			container.add(button);
			
		}
		
		vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		vSplitPane.setTopComponent(hSplitPane);
		vSplitPane.setBottomComponent(container);
		vSplitPane.setResizeWeight(1);
		
		contentPane.add(vSplitPane);
		
		frame.setVisible(true);
		
		hSplitPane.setDividerLocation(0.5);
		vSplitPane.setDividerLocation(0.5);
		
	}
	
	private void update(Document document){
		
		BigDecimal answer;
		String text;
		String outputText;
		String[] lines;
		
		if(updating)
			return;
		
		updating = true;
		
		try{
			
			text = document.getText( 0, document.getLength() );
			lines = text.split("\n", -1);
			outputText = "";
			
			for(int i = 0; i < lines.length; i++){
				
				answer = calculator.calculate2(lines[i], 35, 200);
				outputText += (answer != null ? answer.toPlainString() : "") + (i < lines.length - 1 ? "\n" : "");
				
			}
			
			outputArea.setText(outputText);
			
		}catch(BadLocationException e){}
		
		inputScrollPane.revalidate();
		outputScrollPane.revalidate();
		updating = false;
		
	}
	
	public void executeButton(JButton button){
		
		String buttonText = button.getText();
		
		if(buttonText == null || buttonText.length() == 0)
			return;
		
		if( buttonText.equals("CE") )
			try{ inputDocument.remove( 0, inputDocument.getLength() ); }catch(BadLocationException e){}
		else if( buttonText.equals("C") )
			clearLine();
		else if( buttonText.equals("Del") )
			delete();
		else if( buttonText.equals("NL") )
			try{ inputDocument.insertString(inputArea.getCaretPosition(), "\n", null); }catch(BadLocationException e){}
		else
			try{ inputDocument.insertString(inputArea.getCaretPosition(), buttonText, null); }catch(BadLocationException e){}
		
	}
	
	public void clearLine(){
		
		int caretIndex = inputArea.getCaretPosition();
		int lineStartIndex = 0;
		int index = 0;
		char[] text;
		
		try{
			
			text = inputDocument.getText( 0, inputDocument.getLength() ).toCharArray();
			
			for(char current : text){
				
				if(current == '\n'){
					
					if(index < caretIndex)
						lineStartIndex = index + 1;
					else if(index >= caretIndex)
						break;
					
				}
				
				index++;
				
			}
			
			index--;
			try{ inputDocument.remove(lineStartIndex, index - lineStartIndex + 1); }catch(BadLocationException e){}
			
		}catch(BadLocationException e){}
		
	}
	
	public void delete(){
		
		int start = inputArea.getSelectionStart();
		int end = inputArea.getSelectionEnd();
		
		try{
			
			if(start == end)
				inputDocument.remove(start - 1, 1);
			else
				inputDocument.remove(start, end - start);
			
		}catch(BadLocationException e){}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		Object object = e.getSource();
		
		if(object instanceof JButton)
			executeButton( (JButton)object );
		
	}
	
	@Override
	public void insertUpdate(DocumentEvent e){
		
		update( e.getDocument() );
		
	}
	
	@Override
	public void removeUpdate(DocumentEvent e){
		
		update( e.getDocument() );
		
	}
	
	@Override
	public void changedUpdate(DocumentEvent e){}
	
}