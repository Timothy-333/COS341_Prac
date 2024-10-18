import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.io.*;

public class Parser {
    public List<TokenClass> tokenList;
    public  XMLParseTree root;
    public static int id;
    public Parser(List<TokenClass> tokenList) {
        this.tokenList = tokenList;
    }

    public void parse() {
        Stack<Integer> stateStack = new Stack<>();
        Stack<Object> semanticStack = new Stack<>();
        Stack<XMLParseTree> treeStack = new Stack<>();
        stateStack.push(0); // Initial state
        // treeStack.push(root);
    
        int index = 0;
        try {
            while (true) {
                TokenClass token = index < tokenList.size() ? tokenList.get(index) : new TokenClass(Lexer.TokenClassType.$, "$"); // Use a special EOF token when out of tokens
                int state = stateStack.peek();
                String action = getAction(state, token);
    
                if (action == null) {
                    throw new RuntimeException("Syntax error at token: " + token + " in state: " + state);
                } else if (action.startsWith("s")) {
                    // Shift action
                    int nextState = Integer.parseInt(action.substring(1));
                    stateStack.push(nextState);
                    semanticStack.push(token); // Push the token onto the semantic stack
                    
                    XMLParseTree leaf = new XMLParseTree(token.getAbsoluteType(),id++);
                    
                    leaf.setValue(token.getWord()); //Set Value of Leaf
                    treeStack.push(leaf); //Add to Tree Stack
                    index++;
                } else if (action.startsWith("r")) {
                    // Reduce action
                    int ruleNumber = Integer.parseInt(action.substring(1));
                    if (ruleNumber < 0 || ruleNumber >= rules.size()) {
                        throw new RuntimeException("Invalid rule number: " + ruleNumber);
                    }
                    Rule rule = rules.get(ruleNumber);
                    XMLParseTree node = new XMLParseTree(rule.getLhs(),id++); // Create a new node for the rule
                    
                    if (!rule.getRhs().get(0).equals("")) {
                        List<XMLParseTree> children = new ArrayList<>();
                        for (int i = 0; i < rule.getRhs().size(); i++) {
                            stateStack.pop();
                            semanticStack.pop(); // Pop the semantic stack
                            children.add(treeStack.pop()); // Pop Tree Stack and Add to Children List
                        }
                        //Reversiing Stack to be in correct order
                        for (int i = children.size() - 1; i >= 0; i--) {
                            node.addChild(children.get(i));                  
                        }
                        
                        
                        
                    }
                    int newState = stateStack.peek();
                    int gotoState = getGoto(newState, rule.getLhs());
                    if (gotoState == -1) {
                        throw new RuntimeException("Goto state not found for non-terminal: " + rule.getLhs());
                    }
                    stateStack.push(gotoState);
                    // Push the result of the reduction onto the semantic stack and tree stack
                    treeStack.push(node);          
                    semanticStack.push(rule.getLhs());


                } else if (action.equals("acc")) {
                    // Accept action
                    System.out.println("Parsing completed successfully.\n");
                    this.root = new XMLParseTree("PROG",id++);
                    
                    Stack<XMLParseTree> temp = new Stack<>();
                    while (!treeStack.isEmpty()) {

                        temp.add(treeStack.pop());
                    }

                    while (!temp.isEmpty()) {
                        this.root.addChild(temp.pop());
                    }

                    TypeChecker t = new TypeChecker();
                    if (t.typeCheck(this.root)){
                        System.out.println("TERRIFIC!");
                    }else{
                        System.out.println("TERRIBLE!");};

                    String xmlFileHeader  = "<? xml=\"1.0\" encoding=\"UTF-8\" ?>\n";
                    String xmlFileBody = root.toString();
                    String xmlFile = xmlFileHeader + xmlFileBody;

                    System.out.println("XML Parse Tree:");
                    System.out.println(xmlFile);

                    writeToFile("result.xml",xmlFile);
                
                    return;
                } else {
                    throw new RuntimeException("Unknown action: " + action);
                }
                System.out.println("Token: " + token + ", Action: " + action + ", State Stack: " + stateStack + ", Semantic Stack: " + semanticStack);
            }
        } catch (Exception e) {
            System.err.println("Error during parsing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getAction(int state, TokenClass token) {
        int columnIndex = getColumnIndex(token);
        return SLRParseTable.PARSE_TABLE[state][columnIndex];
    }

    private int getColumnIndex(TokenClass token) {
        for (int i = 0; i < SLRParseTable.HEADERS.length; i++) {
            if (SLRParseTable.HEADERS[i].equals(token.getType())) {
                return i;
            }
        }
        throw new RuntimeException("Invalid token type: " + token.getType());
    }

    private int getGoto(int state, String nonTerminal) {
        int columnIndex = getColumnIndex(nonTerminal);
        System.out.println("State: " + state + ", Non-terminal: " + nonTerminal + ", Column index: " + columnIndex);
        String value = SLRParseTable.PARSE_TABLE[state][columnIndex];
        if (value == null) {
            // Handle nullable non-terminal case
            System.out.println("Goto value is null for non-terminal: " + nonTerminal + " in state: " + state);
            return -1; // Return a default state or handle it as needed
        }
        try {
            return (int) Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid goto value: " + value + " for non-terminal: " + nonTerminal + " in state: " + state, e);
        }
    }

    private int getColumnIndex(String nonTerminal) {
        for (int i = 0; i < SLRParseTable.HEADERS.length; i++) {
            if (SLRParseTable.HEADERS[i].equals(nonTerminal)) {
                return i;
            }
        }
        throw new RuntimeException("Invalid non-terminal: " + nonTerminal);
    }

    private static class Rule {
        private String lhs;
        private List<String> rhs;

        public Rule(String lhs, List<String> rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public String getLhs() {
            return lhs;
        }

        public List<String> getRhs() {
            return rhs;
        }
    }



    private static final List<Rule> rules = new ArrayList<>(Arrays.asList(
        new Rule("PROG", Arrays.asList("main", "GLOBVARS", "ALGO", "FUNCTIONS")),
        new Rule("GLOBVARS", Arrays.asList("")),
        new Rule("GLOBVARS", Arrays.asList("VTYP", "VNAME", ",", "GLOBVARS")),
        new Rule("VTYP", Arrays.asList("num")),
        new Rule("VTYP", Arrays.asList("text")),
        new Rule("VNAME", Arrays.asList("tokenv")),
        new Rule("ALGO", Arrays.asList("begin", "INSTRUC", "end")),
        new Rule("INSTRUC", Arrays.asList("")),
        new Rule("INSTRUC", Arrays.asList("COMMAND", ";", "INSTRUC")),
        new Rule("COMMAND", Arrays.asList("skip")),
        new Rule("COMMAND", Arrays.asList("halt")),
        new Rule("COMMAND", Arrays.asList("print", "ATOMIC")),
        new Rule("COMMAND", Arrays.asList("return", "ATOMIC")),
        new Rule("COMMAND", Arrays.asList("ASSIGN")),
        new Rule("COMMAND", Arrays.asList("CALL")),
        new Rule("COMMAND", Arrays.asList("BRANCH")),
        new Rule("ATOMIC", Arrays.asList("VNAME")),
        new Rule("ATOMIC", Arrays.asList("CONST")),
        new Rule("CONST", Arrays.asList("tokenn")),
        new Rule("CONST", Arrays.asList("tokent")),
        new Rule("ASSIGN", Arrays.asList("VNAME", "<", "input")),
        new Rule("ASSIGN", Arrays.asList("VNAME", "=", "TERM")),
        new Rule("CALL", Arrays.asList("FNAME", "(", "ATOMIC", ",", "ATOMIC", ",", "ATOMIC", ")")),
        new Rule("BRANCH", Arrays.asList("if", "COND", "then", "ALGO", "else", "ALGO")),
        new Rule("TERM", Arrays.asList("ATOMIC")),
        new Rule("TERM", Arrays.asList("CALL")),
        new Rule("TERM", Arrays.asList("OP")),
        new Rule("OP", Arrays.asList("UNOP", "(", "ARG", ")")),
        new Rule("OP", Arrays.asList("BINOP", "(", "ARG", ",", "ARG", ")")),
        new Rule("ARG", Arrays.asList("ATOMIC")),
        new Rule("ARG", Arrays.asList("OP")),
        new Rule("COND", Arrays.asList("SIMPLE")),
        new Rule("COND", Arrays.asList("COMPOSIT")),
        new Rule("SIMPLE", Arrays.asList("BINOP", "(", "ATOMIC", ",", "ATOMIC", ")")),
        new Rule("COMPOSIT", Arrays.asList("BINOP", "(", "SIMPLE", ",", "SIMPLE", ")")),
        new Rule("COMPOSIT", Arrays.asList("UNOP", "(", "SIMPLE", ")")),
        new Rule("UNOP", Arrays.asList("not")),
        new Rule("UNOP", Arrays.asList("sqrt")),
        new Rule("BINOP", Arrays.asList("or")),
        new Rule("BINOP", Arrays.asList("and")),
        new Rule("BINOP", Arrays.asList("eq")),
        new Rule("BINOP", Arrays.asList("grt")),
        new Rule("BINOP", Arrays.asList("add")),
        new Rule("BINOP", Arrays.asList("sub")),
        new Rule("BINOP", Arrays.asList("mul")),
        new Rule("BINOP", Arrays.asList("div")),
        new Rule("FNAME", Arrays.asList("tokenf")),
        new Rule("FUNCTIONS", Arrays.asList("")),
        new Rule("FUNCTIONS", Arrays.asList("DECL", "FUNCTIONS")),
        new Rule("DECL", Arrays.asList("HEADER", "BODY")),
        new Rule("HEADER", Arrays.asList("FTYP", "FNAME", "(", "VNAME", ",", "VNAME", ",", "VNAME", ")")),
        new Rule("FTYP", Arrays.asList("num")),
        new Rule("FTYP", Arrays.asList("void")),
        new Rule("BODY", Arrays.asList("PROLOG", "LOCVARS", "ALGO", "EPILOG", "SUBFUNCS", "end")),
        new Rule("PROLOG", Arrays.asList("{")),
        new Rule("EPILOG", Arrays.asList("}")),
        new Rule("LOCVARS", Arrays.asList("VTYP", "VNAME", ",", "VTYP", "VNAME", ",", "VTYP", "VNAME", ",")),
        new Rule("SUBFUNCS", Arrays.asList("FUNCTIONS"))
    ));

public void writeToFile(String fileName, String root) {
    // Defining the folder and file path
    String folderPath = "outputs/";
    File folder = new File(folderPath);

    // Create the folder if it doesn't exist
    if (!folder.exists()) {
        boolean isCreated = folder.mkdirs();
        if (!isCreated) {
            System.err.println("Failed to create output directory.");
            return;
        }
    }

    // Write to the file
    try (FileWriter writer = new FileWriter(folderPath + fileName)) {
        if (root != null) {
            writer.write(root);
            System.out.println("Data written to file: " + fileName);
        } else {
            System.out.println("No data found to write.");
        }
    } catch (IOException e) {
        System.err.println("Error writing to file: " + e.getMessage());
    }
}
}