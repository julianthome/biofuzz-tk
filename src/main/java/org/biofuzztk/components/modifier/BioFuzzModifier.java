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

package org.biofuzztk.components.modifier;

import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackCfgMgr;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;
import org.biofuzztk.components.BioFuzzTracer;
import org.biofuzztk.components.BioFuzzTracer.BioFuzzQuery;
import org.biofuzztk.components.BioFuzzTracer.TraceType;
import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.biofuzztk.ptree.BioFuzzTokLst;

/**
 * 
 * This component is responsible for applying modifications on 
 * parse trees. In GP, modification is also referred to as mutation.
 * 
 * @author julian
 *
 */
public class BioFuzzModifier {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzModifier.class);
	
	private BioFuzzAttackCfgMgr mgr = null;
	private BioFuzzTracer tracer = null;
	
	private List<BioFuzzMutator> mutators = null;


	/**
	 * 
	 * Constructor. It needs access to the CFG-graph, which is the reason
	 * why we have to pass it as parameter here.
	 * 
	 * @param mgr the CFG-graph.
	 * 
	 */
	public BioFuzzModifier(BioFuzzAttackCfgMgr mgr,  List<BioFuzzMutator> mutators) {
		this.mgr = mgr;
		this.tracer = new BioFuzzTracer();
		logger.debug(this.mgr.toString());	
		this.mutators = mutators;
	}
	
	/**
	 * 
	 * Constructor. It needs access to the CFG-graph, which is the reason
	 * why we have to pass it as parameter here. THe list of mutators is empty
	 * 
	 * @param mgr the CFG-graph.
	 * 
	 */
	public BioFuzzModifier(BioFuzzAttackCfgMgr mgr) {
		this.mgr = mgr;
		this.tracer = new BioFuzzTracer();
		this.mutators = new Vector<BioFuzzMutator>();
		logger.debug(this.mgr.toString());	
	}
	
	
	/**
	 * 
	 * Deletes all tokens of the parse-trees token-list 
	 * that correspond to a node of a the parse-tree.
	 * 
	 * @param tree nodes of this tree will be deleted.
	 * @param node the node whose corresponding tokens in the parse-tree's
	 * token-list will be deleted.
	 * 
	 */
	private void doRmTok(BioFuzzParseTree tree, BioFuzzParseNode node) {
		if (!node.hasChildren())
			return;
		
		int maxTok = rmTok(tree,node,node.getTokIdx());
		int minTok = node.getTokIdx();
		
		BioFuzzTokLst tokLst = tree.getTokLst();
		assert(tokLst != null);
		
		logger.debug("Tok Range: [" + minTok + ":" + maxTok + "]");
		
		for(int i = maxTok; i >= minTok; i--) {
			String s = tokLst.remove(i);
			logger.debug("Remove: " + s);
		}
		
		// Set cursor to the min position
		tokLst.setCursor(minTok);
		
	}
	
	/**
	 * 
	 * Returns the max token index of a node. Keep in mind that if you
	 * have a non-terminal node of a parse tree, it can have multiple
	 * terminal node children.
	 * 
	 * @param tree parse-tree whose token-list gets reduced.
	 * @param node node to check.
	 * @param max max token index.
	 * @return the upper bound of the token idx range of the given node.
	 * 
	 */
	private int rmTok(BioFuzzParseTree tree, BioFuzzParseNode node, int max) {
	
		int ret = 0;
		// span up recursion
		for (BioFuzzParseNode child : node.getChildren()) {
			ret = rmTok(tree,child, max);
			max = (ret > max) ? ret : max;
		}
		
		if (node.getTokIdx() > max)
			return node.getTokIdx();
		else
			return max;
	}
	
	/**
	 * 
	 * Inserts a token from a source token-list to a dest token-list.
	 * 
	 * @param tokLstSrc token-list of source-parse-tree.
	 * @param tokLstDest token-list of destination-parse-tree.
	 * @param srcNode node of source parse-tree.
	 * 
	 */
	private void doInsTok(BioFuzzTokLst tokLstSrc, 
			BioFuzzTokLst tokLstDest,
			BioFuzzParseNode srcNode) {

		insTok(tokLstSrc, tokLstDest, srcNode, 0);

	}
	
	
	/**
	 * 
	 * Recursive function that inserts a token from a 
	 * source token-list to a dest token-list.
	 * 
	 * @param tokLstSrc token-list of source-parse-tree.
	 * @param tokLstDest token-list of destination-parse-tree.
	 * @param srcNode node of source parse-tree.
	 * @param prev keep track of lastly handled node.
	 * 
	 */
	private void insTok(BioFuzzTokLst tokLstSrc, BioFuzzTokLst tokLstDest,
			BioFuzzParseNode srcNode, int prev) {

		if (prev != srcNode.getTokIdx()) {
			prev = srcNode.getTokIdx();
			String tokToIns = tokLstSrc.get(prev);
			tokLstDest.append(tokToIns);
		}
		
		for (BioFuzzParseNode child : srcNode.getChildren()) {
			insTok(tokLstSrc, tokLstDest, child, prev);
		}
	}
	
	/**
	 * 
	 * After modifying the token-list, the indices of the corresponding 
	 * parse-tree nodes have to be sanitized. 
	 * 
	 * @param tree the tree to sanitize.
	 * 
	 */
	private void doSanitizeTokIdx(BioFuzzParseTree tree) {
		BioFuzzQuery qTerm = new BioFuzzQuery() {
			public Boolean condition(BioFuzzParseNode node) {
				return node.getAtagType() != TagType.NON_TERMINAL && node.getAtagType() != TagType.ROOT;
			}
		};
		List<BioFuzzParseNode> lterm = tracer.doTrace(tree, qTerm, TraceType.DFS);
		
		int termSize = lterm.size();
		
		// start from the end - per definition each non terminal gets the tokIdx of
		// the first child nonterminal
		for(int i = termSize - 1; i>= 0; i--) {
			
			BioFuzzParseNode term = lterm.get(i);
			BioFuzzParseNode parent = term.getParent();
			term.setTokIdx(i);
			
			while(parent != null && parent.getAtagType() != TagType.ROOT) {
				parent.setTokIdx(i);
				parent = parent.getParent();
			}	
		}
		
		// set cursor to last position (Important if further tokens are added)
		tree.getTokLst().setCursor(termSize);
		
	}	
	
	/**
	 * 
	 * This function is responsible for crossing-over two parent parse-trees.
	 * 
	 * @param treeA parent tree A.
	 * @param treeB parent tree B.
	 * @return child parse-tree produced by crossing-over parent A and parent B.
	 */
	public BioFuzzParseTree 
	doCrossOver(BioFuzzParseTree treeA, BioFuzzParseTree treeB) {
		Random rand = new Random();
		
		// create copies of the original trees
		BioFuzzParseTree treeAcp = new BioFuzzParseTree(treeA);
		BioFuzzParseTree treeBcp = new BioFuzzParseTree(treeB);
		
		// search for all non terminals in trees
		treeAcp.doResetNtSet();
		treeBcp.doResetNtSet();
		
		assert(treeAcp != null);
		assert(treeBcp != null);
		
		// just consider nts after the pfx barrier for A = receiving tree
		Set<String> ntSetA = treeAcp.getNtSuffixSet();
		logger.debug("NT Suffix Set tree A : " + ntSetA);
		
		// consider all nts for b = providing tree
		Set<String> ntSetB = treeBcp.getNtSet();
		logger.debug("NT Set tree B : " + ntSetB);
		
		// find intersection between all of the non terminals
		ntSetB.retainAll(ntSetA);
		
		if(ntSetB.isEmpty()){
			logger.debug("No intersection between nt sets");
			return null;
		}
		
		logger.debug(ntSetB.toString());
		
		// get a random index to access nt
		int ntIdx = rand.nextInt(ntSetB.size()); 
		
		int i = 0;
		
		String ntName = null;
		
		for(String s : ntSetB) {
		    if (i == ntIdx)
		        ntName = s;
		    i = i + 1;
		}
		if (ntName == null)
			return null;
		
		logger.debug("ntName to cross over: " + ntName);
		
		//BioFuzzParseNode ntA = treeAcp.getNtByName(ntName);
		
		List<BioFuzzParseNode> ntAl = treeAcp.getNtsByName(ntName);
		
		BioFuzzParseNode ntA = null;
		
		for(BioFuzzParseNode ntAtmp : ntAl) {
			if(ntAtmp.getTokIdx() > treeAcp.getPfxBarrier()) {
				ntA = ntAtmp;
				break;
			}
		}
		
		if(ntA == null)
			return null;
		
		BioFuzzParseNode ntB = treeBcp.getNtByName(ntName);
		
		assert(ntA != null);
		assert(ntB != null);
		
		BioFuzzParseNode ntApar = ntA.getParent();
		BioFuzzParseNode ntBpar = ntB.getParent();
		
		assert(ntApar != null && ntBpar != null);
		
		List<BioFuzzParseNode> ntAparChildren = ntApar.getChildren();
		
		doRmTok(treeAcp, ntA);
		
		logger.debug("TokLst (a) " + treeAcp.getTokLst().toString());
		
		doInsTok(treeBcp.getTokLst(), treeAcp.getTokLst(), ntB);
		
		int idxA = ntAparChildren.indexOf(ntA);
		// important for validtation
		
		//logger.debug(">>> B choices before " + ntB.getChoices());
		
		ntB.setDescIdx(ntA.getDescIdx());
		
		ntAparChildren.set(idxA, ntB);
		// set parent
		ntB.setParent(ntApar);
		
		ntB.setCfg(ntA.getCfg());
		//logger.debug(">>> B choices " + ntB.getChoices());
		
		doSanitizeTokIdx(treeAcp);
		treeAcp.doResetNtSet();
		
		//logger.debug(treeAcp.toString());
		logger.debug("RESULT (a) " + treeAcp.getTokLst().toString());
		assert(treeAcp != null);
		return treeAcp;
	}
	
	public String charConcat(String orig) {
		String out = "CONCAT(";
		char[] chars = orig.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			int cN = c;
			
			out += "CHAR(" + cN +")";
			
			if(i < chars.length -1)
				out +=",";
			
		}		
		
		return out + ")";
	}
	
	/**
	 * 
	 * Find applicable mutators.
	 * 
	 * @param s the string to which the mutator should be applicable to.
	 * @return a list of applicable mutators.
	 * 
	 */
	private List<BioFuzzMutator> findMutators(String s) {
		List<BioFuzzMutator> applicable = new Vector<BioFuzzMutator>();
		
		for(BioFuzzMutator mut:this.mutators) {
			if (mut.matches(s)) {
				applicable.add(mut);
			}
		}
		
		return applicable;
	}
	
	/**
	 * 
	 * Gets a terminal node and mutates it. One
	 * can define the range of the token list from which a token index
	 * is randomly picked. The token associated with this index will
	 * be mutated.
	 * 
	 * @param tree parse tree to apply mutation on.
	 * @param lrange lower bound of the range.
	 * @param rrange upper bound of the range.
	 * @return true if mutation was applied, false otherwise.
	 * 
	 */
	public boolean
	mutate(BioFuzzParseTree tree, int lrange, int rrange) {

		Random ridx = new Random();
		BioFuzzTokLst tokLst = tree.getTokLst();
		
		assert(tokLst != null);
		assert(rrange > lrange);
		assert(rrange >= 0);
		assert(tokLst.getSize() > 1);
		
		int tidx = ridx.nextInt((rrange - lrange) + 1) + lrange;
		
		String s = tokLst.get(tidx);
		String oldS = s;
		
		assert(s != null);
		
		// Find all applicable mutators
		List<BioFuzzMutator> applicable = findMutators(s);
		if(applicable.size() == 0) {
			logger.debug("No applicable mutators found");
			return false;
		}
		

		int idx = ridx.nextInt(applicable.size());
		BioFuzzMutator mutator = applicable.get(idx);
		
		logger.debug("mutate token " + tidx + ": " + s);
		mutator.mutate(tokLst, tidx);

		return !oldS.equals(s);
	}
	
	
	/**
	 * 
	 * Gets the last added terminal node and mutates it. 
	 * 
	 * @param tree parse tree to apply mutation on.
	 * @return true if mutation was applied, false otherwise.
	 * 
	 */
	public boolean
	mutate(BioFuzzParseTree tree) {

		BioFuzzTokLst tokLst = tree.getTokLst();
		assert(tokLst != null);
		assert(tokLst.getSize() > 1);
		
		String s = tokLst.get(tokLst.getSize()-2);
		String oldS = s;
		
		assert(s != null);
		
		// Find all applicable mutators
		List<BioFuzzMutator> applicable = findMutators(s);
		if(applicable.size() == 0) {
			logger.debug("No applicable mutators found");
			return false;
		}
		
		Random ridx = new Random();
		int idx = ridx.nextInt(applicable.size());
		BioFuzzMutator mutator = applicable.get(idx);
		
		mutator.mutate(tokLst, tokLst.getSize()-2);

		return !oldS.equals(s);
	}
	
}
