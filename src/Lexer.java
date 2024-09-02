import java.util.*;

public class Lexer {
    public String text;
    public int pos;
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
            case MAIN:
            case NUM:
            case TEXT:
            case BEGIN:
            case END:
            case SKIP:
            case HALT:
            case PRINT:
            case INPUT:
            case IF:
            case THEN:
            case ELSE:
            case NOT:
            case SQRT:
            case OR:
            case AND:
            case EQ:
            case GRT:
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case VOID:
                return new TokenClass(7, type.name().toLowerCase(), word);
            default:
                return null;
        }
    }

    public Lexer(String text) {
        this.text = text;
        this.pos = 0;
    }

    public void advance() {
        pos++; // Move 1 character forward in the text
    }

    public void lex() {
        StringBuilder textBuilder = new StringBuilder();
        while (pos < text.length()) {
            char currentChar = text.charAt(pos);
            if (Character.isWhitespace(currentChar)) {
                advance();
                continue;
            }

            if (Character.isLetter(currentChar)) {
                textBuilder.setLength(0);
                while (pos < text.length() && Character.isLetter(text.charAt(pos))) {
                    textBuilder.append(text.charAt(pos));
                    advance();
                }
                String word = textBuilder.toString();
                if (isKeyword(word)) {
                    tokenList.add(assignType(TokenClassType.valueOf(word.toUpperCase()), word));
                } else {
                    throw new RuntimeException("Invalid token: " + word);
                }
                continue;
            }

            switch (currentChar) {
                case '{':
                case '}':
                case '(':
                case ')':
                case '=':
                case ',':
                case ';':
                    tokenList.add(assignType(TokenClassType.res_key, String.valueOf(currentChar)));
                    advance();
                    break;

                default:
                    throw new RuntimeException("Invalid token: " + currentChar);
            }
        }
    }

    private boolean isKeyword(String word) {
        try {
            TokenClassType.valueOf(word.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}