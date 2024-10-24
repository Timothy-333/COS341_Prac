import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
public class main {
    public static void main(String[] args) {
        try {
            // Read input from file
            String text = new String(Files.readAllBytes(Paths.get("src/input2.txt")), StandardCharsets.UTF_8);
            // Lexical analysis
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
            CodeGenerator icg = new CodeGenerator(symbolTable, parser.getRoot());
            String intermediateCode = icg.generateIntermediateCode(false);
            System.out.println(intermediateCode);
            // Write intermediate code to file
            Files.write(Paths.get("src/outputs/intermediateCode.txt"), intermediateCode.getBytes(StandardCharsets.UTF_8));
            String targetCode = icg.generateIntermediateCode(true);
            System.out.println(targetCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
