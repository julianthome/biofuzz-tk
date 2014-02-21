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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;


import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackTag;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;
import org.biofuzztk.ptree.BioFuzzParseNode;

public class BioFuzzParseTree {

	final static Logger logger = LoggerFactory.getLogger(BioFuzzParseTree.class);

	private BioFuzzParseNode root;
	private BioFuzzTokLst tokLst;
	private Set<String> ntSet;
	private Set<String> ntSuffixSet;
	// for quicker lookup
	private Map<String, List<BioFuzzParseNode>> ntDict;
	private int pfxBarrier;

	private BioFuzzParseNode lnt;
	private BioFuzzParseNode lt;

	public BioFuzzParseTree(String [] tokLst) {

		this.ntDict = new HashMap<String, List<BioFuzzParseNode>>();
		this.ntSet = new HashSet<String>();
		this.ntSuffixSet = new HashSet<String>();
		this.tokLst = new BioFuzzTokLst(tokLst);
		this.pfxBarrier = 0;
		this.root = new BioFuzzParseNode(this);
		this.lnt = root;
		this.lt = root;

	}
	
	public BioFuzzParseTree() {

		this.ntDict = new HashMap<String, List<BioFuzzParseNode>>();
		this.ntSet = new HashSet<String>();
		this.ntSuffixSet = new HashSet<String>();
		//this.tokLst = new BioFuzzTokLst(tokLst);
		this.tokLst = null;
		this.pfxBarrier = 0;
		this.root = new BioFuzzParseNode(this);
		this.lnt = root;
		this.lt = root;

	}
	
	
	public BioFuzzParseTree(BioFuzzParseTree tree) {
		this.ntDict = new HashMap<String, List<BioFuzzParseNode>>();
		this.ntSet = new HashSet<String>();
		this.ntSuffixSet = new HashSet<String>();
		if(tree.tokLst != null) {
			this.tokLst = new BioFuzzTokLst(tree.getTokLst());
		} else {
			this.tokLst = null;
		}
		this.pfxBarrier = tree.pfxBarrier;
		
		this.root = new BioFuzzParseNode(this,null,tree.root);
		
	}

	public BioFuzzParseNode getRootNode() {
		return root;
	}

	public void setRoot(BioFuzzParseNode root) {
		this.root = root;
		this.lnt = root;
		this.lt = root;
	}

	public BioFuzzTokLst getTokLst() {
		return this.tokLst;
	}

	public Set<String> getNtSet() {
		return this.ntSet;
	}
	
	public Set<String> getNtSuffixSet() {
		return this.ntSuffixSet;
		
	}

	public void setNtSet(Set<String> ntSet) {
		this.ntSet = ntSet;
	}

	public Map<String, List<BioFuzzParseNode>> getNtDict() {
		return ntDict;
	}

	public void setNtDict(Map<String, List<BioFuzzParseNode>> ntDict) {
		this.ntDict = ntDict;
	}

	public int getPfxBarrier() {
		return pfxBarrier;
	}

	public void setPfxBarrier(int pfxBarrier) {
		this.pfxBarrier = pfxBarrier;
	}

	public void doResetNtSet() {
		this.ntDict.clear();
		this.ntSet.clear();
		this.ntSuffixSet.clear();
		assert(this.root != null);
		resetNtSet(this.root);
	}
	
	public boolean getVal() {
		assert(this.root != null);
		return this.root.getVal();
	}

	private void resetNtSet(BioFuzzParseNode node) {
		assert(node != null);
		BioFuzzAttackTag atag = node.getAtag();
		assert(atag != null);
		
		if ((atag.getTagType() == TagType.ROOT || atag.getTagType() == TagType.NON_TERMINAL) && 
				node.getVal()) {
			addNtElem(node.getAtagName(), node);
		}

		assert(node.getChildren() != null);
		
		for(BioFuzzParseNode child : node.getChildren()) {
			resetNtSet(child);
		}

	}

	public void addNtElem(String ntName, BioFuzzParseNode elem) {

		//logger.debug("add " + ntName + "to set of NTs");
		
		// root is a non-terminal but should not be in this set
		// since it is required for crossover
		if(elem.getAtagType() == TagType.ROOT || elem.getAtagType() == TagType.TERMINAL ||
				elem.getAtagType() == TagType.TOK_TERMINAL) {
			return;
		}
		
		assert(elem != null);
		
		// this part is important for crossover
		if(elem.getTokIdx() > this.pfxBarrier) {
			this.ntSuffixSet.add(ntName);
		}
	
		this.ntSet.add(ntName);

		if(!this.ntDict.containsKey(ntName)) {
			this.ntDict.put(ntName, new ArrayList<BioFuzzParseNode>());
		}

		this.ntDict.get(ntName).add(elem);
	}

	public List<BioFuzzParseNode> getNtsByName(String name) {
		if (this.ntDict.containsKey(name)) {
			return this.ntDict.get(name);
		}
		return null;
	}

	public BioFuzzParseNode getNtByName(String name) {
		List<BioFuzzParseNode> lst = getNtsByName(name);
		Random rand = new Random();

		if (lst != null) {
			BioFuzzParseNode rnode = lst.get(rand.nextInt(lst.size()));
			assert(rnode != null);
			return rnode;
		} else {
			return null;
		}

	}

	public int doGetNodeCnt() {
		assert(this.root != null);
		return getNodeCnt(this.root);
	}

	public void setTokLst(BioFuzzTokLst tokLst) {
		this.tokLst = tokLst;
	}

	private int getNodeCnt(BioFuzzParseNode node) {
		if(node == null)
			return 0;

		int s = 1;

		List<BioFuzzParseNode> children = null;

		if (node.hasChildren() && (children = node.getChildren()) != null) {
			Iterator<BioFuzzParseNode> iter = children.iterator();

			while(iter.hasNext()) {
				s += getNodeCnt(iter.next());
			}
		}
		return s;
	}

	public BioFuzzParseNode doGetNodeByIdx(int tokIdx) {
		return getNodeByIdx(this.root, tokIdx);
	}

	private BioFuzzParseNode getNodeByIdx(BioFuzzParseNode node, int tokIdx) {

		if (node.getTokIdx() == tokIdx) {
			return node;
		} 

		List<BioFuzzParseNode> children = null;

		if ((children = node.getChildren()) != null && node.hasChildren()) {
			Iterator<BioFuzzParseNode> iter = children.iterator();
			while (iter.hasNext()) {
				BioFuzzParseNode ret = getNodeByIdx(iter.next(), tokIdx);
				if (ret != null)
					return ret;
			}
		}

		return null;
	}
	
	public BioFuzzParseNode doGetNodeById(int id) {
		if(id == 0) {
			return this.root;
		} else {
			return getNodeById(this.root,id);
		}
	}

	private BioFuzzParseNode getNodeById(BioFuzzParseNode node, int id) {
		
		BioFuzzParseNode res = null;
		
		
		if(node == null)
			return res;
		
		//logger.debug("node id: " + node.getId() + "id " + id);
		
		if (node.getId() == id) {
			//logger.debug("here");
			res = node;
		} 

		if (node.hasChildren() && res == null) {
			for(BioFuzzParseNode child : node.getChildren()) {
				if((res = getNodeById(child, id)) != null) {
					break;
				}
			}
		}
		
		return res;
	}



	public String getStrByIdx(int sIdx, int eIdx) {
		assert(eIdx > sIdx);

		String s = "";

		assert(eIdx < (this.tokLst.getSize() - 1));

		for(int idx=sIdx+1; idx < eIdx +1; idx++ ) {
			s += this.tokLst.get(idx) + ' ';
		}

		return s;

	}
	
	public String getTokByIdx(int idx) {
		assert(this.tokLst != null);
		assert(idx < this.tokLst.getSize() - 1);
		return this.tokLst.get(idx);
	}
	
	public String getTokByNode(BioFuzzParseNode node) {
		
		assert(node != null);
		assert(node.getMyTree() == this);
		
		return getTokByIdx(node.getTokIdx());
	}


	public void setLnt(BioFuzzParseNode lnt) {
		this.lnt = lnt;
	}

	public void setLt(BioFuzzParseNode lt) {
		this.lt = lt;
	}
	
	public void setPfxBarrierToLastTok() {
		this.pfxBarrier = this.tokLst.getCursor();
	}

	private String genStr(BioFuzzParseNode node, int level) {
		String s = "";

		List<BioFuzzParseNode> children = null;

		//if ((children = node.getChildren()) != null) {
		if (node.hasChildren()) {
			children = node.getChildren();
			assert(children != null);
			Iterator<BioFuzzParseNode> iter = children.iterator();

			while(iter.hasNext()) {
				BioFuzzParseNode child = iter.next();
				s += StringUtils.repeat(" ", level) + "|--" + child.toString();
				s += "[AttackTag: " + child.getAtagName() +
						"(" + child.getAtagType().toString() + ")" +
						"(" + child.getVal() +")]\n" + genStr(child,level+1);
			}
		} else {
			if(this.tokLst != null) {
				s += StringUtils.repeat(" ", level) + "|--" + node.toString();
				
				s += "[Tok: " + this.tokLst.get(node.getTokIdx()) +
							"(" + node.getAtagType().toString() + ")" +
							"(" + node.getVal() + ")]\n";
			}
		}
		//}
		return s;
	}

	public int getTokLstLen() {
		return this.tokLst.getSize();
	}
	
	public void addCheckpoint(int bp) {
		this.tokLst.addCheckpoint(bp);
	}
	
	
	@Override
	public String toString() {
		String s = "";

		s = "-----------------------<Parse Tree>-----------------------\n";
		s += "* [ ROOT(" + this.root.getAtagName() + "): validity:(" + this.root.getVal() + ")]\n";
		s += genStr(this.root,0);
		s += "\n";
		s += "#Nodes: " + this.doGetNodeCnt() + "\n";
		s += "#NTs: " + this.ntSet.size() + "\n";
		s += "NTs: " + this.ntSet + "\n";
		s += "NTUbound: " + this.ntSuffixSet + "\n";
		s += "Last NT: " + this.lnt + "\n";
		
		if(tokLst != null) {
			s += "TokLst: " + this.tokLst.toString() + "\n";
			s += "TokLstStr: " + this.tokLst.getStrFromTokens() + "\n";
		}
		s += "Pfx Barrier: " + this.pfxBarrier + "\n";
		s += "-----------------------------------------------------------\n";

		return s;
	}
}
