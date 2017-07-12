/* *** This file is given as part of the programming assignment. *** */

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }

    private void program() {
	block();
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
	mustbe(TK.ID);
	while( is(TK.COMMA) ) {
	    scan();
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

    private void reference_id()
    {
        if(is(TK.TILDE)) // ~
        {
            scan();
            if(is(TK.NUM))
                scan();
        }
        mustbe(TK.ID);
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
            block();
        }
        mustbe(TK.ENDIF); // ]
    }

  
    private void guarded_command()
    {
        expression();
        mustbe(TK.THEN); // :
        block();
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
}
