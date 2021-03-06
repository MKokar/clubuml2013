package uml2parser;

import java.util.Stack;
import logging.Log;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;

/**
 * 
 * @author Prashant Shukla
 *
 */
public class ParseXmi extends org.xml.sax.helpers.DefaultHandler {
	
	
	public String fileName; 
	public Stack stack;
	public ModelFileInfo fileInfo;
	
	/**
	 * 
	 * @param fileInfo
	 */
	public ParseXmi(ModelFileInfo fileInfo ) {
		fileName = fileInfo.getFileName();
		this.fileInfo = fileInfo;
		stack = new Stack();
	}
	
	/**
	 * 
	 */
	public void parseDoc() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		try {
			SAXParser sp = factory.newSAXParser();
			sp.parse(fileName,this);
		}catch (Exception ex) {
			Log.LogCreate().Info("exception " + ex.getLocalizedMessage());
		}	
	}
	
	/**
	 * 
	 */
	@Override
	public void startDocument() {
		
	}
	
	/**
	 * 
	 */
	@Override
	public void startElement(String nameSpaceURI, String sName, String qname, Attributes attrs) {
		
		XmiElement elem = new XmiElement(qname,attrs);
		if (!stack.empty()) {
			XmiElement parentElem = (XmiElement)stack.peek();
			//Log.LogCreate().Info("Adding Parent Element " + elem.getElementName()  + " parent element " + parentElem.getElementName());
			elem.setParentElem(parentElem);		
			
			fileInfo.AddChildElement(parentElem, elem);
			
		} else {
			//Log.LogCreate().Info("Adding Element " + elem.getElementName() );
			fileInfo.addElement(elem);
		}
		stack.push(elem);
		
	}
	
	/**
	 * 
	 */
	@Override
	public void characters(char buf [] , int offset, int len) {
		String s = new String(buf, offset, len);
	}
	
	/**
	 * 
	 */
	@Override
	public void endElement(String nameSpaceURI, String sName, String qName) {
		/*
		XmiElement childElem = null;
		if (!stack.empty()) {
			 childElem= (XmiElement)stack.pop();
		}
		if (!stack.empty()) {
			XmiElement parentElem = (XmiElement)stack.peek();
			
		} */
		if (!stack.empty())
			stack.pop();
		
	}
	

}

