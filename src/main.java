import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
public class main {
    public static void main(String[] args) {
        try {
            // Read input from file
            System.out.println("\n\033[0;37m==================== Reading input from file ====================\033[0m");
            String text = Files.readString(Paths.get("src/input3.txt"), StandardCharsets.US_ASCII);
            System.out.println("\033[0;32mReading input from file passed\033[0m");
            
            // Lexical analysis
            System.out.println("\n\033[0;32m==================== Performing lexical analysis ====================\033[0m");
            Lexer lexer = new Lexer(text);
            List<TokenClass> tokenList = lexer.lex();
            System.out.println("\033[0;32mLexical analysis passed\033[0m");
            
            // Parsing
            System.out.println("\n\033[0;36m==================== Parsing ====================\033[0m");
            Parser parser = new Parser(tokenList);
            parser.parse();
            
            // Scope analysis
            System.out.println("\n\033[0;35m==================== Performing scope analysis ====================\033[0m");
            ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
            XMLParseTree root = parser.getRoot();
            Map<String, ScopeAnalyzer.SymbolInfo> symbolTable = scopeAnalyzer.analyze(parser.getRoot());
            scopeAnalyzer.printSymbolTable();
            System.out.println("\033[0;32mScope analysis passed\033[0m");
            
            // Type checking
            System.out.println("\n\033[0;33m==================== Performing type checking ====================\033[0m");
            TypeChecker typeChecker = new TypeChecker(symbolTable);
            if (typeChecker.typeCheck(parser.getRoot())) {
            System.out.println("\033[0;32mType checking passed\033[0m");
            } else {
            System.out.println("\033[0;31mType checking failed\033[0m");
            }
            
            // Intermediate code generation 5.a
            System.out.println("\n\033[0;31m==================== Generating intermediate code (5.a) ====================\033[0m");
            CodeGenerator cg = new CodeGenerator(symbolTable, parser.getRoot());
            String intermediateCode = cg.generateIntermediateCode(false);
            System.out.println(intermediateCode);
            Files.write(Paths.get("src/outputs/intermediateCode5.a.txt"), intermediateCode.getBytes(StandardCharsets.UTF_8));
            System.out.println("\nData Written to intermediateCode5.a.txt");
            System.out.println("\033[0;32mIntermediate code generation (5.a) passed\033[0m");
            
            // Intermediate code generation 5.b
            System.out.println("\n\033[0;34m==================== Generating intermediate code (5.b) ====================\033[0m");
            intermediateCode = cg.generateIntermediateCode(true);
            System.out.println(intermediateCode);
            Files.write(Paths.get("src/outputs/intermediateCode5.b.txt"), intermediateCode.getBytes(StandardCharsets.UTF_8));
            System.out.println("\nData Written to intermediateCode5.b.txt");
            System.out.println("\033[0;32mIntermediate code generation (5.b) passed\033[0m");

            // Target code generation 5.b
            System.out.println("\n\033[0;34m==================== Generating target code (5.b) ====================\033[0m");
            String targetCode = cg.translateToBasic(intermediateCode);
            System.out.println(targetCode);
            Files.write(Paths.get("src/outputs/targetCode5.b.txt"), targetCode.getBytes(StandardCharsets.UTF_8));
            System.out.println("\nData Written to targetCode5.b.txt");
            System.out.println("\033[0;32mTarget code generation (5.b) passed\033[0m");

        } catch (Exception e) {
            System.out.println("\033[0;31m" + e.getMessage() + "\033[0m");
        }
    }
}
