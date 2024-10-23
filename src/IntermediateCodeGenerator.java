import java.util.Map;
import java.util.HashMap;

public class IntermediateCodeGenerator {
    private Map<String, ScopeAnalyzer.SymbolInfo> symbolTable;
    private Parser.XMLParseTree rootNode;
    private Map<String, String> functionMap = new HashMap<>();
    int labelCounter, varCounter = 0;

    public IntermediateCodeGenerator(Map<String, ScopeAnalyzer.SymbolInfo> symbolTable, Parser.XMLParseTree rootNode) {
        this.symbolTable = symbolTable;
        this.rootNode = rootNode;
        this.functionMap.put("not", "!");
        this.functionMap.put("sqrt", "SQR");
        this.functionMap.put("or", "||");
        this.functionMap.put("and", "&&");
        this.functionMap.put("eq", "=");
        this.functionMap.put("grt", ">");
        this.functionMap.put("add", "+");
        this.functionMap.put("sub", "-");
        this.functionMap.put("mul", "*");
        this.functionMap.put("div", "/");
    }

    private String newVar() {
        return "t" + varCounter++;
    }

    private String newLabel() {
        return "L" + labelCounter++;
    }

    public String generateIntermediateCode() {
        return translatePROG(rootNode);
    }

    public String translatePROG(Parser.XMLParseTree node) {
        String aCode = translateALGO(node.getChild(2)); // ALGO is the third child
        // String fCode = translateFUNCTIONS(node.getChild(3)); // FUNCTIONS is the fourth child
        return aCode + "\nSTOP ";
        // + fCode;
    }

    private String translateALGO(Parser.XMLParseTree node) {
        // ALGO ::= begin INSTRUC end
        return translateINSTRUC(node.getChild(1)); // INSTRUC is the second child
    }

    private String translateINSTRUC(Parser.XMLParseTree node) {
        if (node.getChildren().isEmpty()) {
            return "\nREM END";
        }
        // Handle INSTRUC1 ::= COMMAND ; INSTRUC2
        StringBuilder result = new StringBuilder();
        Parser.XMLParseTree commandNode = node.getChild(0);
        result.append(translateCOMMAND(commandNode)).append("\n");
        result.append(translateINSTRUC(node.getChild(2)));
        return result.toString().trim();
    }

    private String translateCOMMAND(Parser.XMLParseTree node) {
        // Get the first child node which represents the actual command
        if (node.getChildren().isEmpty()) {
            return "";
        }
        Parser.XMLParseTree commandNode = node.getChild(0);
        String nodeName = commandNode.getTag();

        switch (nodeName) {
            case "res_key":
                switch (commandNode.getValue()) {
                    case "skip":
                        return "REM DO NOTHING";
                    case "halt":
                        return "STOP";
                    case "print":
                        return "PRINT " + translateATOMIC(node.getChild(1));
                    default:
                        throw new IllegalArgumentException("Unknown command: " + commandNode.getValue());
                }
            case "ASSIGN":
                return translateASSIGN(commandNode);
            case "CALL":
                return translateCALL(commandNode);
            case "BRANCH":
                return translateBRANCH(commandNode);
            default:
                throw new IllegalArgumentException("Unknown command: " + nodeName);
        }
    }

    private String translateASSIGN(Parser.XMLParseTree node) {
        if (node.getChild(2).getValue() != null && node.getChild(2).getValue().equals("input")) {
            return "INPUT " + translateVNAME(node.getChild(0));
        } else {
            String place = newVar();
            String x = translateVNAME(node.getChild(0));
            return translateTERM(node.getChild(2), place) + "\n" + x + " := " + place;
        }
    }

    private String translateCALL(Parser.XMLParseTree node) {
        String functionName = node.getChild(0).getChild(0).getValue();
        String p1 = translateATOMIC(node.getChild(2));
        String p2 = translateATOMIC(node.getChild(4));
        String p3 = translateATOMIC(node.getChild(6));
        return "CALL_" + functionName + "(" + p1 + "," + p2 + "," + p3 + ")";
    }

    private String translateBRANCH(Parser.XMLParseTree node) {
        String label1 = newLabel();
        String label2 = newLabel();
        String label3 = newLabel();
        String code1 = translateCOND(node.getChild(1), label1, label2);
        String code2 = translateALGO(node.getChild(3));
        String code3 = translateALGO(node.getChild(5));
        return code1 + "\nLABEL " + label1 + "\n" + code2 + "\nGOTO " + label3 + "\nLABEL " + label2 + "\n" + code3 + "\nLABEL " + label3;
    }

    private String translateCOND(Parser.XMLParseTree node, String labelT, String labelF) {
        // Handle SIMPLE and COMPOSIT conditions;
        String tag = node.getChild(0).getTag();
        if (tag.equals("SIMPLE")) {
            return translateSIMPLE(node.getChild(0), labelT, labelF);
        } else if (tag.equals("COMPOSIT")) {
            return translateCOMPOSIT(node.getChild(0), labelT, labelF);
        }
        throw new IllegalArgumentException("Unknown COND type: " + node.getTag());
    }

    private String translateSIMPLE(Parser.XMLParseTree node, String labelT, String labelF) {
        String t1 = newVar();
        String t2 = newVar();
        String binop = functionMap.get(node.getChild(0).getChild(0).getValue());
        String code1 = translateATOMIC(node.getChild(2), t1);
        String code2 = translateATOMIC(node.getChild(4), t2);
        return code1 + "\n" + code2 + "\nIF " + t1 + " " + binop + " " + t2 + " THEN " + labelT + " ELSE " + labelF;
    }

    private String translateCOMPOSIT(Parser.XMLParseTree node, String labelT, String labelF) {
        // Handle BINOP and UNOP cases
        if (node.getChild(0).getTag().equals("BINOP")) {
            return translateBINOP(node, labelT, labelF);
        } else if (node.getChild(0).getTag().equals("UNOP")) {
            return translateUNOP(node, labelT, labelF);
        }
        throw new IllegalArgumentException("Unknown COMPOSIT type: " + node.getChild(0).getTag());
    }

    private String translateBINOP(Parser.XMLParseTree node, String labelT, String labelF) {
        String binop = node.getChild(0).getChild(0).getValue();
        if (binop.equals("or")) {
            String arg2 = newLabel();
            String code1 = translateSIMPLE(node.getChild(2), labelT, arg2);
            String code2 = translateSIMPLE(node.getChild(4), labelT, labelF);
            return code1 + "\nLABEL " + arg2 + "\n" + code2;
        }
        if (binop.equals("and")) {
            String arg2 = newLabel();
            String code1 = translateSIMPLE(node.getChild(2), arg2, labelF);
            String code2 = translateSIMPLE(node.getChild(4), labelT, labelF);
            return code1 + "\nLABEL " + arg2 + "\n" + code2;
        } else {
            throw new IllegalArgumentException("Unknown BINOP type: " + binop);
        }
    }

    private String translateUNOP(Parser.XMLParseTree node, String labelT, String labelF) {
        String unop = node.getChild(0).getChild(0).getValue();
        if (unop.equals("not")) {
            return translateCOND(node.getChild(2), labelF, labelT);
        } else {
            throw new IllegalArgumentException("Unknown UNOP type: " + unop);
        }
    }

    private String translateATOMIC(Parser.XMLParseTree node, String place) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid ATOMIC node");
        }
        node = node.getChild(0);
        if (node.getTag().equals("VNAME")) {
            return place + " := " + translateVNAME(node);
        } else if (node.getTag().equals("CONST")) {
            return place + " := " + node.getChild(0).getValue();
        }
        throw new IllegalArgumentException("Invalid ATOMIC node" + node.getValue());
    }

    private String translateATOMIC(Parser.XMLParseTree node) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid ATOMIC node");
        }
        node = node.getChild(0);
        if (node.getTag().equals("VNAME")) {
            return translateVNAME(node);
        } else if (node.getTag().equals("CONST")) {
            return node.getChild(0).getValue();
        }
        throw new IllegalArgumentException("Invalid ATOMIC node" + node.getValue());
    }

    private String translateVNAME(Parser.XMLParseTree node) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid VNAME node");
        }
        String vname = node.getChild(0).getValue();
        if (!symbolTable.containsKey(vname)) {
            throw new IllegalArgumentException("Variable not found in symbol table: " + vname);
        }
        return vname;
    }

    private String translateTERM(Parser.XMLParseTree node, String place) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid TERM node");
        }
        String tag = node.getChild(0).getTag();
        switch (tag) {
            case "ATOMIC":
                return translateATOMIC(node.getChild(0), place);
            case "CALL":
                return translateCALL(node.getChild(0));
            case "OP":
                return translateOP(node.getChild(0), place);
            default:
                throw new IllegalArgumentException("Unknown TERM type: " + tag);
        }
    }

    private String translateOP(Parser.XMLParseTree node, String place) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid OP node");
        }
        String opName = node.getChild(0).getTag();
        if (opName.equals("UNOP")) {
            String place1 = newVar();
            String unop = functionMap.get(node.getChild(0).getChild(0).getValue());
            String arg = translateARG(node.getChild(2), place1);
            return arg + "\n" + place + " := " + unop + " " + place1;
        }
        if (opName.equals("BINOP")) {
            String place1 = newVar();
            String place2 = newVar();
            String binop = functionMap.get(node.getChild(0).getChild(0).getValue());
            String arg1 = translateARG(node.getChild(2), place1);
            String arg2 = translateARG(node.getChild(4), place2);
            return arg1 + "\n" + arg2 + "\n" + place + " := " + place1 + " " + binop + " " + place2;
        } else {
            throw new IllegalArgumentException("Unknown OP type: " + opName);
        }
    }

    private String translateARG(Parser.XMLParseTree node, String place) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid ARG node");
        }
        if (node.getChild(0).getTag().equals("ATOMIC")) {
            return translateATOMIC(node.getChild(0), place);
        } else {
            return translateOP(node.getChild(0), place);
        }
    }
}
