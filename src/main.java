import java.util.*;
public class main {
    public static void main(String[] args) {
        String text = "main\n" + //
                        "num V_x , text V_y , num V_a , num V_b , num V_c ,\n" + //
                        "begin\n" + //
                        "  V_x < input ;\n" + //
                        "  V_y = \"Hello\" ;\n" + //
                        "  if grt ( V_x , 0 ) then\n" + //
                        "    begin\n" + //
                        "      print V_y ;\n" + //
                        "      print V_x ;\n" + //
                        "    end\n" + //
                        "  else\n" + //
                        "    begin\n" + //
                        "      print \"Bye\" ;\n" + //
                        "    end ;\n" + //
                        "  V_x = F_test ( V_x , 2 , 3 ) ;\n" + //
                        "  return 0 ;\n" + //
                        "end\n" + //
                        "\n" + //
                        "num F_test ( V_a , V_b , V_c )\n" + //
                        "{\n" + //
                        "  num V_result ,\n" + //
                        "  text V_d ,\n" + //
                        "  num V_y ,\n" + //
                        "  begin\n" + //
                        "    V_result = add ( V_a , V_y ) ;\n" + //
                        "    V_result = sub ( V_result , V_c ) ;\n" + //
                        "    V_result = F_test ( V_result , 2 , 5 ) ;\n" + //
                        "    return V_result ;\n" + //
                        "end\n" + //
                        "}\n" + //
                        "end";
        Lexer lexer = new Lexer(text);
        try {
            List<TokenClass> tokenList = lexer.lex();
            Parser parser = new Parser(tokenList);
            parser.parse();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
