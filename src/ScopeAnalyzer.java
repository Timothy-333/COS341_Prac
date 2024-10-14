

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ScopeAnalyzer {
    private SymbolTable symbolTable = new SymbolTable();

    public void analyze(Parser.XMLParseTree root) {
        symbolTable.enterScope(); // Enter global scope
        traverseTree(root);
        symbolTable.exitScope(); // Exit global scope
    }

    private void traverseTree(Parser.XMLParseTree node) {
        switch (node.getTag()) {
            case "PROG":
                // Global program scope
                symbolTable.enterScope();
                break;
            case "FUNCTION":
                // New function scope
                symbolTable.enterScope();
                String functionName = node.getValue(); // Assuming function name is stored in value
                symbolTable.declareSymbol(functionName, "function");
                break;
            case "DECL":
                // Variable declaration
                String varName = node.getValue(); // Assuming variable name is stored in value
                String varType = getVarType(node); // You'll need to implement getVarType based on the node structure
                symbolTable.declareSymbol(varName, varType);
                break;
            case "CALL":
                // Function call
                String calledFunction = node.getValue(); // Assuming function call name is stored in value
                if (symbolTable.lookupSymbol(calledFunction) == null) {
                    throw new RuntimeException("Function " + calledFunction + " not declared");
                }
                break;
            // Add other cases as needed
        }

        for (Parser.XMLParseTree child : node.getChildren()) {
            traverseTree(child);
        }

        // Exit scope at the end of blocks or functions
        if (node.getTag().equals("PROG") || node.getTag().equals("FUNCTION")) {
            symbolTable.exitScope();
        }
    }

    private String getVarType(Parser.XMLParseTree node) {
        // Logic to determine the variable type based on node's structure
        // This could involve checking the tag or an attribute in the XML node
        return node.getAttribute("type");
    }
}

class Scope {
    private Map<String, String> symbols = new HashMap<>();
    private Scope parentScope;

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public void declareSymbol(String name, String type) {
        symbols.put(name, type);
    }

    public String lookupSymbol(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        } else if (parentScope != null) {
            return parentScope.lookupSymbol(name);
        } else {
            return null;
        }
    }
}

class SymbolTable {
    private Stack<Scope> scopeStack = new Stack<>();

    public void enterScope() {
        Scope newScope = new Scope(currentScope());
        scopeStack.push(newScope);
    }

    public void exitScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
        }
    }

    public void declareSymbol(String name, String type) {
        if (!scopeStack.isEmpty()) {
            scopeStack.peek().declareSymbol(name, type);
        }
    }

    public String lookupSymbol(String name) {
        if (!scopeStack.isEmpty()) {
            return scopeStack.peek().lookupSymbol(name);
        }
        return null;
    }

    private Scope currentScope() {
        return scopeStack.isEmpty() ? null : scopeStack.peek();
    }
}