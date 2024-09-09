import java.util.*;
import java.util.regex.*;

public class Lexer {
    public String text;
    public List<TokenClass> tokenList = new ArrayList<TokenClass>();

    public enum TokenClassType {
        T,
        N,
        F,
        V,
        res_key,
        MAIN,
        NUM,
        TEXT,
        BEGIN,
        END,
        SKIP,
        HALT,
        PRINT,
        INPUT,
        IF,
        THEN,
        ELSE,
        NOT,
        SQRT,
        OR,
        AND,
        EQ,
        GRT,
        ADD,
        SUB,
        MUL,
        DIV,
        VOID
    };

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("V_[a-z]([a-z]|[0-9])*");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("F_[a-z]([a-z]|[0-9])*");
    private static final Pattern TEXT_PATTERN = Pattern.compile("\"[A-Z][a-z]{0,7}\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("0|0\\.([0-9])*|\\-0\\.([0-9])*|[1-9]([0-9])*|\\-[1-9]([0-9])*|[1-9]([0-9])*\\.([0-9])*|\\-[1-9]([0-9])*\\.([0-9])*");

    public TokenClass assignType(TokenClassType type, String word) {
        switch (type) {
            case T:
                return new TokenClass(2, "T", word);
            case N:
                return new TokenClass(3, "N", word);
            case F:
                return new TokenClass(4, "F", word);
            case V:
                return new TokenClass(5, "V", word);
            case res_key:
                return new TokenClass(6, "reserved_keyword", word);
            default:
                return null;
        }
    }

    public Lexer(String text) {
        this.text = text;
    }

    public void lex() {
        Scanner scanner = new Scanner(text);
        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.length() == 1 && isSingleCharacterToken(token.charAt(0))) {
                tokenList.add(assignType(TokenClassType.res_key, token));
            } else if (token.startsWith("V_") && isVariable(token)) {
                tokenList.add(assignType(TokenClassType.V, token));
            } else if (token.startsWith("F_") && isFunction(token)) {
                tokenList.add(assignType(TokenClassType.F, token));
            } else if (isKeyword(token)) {
                tokenList.add(assignType(TokenClassType.res_key, token));
            } else if (isText(token)) {
                tokenList.add(assignType(TokenClassType.T, token));
            } else if (isNumber(token)) {
                tokenList.add(assignType(TokenClassType.N, token));
            } else {
                throw new RuntimeException("Invalid token: " + token);
            }
        }
        scanner.close();
    }

    private boolean isKeyword(String word) {
        try {
            TokenClassType.valueOf(word.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isSingleCharacterToken(char ch) {
        return "{}()=,;".indexOf(ch) != -1;
    }

    private boolean isVariable(String token) {
        return VARIABLE_PATTERN.matcher(token).matches();
    }

    private boolean isFunction(String token) {
        return FUNCTION_PATTERN.matcher(token).matches();
    }

    private boolean isText(String token) {
        return TEXT_PATTERN.matcher(token).matches();
    }

    private boolean isNumber(String token) {
        return NUMBER_PATTERN.matcher(token).matches();
    }
}