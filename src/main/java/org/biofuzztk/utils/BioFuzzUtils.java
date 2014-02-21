/** 
 *  The BioFuzz Toolkit for input parsing/generation/modification of
 *  structured input.
 *  
 *  Copyright (C) 2014 Julian Thome (frostisch@yahoo.de)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.biofuzztk.utils;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class BioFuzzUtils {

	final static Logger logger = LoggerFactory.getLogger(BioFuzzUtils.class);
	final static Pattern paramPat = Pattern.compile("(\\w*)=(.*)",Pattern.CASE_INSENSITIVE);
	
	public static int getStrDist(String s1, String s2) {
		return BioFuzzLevenshtein.getLdist(s1, s2);
	}
	
	public static double getNormalziedStrDist(String s1, String s2) {
		return BioFuzzLevenshtein.getNormalizedLdist(s1, s2);
	}

	public static double max(double a, double b) {
		return a > b ? a : b;
	}
	
    public static String strArrayToStr(String [] sa) {
        String s = "";

        for(int i = 0; i < sa.length; i++) {
                s += sa[i] + ' ';
        }

        return s;

    }
    
    public static Document stringToDom(String xml) throws
    SAXException, ParserConfigurationException, IOException {
    	DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
    	fact.setNamespaceAware(false);
    	DocumentBuilder builder = fact.newDocumentBuilder();
    	
    	return builder.parse(new InputSource( new StringReader(xml)));	
    }
    
    public static String readFile(String file) throws IOException {
    	BufferedReader reader = new BufferedReader( new FileReader(file));
    	String line = null;
    	StringBuilder stringBuilder = new StringBuilder();
    	String ls = System.getProperty("line.separator");
    	
    	while((line = reader.readLine()) != null) {
    		stringBuilder.append( line );
    		stringBuilder.append(ls);
    	}
    	reader.close();
    	return stringBuilder.toString();
    }
    
    public static String domToString(Document doc) {
    	
    	TransformerFactory tfact = TransformerFactory.newInstance();
    	Transformer trans = null;
		try {
			trans = tfact.newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    	trans.setOutputProperty(OutputKeys.INDENT, "yes");
    	
    	StringWriter sw = new StringWriter();
    	StreamResult res = new StreamResult(sw);
    	DOMSource src = new DOMSource(doc);
    	try {
			trans.transform(src, res);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String sxml = sw.toString();
    	
    	return sxml;
    	
    }
   
    
    public static String getParentFormXPath (String path) {
		String [] t = path.split("//");
		
		String out = "";
		int i = 0;
		for(i = t.length-1; i >= 0; i--) {
			if (t[i].toUpperCase().matches("FORM.*") == true) {
				break;
			}
		}
		
		for (int k = 0; k <= i; k++ ) {
			out += t[k];
			
			if(k != i)
				out += "//";
		}
		
		return out;
		
    }
    
    public static String doGetXPathFromNode(Node node) {
    	assert(node != null);
    	String s = null;
    	logger.debug("getXPathFromNode : " + node.getNodeName());
    	//s = getXPathFromNode(node, "");
    	s = getShortXPathFromNode(node, "");
    	return s;
    }

    
    private static String getShortXPathFromNode(Node node, String path) {
    	if (node == null)
    		return "";
    			
    	String ename = "";
    	
    	if (node instanceof Element) {
    		int i = 0;
    		boolean append = false;
    		ename = ((Element)node).getNodeName();
    		
    		
    		if(!ename.equals("HTML") 
    				&& !ename.equals("TABLE") 
    				&& !ename.equals("TBODY") 
    				&& !ename.equals("TR") 
    				&& !ename.equals("TD")
    				&& !ename.equals("BR")
    				&& !ename.equals("UL")
    				&& !ename.equals("LI")
    				&& !ename.equals("P")
    				&& !ename.equals("DIV")) {
    		
	    		NamedNodeMap nmap = node.getAttributes();
	    		String alist = "[";
	    		for(i = 0; i < nmap.getLength(); i++) {

	    			Node attr = nmap.item(i);
	    			
	    			// filter out specific (dangerous) attributes
	    			if(attr.getNodeName().equals("onclick") || 
	    					attr.getNodeName().equals("value") ||
	    					attr.getNodeName().equals("onload"))
	    				continue;
	    			
	    			if(append) {
	    				alist += " and ";
	    			}
	    			
	    			alist += "@" + attr.getNodeName() + "='" + attr.getNodeValue() + "'";
	    			append = true;
	    		}
	    		alist += "]";
	    		if (i > 0)
	    			ename += alist;
    		} else {
    			
    			ename = "";
    		
    		}
    	}
    	
    	Node parent = node.getParentNode();
    	if(parent == null | ename.equals("HTML")) {
    		return path;
    	}
    	
    	String pfx = ename.length() > 0 ? "//" + ename : "";
    	
    	return getShortXPathFromNode(parent, pfx + path);
    }

    
    public static String getNormalizedDom(String html) {
    	
    	//String wPat = "[a-zA-Z0-9=\"\' ]*";
    	//String valPat = "VALUE=[\"\']" + wPat + "[\"|\']";
    	//String txtPat = "TYPE=[\"\']TEXT[\"|\']";
    	//String pwdPat = "TYPE=[\"\']TEXT[\"|\']";
    	
    	String s = Pattern.quote(html.toUpperCase());
    	//String s = html;
    	
    	s = s.replaceAll("\t", " ");
    	s = s.replaceAll("\n", " ");
    	s = s.replaceAll(" +", " ");
    	//s = s.replaceAll("<SCRIPT.*>.*</SCRIPT>", "");
    	s = s.replaceAll("> *", ">");
    	s = s.replaceAll(" *<", "<");
    	
    	/**s = s.replaceAll("(< *INPUT" + wPat + " " + txtPat + wPat + ") " + valPat +"(" + wPat + ">)","$1 VALUE='' $2");
    	s = s.replaceAll("(< *INPUT" + wPat + ") " + valPat + "( " + wPat + txtPat  + wPat + ">)", "$1 VALUE='' $2");
    	
    	s = s.replaceAll("(< *INPUT" + wPat + " " + pwdPat + wPat + ") " + valPat +"(" + wPat + ">)","$1 VALUE='' $2");
    	s = s.replaceAll("(< *INPUT" + wPat + ") " + valPat + "( " + wPat + pwdPat  + wPat + ">)", "$1 VALUE='' $2");**/
    	
    	s = s.replaceAll("(<\\w+)[^>]*(>)","$1$2");
    	
    	return s;
    }
    
    
    
    public static HashMap<String,List<String>> getTableInfo(String s) {
    
		HashMap<String,List<String>> pat = new HashMap<String,List<String>>();
		
		String termSplit = s.replaceAll(" *([^ ,=]* *= *['\"][^'\"=]*['\"])[ *,?]", "\t$1\n");
		termSplit = termSplit.replaceAll("[^\t]*\t(.*)\n[^\n]*", "$1\n");
		
		String [] cols = termSplit.split("\n");
		
		for(String col : cols) {
			String key = col.replaceAll("([^=]*)=.*", "$1");
			logger.debug("col: " + col);
			if(pat.containsKey(key)) {
				pat.get(key).add(col);
			} else {
				List <String> v = new Vector<String>();
				v.add(col);
				pat.put(key,v);
			}
		}
		
		return pat;
    
    }
    
}
