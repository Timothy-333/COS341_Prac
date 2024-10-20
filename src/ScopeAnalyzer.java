import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;

public class ScopeAnalyzer {
    private Map<String, SymbolInfo> symbolTable = new HashMap<>();
    private int uniqueIdCounter = 0;
    private String typeEncountered = null; // Tracks the current type (e.g., "text" or "num") being processed
    private Deque<Scope> scopeStack = new ArrayDeque<>();

    // Entry point for analysis
    public void analyze(Parser.XMLParseTree root) {
        System.out.println("Starting analysis...");
        scopeStack.push(new Scope(root.getId())); // Initialize scope with the root's ID
        traverseTree(root); // Start traversal from the root of the parse tree
        System.out.println("Analysis complete.");
    }

    // Traverses the XML parse tree recursively
    private void traverseTree(Parser.XMLParseTree node) {
        if (node == null) return;

        processNode(node); // Process the current node

        for (Parser.XMLParseTree child : node.getChildren()) {
            traverseTree(child); // Recursively process child nodes
        }

        if (node.getTag().equals("return")) {
            scopeStack.pop(); // Exit scope when encountering a return statement
        }
    }

    // Processes an individual XML parse tree node
    private void processNode(Parser.XMLParseTree node) {
        String tag = node.getTag();
        String value = node.getValue();
        Scope currentScope = scopeStack.peek(); // Get the current scope
        System.out.println("Processing node with tag: " + tag + ", value: " + value);

        // Handle type declaration
        if (tag.equals("res_key") && (value.equals("text") || value.equals("num"))) {
            typeEncountered = value; // Set the current type to be encountered
        }

        // Handle variable declaration or usage
        if (isUserDefinedVariable(tag)) {
            handleVariable(node, value, currentScope);
        } 
        // Handle function declaration or usage
        else if (isUserDefinedFunction(tag)) {
            handleFunction(node, value, currentScope);
        }
    }

    // Handle variable declaration or usage
    private void handleVariable(Parser.XMLParseTree node, String value, Scope currentScope) {
        String declaredName = findDeclaredVariableName(value);

        // Check if the variable has already been declared
        if (declaredName != null) {
            if (typeEncountered != null) {
                throw new RuntimeException("Error: Variable '" + value + "' redeclared in scope " + currentScope.getId());
            }
            node.setValue(declaredName); // Use the previously declared variable name
            System.out.println("Using declared variable '" + declaredName + "' for '" + value + "' in scope " + currentScope.getId());
        } else {
            // If the variable has not been declared, and a type is encountered, declare it
            if (typeEncountered != null) {
                String uniqueName = generateUniqueName("v");
                symbolTable.put(uniqueName, new SymbolInfo(value, currentScope.getId(), typeEncountered, node.getId()));
                currentScope.addVariable(value, uniqueName); // Add variable to the current scope
                node.setValue(uniqueName); // Rename the variable in the parse tree
                System.out.println("Declared and renamed variable '" + value + "' to '" + uniqueName + "' in scope " + currentScope.getId());
                typeEncountered = null; // Clear the type after declaration
            } else {
                throw new RuntimeException("Error: Variable '" + value + "' used without declaration in scope " + currentScope.getId());
            }
        }
    }

    // Handle function declaration or usage
    private void handleFunction(Parser.XMLParseTree node, String value, Scope currentScope) {
        if (typeEncountered != null) {
            String uniqueName = generateUniqueName("f");
            symbolTable.put(uniqueName, new SymbolInfo(value, currentScope.getId(), typeEncountered, node.getId()));
            currentScope.addFunction(value, uniqueName); // Add function to the current scope
            node.setValue(uniqueName); // Rename the function in the parse tree
            scopeStack.push(new Scope(node.getId())); // Enter a new scope for the function body
            System.out.println("Declared and renamed function '" + value + "' to '" + uniqueName + "' in scope " + currentScope.getId());
            typeEncountered = null; // Clear the type after function declaration
        }
    }

    // Finds a declared variable in the current or parent scope
    private String findDeclaredVariableName(String variableName) {
        for (Scope scope : scopeStack) {
            String declaredName = scope.getVariable(variableName);
            if (declaredName != null) {
                return declaredName; // Return the unique name if found
            }
        }
        return null; // Return null if not found
    }

    // Determines if a tag represents a user-defined variable
    private boolean isUserDefinedVariable(String tag) {
        return tag.equals("tokenv"); // Adjust this condition based on your grammar
    }

    // Determines if a tag represents a user-defined function
    private boolean isUserDefinedFunction(String tag) {
        return tag.equals("tokenf"); // Adjust this condition based on your grammar
    }

    // Generates a unique name for variables or functions
    private String generateUniqueName(String prefix) {
        return prefix + uniqueIdCounter++; // Increment the counter for unique names
    }

    // Gets information about a symbol from the symbol table
    public SymbolInfo getSymbolInfo(String uniqueName) {
        return symbolTable.get(uniqueName);
    }

    // Prints the contents of the symbol table for debugging purposes
    public void printSymbolTable() {
        System.out.println("Symbol Table:");
        for (Map.Entry<String, SymbolInfo> entry : symbolTable.entrySet()) {
            SymbolInfo info = entry.getValue();
            System.out.println("Name: " + entry.getKey() + ", Original Name: " + info.getOriginalName() + 
                               ", Scope: " + info.getScopeId() + ", Type: " + info.getType() + 
                               ", Tree ID: " + info.getTreeId());
        }
    }

    // Class to store information about symbols in the symbol table
    public static class SymbolInfo {
        private String originalName; // The original name of the symbol (variable or function)
        private int scopeId; // The scope in which the symbol is declared
        private String type; // The type of the symbol (e.g., "text", "num")
        private int treeId; // The ID of the parse tree node where the symbol is declared

        public SymbolInfo(String originalName, int scopeId, String type, int treeId) {
            this.originalName = originalName;
            this.scopeId = scopeId;
            this.type = type;
            this.treeId = treeId;
        }

        public String getOriginalName() {
            return originalName;
        }

        public int getScopeId() {
            return scopeId;
        }

        public String getType() {
            return type;
        }

        public int getTreeId() {
            return treeId;
        }
    }

    // Class to represent a scope
    private static class Scope {
        private int id;
        private Map<String, String> variables = new HashMap<>();
        private Map<String, String> functions = new HashMap<>();

        public Scope(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void addVariable(String originalName, String uniqueName) {
            variables.put(originalName, uniqueName);
        }

        public String getVariable(String originalName) {
            return variables.get(originalName);
        }

        public void addFunction(String originalName, String uniqueName) {
            functions.put(originalName, uniqueName);
        }

        public String getFunction(String originalName) {
            return functions.get(originalName);
        }
    }
}