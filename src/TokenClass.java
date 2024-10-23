public class TokenClass{
private Lexer.TokenClassType tokenClassType;
private String tokenClassWord;

TokenClass(Lexer.TokenClassType tokenClassType, String tokenClassWord){
    this.tokenClassType = tokenClassType;
    this.tokenClassWord = tokenClassWord;
}

TokenClass(){}
public String getType(){
    if (tokenClassType == Lexer.TokenClassType.res_key){
        return tokenClassWord;
    }
    else 
    {
        return tokenClassType.name();
    }
}

public String getAbsoluteType(){
        return tokenClassType.name();
}
public String getWord(){
    return tokenClassWord;
}
public String toString(){
    return tokenClassWord;
}
};