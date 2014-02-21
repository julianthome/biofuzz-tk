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
import org.biofuzztk.components.modifier.mutators.BioFuzzCaseMutator;
import org.biofuzztk.components.modifier.mutators.BioFuzzCommentMutator;
import org.biofuzztk.components.modifier.mutators.BioFuzzQuoteMutator;
import org.biofuzztk.components.parser.BioFuzzParser;
import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.biofuzztk.ptree.BioFuzzTokLst;

public class BioFuzzModifier {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzModifier.class);
	
	private BioFuzzAttackCfgMgr mgr = null;
	private BioFuzzTracer tracer = null;
	
	private List<BioFuzzMutator> quoteMutator = new Vector<BioFuzzMutator>();
	private List<BioFuzzMutator> tokMutator = new Vector<BioFuzzMutator>();

	
	public BioFuzzModifier(BioFuzzAttackCfgMgr mgr) {
		this.mgr = mgr;
		this.tracer = new BioFuzzTracer();
		logger.debug(this.mgr.toString());	

		
		quoteMutator.add(new BioFuzzQuoteMutator());
		quoteMutator.add(new BioFuzzCommentMutator());
		tokMutator.add(new BioFuzzCaseMutator());
		tokMutator.add(new BioFuzzCommentMutator());

		
	}
	
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
	
	
	private void doInsTok(BioFuzzTokLst tokLstSrc, 
			BioFuzzTokLst tokLstDest,
			BioFuzzParseNode srcNode) {

		insTok(tokLstSrc, tokLstDest, srcNode, 0);

	}
	
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
	
	public BioFuzzParseTree 
	doCrossOver(BioFuzzParseTree treeA, BioFuzzParseTree treeB, BioFuzzParser parser) {
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
	
	
	public boolean
	mutate(BioFuzzParseTree tree) {

		
		BioFuzzTokLst tokLst = tree.getTokLst();
		assert(tokLst != null);
		
		assert(tokLst.getSize() > 1);
		
		String s = tokLst.get(tokLst.getSize()-2);
		String oldS = s;
		
		assert(s != null);
		
		if(s.matches("[\"']") && s.length() == 1) {
			// call quote mutator or token list mutator
			Random ridx = new Random();
			int idx = ridx.nextInt(this.quoteMutator.size());
			BioFuzzMutator mutator = this.quoteMutator.get(idx);
			mutator.mutate(tokLst);
		} else {
			Random ridx = new Random();
			int idx = ridx.nextInt(this.tokMutator.size());
			BioFuzzMutator mutator = this.tokMutator.get(idx);
			mutator.mutate(tokLst);
		}
		

		return !oldS.equals(s);
	}
	
	
}
