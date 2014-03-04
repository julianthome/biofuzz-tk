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
import java.util.Vector;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackCfg;
import org.biofuzztk.cfg.BioFuzzAttackCfgMgr;
import org.biofuzztk.cfg.BioFuzzConfigReader;
import org.biofuzztk.components.BioFuzzTracer.BioFuzzQuery;
import org.biofuzztk.components.BioFuzzTracer.TraceType;
import org.biofuzztk.components.modifier.BioFuzzModifier;
import org.biofuzztk.components.modifier.BioFuzzMutator;
import org.biofuzztk.components.parser.BioFuzzParser;
import org.biofuzztk.components.parser.BioFuzzParserConfig;
import org.biofuzztk.components.parser.BioFuzzParsingStatus;
import org.biofuzztk.components.tokenizer.BioFuzzTokenizer;
import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.biofuzztk.ptree.BioFuzzTokLst;

/**
 * 
 * The BioFuzz manager is responsible for the generation,
 * validation, modification and extension of parse-trees.
 * It is also responsible for searching within a parse-tree.
 * 
 * @author julian
 *
 */
public class BioFuzzMgr {
	
	private BioFuzzParser parser;
	private BioFuzzTokGen generator;
	private BioFuzzValidator validator;
	private BioFuzzModifier modifier;
	private BioFuzzTracer tracer;
	
	private BioFuzzAttackCfgMgr mgr;
	
	private BioFuzzParserConfig config;
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzMgr.class);
	
	public BioFuzzMgr(String fname, BioFuzzTokenizer tokenizer, List<BioFuzzMutator> mutators) {
		
		this.mgr = BioFuzzConfigReader.readConfigFile(fname);
		assert(this.mgr != null);
		assert(tokenizer != null);
		
		this.config = new BioFuzzParserConfig(20, BioFuzzParsingStatus.FINISHED, 400);
		
		this.parser = new BioFuzzParser(mgr,this.config, tokenizer);
		this.generator = new BioFuzzTokGen(mgr);
		this.validator = new BioFuzzValidator(mgr);
		this.modifier = new BioFuzzModifier(mgr,mutators);
		
		this.tracer = new BioFuzzTracer();
		
		logger.debug(this.mgr.toString());	
	}
	
	public BioFuzzMgr(String fname, BioFuzzTokenizer tokenizer) {
		
		this.mgr = BioFuzzConfigReader.readConfigFile(fname);
		assert(this.mgr != null);
		assert(tokenizer != null);
		
		this.config = new BioFuzzParserConfig(20, BioFuzzParsingStatus.FINISHED, 400);
		
		this.parser = new BioFuzzParser(mgr,this.config, tokenizer);
		this.generator = new BioFuzzTokGen(mgr);
		this.validator = new BioFuzzValidator(mgr);
		// Simple modifer without any mutators
		this.modifier = new BioFuzzModifier(mgr);
		
		this.tracer = new BioFuzzTracer();
		
		logger.debug(this.mgr.toString());	
	}
	
	public BioFuzzMgr(String fname) {
		
		this.mgr = BioFuzzConfigReader.readConfigFile(fname);
		assert(this.mgr != null);
		
		this.config = new BioFuzzParserConfig(20, BioFuzzParsingStatus.FINISHED, 400);	
		this.parser = null;
		this.generator = new BioFuzzTokGen(mgr);
		this.validator = new BioFuzzValidator(mgr);
		this.modifier = null;
		
		this.tracer = new BioFuzzTracer();
		
		logger.debug(this.mgr.toString());	
	}
	
	public BioFuzzParserConfig getParserConfig() {
		return this.config;
	}
	
	public BioFuzzAttackCfgMgr getAtackCfgMgr() {
		return this.mgr;
	}
	
	public BioFuzzParseTree getNewParseTree() {
		
		BioFuzzAttackCfg cfg = this.mgr.getAttackCfgByKey("S");
		BioFuzzParseTree tree = new BioFuzzParseTree();
		BioFuzzTokLst tLst = new BioFuzzTokLst();
		tree.setTokLst(tLst);
		BioFuzzParseNode ptr = tree.getRootNode();
		
		ptr.setCfg(cfg);
		ptr.setAtagName("S");
		
		return tree;
	}
	
	/**
	 * 
	 * Takes a string as input at returns a set of parse-trees in accordance
	 * to the user-defined CFG. The parse can deal with ambiguities.
	 * 
	 * @param s the string to parse.
	 * @return a list of corresponding parse-trees or null.
	 * 
	 */
	public List<BioFuzzParseTree> buildTrees( String s ) {
		if(this.parser == null) {
			logger.debug("No parser available");
			return null;
		}
		return parser.buildTrees(s);
	}
	
	/**
	 * 
	 * This function checks whether a parse-tree is complete according to a
	 * CFG definition.
	 * 
	 * @param tree a parse-tree.
	 * 
	 */
	public void validate( BioFuzzParseTree tree) {
		logger.debug("do validate");
		if(this.validator == null) {
			logger.debug("No validator available");
			return;
		}
		validator.doValidate(tree);
	}
	
	/**
	 * 
	 * Extends a given parse-tree by one terminal.
	 * 
	 * @param tree the parse tree to extend
	 * 
	 */
	public void extend( BioFuzzParseTree tree ) {
		
		if(this.generator == null) {
			logger.debug("No generator available");
			return;
		}
		
		// proper extension requires validation before and after
		logger.debug("extend tree");
		validator.doValidate(tree);
		generator.doExtendTree(tree);
		validator.doValidate(tree);
	}
	
	/**
	 * 
	 * Picks the last terminal and mutates by picking a mutator 
	 * randomly and applying it.
	 * 
	 * @param tree
	 * @return true if mutation was successful.
	 * 
	 */
	public Boolean mutate(BioFuzzParseTree tree) {
		
		if(this.modifier == null) {
			logger.debug("No modifier available");
			return false;
		}
		
		return modifier.mutate(tree);
	}
	
	/**
	 * 
	 * Picks a terminal whose index lies in [lrange,rrange] 
	 * and mutates by picking a mutator randomly and applying it.
	 * 
	 * @param tree
	 * @param lrange lower bound of the range.
	 * @param rrange upper bound of the range.
	 * @return true if mutation was successful.
	 * 
	 */
	public Boolean mutate(BioFuzzParseTree tree, int lrange, int rrange) {
		
		if(this.modifier == null) {
			logger.debug("No modifier available");
			return false;
		}
		
		return modifier.mutate(tree, lrange, rrange);
	}
	
	/**
	 * 
	 * Performs a cross-over operation on two parse-trees. In this
	 * context crossing-over two parse-trees means simply the exchange
	 * of semantically equivalent subtrees based on the CFG.
	 * 
	 * @param treeA a parent parse-tree.
	 * @param treeB a parent parse-tree.
	 * @return a child parse-tree or null.
	 * 
	 */
	public BioFuzzParseTree crossover (BioFuzzParseTree treeA, BioFuzzParseTree treeB) {
		
		if(this.modifier == null) {
			logger.debug("No modifier available");
			return null;
		}
		
		BioFuzzParseTree tree  = null;
		validator.doValidate(treeA);
		validator.doValidate(treeB);
		tree = modifier.doCrossOver(treeA, treeB);
		// tree might be null
		if(tree != null) {
			validator.doValidate(tree);
		}
		
		return tree;
	}
	
	/**
	 * 
	 * Tracing for nodes within the parse tree. The query defines the constraints or
	 * conditions that have to be fulfilled for a node to be added to the return set.
	 * 
	 * @param tree the parse-tree to analyze.
	 * @param q the query that defines the conditions for a node to be added to the return set.
	 * @param type search algorithm to be used.
	 * @return list of nodes that fulfill q or null
	 * 
	 */
	public List<BioFuzzParseNode> trace (BioFuzzParseTree tree, BioFuzzQuery q, TraceType type) {
		
		if(this.tracer == null) {
			logger.debug("No tracer available");
			return null;
		}
		
		return tracer.doTrace(tree, q, type);
	}
	
	/**
	 * 
	 * Given a node n, this function traces for child nodes of n that fulfill q.
	 * 
	 * @param node node whose children are analyzed.
	 * @param q the query that defines the conditions for a node to be added to the return set.
	 * @param type search algorithm to be used.
	 * @return list of nodes that fulfill q or null
	 */
	public List<BioFuzzParseNode> traceSubNodes (BioFuzzParseNode node, BioFuzzQuery q, TraceType type) {
		
		if(this.tracer == null) {
			logger.debug("No tracer available");
			return null;
		}
		
		return tracer.doTraceSubNodes(node, q,type);
	}
	
	/**
	 * 
	 * Tracing for nodes within the parse tree. The list of queries define the constraints or
	 * conditions that have to be fulfilled for a node to be added to the return set.
	 * 
	 * @param tree parse-tree to analyze.
	 * @param qlist list of queries (combined with a logical 'AND').
	 * @param type search algorithm to be used.
	 * @return list of nodes that fulfill qlist or null
	 * 
	 */
	public List<BioFuzzParseNode> traceAll(BioFuzzParseTree tree, List<BioFuzzQuery> qlist, TraceType type) {
		
		assert(tree != null);
		assert(qlist.size() > 0);
		
		if(this.tracer == null) {
			logger.debug("No tracer available");
			return null;
		}
		
		List<BioFuzzParseNode> res = null;
		
		
		res = trace(tree, qlist.get(0), type);
		
		if(res == null)
			return null;
		
		for(int i = 1; i < qlist.size(); i++) {
			res = traceAll(res,qlist.get(i),type);
			if(res == null || res.size() == 0)
				return null;
		}

		return res;
	}
	
	/**
	 * 
	 * Iterates over the given list of parse nodes and their children 
	 * and checks whether they fulfill the constraints given by the query.
	 * 
	 * @param nlist a list of parse nodes.
	 * @param q the query that defines the conditions for a node to be added to the return set.
	 * @param type search algorithm to be used.
	 * @return all nodes of nlist that fulfill q.
	 * 
	 */
	public List<BioFuzzParseNode> traceAll(List<BioFuzzParseNode> nlist, BioFuzzQuery q,TraceType type) {
		List<BioFuzzParseNode> res = new Vector<BioFuzzParseNode>();
		assert(nlist != null);
		
		if(this.tracer == null) {
			logger.debug("No tracer available");
			return null;
		}
		
		if(nlist.size() <=0)
			return null;
		
		for(BioFuzzParseNode n : nlist) {
			
			List<BioFuzzParseNode> tmp = traceSubNodes(n,q,type);
			
			if(tmp != null && tmp.size() > 0)
				res.addAll(tmp);
		}
		if(res.size() <= 0)
			return null;
		
		return res;
		
	}
	

//	public boolean traceAndReplaceAll(BioFuzzParseTree tree, BioFuzzQuery q, List<String> replacements,TraceType type)  {
//		assert(tree != null);
//		assert(q != null);
//		assert(replacements != null);
//		assert(replacements.size() > 0);
//		
//		boolean ret = false;
//		
//		
//		List<BioFuzzQuery> ql = new Vector<BioFuzzQuery>();
//		
//		ql.add(q);
//		
//		ql.add(	new BioFuzzQuery() {
//			public Boolean condition(BioFuzzParseNode node) {
//				return node.getAtagType() == TagType.TERMINAL || node.getAtagType() == TagType.REGEXP;
//		};});
//		
//		List<BioFuzzParseNode> nl = traceAll(tree,ql,type);
//		
//		if(nl == null || nl.size() <= 0)
//			return false;
//		
//		for(BioFuzzParseNode n : nl) {
//			assert(n.getAtagType() == TagType.TERMINAL || n.getAtagType() == TagType.REGEXP );
//			logger.debug("replace");
//			//tree.getTokLst().replace(n.getTokIdx(), replacement);
//			ret = true;
//		}
//		return ret;
//		
//	}
//	
//	
//	public boolean traceAndReplaceAll(BioFuzzParseTree tree, BioFuzzQuery q, String replacement,TraceType type)  {
//		boolean ret = false;
//		List<BioFuzzQuery> ql = new Vector<BioFuzzQuery>();
//		
//		ql.add(q);
//		
//		ql.add(	new BioFuzzQuery() {
//			public Boolean condition(BioFuzzParseNode node) {
//				return node.getAtagType() == TagType.TERMINAL || node.getAtagType() == TagType.REGEXP;
//		};});
//		
//		List<BioFuzzParseNode> nl = traceAll(tree,ql,type);
//		
//		if(nl == null || nl.size() <= 0)
//			return false;
//		
//		for(BioFuzzParseNode n : nl) {
//			assert(n.getAtagType() == TagType.TERMINAL || n.getAtagType() == TagType.REGEXP );
//			logger.debug("replace");
//			tree.getTokLst().replace(n.getTokIdx(), replacement);
//			ret = true;
//		}
//		return ret;
//		
//	}
	
	@Override
	public String toString() {
		String s = "BioFuzzMgr: \n";
		s += this.mgr.toString();
		return s;
	}
	

}
