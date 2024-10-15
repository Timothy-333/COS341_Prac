import java.util.HashMap;
import java.util.Map;

public class ScopeAnalyzer {
    private Map<String, SymbolInfo> symbolTable = new HashMap<>();
    private int uniqueIdCounter = 0;

    public void analyze(Parser.XMLParseTree root) {
        System.out.println("Starting analysis...");
        traverseTree(root, 0); // Start with the global scope (scope ID 0)
        System.out.println("Analysis complete.");
    }

    private void traverseTree(Parser.XMLParseTree node, int scopeId) {
        if (node == null) return;

        // Process the current node
        processNode(node, scopeId);

        // Recursively process the children
        for (Parser.XMLParseTree child : node.getChildren()) {
            traverseTree(child, scopeId); // Pass the current scope ID to children
        }
    }

    private void processNode(Parser.XMLParseTree node, int scopeId) {
        String tag = node.getTag();
        String value = node.getValue();

        System.out.println("Processing node with tag: " + tag + ", value: " + value);

        if (isUserDefinedVariable(tag)) {
            String uniqueName = generateUniqueName("v");
            symbolTable.put(uniqueName, new SymbolInfo(tag, value, scopeId));
            node.setValue(uniqueName); // Rename the variable in the tree
            System.out.println("Renamed variable '" + value + "' to '" + uniqueName + "'");
        } else if (isUserDefinedFunction(tag)) {
            String uniqueName = generateUniqueName("f");
            symbolTable.put(uniqueName, new SymbolInfo(tag, value, scopeId));
            node.setValue(uniqueName); // Rename the function in the tree
            System.out.println("Renamed function '" + value + "' to '" + uniqueName + "'");
        }
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
        private String uniqueName;
        private int scopeId;

        public SymbolInfo(String originalName, String uniqueName, int scopeId) {
            this.originalName = originalName;
            this.uniqueName = uniqueName;
            this.scopeId = scopeId;
        }

        public String getOriginalName() {
            return originalName;
        }

        public String getUniqueName() {
            return uniqueName;
        }

        public int getScopeId() {
            return scopeId;
        }
    }
}