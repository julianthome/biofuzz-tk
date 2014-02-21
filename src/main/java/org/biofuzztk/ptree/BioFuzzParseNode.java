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

package org.biofuzztk.ptree;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackCfg;
import org.biofuzztk.cfg.BioFuzzAttackTag;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;

public class BioFuzzParseNode {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzParseNode.class);
	
	private int tokIdx;
	private int descIdx;
	private Boolean val;
	private BioFuzzParseNode parent;
	private List<BioFuzzParseNode> children;
	private BioFuzzAttackTag atag;
	private BioFuzzParseTree myTree;
	private int id;
	
	// used to speed up lookup
	private BioFuzzAttackCfg cfg;
	

	public BioFuzzAttackTag getAtag() {
		return atag;
	}

	public void setAtag(BioFuzzAttackTag atag) {
		this.atag = atag;
	}

	public BioFuzzParseNode(BioFuzzParseTree myTree, 
			BioFuzzAttackCfg cfg, int descIdx, int tokIdx, int id) {
		this.myTree = myTree;
		this.tokIdx = tokIdx;
		this.descIdx = descIdx;
		this.val= false;
		this.cfg = cfg;
		this.children = new Vector<BioFuzzParseNode>();
		this.parent = null;
		this.atag = this.cfg.getAtagByIdx(descIdx);
		this.id = id;

	}
	
	/**
	 * Only for root
	 * @param myTree
	 */
	public BioFuzzParseNode(BioFuzzParseTree myTree){
		this.myTree = myTree;
		this.tokIdx = 0;
		this.descIdx = 0;
		this.val = false;
		this.cfg = null;
		this.children =  new Vector<BioFuzzParseNode>();
		this.parent = null;
		this.id = 0;
		// Special attack tag for root
		this.atag = new BioFuzzAttackTag();
	}	
	
	/**
	 * Copy tree
	 * @param myTree
	 * @param parent
	 * @param node
	 */
	public BioFuzzParseNode(BioFuzzParseTree myTree,  BioFuzzParseNode parent, BioFuzzParseNode node){
		this.myTree = myTree;
		this.tokIdx = node.tokIdx;
		this.descIdx = node.descIdx;
		this.val = node.val;
		this.cfg = node.cfg;
		this.id = node.id;
		this.children =  new Vector<BioFuzzParseNode>();
		this.parent = parent;
		this.atag = new BioFuzzAttackTag(node.getAtag());
		
		if (this.atag.getTagType() == TagType.NON_TERMINAL) {
			this.myTree.addNtElem(this.atag.getName(), this);
			this.myTree.setLnt(this);
		} else if (this.atag.getTagType() == TagType.TERMINAL ||
				this.atag.getTagType() == TagType.REGEXP) {
			this.myTree.setLt(this);
		}
		
		if(node.hasChildren()) {
			List<BioFuzzParseNode> children = node.getChildren();
			Iterator<BioFuzzParseNode> iter = children.iterator();
			while(iter.hasNext()) {
				BioFuzzParseNode child = iter.next();
				BioFuzzParseNode newNode = new BioFuzzParseNode(myTree, this , child);
				this.children.add(newNode);
			}
		}
		
	}
	
	public List<Number> getChoices() {
		assert(this.cfg != null);
		return this.cfg.getChoicesByIdx(this.descIdx);
	}
	
	public BioFuzzParseNode getParent() {
		return parent;
	}
	
	public Boolean isRoot() {
		return (this.atag == null);
	}

	public void setParent(BioFuzzParseNode parent) {
		this.parent = parent;
	}

	public int getDescIdx() {
		return descIdx;
	}

	public void setDescIdx(int descIdx) {
		this.descIdx = descIdx;
	}

	
	public BioFuzzParseNode getRoc() {
		if(this.children != null) {
			return children.get(children.size()-1);
		} 
		return null;
	}
	
	public int getChildCnt() {
		if(hasChildren()) {
			return this.children.size();
		}
		return 0;
	}
	
	public void addChild(BioFuzzParseNode child) {
		
		if(child.getAtagType() == TagType.NON_TERMINAL){
			// leave out root element
			if(this.myTree.getPfxBarrier() >= 0)
				this.myTree.addNtElem(child.getAtagName(), child);
			this.myTree.setLnt(child);
		} else {
			this.myTree.setLt(child);
		}
		child.parent = this;
		this.children.add(child);
	}
	
	public boolean removeChild(BioFuzzParseNode node) {
		return this.children.remove(node);
	}

	@Override
	public String toString() {
		return "(tok_idx: " + this.tokIdx + " descIdx: " + this.descIdx + " id:" + this.id + ")";
	}
	
	public int getTokIdx() {
		return tokIdx;
	}
	
	public int getId() {
		return this.id;
	}


	public void setTokIdx(int tokIdx) {
		this.tokIdx = tokIdx;
	}


	public Boolean getVal() {
		return val;
	}


	public void setVal(Boolean val) {
		this.val = val;
	}


	public List<BioFuzzParseNode> getChildren() {
		return children;
	}


	public void setChildren(List<BioFuzzParseNode> children) {
		this.children = children;
	}


	public BioFuzzAttackCfg getCfg() {
		return cfg;
	}


	public void setCfg(BioFuzzAttackCfg cfg) {
		this.cfg = cfg;
	}
	
	public Boolean hasParent() {
		return (this.parent == null ? false : true);
	}
	
	public String getTok() {
		assert(this.myTree != null);
		return this.myTree.getTokByNode(this);
	}
	
	public Boolean hasChildren() {
		return ((this.children != null) && 
				(this.children.size() > 0));
	}
	
	public TagType getAtagType() {
		assert(this.atag != null);
		return this.atag.getTagType();
	}
	
	public String getAtagName() {
		assert(this.atag != null);
		return this.atag.getName();
	}
	
	public String genStr() {
		String s = "";
		s += this.toString();
		
		Iterator<BioFuzzParseNode> iter = this.children.iterator();
		
		while(iter.hasNext()) {
			s += iter.next().toString();
		}
		return s;
		
	}

	public String getTokens() {
		String s = "";
		if (this.hasChildren()) {
			for(BioFuzzParseNode child : this.children) {
				s += ((s.length() > 0) ? " " : "") + child.getTokens();
			}
		} else {
			 return this.getTok();
		}
		return s;
	}
	
	public BioFuzzParseTree getMyTree() {
		return myTree;
	}

	public void setMyTree(BioFuzzParseTree myTree) {
		this.myTree = myTree;
	}
	
	public void setAtagName(String name) {
		assert(this.atag != null);
		this.atag.setName(name);
	}
	

}
