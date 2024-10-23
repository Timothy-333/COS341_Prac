import java.util.*;

public class TypeChecker {

public Map<String, ScopeAnalyzer.SymbolInfo> symbolTable;;

TypeChecker() { 
}

TypeChecker(Map<String, ScopeAnalyzer.SymbolInfo> symbolTable) {
    this.symbolTable = symbolTable;
}
public String findSymbolType(String symbol) {
    return  symbolTable.get(symbol).getType(); 
}   

 public boolean typeCheck(XMLParseTree xml) {
    String tag = xml.getTag();
    
    // String token = xml.getValue();
    List<XMLParseTree> children = xml.getChildren();

    switch (tag) {
        case "PROG": //main(0) GLOBAVARS(1) ALGO(2) FUNCTIONS(3)
        return typeCheck(children.get(1)) && typeCheck(children.get(2)) && typeCheck(children.get(3));

        case "GLOBVARS": //nullable
        if (children.isEmpty()) {
            return true;
        }else if(children.size()==4){//VTYP(0) VNAME(1)  ,(2) GLOBVARS(3)  
            return typeCheck(children.get(3));
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

        // case "ASSIGN":
        case "ASSIGN"://ASSIGN := VNAME(0) <(1) input(2)
        if(children.get(2).getValue()==null){
            System.out.println("input has no value");
        }
        if(!children.isEmpty()&&children.get(0).getTag().equals("VNAME")&&children.get(2).getValue().equals("input")){
            if(typeOf(children.get(0))=='n'){
                return true;
            }else{
                // return false;
                throw new RuntimeException("Type Error: Invalid ASSIGN type for 'VNAME input'");
            }
        }else if(!children.isEmpty()&&children.get(0).getTag().equals("VNAME")&&children.get(2).getTag().equals("TERM")){
            if(typeOf(children.get(0))==typeOf(children.get(2))){
                return true;
            }else{
            // return false;
            throw new RuntimeException("Type Error: Invalid ASSIGN type for 'VNAME TERM'");
        }
        }
        break;
        

        case "COMMAND"://COMMAND := skip(0) | halt(0) 
        if(children.get(0).getValue()==null){
            System.out.println("skip has no value");
        }
        if(children.get(0).getValue()!=null){
            if(children.get(0).getValue().equals("skip")||children.get(0).getValue().equals("halt")){
            return true;
            }
        //print ATOMIC
        else if(children.get(0).getValue()!=null && !children.isEmpty()&&children.get(0).getValue().equals("print")&&children.get(1).getTag()=="ATOMIC"){ 
            if (typeOf(children.get(1))=='n'){
                return true;
            }else if(typeOf(children.get(1))=='t'){
                return true;
            }
            else{
                // return false;
                throw new RuntimeException("Type Error:Invalid COMMAND type for 'print ATOMIC'");
            } 
        //return ATOMIC
        }
        else if (children.size()==2&&children.get(0).getValue().equals("return")&&children.get(1).getTag().equals("ATOMIC")){ //TODO: Need help understanding this
            return checkReturnInFunctionScope(xml, children.get(1));

        //COMMAND := ASSIGN (0)
        }
        }else if(!children.isEmpty()&&children.get(0).getTag()=="ASSIGN"){
            return typeCheck(children.get(0));
            

        //COMMAND := CALL (0)
        }else if(!children.isEmpty()&&children.get(0).getTag().equals("CALL")){
            if(typeOf(children.get(0))=='v'){
                return true;
            }else{
                throw new RuntimeException("Invalid COMMAND type");
            }
        //COMMAND := BRANCH (0)
        }else if(!children.isEmpty()&&children.get(0).getTag().equals("BRANCH")){
            typeCheck(children.get(0));
        }
        break;

        case "BRANCH":  //BRANCH := if(0) COND(1) then(2) ALGO(3) else(4) ALGO(5)
        if(typeOf(children.get(1))=='b'){
            return typeCheck(children.get(3))&&typeCheck(children.get(5));
        }else{
            // return false;
            throw new RuntimeException("Invalid BRANCH type");
        }
        
        case "FUNCTIONS": //nullable
        if (children.isEmpty()){
            System.out.println("REACHED BASE-CASE OF FUNCTIONS");
            return true;

        //DECL(0) FUNCTIONS(1)
        }else if(!children.isEmpty()&&children.size()==2){
            return typeCheck(children.get(0))&&typeCheck(children.get(1));
        }

        case "SUBFUNCS": //SUBFUNCS := FUNCTIONS(0)
         return typeCheck(children.get(0));
        
        case "PROLOG": //PROLOG := {
        System.out.println("REACHED BASE-CASE OF PROLOG");
         return true;

        case "EPILOG": //EPILOG := }
         System.out.println("REACHED BASE-CASE OF EPILOG");
         return true;
         
        case "LOCVARS": //LOCVARS := VTYP(0) VNAME(1) ,(2) VTYP(3) VNAME(4) ,(5) VTYP(6) VNAME(7)
        //*Taken care of by symbol Table */

        return true;

        case "DECL": //HEADER BODY
        return typeCheck(children.get(0))&&typeCheck(children.get(1));

        case "HEADER": //FTYP(0) FNAME(1) ((2) VNAME1(3) ,(4) VNAME2(5) ,(6) VNAME3(7) )(8)
        if(typeOf(children.get(3))==typeOf(children.get(5))&&typeOf(children.get(5))==typeOf(children.get(7))&&typeOf(children.get(7))=='n'){
            return true;
        }else{
            throw new RuntimeException("Invalid HEADER type"); 
        }

        
        
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

        case "VNAME":
        if(findSymbolType(children.get(0).getValue())=="text"){
            return 't';
        }else if(findSymbolType(children.get(0).getValue())=="num"){
            return 'n';
        }
        break;

        case "TERM": //TERM := ATOMIC(0) | CALL (0) | OP (0)
        return typeOf(children.get(0));


        
        case "BINOP": //BINOP := or | and |  eq | grt | add | sub | mul | div | not | sqrt
        if(children.get(0).getValue()==null){
            System.err.println("ERROR IN BINOP");
        }
            if (children.get(0).getValue().equals("or") || children.get(0).getValue().equals("and")) {
                return 'b';
            } else if (children.get(0).getValue().equals("eq") || children.get(0).getValue().equals("grt")) {
                return 'c';
            } else if (children.get(0).getValue().equals("add") || children.get(0).getValue().equals("sub") || children.get(0).getValue().equals("mul") || children.get(0).getValue().equals("div")) {
                return 'n';
            }
            break;

        case "UNOP":
        if(children.get(0).getValue()==null){
            System.err.println("ERROR IN UNOP");
        }
        if (children.get(0).getValue().equals("not")){
            return 'b';
        }else if(children.get(0).getValue().equals("sqrt")){
            return 'n';
        }
        break;

        case "OP": //UNOP(0) ( (1) and ARG(2) ) (3)
        if(children.get(0).getTag().equals("UNOP")){      

        if((typeOf(children.get(0))==typeOf(children.get(2)))&&typeOf(children.get(2))=='b'){
            return 'b';
        }else if((typeOf(children.get(0))==typeOf(children.get(2)))&&typeOf(children.get(2))=='n'){
            return 'n';
        }
    } else if(children.get(0).getTag().equals("BINOP")){//BINOP(0) ((1) ARG1(2) ,(3) ARG2(4) )(5)
        if((typeOf(children.get(0))==typeOf(children.get(2)))&&typeOf(children.get(2))==typeOf(children.get(4))&&typeOf(children.get(4))=='b'){
            return 'b';
        }else if((typeOf(children.get(0))==typeOf(children.get(2)))&&typeOf(children.get(2))==typeOf(children.get(4))&&typeOf(children.get(4))=='n'){
            return 'n';
        }else if(typeOf(children.get(0))=='c'&&typeOf(children.get(2))==typeOf(children.get(4))&&typeOf(children.get(4))=='n'){
            return  'b';
        }
        else{
            return 'u';
        }
        
    }
        break;


        case "ARG": //ARG := ATOMIC(0) | OP(0)
        return typeOf(children.get(0));
 
        case "SIMPLE": //BINOP(0)  ((1) ATOMIC(2) ,(3) ATOMIC(4)   )(5)
        if (typeOf(children.get(0)) == typeOf(children.get(2))&&typeOf(children.get(2))==typeOf(children.get(4))&&typeOf(children.get(4))=='b') {
            return 'b';
        }else if(typeOf(children.get(0))=='c'&&typeOf(children.get(2))==typeOf(children.get(4))&&typeOf(children.get(4))=='n'){
            return  'b';            
        }else{
            return 'u';
        }

        case "COMPOSIT"://BINOP(0)  ((1) SIMPLE(2) ,(3) SIMPLE(4)   )(5)
        if(children.size()==6){
        if(typeOf(children.get(0))==typeOf(children.get(2))&&typeOf(children.get(2))==typeOf(children.get(4))&&typeOf(children.get(4))=='b'){
            return 'b';
        }else{
            return 'u';
        }
        }else if(children.size()==4){ //UNOP(0) ((1) SIMPLE(2) )(3)
            if(typeOf(children.get(0))==typeOf(children.get(2))&&typeOf(children.get(2))=='b'){
                return 'b';
            }else{ return 'u';
        }
        }
         break;

        case "COND": //COND := SIMPLE(0) | COMPOSIT(0)
        return typeOf(children.get(0));

        case "FNAME":
        if(findSymbolType(children.get(0).getValue())=="void"){
            return 'v';
        }else if(findSymbolType(children.get(0).getValue())=="num"){
            return 'n';
        }
        break;
        
        default:
            return 'u';  // Handle unknown cases
    }

    // If no match is found in the BINOP case, return 'u'
    return 'u';
}

    // Tree-crawling method to find the return type in the enclosing function
    public boolean checkReturnInFunctionScope(XMLParseTree returnNode, XMLParseTree atomicNode) {
        XMLParseTree current = returnNode.getParent();

        // Crawl up the tree until we find a function HEADER
        while (current != null) {
            if (current.getChildren().get(0).getTag().equals("HEADER")) {
                XMLParseTree ftypNode = current.getChildren().get(0).getChildren().get(0); // Assuming FTYP is the first child in HEADER
                char expectedType = typeOf(ftypNode);
                return typeOf(atomicNode) == expectedType && typeOf(atomicNode)=='n';
            }
            current = current.getParent();
        }
        return false; // If no function scope was found
    }  
}