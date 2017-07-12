/* *** This file is given as part of the programming assignment. *** */

import java.util.*;

public class Parser {
    private ArrayList<ArrayList<String>> stack; // main stack
    private ArrayList<String> temp_table; // symbol table
    private int block_num; // current block number
    
    

    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	
	block_num = -1;
	stack = new ArrayList<ArrayList<String>>();
	
	scan();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }

    private void program() {
	push();
	block();
	pop();
    }

    private void block(){
	declaration_list();
	statement_list();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
	while( is(TK.DECLARE) ) {
	    declaration();
	}
    }

    private void declaration() {
	mustbe(TK.DECLARE);
	check_scope(tok.string);
	mustbe(TK.ID);
	while( is(TK.COMMA) ) {
	    scan();
	    check_scope(tok.string);
	    mustbe(TK.ID);
	}
    }

    private void statement_list()
    {
        while(statement());
    }

    private boolean statement()
    {
        if(is(TK.TILDE) || is(TK.ID))
            assignment();
        else if(is(TK.PRINT)) // !
            print();
        else if(is(TK.DO)) // <
            do_statement();
        else if(is(TK.IF)) // [
            if_statement();
        else
            return false;
        return true;
    }

    private void print()
    {
        scan(); // !
        expression();
    }

    private void assignment()
    {
        reference_id();
        mustbe(TK.ASSIGN);
        expression();
    }

    private void reference_id() //change
    {
        if(is(TK.TILDE)) // ~
        {
            String temp_str = tok.string;
            scan();
            //test();
            if(is(TK.NUM)){
                int temp_num = Integer.parseInt(tok.string);
                scan();
                
        
                if (block_num < temp_num){
                    System.out.println("no such variable " + temp_str + "" + temp_num + "" + tok.string+ " on line " + tok.lineNumber);
                    System.exit(1);

                }

                else if(stack.get(block_num - temp_num).indexOf(tok.string) < 0){
                    System.out.println("no such variable " + temp_str + "" + temp_num + "" + tok.string+ " on line " + tok.lineNumber);
                    System.exit(1);

                    }
            }
            else if(stack.get(0).indexOf(tok.string) < 0){
                    System.out.println("no such variable " + temp_str + "" + tok.string+ " on line " + tok.lineNumber);
                    System.exit(1);
            }

        }
        check_declared(tok.string);
        mustbe(TK.ID);
        
    }
    
    private void expression()
    {
        term();
        while(addop()) // + -
        //while(is(TK.PLUS) || is(TK.MINUS))
             term();
    }

  
    private void term()
    {
        factor();
        while(multop()) // * /
       //while(is(TK.TIMES) || is(TK.DIVIDE))
            factor();
    }
    
     private boolean addop() {
        if(is(TK.PLUS) || is(TK.MINUS)) // + -
        {
            scan();
            return true;
        }
        return false;
    }

    
    private boolean multop() {
        if(is(TK.TIMES) || is(TK.DIVIDE)) // * /
        {
            scan();
            return true;
        }
        return false;
    }

    private void do_statement()
    {
        scan(); // <
        guarded_command();
        mustbe(TK.ENDDO); // >
    }

    private void if_statement()
    {
        scan(); // [
        guarded_command();
        while(is(TK.ELSEIF)) // |
        {
            scan();
            guarded_command();
        }
        if(is(TK.ELSE)) // %
        {
            scan();
            push();
            block();
            pop();
        }
        mustbe(TK.ENDIF); // ]
    }

  
    private void guarded_command()
    {
        expression();
        mustbe(TK.THEN); // :
        push();
        block();
        pop();
    }

 
    private void factor()
    {
        if( is(TK.LPAREN) ) // (
        {
            scan();
            expression();
            mustbe(TK.RPAREN); // )
        }
        else if(is(TK.TILDE) || is(TK.ID))
            reference_id();
        else
            mustbe(TK.NUM);
    }

    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
// check_scope fucntion for part3 to check if redeclaration
    private boolean check_scope(String variable_name){
        
        for(int index = stack.get(block_num).size() - 1; 
          index > -1; index--){
            if(variable_name.equals(stack.get(block_num).get(index))){
                System.err.println("redeclaration of variable " + variable_name);

                return false;

            }
        }
        stack.get(block_num).add(new String(variable_name));
        return true;


    }
    
    // Part 3 check if declared or not
    private void check_declared(String variable_name){
        
        for(int block_index = block_num; block_index > -1; block_index--){
            for(int array_index = stack.get(block_index).size() - 1;
            array_index > -1; array_index--){

                if (variable_name.equals(stack.get(block_index).get(array_index))){
                    return;
                }
            }
            
        }
        System.err.println(variable_name + " is an undeclared variable on line " + tok.lineNumber);

        System.exit(1);
        
    }
    

    // Part 3 push function is to add a table and increment for current block number
    private void push(){
        block_num++;
        temp_table = new ArrayList<String>();
        stack.add(temp_table);
    }

    // Part 3 pop function is to remove a table and decrement for current block number

    private void pop(){
        stack.remove(block_num);
        block_num--;
    }
    
        
}//Parser Class
