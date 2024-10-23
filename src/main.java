import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
public class main {
    public static void main(String[] args) {
        try {
            String text = new String(Files.readAllBytes(Paths.get("COS341_Prac/src/input.txt")), StandardCharsets.UTF_8);
            Lexer lexer = new Lexer(text);
            List<TokenClass> tokenList = lexer.lex();
            Parser parser = new Parser(tokenList);
            parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
