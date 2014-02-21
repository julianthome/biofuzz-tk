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

import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackCfg;
import org.biofuzztk.cfg.BioFuzzAttackCfgMgr;
import org.biofuzztk.cfg.BioFuzzAttackTag;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;
import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.biofuzztk.ptree.BioFuzzTokLst;

public class BioFuzzTokGen {

	final static Logger logger = LoggerFactory.getLogger(BioFuzzTokGen.class);

	private List<BioFuzzParseNode> exPnts = null;

	private BioFuzzAttackCfgMgr mgr;

	public BioFuzzTokGen(BioFuzzAttackCfgMgr mgr) {
		this.mgr = mgr;
		logger.debug(this.mgr.toString());
		this.exPnts = new Vector<BioFuzzParseNode> ();
	}

	private void doGetExtPt(BioFuzzParseTree tree) {
		if(tree.getVal()) {
			logger.debug("Tree is valid");
			getExtPtVal(tree.getRootNode());
		} else {
			logger.debug("Tree is invalid");
			getExtPtInval(tree.getRootNode());
		}
	}


	private void getExtPtInval(BioFuzzParseNode node) {

		logger.debug("inval");
		if(node == null || node.hasChildren() == false)
			return;

		if(node.getVal() == false && node.getAtagType() == TagType.NON_TERMINAL || 
				node.getAtagType() == TagType.ROOT) {
			this.exPnts.add(node);
		}


		getExtPtInval(node.getRoc());
	}


	private void getExtPtVal(BioFuzzParseNode node) {
		logger.debug("val");
		if(node == null || node.hasChildren() == false)
			return;

		List<Number> nxtChoices = node.getChoices();
		
		
		logger.debug(" -- choices are " + nxtChoices + " for node " + node.getAtagName());
		logger.debug("--- desc idx: " + node.getDescIdx());
		if(nxtChoices.size() > 1) {
			if(node.hasParent())
				this.exPnts.add(node.getParent());
		}


		getExtPtVal(node.getRoc());

	}

	public TagType doExtendTree(BioFuzzParseTree tree) {

		logger.debug("do Extend Tree");

		this.exPnts.clear();
		//logger.debug(this.mgr.toString());
		doGetExtPt(tree);

		if(this.exPnts.size() <= 0) {
			logger.debug("no extension point determined");
			return TagType.STOP;
		}
		
		logger.debug("Extension Points");

		for(BioFuzzParseNode n : this.exPnts) {
			logger.debug("Point: "  + n.getAtagName());
		}
		
		BioFuzzParseNode lnt = null;
		
		if(tree.getVal()) {
			
			Random rand = new Random();
			int idx = rand.nextInt(this.exPnts.size());
			lnt = this.exPnts.get(idx);
		} else {
			
			lnt = this.exPnts.get(this.exPnts.size()-1);
		}

		assert(lnt != null);
		
		logger.debug("extension point is " + lnt.getAtagType() + " " + lnt.getAtagName());

		BioFuzzParseNode roc = lnt;
		if(lnt.hasChildren())
			roc = lnt.getRoc();

		logger.debug("cfg " + roc.getCfg().toString());

		logger.debug("right outermost child " + roc.getAtagType() + " " + roc.getAtagName());
		logger.debug("control: " + this.mgr.getAttackCfgByKey(roc.getAtagName()));


		BioFuzzTokLst tokLst = tree.getTokLst();
		assert(tokLst != null);

		tokLst.flushLfr();
		tokLst.pushLfr(roc.getDescIdx());

		return extendTree(roc.getCfg(),lnt,tree);

	}


	private TagType extendTree(BioFuzzAttackCfg cfg, BioFuzzParseNode node, 
			BioFuzzParseTree tree) {

		assert(cfg != null);
		assert(node != null);
		assert(tree != null);

		BioFuzzTokLst tokLst = tree.getTokLst();
		logger.debug("Node: " + node.getAtagName());
		logger.debug("TokLst: " + tokLst.toString());
		logger.debug("Config: " + cfg);

		// get last firing rule
		int lfr = tokLst.popLfr();
		logger.debug("Lfr: " + lfr);

		List<Number> choices = cfg.getChoicesByIdx(lfr);

		logger.debug("choices: " + choices);

		// Get random choice
		Random rand = new Random();
		int choice = 0;
		if(choices.size() > 1)
			choice = rand.nextInt(choices.size());

		int nxtChoice = choices.get(choice).intValue();

		logger.debug("nxtChoice is: " + nxtChoice);

		BioFuzzAttackTag atag = cfg.getAtagByIdx(nxtChoice);

		logger.debug("Token to add is: " + atag.getName());
		logger.debug(atag.getTagType() + " detected");
		switch(atag.getTagType()) {

		case NON_TERMINAL: {
			logger.debug("Type: NON_TERMINAL");
			// non terminals do always have a fitness value of 1
			BioFuzzParseNode ntNode = new BioFuzzParseNode(tree,cfg,nxtChoice,tokLst.getCursor(), node.getId() + 1);
			tokLst.pushLfr(0);
			// Resolve destination configuration
			logger.debug("get configuration");
			logger.debug("get config for " + atag.getName());
			BioFuzzAttackCfg ntCfg = this.mgr.getAttackCfgByKey(atag.getName());
			assert(ntCfg != null);

			logger.debug("extend Tree");
			TagType ret = extendTree(ntCfg, ntNode, tree);
			logger.debug("done");

			if( ret == TagType.NON_TERMINAL 
					|| ret == TagType.TERMINAL 
					|| ret == TagType.REGEXP
					|| ret == TagType.STOP) {

				tree.addNtElem(ntNode.getAtagName(), ntNode);

				// return from recursive call - go back to previous lfr
				tokLst.popLfr();

				tokLst.pushLfr(nxtChoice);
				node.addChild(ntNode);

				logger.debug(atag.getTagType() + " + node added");

				return TagType.NON_TERMINAL;
			} else if (ret == TagType.ERROR ) {
				if (tokLst.isFinished()) {
					tokLst.popLfr();
					tokLst.pushLfr(choice);
					node.addChild(ntNode);
					logger.debug(atag.getTagType() + " + node added");

					return TagType.ERROR;	

				} 

			} else {
				// ret is equals STOP or START
				tokLst.popLfr();
				return ret;
			}

		}
		case TERMINAL:
			logger.debug("Type: TERMINAL");
		case TOK_TERMINAL: {
			logger.debug("Type: TOK_TERMINAL");
			tokLst.append(atag.getName());
			BioFuzzParseNode tNode = new BioFuzzParseNode(tree, cfg,nxtChoice,tokLst.getCursor()-1, node.getId()+1);
			tokLst.pushLfr(choice);
			node.addChild(tNode);
			logger.debug(atag.getTagType() + " + node added");
			// Switch cursor to next token
			return TagType.TERMINAL;
		}
		case REGEXP: {
			logger.debug("Type: REGEXP");
			String rexp = atag.getName();
			BioFuzzRexpGen rexpGen = new BioFuzzRexpGen(rexp);
			String s = rexpGen.doGenerate();

			assert(s != null);

			logger.debug("String generated from regex: " + rexp + ": '" + s + "'");
			tokLst.append(s);
			BioFuzzParseNode tNode = new BioFuzzParseNode(tree, cfg,nxtChoice,tokLst.getCursor()-1, node.getId() +1);
			tokLst.pushLfr(choice);
			node.addChild(tNode);
			logger.debug(atag.getTagType() + " + node added");
			return TagType.REGEXP;
		}
		case STOP: {
			logger.debug("Type: STOP");
			return TagType.STOP;
		}
		default:
			break;

		}
		return TagType.ERROR;
	}
}