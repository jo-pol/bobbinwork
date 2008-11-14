package nl.BobbinWork.diagram.xml;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlHandler {

  private static final String BASE = "http://bobbinwork.googlecode.com/svn/trunk/src/"; //$NON-NLS-1$
  private static final String SAMPLE_URL = "http://bobbinwork.googlecode.com/web/";//$NON-NLS-1$
  private static final String PATH = "nl/BobbinWork/diagram/xml/";//$NON-NLS-1$
  private static final String SCHEMA_PATH = PATH + "bw.xsd";//$NON-NLS-1$
  private static final String SCHEMA_URL = BASE + SCHEMA_PATH;
  private static final String SCHEMA_RESOURCE = XmlHandler.class.getClassLoader().getResource(SCHEMA_PATH).toString();
  public  static final String ROOT_ATTRIBUTES = 
    " xmlns='http://BobbinWork.googlecode.com/bw.xsd'" + //$NON-NLS-1$
    " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" + //$NON-NLS-1$
    " xsi:schemaLocation='http://BobbinWork.googlecode.com bw.xsd'" + //$NON-NLS-1$
    " xmlns:xi='http://www.w3.org/2001/XInclude'" ; //$NON-NLS-1$

  
  static private Validator validator = newValidator();
  static private XPath path = XPathFactory.newInstance().newXPath();
  private DocumentBuilder parser = newParser();

  public XmlHandler( ) throws ParserConfigurationException 
  {
    if ( parser != null ) parser = newParser();
  }
  
  public NodeList evaluate (String xPath, Document document) throws XPathExpressionException {
    return (NodeList) path.evaluate(xPath,document, XPathConstants.NODESET);
  }
  
  /** Gets the XML definition of a twist. The thread segments form a plus sign.
   * The left thread (in the back) goes from east to west.
   * The right thread (in the front) goes from north to south.
   * 
   * @param w length from end of a thread segment to the core of the twist 
   * @return
   * @throws IOException 
   * @throws SAXException 
   */
  public Document getTwist (int w) throws SAXException, IOException {
    String s = "<?xml version='1.0'?>" + //$NON-NLS-1$
    "<twist bobbins='1-2' " + ROOT_ATTRIBUTES + ">" + //$NON-NLS-1$ $NON-NLS-2$
    "<back start='0," + w + "' end='" + (w*2) + "," + w + "'/>" + //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
    "<front start='" + w + ",0' end='" + w + "," + (w*2) + "' />" + //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
    "</twist>"; //$NON-NLS-1$
    byte[] bytes = s.getBytes();
    ByteArrayInputStream inputStream = new ByteArrayInputStream (bytes);
    return parser.parse(inputStream);
  }

  public Document parse(File xmlFile) 
  throws SAXException, IOException {
    
    return parser.parse(xmlFile);
  }
  
  public Document parse(String xmlContent) 
  throws IOException, SAXException {
    
    InputStream inputStream = new ByteArrayInputStream (xmlContent.getBytes());
    try {
      return parser.parse(inputStream,SCHEMA_RESOURCE);
    } catch (SAXException e) {
      String regExp = ".*[Ii]nlclude.*";
      if ( e.getMessage().matches(regExp) ) {
        try {
          return parser.parse(inputStream,SCHEMA_URL);
        } catch (SAXException e1) {
          if ( e.getMessage().matches(regExp) ) {
            return parser.parse(inputStream,SAMPLE_URL);
          } else throw e;
        }
      } else throw e;
    }
  }

  public void validate(File xmlFile) 
  throws IOException, SAXException {
    
    if ( validator == null ) return;
    validator.validate(new DOMSource( parse(xmlFile) ));
  }
  
  public void validate(Document source) 
  throws IOException, SAXException {
    
    if ( validator == null ) return;
    validator.validate(new DOMSource(source,SCHEMA_RESOURCE));
  }
  
  public String toXmlString(Document doc) throws TransformerException {
    
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    StreamResult result = new StreamResult(new StringWriter());
    transformer.transform(new DOMSource(doc), result);
    return result.getWriter().toString();
  }

  public void validate(String xmlContent) 
  throws SAXException, IOException, TransformerException {
    
    newValidator().validate( new DOMSource( parse(xmlContent)));
  }

  private static DocumentBuilder newParser() 
  throws ParserConfigurationException {
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setXIncludeAware(true); 
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder();
  }
  
  private static Validator newValidator() {
    
    SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
    try {
      return factory.newSchema( new File("src/"+SCHEMA_PATH) ).newValidator();
    } catch (SAXException e) {
      // validation is not essential
      System.out.println("validation attempts will be ignored");
      e.printStackTrace();
      return null;
    }
  }

  public String getLocation(SAXParseException e) {
    
    return "line:"+e.getLineNumber()+" col:"+e.getColumnNumber();
  }
}