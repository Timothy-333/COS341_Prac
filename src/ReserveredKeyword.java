public class ReserveredKeyword extends TokenClass{

public String info ;
public int pos;

public void advance(){

}


//Possibly useful
public enum reservedKeywordType{
    MAIN,
    NUM,
    TEXT,
    BEGIN,
    END,
    SKIP,
    HALT,
    PRINT,
    INPUT,
    EQUAL,
    IF,
    THEN,
    ELSE,
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    COMMA
}

public reservedKeywordType type;


public boolean lex(String keyword) {
    boolean validKeyWord = false;
    // boolean inValidChracter = false;


    switch (keyword) {
        case "main":
        type = reservedKeywordType.MAIN;
        validKeyWord =true;
            break;
        case "num":
        type = reservedKeywordType.NUM;
        validKeyWord =true;
            break;
        case "text":
        validKeyWord =true;
            break;
        case "begin":
        validKeyWord =true;
            break;
        case "end":
        validKeyWord = true;
            break;
        case "skip":
        validKeyWord = true;
            break;
        case "halt":
        validKeyWord = true;
            break;
        case "print":
        validKeyWord = true;
            break;
        case "<input":
        validKeyWord = true;
            break;
        case "=":
        validKeyWord = true;
            break;
        case "if":
        validKeyWord = true;
            break;  
        case "then":
        validKeyWord = true;
            break;
        default:
        case "else":
        validKeyWord = true;
        case "(":
        validKeyWord = true; 
            break;
        case ")":
        validKeyWord = true;
            break;
        case "{":
        validKeyWord = true;
            break;
        case "}":
        validKeyWord = true;
            break;
        case ",":
        validKeyWord = true;
            break;
    }
    return validKeyWord;
}    

};