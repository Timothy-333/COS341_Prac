import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
public class ScopeAnalyzer {
    private Map<String, SymbolInfo> symbolTable = new HashMap<>();
    private int uniqueIdCounter = 0;
    private String typeEncountered = null; // Tracks the current type (e.g., "text" or "num") being processed
    private Deque<Scope> scopeStack = new ArrayDeque<>();

    // Entry point for analysis
        public void analyze(Parser.XMLParseTree root) {
        System.out.println("Starting analysis...");
        scopeStack.push(new Scope(root.getId(), "F_main")); // Initialize scope with the root's ID
        traverseTree(root); // Start traversal from the root of the parse tree
    
        // Check for function calls without declarations
        for (Scope scope : scopeStack) {
            if (!scope.getCallsWithoutDeclarations().isEmpty()) {
                throw new RuntimeException("Error: Function '" + scope.getName() + "' called without declaration in scope " + scope.getId());
            }
        }
    
        // Check for conflicts between variable names and function names
        checkForNameConflicts();
    
        System.out.println("Analysis complete.");
    }
    
    private void checkForNameConflicts() {
        Map<String, String> variableNames = new HashMap<>();
        Map<String, String> functionNames = new HashMap<>();
    
        // Separate variable and function names
        for (Map.Entry<String, SymbolInfo> entry : symbolTable.entrySet()) {
            SymbolInfo info = entry.getValue();
            if (entry.getKey().startsWith("v")) {
                variableNames.put(info.getOriginalName().substring(2), entry.getKey());
            } else if (entry.getKey().startsWith("f")) {
                functionNames.put(info.getOriginalName().substring(2), entry.getKey());
            }
        }
        // Check for conflicts
        for (String varName : variableNames.keySet()) {
            System.out.println("Checking for conflicts with variable '" + varName + "'");
            if (functionNames.containsKey(varName)) {
                throw new RuntimeException("Error: Name conflict between variable '" + varName + "' and function '" + varName + "'");
            }
        }
    }

    // Traverses the XML parse tree recursively
    private void traverseTree(Parser.XMLParseTree node) {
        if (node == null) return;

        processNode(node); // Process the current node

        for (Parser.XMLParseTree child : node.getChildren()) {
            traverseTree(child); // Recursively process child nodes
        }

        if (node.getTag().equals("return")) {
            if(!scopeStack.peek().getCallsWithoutDeclarations().isEmpty()) {
                throw new RuntimeException("Error: Function '" + scopeStack.peek().getName() + "' called without declaration in scope " + scopeStack.peek().getId());
            }
            scopeStack.pop(); // Exit scope when encountering a return statement
        }
    }

    // Processes an individual XML parse tree node
    private void processNode(Parser.XMLParseTree node) {
        String tag = node.getTag();
        String value = node.getValue();
        if (tag.equals("res_key") && (value.equals("text") || value.equals("num"))) {
            typeEncountered = value; // Set the current type to be encountered
        }
        if (tag.equals("tokenv") || tag.equals("tokenf")) {
            Scope currentScope = scopeStack.peek(); // Get the current scope
            System.out.println("Processing node with tag: " + tag + ", value: " + value);

            if (tag.equals("tokenv")) {
                handleVariable(node, value, currentScope);
            } 
            // Handle function declaration or usage
            else if (tag.equals("tokenf")) {
                handleFunction(node, value, currentScope);
            }
        }
    }

    // Handle variable declaration or usage
    private void handleVariable(Parser.XMLParseTree node, String value, Scope currentScope) {
        String declaredName = findDeclaredVariableNameInScopes(value);
        if(Lexer.isKeyword(value.substring(2))) {
            throw new RuntimeException("Error: Variable '" + value + "' is a keyword in scope " + currentScope.getId());
        }
        // Check if the variable has already been declared
        if (declaredName != null) {
            if (typeEncountered != null && findDeclaredVariableNameInScope(value, currentScope) != null) {
                throw new RuntimeException("Error: Variable '" + value + "' redeclared in scope " + currentScope.getId());
            }
            System.out.println("Using declared variable '" + declaredName + "' for '" + value + "' in scope " + currentScope.getId());
            symbolTable.get(declaredName).addTreeId(node.getId()); // Add the tree ID to the symbol's list
        } else {
            // If the variable has not been declared, and a type is encountered, declare it
            if (typeEncountered != null) {
                String uniqueName = generateUniqueName("v");
                symbolTable.put(uniqueName, new SymbolInfo(value, currentScope.getId(), typeEncountered, node.getId()));
                currentScope.addVariable(value, uniqueName); // Add variable to the current scope
                System.out.println("Declared variable '" + value + "' to '" + uniqueName + "' in scope " + currentScope.getId());
                typeEncountered = null; // Clear the type after declaration
            } else {
                throw new RuntimeException("Error: Variable '" + value + "' used without declaration in scope " + currentScope.getId());
            }
        }
    }

    private void handleFunction(Parser.XMLParseTree node, String value, Scope currentScope) {
        if (typeEncountered != null) {
            if (findDeclaredFunctionNameInScope(value, currentScope) != null || scopeStack.peek().getName().equals(value)) {
                throw new RuntimeException("Error: Function '" + value + "' redeclared in scope " + currentScope.getId());
            }
            String uniqueName = generateUniqueName("f");
            symbolTable.put(uniqueName, new SymbolInfo(value, currentScope.getId(), typeEncountered, node.getId()));
            currentScope.addFunction(value, uniqueName); // Add function to the current scope
            List<Parser.XMLParseTree> callsWithoutDeclarations = currentScope.getCallsWithoutDeclarations(value);
            for (Parser.XMLParseTree call : callsWithoutDeclarations) {
                symbolTable.get(uniqueName).addTreeId(call.getId());
                currentScope.removeCallWithoutDeclaration(call);
            }
            scopeStack.push(new Scope(node.getId(), value)); // Enter a new scope for the function body
            System.out.println("Declared and renamed function '" + value + "' to '" + uniqueName + "' in scope " + currentScope.getId());
            typeEncountered = null; // Clear the type after function declaration
        }
        else {
            String declaredName = findDeclaredFunctionNameInScope(value, currentScope);
            // Check if the function has already been declared in the current scope
            if (declaredName != null) {
                System.out.println("Using declared function '" + declaredName + "' for '" + value + "' in scope " + currentScope.getId());
                symbolTable.get(declaredName).addTreeId(node.getId()); // Add the tree ID to the symbol's list
            }
            //Recursive case
            if (scopeStack.peek().getName().equals(value) && !value.equals("F_main")) {
                System.out.println("Using declared function '" + value + "' for '" + value + "' in scope " + currentScope.getId());
                Scope parentScope = scopeStack.pop();
                String parentUniqueName = scopeStack.peek().getFunction(value);
                scopeStack.push(parentScope);
                symbolTable.get(parentUniqueName).addTreeId(node.getId());
            }
            else {
                currentScope.addCallWithoutDeclaration(node);
            }
        }
    }

    private String findDeclaredVariableNameInScopes(String originalName) {
        // Iterate over the scope stack in reverse order
        Iterator<Scope> iterator = scopeStack.descendingIterator();
        while (iterator.hasNext()) {
            Scope scope = iterator.next();
            String uniqueName = findDeclaredVariableNameInScope(originalName, scope);
            if (uniqueName != null) {
                return uniqueName;
            }
        }
        return null;
    }
    private String findDeclaredVariableNameInScope(String originalName, Scope scope) {
        return scope.getVariable(originalName);
    }
    private String findDeclaredFunctionNameInScope(String originalName, Scope scope) {
        return scope.getFunction(originalName);
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
                               ", Tree ID: " + info.getTreeIds() + ", Declaration ID: " + info.getDeclarationID());
        }
    }

    // Class to store information about symbols in the symbol table
    public static class SymbolInfo {
        private String originalName; // The original name of the symbol (variable or function)
        private int scopeId; // The scope in which the symbol is declared
        private String type; // The type of the symbol (e.g., "text", "num")
        private int declarationID; // The ID of the declaration node
        private List<Integer> treeIds; // The tree IDs where the symbol is used

        public SymbolInfo(String originalName, int scopeId, String type, int treeId) {
            this.originalName = originalName;
            this.scopeId = scopeId;
            this.type = type;
            this.treeIds = new ArrayList<>();
            declarationID = treeId;
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

        public List<Integer> getTreeIds() {
            return treeIds;
        }
        public int getDeclarationID() {
            return declarationID;
        }
        public void addTreeId(int treeId) {
            treeIds.add(treeId);
        }
    }

    // Class to represent a scope
    private static class Scope {
        private int id;
        private String name;
        private List<Parser.XMLParseTree> callsWithoutDeclarations = new ArrayList<>();
        private Map<String, String> variables = new HashMap<>();
        private Map<String, String> functions = new HashMap<>();

        public Scope(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }
        public String getName() {
            return name;
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
        public void addCallWithoutDeclaration(Parser.XMLParseTree node) {
            callsWithoutDeclarations.add(node);
        }
        public void removeCallWithoutDeclaration(Parser.XMLParseTree node) {
            callsWithoutDeclarations.remove(node);
        }
        public List<Parser.XMLParseTree> getCallsWithoutDeclarations() {
            return callsWithoutDeclarations;
        }
        public List<Parser.XMLParseTree> getCallsWithoutDeclarations(String name) {
            List<Parser.XMLParseTree> calls = new ArrayList<>();
            for (Parser.XMLParseTree call : callsWithoutDeclarations) {
                if (call.getValue().equals(name)) {
                    calls.add(call);
                }
            }
            return calls;
        }
    }
}