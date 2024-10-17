import java.util.HashMap;
import java.util.Map;

import java.util.*;

public class ScopeAnalyzer {
    private Map<String, SymbolInfo> symbolTable = new HashMap<>();
    private int uniqueIdCounter = 0;
    private boolean typeEncountered = false;
    private Deque<Integer> scopeStack = new ArrayDeque<>();

    public void analyze(Parser.XMLParseTree root) {
        System.out.println("Starting analysis...");
        scopeStack.push(root.getId());
        traverseTree(root);
        System.out.println("Analysis complete.");
    }

    private void traverseTree(Parser.XMLParseTree node) {
        if (node == null) return;

        processNode(node);

        for (Parser.XMLParseTree child : node.getChildren()) {
            traverseTree(child);
        }

        if (node.getTag().equals("return")) {
            scopeStack.pop();
        }
    }

    private void processNode(Parser.XMLParseTree node) {
        String tag = node.getTag();
        String value = node.getValue();
        int scopeId = scopeStack.peek();
        System.out.println("Processing node with tag: " + tag + ", value: " + value);

        if (tag.equals("res_key") && (value.equals("text") || value.equals("num"))) {
            typeEncountered = true;
        }

        if (isUserDefinedVariable(tag) && typeEncountered) {
            typeEncountered = false;
            String uniqueName = generateUniqueName("v");
            symbolTable.put(uniqueName, new SymbolInfo(value, scopeId));
            node.setValue(uniqueName); // Rename the variable in the tree
            System.out.println("Renamed variable '" + value + "' to '" + uniqueName + "'" + " in scope " + scopeId);
        } else if (isUserDefinedVariable(tag)) {
            // Check if the variable is already declared in the current scope or any parent scope
            String declaredName = findDeclaredVariableName(value, scopeId);
            if (declaredName != null) {
                node.setValue(declaredName); // Use the declared variable name
                System.out.println("Using declared variable '" + declaredName + "' for '" + value + "' in scope " + scopeId);
            } else {
                // Handle the case where the variable is not declared (optional)
                System.err.println("Error: Variable '" + value + "' used without declaration in scope " + scopeId);
            }
        } else if (isUserDefinedFunction(tag) && typeEncountered) {
            typeEncountered = false;
            String uniqueName = generateUniqueName("f");
            symbolTable.put(uniqueName, new SymbolInfo(value, scopeId));
            node.setValue(uniqueName); // Rename the function in the tree
            System.out.println("Renamed function '" + value + "' to '" + uniqueName + "'" + " in scope " + scopeId);
        }
    }

    private String findDeclaredVariableName(String variableName, int scopeId) {
        // Traverse the symbol table to find the variable declaration in the current or parent scopes
        for (Map.Entry<String, SymbolInfo> entry : symbolTable.entrySet()) {
            SymbolInfo info = entry.getValue();
            System.out.println("Checking variable '" + info.getOriginalName() + "' in scope " + info.getScopeId());
            if (info.getOriginalName().equals(variableName) && isInScope(info.getScopeId(), scopeId)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private boolean isInScope(int declarationScopeId, int currentScopeId) {
        // Implement logic to check if the declaration scope is a parent of the current scope
        // This depends on how you manage scope hierarchy in your symbol table
        // For simplicity, assuming scopeId is hierarchical (e.g., 1, 1.1, 1.1.1)
        return declarationScopeId <= currentScopeId;
    }

    private boolean isUserDefinedVariable(String tag) {
        // Implement logic to determine if the tag represents a user-defined variable
        return tag.equals("tokenv");
    }

    private boolean isUserDefinedFunction(String tag) {
        // Implement logic to determine if the tag represents a user-defined function
        return tag.equals("tokenf");
    }

    private String generateUniqueName(String prefix) {
        return prefix + uniqueIdCounter++;
    }

    public SymbolInfo getSymbolInfo(String uniqueName) {
        return symbolTable.get(uniqueName);
    }

    public static class SymbolInfo {
        private String originalName;
        private int scopeId;

        public SymbolInfo(String originalName, int scopeId) {
            this.originalName = originalName;
            this.scopeId = scopeId;
        }

        public String getOriginalName() {
            return originalName;
        }

        public int getScopeId() {
            return scopeId;
        }
    }
}