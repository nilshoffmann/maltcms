package net.sourceforge.maltcms.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XIncludeProcessor {

    private final String in;
    private final String out;

    public XIncludeProcessor(String in, String out) {
        this.in = in;
        this.out = out;
    }

    public void process() {
        Map<String,String> authorMap = new HashMap<String,String>();
        authorMap.put("hoffmann","Nils Hoffmann");
        authorMap.put("nils","Nils Hoffmann");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(in));

            
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(
                    "//@lastchanged", doc, XPathConstants.NODESET);
            System.out.println("Found " + nodes.getLength() + " matching nodes for query!");
            // Rename these nodes
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int idx = 0; idx < nodes.getLength(); idx++) {
                String id = nodes.item(idx).getTextContent();
                try {
                    System.out.print("Replacing " + id);
                    String name = authorMap.get(System.getProperty("user.name"));
                    id = id.replaceAll("\\$Author\\$",name==null?"N.N.":name);
                    id = id.replaceAll("\\$Date\\$",sdf.format(date));
//                    id = id.replaceAll("\\$", "");
//                    String[] split = id.split(" ");
//                    System.out.println("with " + split[1] + " " + split[2] + " " + split[3] + " by " + split[11]);
                    System.out.println(" with "+id);
//                    nodes.item(idx).setTextContent(split[1] + " " + split[2] + " " + split[3] + " (GMT) by " + split[11]);
                    nodes.item(idx).setTextContent(id);
                }catch(ArrayIndexOutOfBoundsException ae) {
                    
                }
            }
            
            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(this.out)));
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer serializer;
            try {
                serializer = tfactory.newTransformer();
                //Setup indenting to "pretty print"
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                serializer.transform(new DOMSource(doc), new StreamResult(out));
            } catch (TransformerException e) {
                // this is fatal, just dump the stack and throw a runtime exception
                e.printStackTrace();

            }
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Processing file: " + args[0]);
        System.out.println("Writing result file: " + args[1]);
        XIncludeProcessor xip = new XIncludeProcessor(args[0], args[1]);
        xip.process();
        System.out.println("Done!");
    }
}
