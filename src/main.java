import java.util.*;
public class main {
    public static void main(String[] args) {
        String text = "main void else";
        Lexer lexer = new Lexer(text);
        try {
            List<TokenClass> tokenList = lexer.lex();
            Parser parser = new Parser(tokenList);
            parser.parse();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
