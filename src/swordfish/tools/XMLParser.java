package swordfish.tools;

import java.io.IOException;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/**
 *
 * @author jdavidso
 */
public class XMLParser {
    private DocumentBuilder builder;
    private Document doc;

    /**
     * Set up the XMLParser for the specified file
     * @param file A string for the file to be parsed.  Must be the full path
     */
    public XMLParser(String file) throws ParserConfigurationException, SAXParseException,
    SAXException, IOException{                  
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = (Document) builder.parse(file);
        doc.getDocumentElement().normalize();        
    }
    
    /**
     * Used to parse out the elements in the XML matching the specified tag
     * 
     * @param tag The tag representing the tag of the element to parse out
     * @return A NodeList of the elements in the document matching the given tag
     */
    public NodeList parseElements(String tag) {
        return doc.getElementsByTagName(tag);
    }          
    
    /**
     * Used to get the value of the content at a given node and return it as a 
     * String
     * @param node A Node in the XMLDocument
     * @param tag A tag representing the tag of the element to extract the value
     * @return The value of the XML Element
     */
    public String getStringFromNode(Node node, String tag){
        String entry = "";
        try {
            entry = ((Element)node).getElementsByTagName(tag).item(0).
                    getChildNodes().item(0).getNodeValue().trim();
        } catch (NullPointerException ex) {            
            entry = "None";
        }
        return entry;
    }
    
    /**     
     * Used to get the value of the content at a given node and return it as a 
     * int
     * 
     * @param node A Node in the XMLDocument
     * @param tag A tag representing the tag of the element to extract the value
     * @return The value of the XML Element
     */
    public int getIntFromNode(Node node, String tag) {
        int entry;
        try {
            entry = new Integer(((Element)node).getElementsByTagName(tag).item(0).
                    getChildNodes().item(0).getNodeValue().trim()).intValue();
        } catch (NullPointerException ex) {            
            entry = 0;
        } catch (NumberFormatException ex) {
            entry = 0;
        }
        return entry;
    }
    
    /**     
     * Used to get the value of the content at a given node and return it as a 
     * array of ints.  The string value must be a series of ints delimited by a
     * '|'
     * 
     * @param node A Node in the XMLDocument
     * @param tag A tag representing the tag of the element to extract the value
     * @return The value of the XML Element
     */
    public int[] getArrayFromNode(Node node, String tag) {
        int[] entry;
        try {
            StringTokenizer st = new StringTokenizer(((Element)node).
                    getElementsByTagName(tag).item(0).getChildNodes().item(0).
                    getNodeValue().trim(),"|");
            entry = new int[st.countTokens()];
            int i = 0;
            while(st.hasMoreTokens()) {
                entry[i++] = new Integer(st.nextToken()).intValue();
            }
        } catch (NullPointerException ex) {            
            entry = new int[] {};
        } catch (NumberFormatException ex) {
            entry = new int[] {};
        }
        return entry;
    }
    
    /**     
     * Used to get the value of the content at a given node and return it as a 
     * boolean
     * 
     * @param node A Node in the XMLDocument
     * @param tag A tag representing the tag of the element to extract the value
     * @return The value of the XML Element
     */
    public boolean getBooleanFromNode(Node node, String tag) {
        boolean entry = true;
        try {
            NodeList nl = ((Element)node).getElementsByTagName(tag);                    
            if(nl.getLength() == 0) {
                return false;
            }
        } catch (NullPointerException ex) {                        
            entry = false;
        } 
        return entry;
    }
    
}
