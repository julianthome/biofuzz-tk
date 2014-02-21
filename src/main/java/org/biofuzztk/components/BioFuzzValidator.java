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

package org.biofuzztk.components;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackCfg;
import org.biofuzztk.cfg.BioFuzzAttackCfgMgr;
import org.biofuzztk.cfg.BioFuzzAttackTag;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;
import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;

public class BioFuzzValidator {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzValidator.class);
	
	private BioFuzzAttackCfgMgr mgr;
	
	public BioFuzzValidator(BioFuzzAttackCfgMgr mgr) {
		this.mgr = mgr;
		logger.debug(this.mgr.toString());	
	}
	
	public void doValidate(BioFuzzParseTree tree) {
		assert(tree != null);
		BioFuzzParseNode root = tree.getRootNode();
		assert(root != null);
		Boolean ret = validate(root);
		root.setVal(ret);
	}
	
	private Boolean validate(BioFuzzParseNode node) {
        BioFuzzAttackTag atag = node.getAtag();
        BioFuzzAttackTag nxtAtag = null;

        Boolean res = true;
        	
        if (node.hasChildren()) {
        	List<BioFuzzParseNode> children = node.getChildren();
        	assert(children != null);
        	Iterator<BioFuzzParseNode> iter = children.iterator();
        	
        	while(iter.hasNext()) {
        		BioFuzzParseNode child = iter.next();
        		//logger.debug("next child");
        		res = res && validate(child);
        	}
        	
        	BioFuzzParseNode ron = children.get(children.size()-1);
        	BioFuzzAttackCfg ronCfg = ron.getCfg();
        	assert(ronCfg != null);
        	List<Number> nxtChoices = ron.getChoices();
        	
        	if(nxtChoices == null) {
        		logger.debug(ronCfg.toString());
        		logger.debug(ron.getAtagName());
        	}
        	assert(nxtChoices != null);
        	//List<Number> nxtChoices = node.getChoices();
        	int myChoice = 0;

        	myChoice = nxtChoices.get(nxtChoices.size()-1).intValue();
        	//logger.debug("strict choice: " + myChoice);

        	nxtAtag = ronCfg.getAtagByIdx(myChoice);
        	
        	
        	if (nxtAtag.getTagType() == TagType.STOP && res == true) {
        		//logger.debug("setting true for " + ron.getAtagName());
        		node.setVal(true);
        		res = true;
        	} else {
        		if (res == false) {
        			logger.debug( "RES is false - node " + node.getAtagName() + " invalid");
        		}
        		node.setVal(false);
        		res = false;
        	}     	
        }
        //logger.debug("O " + atag.getTagType());
        if(atag != null && (atag.getTagType() == TagType.TERMINAL ||
        		atag.getTagType() == TagType.REGEXP ||
        		atag.getTagType() == TagType.TOK_TERMINAL)) {
        	node.setVal(true);
        	return true;
        }
        return res;
	}
        

}
