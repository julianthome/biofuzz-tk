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

package org.biofuzztk.components.parser;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackCfg;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;
import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.biofuzztk.ptree.BioFuzzTokLst;

public class BioFuzzTupleStack {
    private Stack<BioFuzzParsingTuple> tstack;
    private BioFuzzParsingStatus status;
    private int cur;
    private BioFuzzParseTree tree;
    private BioFuzzParseNode ptr;
    private int nodeCnt;
    private String key;
    
    final static Logger logger = LoggerFactory.getLogger(BioFuzzTupleStack.class);
    
    public BioFuzzTupleStack(String key, int cur) {
    	this.tstack = new Stack<BioFuzzParsingTuple>();
    	this.status = BioFuzzParsingStatus.IN_PROGRESS;
    	this.cur = cur;
    	this.tree = null;
    	this.ptr = null;
    	this.key = key;
    	
    	
    	this.nodeCnt = 1;
    }
    
    public BioFuzzTupleStack(BioFuzzTupleStack t) {	    	
    	this(t.key, t.cur);
    	assert(t.ptr != null);
    	assert(t.tree != null);
    	
    	this.tstack.addAll(t.tstack);
    	
    	//slogger.debug("Tree to copy :" + t.toString());
    	
    	this.tree = new BioFuzzParseTree(t.tree);
    	
    	//ogger.debug("CP: " + tree.toString());
    	
    	//logger.debug("search node with id " + t.ptr.getId());
    	this.ptr = this.tree.doGetNodeById(t.ptr.getId());
    	this.nodeCnt = t.nodeCnt;
    	assert(this.ptr != null);
    }
    
    public void pushTuple(BioFuzzAttackCfg cfg, TagType t, int lfr) {
    	BioFuzzParsingTuple tup = new BioFuzzParsingTuple(cfg,lfr);
    	this.tstack.push(tup);
    	
    	
    	switch(t) {
	    	case START:
	    		//logger.debug("start - nothing to do");
	    		break;
	    	case NON_TERMINAL: { 
	    		//logger.debug("add non terminal to tree");
	    		BioFuzzParseNode nt = new BioFuzzParseNode(this.tree,cfg,lfr,this.cur, this.nodeCnt++);
	    		ptr.addChild(nt);
	    		this.ptr = nt;
	    	}
	    	break;
	    	case TERMINAL:
	    	case REGEXP:{
	    		//logger.debug("add terminal to tree");
	    		BioFuzzParseNode term = new BioFuzzParseNode(this.tree,cfg,lfr,this.cur, this.nodeCnt++);
	    		this.ptr.addChild(term);
	    	}
	    	break;
	    	case ROOT: {
	    		//logger.debug("add root to tree");
	    		this.tree = new BioFuzzParseTree();
	    		this.ptr = this.tree.getRootNode();
	    		this.ptr.setCfg(cfg);
	    		this.ptr.setAtagName(key);
	    		//logger.debug(this.tree.toString());
	    	}
	    	break;
		default:
			break;
    	}
    	
    	
    }
    
    public void rollback() {
		BioFuzzParsingTuple tmp = this.popTuple();

		assert(tmp != null);
		
		//logger.debug("tmp " + tmp.toString());
		
		while(tmp != null) {
			
			if(tmp != null && tmp.getLfr() == 0) {
				break;
			}
			tmp = this.popTuple();
			
		}

		if(ptr.hasParent())
			ptr = ptr.getParent();

    }
    
    public BioFuzzParsingTuple popTuple() {
    	if(this.tstack.size() > 0) {
    		//logger.debug("pop it");
    		return this.tstack.pop();
    	} else {
    		return null;
    	}
    }
    
    public BioFuzzParsingTuple getTuple(int idx) {
    	if(this.tstack.size() > idx && idx >= 0)
    		return this.tstack.get(idx);
    	else
    		return null;
    }
    
    public BioFuzzParsingTuple getLastTuple() {
    	//return getTuple(this.tstack.size()-1);
    	if(this.tstack.size() > 0)
    		return this.tstack.lastElement();
    	else
    		return null;
    }
    
    public int getSize() {
    	return this.tstack.size();
    }
    
    public void changeStatus(BioFuzzParsingStatus status) {
    	this.status = status;
    }
    
    public void setTokLst(BioFuzzTokLst tokLst) {
    	BioFuzzTokLst t = new BioFuzzTokLst(tokLst);
    	t.setCursor(this.cur);
    	this.tree.setTokLst(t);
    }
    
    public BioFuzzParsingStatus getStatus() {
    	return this.status;
    }
    
    public void setCur(int x) {
    	this.cur = x;
    }
    
    public int getCur() {
    	return this.cur;
    }
    
    public void nxtCur() {
    	this.cur++;
    }
    
    public BioFuzzParseTree getParseTree() {
    	return this.tree;
    }
    
	@Override 
	public String toString() {
		String s = "TUPLE STACK------------------------------\n";
		
		for(int i = 0; i < this.tstack.size(); i++) {
			s += this.tstack.get(i).toString();
		}
		s += tree.toString();
		s += "Status: " + this.status + "\n";
		s += "Cur: " + this.cur + "\n";
		s += "Tstack Size: " + this.tstack.size() + "\n";
		s += "Node Cnt " + this.nodeCnt + "\n";
		s += "ptr " + this.ptr.getId() + "\n";
		s += "----------------------------------------\n";
		
		return s;
	}
}