
import java.util.*;

// import javax.xml.parsers.*;
// import org.w3c.dom.*;
// import org.xml.sax.SAXException;
public class TypeChecker {



 public boolean typeCheck(XMLParseTree xml) {
    String tag = xml.getTag();
    // String token = xml.getValue();
    List<XMLParseTree> children = xml.getChildren();

    switch (tag) {
        case "PROG": //main(0) GLOBAVARS(1) ALGO(2) FUNCTIONS(3)
        return typeCheck(children.get(1)) && typeCheck(children.get(2)) && typeCheck(children.get(3));

        case "GLOBVARS": //nullable
        if (children.isEmpty()) {
            System.out.println("BASE-CASE OF GLOBVARS REACHED");
            return true;
        }else if(children.size()==4){//VTYP(0) VNAME(1)  ,(2) GLOBVARS(3)  
            return typeCheck(children.get(1)) && typeCheck(children.get(2)) && typeCheck(children.get(3));
        }
        break;


        case "ALGO": //begin(0) INSTRUC(1) end(2) 
        return typeCheck(children.get(1));

        case "INSTRUC": //nullable
        if (children.isEmpty()) {
            System.out.println("REACHED THE END OF INSTRUC");
            return true;
        } else if (children.size() == 3) {
            return typeCheck(children.get(0)) && typeCheck(children.get(1)) && typeCheck(children.get(2));
        }
        break;

        case "COMMAND"://COMMAND := skip(0) | halt(0) 
        if(!children.isEmpty()&&children.get(0).getValue().equals("skip")||children.get(0).getValue().equals("halt")){
            return true;

        //print ATOMIC
        }else if(!children.isEmpty()&&children.get(0).getValue().equals("print")&&children.get(1).getTag()=="ATOMIC"){ 
            if (typeOf(children.get(1))=='n'){
                return true;
            }else if(typeOf(children.get(1))=='t'){
                return true;
            }
            else{
                return false;
            } 
        //return ATOMIC 
        // }else if (children.size()==2&&children.get(0).getValue().equals("return")&&children.get(1).getTag().equals("ATOMIC")){ //TODO: Need help understanding this
        // if (typeOf())

        //COMMAND := ASSIGN (0)
        }else if(!children.isEmpty()&&children.get(0).getTag()=="ASSIGN"){
            return typeCheck(children.get(0));

        //COMMAND := CALL (0)
        }else if(!children.isEmpty()&&children.get(0).getTag().equals("CALL")){
            if(typeOf(children.get(0))=='v'){
                return true;
            }else{
                return false;
            }

        //COMMAND := BRANCH (0)
        }else if(!children.isEmpty()&&children.get(0).getTag().equals("BRANCH")){
            typeCheck(children.get(0));
        }
        break;

        //COMMAND := if(0) COND(1) then(2) ALGO(3) else(4) ALGO(5)
        case "BRANCH": 
        if(typeOf(children.get(1))=='b'){
            return typeCheck(children.get(3))&&typeCheck(children.get(5));
        }else{
            return false;
        }
        
        case "FUNCTIONS": //nullable
        if (children.isEmpty()){
            System.out.println("REACHED BASE-CASE  OF FUNCTIONS");
            return true;

        //DECL(0) FUNCTIONS(1)
        }else if(!children.isEmpty()&&children.size()==2){
            return typeCheck(children.get(0))&&typeCheck(children.get(1));
        }

        case "SUBFUNCTIONS":
         return typeCheck(children.get(0));
        

        default:
        System.out.println("Something went wrong in typeCheck@");
            return false;
    }
    System.out.println("Something went wrong in typeCheck");
    return false;
 }

 public char typeOf(XMLParseTree xml) {
    String tag = xml.getTag();  // Assuming this gets the tag
    List<XMLParseTree> children = xml.getChildren();

    switch (tag) {
        case "VTYP":
         if(children.get(0).getValue().equals("num")){
            return 'n';
         }else if( children.get(0).getValue().equals("text")){
            return 't';
         }
         break;

        case "CONST":
         if(children.get(0).getTag().equals("tokenn")){
            return 'n';
         }else if(children.get(0).getTag().equals("tokent")){
            return 't';
         }
         break;
        
        
        case "BINOP":
            if (children.get(0).getValue().equals("or") || children.get(0).getValue().equals("and")) {
                return 'b';
            } else if (children.get(0).getValue().equals("eq") || children.get(0).getValue().equals("grt")) {
                return 'c';
            } else if (children.get(0).getValue().equals("add") || children.get(0).getValue().equals("sub") || children.get(0).getValue().equals("mul") || children.get(0).getValue().equals("div")) {
                return 'n';
            }
            break;

        case "UNOP":
        if (children.get(0).getValue().equals("not")){
            return 'b';
        }else if(children.get(0).getValue().equals("sqrt")){
            return 'n';
        }
        break;

        case "OP": //UNOP(0) ( (1) and ARG(2) ) (3)
        if((typeOf(children.get(0))==typeOf(children.get(2)))&&typeOf(children.get(2))=='b'){
            return 'b';
        }else if((typeOf(children.get(0))==typeOf(children.get(2)))&&typeOf(children.get(2))=='n'){
            return 'n';
        }
        break;

        case "COND": //COND := SIMPLE(0) | COMPOSIT(0)
        return typeOf(children.get(0));

        
        default:
            return 'u';  // Handle unknown cases
    }

    // If no match is found in the BINOP case, return 'u'
    return 'u';
}

    
}
