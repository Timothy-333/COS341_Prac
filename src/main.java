import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
public class main {
    public static void main(String[] args) {
        try {
<<<<<<< HEAD
            String text = new String(Files.readAllBytes(Paths.get("COS341_Prac/src/input.txt")), StandardCharsets.UTF_8);
=======
            // Read input from file
            String text = new String(Files.readAllBytes(Paths.get("src/input2.txt")), StandardCharsets.UTF_8);
            // Lexical analysis
>>>>>>> main
            Lexer lexer = new Lexer(text);
            List<TokenClass> tokenList = lexer.lex();
            // Parsing
            Parser parser = new Parser(tokenList);
            parser.parse();
            // Scope analysis
            ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
            Map<String, ScopeAnalyzer.SymbolInfo> symbolTable = scopeAnalyzer.analyze(parser.getRoot());
            scopeAnalyzer.printSymbolTable();
            System.out.println();
            // Intermediate code generation
            IntermediateCodeGenerator icg = new IntermediateCodeGenerator(symbolTable, parser.getRoot());
            String intermediateCode = icg.generateIntermediateCode();
            System.out.println(intermediateCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
