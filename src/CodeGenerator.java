import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator {
    private Map<String, ScopeAnalyzer.SymbolInfo> symbolTable;
    private XMLParseTree rootNode;
    private Map<String, String> functionMap = new HashMap<>();
    int labelCounter, varCounter = 0;

    public CodeGenerator(Map<String, ScopeAnalyzer.SymbolInfo> symbolTable, XMLParseTree rootNode) {

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

    public String generateIntermediateCode(boolean withFunctions) {
        return translatePROG(rootNode, withFunctions);
    }
    private String translatePROG(XMLParseTree node, boolean withFunctions) {
        String aCode = translateALGO(node.getChild(2)); // ALGO is the third child
        String out = aCode + "\nSTOP";
        if (withFunctions) {
            String fCode = translateFUNCTIONS(node.getChild(3));
            out += "\n" + fCode;
        }
        return out;
    }

    private String translateALGO(XMLParseTree node) {
        // ALGO ::= begin INSTRUC end
        return translateINSTRUC(node.getChild(1)); // INSTRUC is the secondschild
    }

    private String translateINSTRUC(XMLParseTree node) {
        if (node.getChildren().isEmpty()) {
            return "REM END";
        }
        // Handle INSTRUC1 ::= COMMAND ; INSTRUC2
        StringBuilder result = new StringBuilder();
        XMLParseTree commandNode = node.getChild(0);
        result.append(translateCOMMAND(commandNode)).append("\n");
        result.append(translateINSTRUC(node.getChild(2)));
        return result.toString().trim();
    }

    private String translateCOMMAND(XMLParseTree node) {
        // Get the first child node which represents the actual command
        if (node.getChildren().isEmpty()) {
            return "";
        }
        XMLParseTree commandNode = node.getChild(0);
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
                    case "return":
                        return "RETURN " + translateATOMIC(node.getChild(1));
                    default:
                        throw new IllegalArgumentException("Unknown command: " + commandNode.getValue());
                }
            case "ASSIGN":
                return translateASSIGN(commandNode);
            case "CALL":
                return translateCALL(commandNode, null);
            case "BRANCH":
                return translateBRANCH(commandNode);
            default:
                throw new IllegalArgumentException("Unknown command: " + nodeName);
        }
    }

    private String translateASSIGN(XMLParseTree node) {
        if (node.getChild(2).getValue() != null && node.getChild(2).getValue().equals("input")) {
            return "INPUT " + translateVNAME(node.getChild(0));
        } else {
            String place = newVar();
            String x = translateVNAME(node.getChild(0));
            return translateTERM(node.getChild(2), place) + "\n" + x + " := " + place;
        }
    }

    private String translateCALL(XMLParseTree node, String place) {
        String functionName = node.getChild(0).getChild(0).getValue();
        String p1 = translateATOMIC(node.getChild(2));
        String p2 = translateATOMIC(node.getChild(4));
        String p3 = translateATOMIC(node.getChild(6));
        String out = "";
        if (place != null) {
            out += place + " := ";
        }
        out += "CALL_" + functionName + "(" + p1 + "," + p2 + "," + p3 + ")";
        return out;
    }

    private String translateBRANCH(XMLParseTree node) {
        String label1 = newLabel();
        String label2 = newLabel();
        String label3 = newLabel();
        String code1 = translateCOND(node.getChild(1), label1, label2);
        String code2 = translateALGO(node.getChild(3));
        String code3 = translateALGO(node.getChild(5));
        return code1 + "\n\nLABEL " + label1 + "\n" + code2 + "\nGOTO " + label3 + "\n\nLABEL " + label2 + "\n" + code3 + "\n\nLABEL " + label3;
    }

    private String translateCOND(XMLParseTree node, String labelT, String labelF) {
        // Handle SIMPLE and COMPOSIT conditions;
        String tag = node.getChild(0).getTag();
        if (tag.equals("SIMPLE")) {
            return translateSIMPLE(node.getChild(0), labelT, labelF);
        } else if (tag.equals("COMPOSIT")) {
            return translateCOMPOSIT(node.getChild(0), labelT, labelF);
        }
        throw new IllegalArgumentException("Unknown COND type: " + node.getTag());
    }

    private String translateSIMPLE(XMLParseTree node, String labelT, String labelF) {
        String t1 = newVar();
        String t2 = newVar();
        String binop = functionMap.get(node.getChild(0).getChild(0).getValue());
        String code1 = translateATOMIC(node.getChild(2), t1);
        String code2 = translateATOMIC(node.getChild(4), t2);
        return code1 + "\n" + code2 + "\nIF " + t1 + " " + binop + " " + t2 + " THEN " + labelT + " ELSE " + labelF;
    }

    private String translateCOMPOSIT(XMLParseTree node, String labelT, String labelF) {
        // Handle BINOP and UNOP cases
        if (node.getChild(0).getTag().equals("BINOP")) {
            return translateBINOP(node, labelT, labelF);
        } else if (node.getChild(0).getTag().equals("UNOP")) {
            return translateUNOP(node, labelT, labelF);
        }
        throw new IllegalArgumentException("Unknown COMPOSIT type: " + node.getChild(0).getTag());
    }

    private String translateBINOP(XMLParseTree node, String labelT, String labelF) {
        String binop = node.getChild(0).getChild(0).getValue();
        if (binop.equals("or")) {
            String arg2 = newLabel();
            String code1 = translateSIMPLE(node.getChild(2), labelT, arg2);
            String code2 = translateSIMPLE(node.getChild(4), labelT, labelF);
            return code1 + "\n\nLABEL " + arg2 + "\n" + code2;
        }
        if (binop.equals("and")) {
            String arg2 = newLabel();
            String code1 = translateSIMPLE(node.getChild(2), arg2, labelF);
            String code2 = translateSIMPLE(node.getChild(4), labelT, labelF);
            return code1 + "\n\nLABEL " + arg2 + "\n" + code2;
        } else {
            throw new IllegalArgumentException("Unknown BINOP type: " + binop);
        }
    }

    private String translateUNOP(XMLParseTree node, String labelT, String labelF) {
        String unop = node.getChild(0).getChild(0).getValue();
        if (unop.equals("not")) {
            return translateSIMPLE(node.getChild(2), labelF, labelT);
        } else {
            throw new IllegalArgumentException("Unknown UNOP type: " + unop);
        }
    }

    private String translateATOMIC(XMLParseTree node, String place) {
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

    private String translateATOMIC(XMLParseTree node) {
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

    private String translateVNAME(XMLParseTree node) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid VNAME node");
        }
        String vname = node.getChild(0).getValue();
        if (!symbolTable.containsKey(vname)) {
            throw new IllegalArgumentException("Variable not found in symbol table: " + vname);
        }
        return vname;
    }

    private String translateTERM(XMLParseTree node, String place) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid TERM node");
        }
        String tag = node.getChild(0).getTag();
        switch (tag) {
            case "ATOMIC":
                return translateATOMIC(node.getChild(0), place);
            case "CALL":
                return translateCALL(node.getChild(0), place);
            case "OP":
                return translateOP(node.getChild(0), place);
            default:
                throw new IllegalArgumentException("Unknown TERM type: " + tag);
        }
    }

    private String translateOP(XMLParseTree node, String place) {
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

    private String translateARG(XMLParseTree node, String place) {
        if (node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Invalid ARG node");
        }
        if (node.getChild(0).getTag().equals("ATOMIC")) {
            return translateATOMIC(node.getChild(0), place);
        } else {
            return translateOP(node.getChild(0), place);
        }
    }

    // Function translation
    private String translateFUNCTIONS(XMLParseTree node) {
        if (node.getChildren().isEmpty()) {
            return "REM END";
        }
        String dCode = translateDECL(node.getChild(0));
        String fCode = translateFUNCTIONS(node.getChild(1));
        return dCode + "\nSTOP\n" + fCode;
    }
    
    private String translateDECL(XMLParseTree node) {
        String header = translateHEADER(node.getChild(0));
        String body = translateBODY(node.getChild(1));
        return header + "\n" + body;
    }
    
    private String translateHEADER(XMLParseTree node) {
        // HEADER ::= FTYP FNAME( VNAME1 , VNAME2 , VNAME3 )
        // FTYP and VNAMEs are ignored for translation purposes
        String fName = translateVNAME(node.getChild(1));
        String v1 = translateVNAME(node.getChild(3));
        String v2 = translateVNAME(node.getChild(5));
        String v3 = translateVNAME(node.getChild(7));
        return "\nFUNC " + fName + "(" + v1 + "," + v2 + "," + v3 + ")";
    }
    
    private String translateBODY(XMLParseTree node) {
        String pCode = translatePROLOG(node.getChild(0));
        String aCode = translateALGO(node.getChild(2));
        String eCode = translateEPILOG(node.getChild(3));
        String sCode = translateSUBFUNCS(node.getChild(4));
        return pCode + "\n" + aCode + "\n" + eCode + "\n" + sCode;
    }
    
    private String translatePROLOG(XMLParseTree node) {
        // Assuming inlining method
        return "REM BEGIN";
    }
    
    private String translateEPILOG(XMLParseTree node) {
        // Assuming inlining method
        return "REM END";
    }
    
    private String translateSUBFUNCS(XMLParseTree node) {
        return translateFUNCTIONS(node.getChild(0));
    }
    public static String translateToBasic(String intermediateCode) {
        String[] lines = intermediateCode.split("\n");
        List<String> basicCode = new ArrayList<>();
        Stack<String> functionStack = new Stack<>();
    
        for (String line : lines) {
            line = line.trim();
    
            // Translate function declarations
            if (line.startsWith("FUNC ")) {
                basicCode.add("FUNCTION " + line.substring(5));
                functionStack.push(line.substring(5, line.indexOf("(")));
            } 
            
            // Translate assignments
            else if (line.contains(" := ")) {
                String[] parts = line.split(" := ");
                if(parts[1].contains("CALL_")) {
                    String functionName = parts[1].substring(5, parts[1].indexOf("("));
                    basicCode.add(parts[0] + " = " + functionName + "(" + parts[1].substring(parts[1].indexOf("(") + 1, parts[1].indexOf(")")) + ")");
                }
                else {
                    basicCode.add(parts[0] + " = " + parts[1]);
                }
            } 
            
            // Translate function calls
            else if (line.startsWith("CALL_")) {
                // Do nothing
            } 
            
            // Translate labels
            else if (line.startsWith("LABEL ")) {
                basicCode.add(line.replace("LABEL ", "") + ":");
            }
            
            // Translate IF-THEN-ELSE statements
            else if (line.startsWith("IF ")) {
                basicCode.add(line.replace(" THEN ", " THEN GOTO ").replace(" ELSE ", " ELSE GOTO "));
            }
            
            // Translate RETURN
            else if (line.startsWith("RETURN ")) {
                if (!functionStack.isEmpty()) {
                    String functionName = functionStack.peek();
                    basicCode.add(functionName + " = " + line.substring(7));
                }
            } 
            
            // Translate STOP
            else if (line.equals("STOP")) {
                basicCode.add("END");
                if (!functionStack.isEmpty()) {
                    String functionName = functionStack.pop();
                    basicCode.add("END FUNCTION ");
                }
            } 
            
            // Translate PRINT statements
            else if (line.startsWith("PRINT ")) {
                basicCode.add("PRINT " + line.substring(6));
            }
            
            // REM statements remain the same
            else if (line.startsWith("REM ")) {
                basicCode.add(line);
            }
        }
        
        return String.join("\n", basicCode);
    }    
}
