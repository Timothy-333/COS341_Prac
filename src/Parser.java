import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Parser {
    public List<TokenClass> tokenList;
    public Parser(List<TokenClass> tokenList) {
        this.tokenList = tokenList;
    }

    public void parse() {
        Stack<Integer> stack = new Stack<>();
        stack.push(0); // Initial state
    
        int index = 0;
        try {
            while (index < tokenList.size()) {
                TokenClass token = tokenList.get(index);
                int state = stack.peek();
                String action = getAction(state, token);
    
                if (action == null) {
                    throw new RuntimeException("Syntax error at token: " + token + " in state: " + state);
                } else if (action.startsWith("s")) {
                    // Shift action
                    int nextState = Integer.parseInt(action.substring(1));
                    stack.push(nextState);
                    index++;
                } else if (action.startsWith("r")) {
                    // Reduce action
                    int ruleNumber = Integer.parseInt(action.substring(1));
                    if (ruleNumber < 0 || ruleNumber >= rules.size()) {
                        throw new RuntimeException("Invalid rule number: " + ruleNumber);
                    }
                    Rule rule = rules.get(ruleNumber);
                    for (int i = 0; i < rule.getRhs().size(); i++) {
                        stack.pop();
                    }
                    int newState = stack.peek();
                    int gotoState = getGoto(newState, rule.getLhs());
                    if (gotoState == -1) {
                        throw new RuntimeException("Goto state not found for non-terminal: " + rule.getLhs());
                    }
                    stack.push(gotoState);
                } else if (action.equals("acc")) {
                    // Accept action
                    System.out.println("Parsing completed successfully.");
                    return;
                } else {
                    throw new RuntimeException("Unknown action: " + action);
                }
                System.out.println("Token: " + token + ", Action: " + action + ", Stack: " + stack);
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
            throw new RuntimeException("Goto value is null for non-terminal: " + nonTerminal + " in state: " + state);
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
}