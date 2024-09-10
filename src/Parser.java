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
        while (index < tokenList.size()) {
            TokenClass token = tokenList.get(index);
            int state = stack.peek();
            String action = getAction(state, token);

            if (action == null) {
                throw new RuntimeException("Syntax error at token: " + token);
            } else if (action.startsWith("s")) {
                // Shift action
                int nextState = Integer.parseInt(action.substring(1));
                stack.push(nextState);
                index++;
            } else if (action.startsWith("r")) {
                // Reduce action
                int ruleNumber = Integer.parseInt(action.substring(1));
                // Assuming you have a method to get the rule by its number
                Rule rule = getRule(ruleNumber);
                for (int i = 0; i < rule.getRhs().size(); i++) {
                    stack.pop();
                }
                int newState = stack.peek();
                stack.push(getGoto(newState, rule.getLhs()));
            } else if (action.equals("acc")) {
                // Accept action
                System.out.println("Parsing completed successfully.");
                return;
            }
            System.out.println("Token: " + token + ", Action: " + action + ", Stack: " + stack);
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
        return Integer.parseInt(SLRParseTable.PARSE_TABLE[state][columnIndex]);
    }

    private int getColumnIndex(String nonTerminal) {
        for (int i = 0; i < SLRParseTable.HEADERS.length; i++) {
            if (SLRParseTable.HEADERS[i].equals(nonTerminal)) {
                return i;
            }
        }
        throw new RuntimeException("Invalid non-terminal: " + nonTerminal);
    }

    private Rule getRule(int ruleNumber) {
        // Implement this method to return the rule by its number
        // This method should return a Rule object that contains the LHS and RHS of the rule
        return null;
    }

    // Define the Rule class
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
}