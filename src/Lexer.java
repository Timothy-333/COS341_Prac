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
        res_key
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
        String textBuilder = "";
        while (pos < text.length()) {
            switch (text.charAt(pos)) { // If the character is whitespace simply move 1 character forward
                case ' ':
                    advance();
                    break;
                case '\n':
                    advance();
                    break;
                case '\t':
                    advance();
                    break;

                // Reserved_Keyword Tokens
                case '{':
                    tokenList.add(assignType(TokenClassType.res_key, "{"));
                    advance();
                    break;
                case '}':
                    tokenList.add(assignType(TokenClassType.res_key, "}"));
                    advance();
                case '(':
                    tokenList.add(assignType(TokenClassType.res_key, "("));
                    advance();
                    break;
                case ')':
                    tokenList.add(assignType(TokenClassType.res_key, ")"));
                    advance();
                case '=':
                    tokenList.add(assignType(TokenClassType.res_key, "="));
                    advance();
                    break;
                case ',':
                    tokenList.add(assignType(TokenClassType.res_key, ","));
                    advance();
                    break;
                // Multi-character tokens for reserved_keyword
                case 'a':
                    textBuilder += text.substring(pos, pos + 1);
                    advance();
                    switch (text.charAt(pos)) {
                        case 'n':
                            textBuilder += text.substring(pos, pos + 1);
                            advance();
                            switch (text.charAt(pos)) {
                                case 'd':
                                    textBuilder += text.substring(pos, pos + 1);
                                    advance();
                                    tokenList.add(assignType(TokenClassType.res_key, textBuilder)); // Built text
                                    // Clearing textBuilder
                                    textBuilder = "";
                                    break;
                                default:
                                    break;
                            }
                            break;

                        default:
                            // Throw Error :Incomplete Keyword
                            break;
                    }
                case 'e':
                    textBuilder += text.substring(pos, pos + 1);
                    advance();
                    switch (text.charAt(pos)) {
                        case 'n':
                            textBuilder += text.substring(pos, pos + 1);
                            advance();
                            switch (text.charAt(pos)) {
                                case 'd':
                                    textBuilder += text.substring(pos, pos + 1);
                                    advance();
                                    tokenList.add(assignType(TokenClassType.res_key, textBuilder)); // Built text
                                    textBuilder = ""; // Clearing textBuilder
                                    break;
                                default:
                                    // Throw Error :Incomplete Keyword
                                    break;
                            }
                            break;
                        case 'l':
                            textBuilder += text.substring(pos, pos + 1);
                            advance();
                            switch (text.charAt(pos)) {
                                case 's':
                                    textBuilder += text.substring(pos, pos + 1);
                                    advance();
                                    switch (text.charAt(pos)) {
                                        case 'e':
                                            tokenList.add(assignType(TokenClassType.res_key, textBuilder)); // Built
                                                                                                            // text
                                            textBuilder = ""; // Clearing textBuilder
                                            break;
                                        default:
                                            // Throw Error :Incomplete Keyword
                                            break;
                                    }
                                    break;
                                default:
                                    // Throw Error :Incomplete Keyword
                                    break;
                            }
                            break;
                        case 'q':
                            textBuilder += text.substring(pos, pos + 1);
                            tokenList.add(assignType(TokenClassType.res_key, textBuilder)); // Built text
                            textBuilder = ""; // Clearing textBuilder
                            advance();
                        default:
                            // Throw Error :Incomplete Keyword
                            break;
                    }
                default:
                    // Throw Error :Incomplete/Invalid Token
                    break;
            }
        }
    }
}