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

package org.biofuzztk.cfg;

import java.io.File;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;



public class BioFuzzConfigReader {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzConfigReader.class);
	
	public BioFuzzConfigReader() {
	}
	
	private static Document read(String fname) {
		try {
			File xmlfile = new File(fname);
			DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFact.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlfile);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	
	private static BioFuzzAttackTag createAndRegAttackTag(Node node, String label, TagType type, BioFuzzAttackCfg cfg) {
		BioFuzzAttackTag atag = new BioFuzzAttackTag(node,label,type,cfg.getDescNrs());
		cfg.addAtag(atag);
		return atag;
	}
	
	private static void handleTok(String ruleName, Node node, BioFuzzAttackCfg cfg, 
			Stack<BioFuzzAttackTag> predStack) {
		
		
		logger.debug("handle Tok called for " + node.getNodeName().toUpperCase());
		logger.debug("Pred Stack CALL:" + predStack.toString());
		switch(node.getNodeName().toUpperCase()){
		
		case "CONST": {
			String label = node.getAttributes().getNamedItem("label").getNodeValue();
			
			BioFuzzAttackTag atag = createAndRegAttackTag(node,label,TagType.TERMINAL,cfg);
			
			while(predStack.size() > 0) {
				BioFuzzAttackTag elem = predStack.pop();
				logger.debug("CONST :" + elem.getName() + " --> " + atag.getName() );
				logger.debug("Coord : " + elem.getCoord() + " " + atag.getCoord() );
				cfg.addPoint(elem.getCoord(), atag.getCoord());
			}
			
			predStack.add(atag);
		}
		break;
		case "REGEXP": {
			String label = node.getAttributes().getNamedItem("label").getNodeValue();
			BioFuzzAttackTag atag = createAndRegAttackTag(node,label,TagType.REGEXP,cfg);
			
			while(predStack.size() > 0) {
				BioFuzzAttackTag elem = predStack.pop();
				logger.debug("REGEXP :" + elem.getName() + " --> " + atag.getName() );
				cfg.addPoint(elem.getCoord(), atag.getCoord());
			}
			
			predStack.add(atag);
		}
		break;
		case "VAR": {
			String label = node.getAttributes().getNamedItem("label").getNodeValue();
			BioFuzzAttackTag atag = createAndRegAttackTag(node,label,TagType.NON_TERMINAL,cfg);
			
			while(predStack.size() > 0) {
				BioFuzzAttackTag elem = predStack.pop();
				logger.debug("VAR :" + elem.getName() + " --> " + atag.getName() );
				cfg.addPoint(elem.getCoord(), atag.getCoord());
			}
			
			predStack.add(atag);
		}
		break;
		case "START": {
			assert(predStack.size() <= 0);
			
			BioFuzzAttackTag atag = createAndRegAttackTag(node,"^",TagType.START,cfg);
			predStack.add(atag);
		}
		break;
		case "STOP": {
			BioFuzzAttackTag atag = createAndRegAttackTag(node,"$",TagType.STOP,cfg);
			
			while(predStack.size() > 0) {
				BioFuzzAttackTag elem = predStack.pop();
				logger.debug("STOP :" + elem.getName() + " --> " + atag.getName() );
				cfg.addPoint(elem.getCoord(), atag.getCoord());
			}
			
			predStack.add(atag);
		}
		break;
		case "GRP": {
			logger.debug("GRP");
			Node grpson = getNxtChildNode(node);
			while(grpson != null) {
				handleTok(ruleName,grpson, cfg, predStack);
				grpson = getNxtNode(grpson);
			}
		}
		break;
		case "ZORONE": {
			logger.debug("ZORONE");
			logger.debug("Pred Stack BEFORE:" + predStack.toString());
			Stack<BioFuzzAttackTag> zonePred = new Stack<BioFuzzAttackTag> ();
			
			if (predStack.size() > 0) {
				//zonePred.add(predStack.get(predStack.size()-1));
				zonePred.addAll(predStack);
			}
			
			Node zoneson = getNxtChildNode(node);
			while(zoneson != null) {
				handleTok(ruleName,zoneson, cfg, zonePred);
				zoneson = getNxtNode(zoneson);
			}
			
			/**if (zonePred.size() > 0)
				predStack.push(zonePred.get(zonePred.size()-1));**/
			predStack.addAll(zonePred);
			logger.debug("Pred Stack AFTER:" + predStack.toString());
				
		}
		break;
		case "ONEOF": {
			logger.debug("ONEOF");
//			BioFuzzAttackTag lelem = null;
//			if (predStack.size() > 0)
//				lelem = predStack.pop();
			

			// Create a copy of predecessors
			Stack <BioFuzzAttackTag> predStackCp = new Stack <BioFuzzAttackTag>();
			predStackCp.addAll(predStack);
			predStack.clear();
			Node ofson = getNxtChildNode(node);
			
			while(ofson != null) {
				Stack <BioFuzzAttackTag> ofstack = new Stack <BioFuzzAttackTag>();
				
//				if (lelem != null)
//					ofstack.push(lelem);
				
				ofstack.addAll(predStackCp);
				handleTok(ruleName,ofson, cfg, ofstack);
				
				while(ofstack.size() > 0) {
					predStack.push(ofstack.pop());
				}
				
				//predStack.addAll(ofstack);
				ofson = getNxtNode(ofson);
			}
			
		}
		break;
		case "ZORMORE": {
			logger.debug("ZORMORE");
			logger.debug("Pred Stack BEFORE:" + predStack.toString());
			Stack <BioFuzzAttackTag> zostack = new Stack<BioFuzzAttackTag>();
			BioFuzzAttackTag first = null,last = null;
			
			/**if (predStack.size() > 0) 
				zostack.push(predStack.get(predStack.size()-1));**/
			
			zostack.addAll(predStack);
			
			Node zoson = getNxtChildNode(node);
			
			while(zoson != null) {
				handleTok(ruleName,zoson,cfg,zostack);
				if (first == null && zostack.size() > 0)
					first = zostack.get(zostack.size()-1);
					assert(first != null);
				zoson = getNxtNode(zoson);
			}
			
			
			if (zostack.size() > 0) {
				last = zostack.get(zostack.size()-1);
				cfg.addPoint(last.getCoord(), first.getCoord());
				//predStack.push(last);
				predStack.addAll(zostack);
			}
			logger.debug("Pred Stack AFTER:" + predStack.toString());
			
		}
		break;
			

		}
		
	}
	
	private static Node getNxtNodeByName(Node sibling, String name) {
		
		Node nxt = sibling;
		
		while(nxt != null) {
			if (nxt.getNodeType() == Node.ELEMENT_NODE && 
					nxt.getNodeName().equals(name))
				return nxt;
			nxt = nxt.getNextSibling();
		}
		
		return null;
	}
	
	private static Node getNxtNode(Node sibling) {
		Node nxt = sibling.getNextSibling();
		
		while(nxt != null) {
			if (nxt.getNodeType() == Node.ELEMENT_NODE)
				return nxt;
			nxt = nxt.getNextSibling();
		}
		
		return null;
	}
	
	private static Node getNxtChildNode(Node father) {
		Node nxt = father.getFirstChild();
		
		while(nxt != null) {
			if (nxt.getNodeType() == Node.ELEMENT_NODE)
				return nxt;
			nxt = nxt.getNextSibling();
		}
		
		return null;
	}
	
	
	public static BioFuzzAttackCfgMgr readConfigFile(String file) {
		System.out.println("parse");
		Document doc = read(file);
		
		NodeList rules = doc.getElementsByTagName("rule");
		
		BioFuzzAttackCfgMgr mgr = new BioFuzzAttackCfgMgr();
		
		// Iterate over rules
		for(int i = 0; i < rules.getLength(); i++) {
			Node currule = rules.item(i);
			
			// get next key node
			Node key = getNxtNodeByName(currule.getFirstChild(), "key");
			// get next val node
			Node val = getNxtNodeByName(currule.getFirstChild(), "val");
				
			assert(key.getNodeName().equals("key") == true);
			assert(val.getNodeName().equals("val") == true);
			
			logger.debug("Node Name " + key.getNodeName());
			logger.debug("Node Value " + val.getNodeName());
				
			String ruleName = key.getAttributes().getNamedItem("label").getNodeValue();
			
			// Create a new attack tag with rule name
			BioFuzzAttackCfg cfg = mgr.createAttackCfg(ruleName);
			Stack<BioFuzzAttackTag> predStack = new Stack<BioFuzzAttackTag>();
			
			Node valson = getNxtChildNode(val);
			while (valson != null){
				handleTok(ruleName,valson,cfg,predStack);	
				valson = getNxtNode(valson);
			}
		}
		
		return mgr;	
	}

}
