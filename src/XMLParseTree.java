import java.util.*;

public class XMLParseTree {
    private String tag;
    private List<XMLParseTree> children;
    private String value;
    private int id;

    public XMLParseTree(String tag, int id) {
        this.id = id;
        this.tag = tag;
        this.children = new ArrayList<>();
    }

    public void addChild(XMLParseTree child) {
        children.add(child);
    }

    public String getTag(){
        return tag;
    }

    public String getValue(){
        return value;
    }

    public List<XMLParseTree> getChildren(){
        return children;
    }

    public int getXMLID(){
        return id;
    }

    public void setValue(String value) {
        if (value.equals("<")){
        this.value = "$lt;";
        }else{
            this.value =value;
        }
    }

    @Override
    public String toString() {
        return toString(0); // Start with depth 0
    }

    // Recursive method with indentation
    public String toString(int depth) {
        StringBuilder sb = new StringBuilder();
        String indent = "\t".repeat(depth); // Two spaces per depth level

        // Collect children IDs
        StringBuilder childrenIds = new StringBuilder();
        for (XMLParseTree child : children) {
            if (childrenIds.length() > 0) {
                childrenIds.append(",");
            }
            childrenIds.append(child.id);
        }

        // Open tag with ID and children IDs
        sb.append(indent)
          .append("<").append(tag)
          .append(" id=\"").append(id).append("\"");

        if (childrenIds.length() > 0) {
            sb.append(" children=\"").append(childrenIds).append("\"");
        } else {
            sb.append(" children=\"\"");
        }
        sb.append(">");

        // Add value or child nodes
        if (value != null) {
            sb.append(value);
        } else {
            sb.append("\n");
            for (XMLParseTree child : children) {
                sb.append(child.toString(depth + 1)); // Recursive call with increased depth
            }
            sb.append(indent); // Closing tag at the same indentation level
        }

        sb.append("</").append(tag).append(">\n");
        return sb.toString();
    }
}

