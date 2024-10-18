import java.util.*;
public class main {
    public static void main(String[] args) {
        String text = "main\n" + //
                        "begin\n" + //
                        "end";
        Lexer lexer = new Lexer(text);
        try {
            List<TokenClass> tokenList = lexer.lex();
            Parser parser = new Parser(tokenList);
            parser.parse();

            TypeChecker t = new TypeChecker();
            if (t.typeCheck(parser.root)){
                System.out.println("TERRIFIC!");
            }else{
                System.out.println("TERRIBLE!");};

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
