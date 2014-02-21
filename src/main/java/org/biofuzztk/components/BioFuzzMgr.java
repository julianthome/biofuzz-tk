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

import org.biofuzztk.cfg.BioFuzzAttackCfgMgr;
import org.biofuzztk.cfg.BioFuzzConfigReader;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;
import org.biofuzztk.components.BioFuzzTracer.BioFuzzQuery;
import org.biofuzztk.components.BioFuzzTracer.TraceType;
import org.biofuzztk.components.modifier.BioFuzzModifier;
import org.biofuzztk.components.parser.BioFuzzParser;
import org.biofuzztk.components.parser.BioFuzzParserConfig;
import org.biofuzztk.components.parser.BioFuzzParsingStatus;
import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;

public class BioFuzzMgr {
	
	private BioFuzzParser parser;
	private BioFuzzTokGen generator;
	private BioFuzzValidator validator;
	private BioFuzzModifier modifier;
	private BioFuzzTracer tracer;
	
	private BioFuzzAttackCfgMgr mgr;
	
	private BioFuzzParserConfig config;
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzMgr.class);
	
	public BioFuzzMgr(String fname) {
		
		this.mgr = BioFuzzConfigReader.readConfigFile(fname);
		assert(this.mgr != null);
		
		this.config = new BioFuzzParserConfig(150, BioFuzzParsingStatus.FINISHED, 310);
		
		this.parser = new BioFuzzParser(mgr,this.config);
		this.generator = new BioFuzzTokGen(mgr);
		this.validator = new BioFuzzValidator(mgr);
		this.modifier = new BioFuzzModifier(mgr);
		
		this.tracer = new BioFuzzTracer();
		
		logger.debug(this.mgr.toString());	
	}
	
	public BioFuzzParserConfig getParserConfig() {
		return this.config;
	}
	
	public BioFuzzAttackCfgMgr getAtackCfgMgr() {
		return this.mgr;
	}
	
	public List<BioFuzzParseTree> buildTrees( String s ) {
		return parser.buildTrees(s);
	}
	
	public void validate( BioFuzzParseTree tree) {
		logger.debug("do validate");
		validator.doValidate(tree);
	}
	
	public void extend( BioFuzzParseTree tree ) {
		// proper extension requires validation before and after
		logger.debug("extend tree");
		validator.doValidate(tree);
		generator.doExtendTree(tree);
		validator.doValidate(tree);
	}
	
	public Boolean mutate(BioFuzzParseTree tree) {
		return modifier.mutate(tree);
	}
	
	public BioFuzzParseTree crossover (BioFuzzParseTree treeA, BioFuzzParseTree treeB) {
		BioFuzzParseTree tree  = null;
		validator.doValidate(treeA);
		validator.doValidate(treeB);
		tree = modifier.doCrossOver(treeA, treeB, parser);
		// tree might be null
		if(tree != null) {
			validator.doValidate(tree);
		}
		
		return tree;
	}
	
	public List<BioFuzzParseNode> trace (BioFuzzParseTree tree, BioFuzzQuery q, TraceType type) {
		return tracer.doTrace(tree, q, type);
	}
	
	public List<BioFuzzParseNode> traceSubNodes (BioFuzzParseNode node, BioFuzzQuery q, TraceType type) {
		return tracer.doTraceSubNodes(node, q,type);
	}
	
	public List<BioFuzzParseNode> traceAll(BioFuzzParseTree tree, List<BioFuzzQuery> qlist, TraceType type) {
		
		assert(tree != null);
		assert(qlist.size() > 0);
		
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
	
	public List<BioFuzzParseNode> traceAll(List<BioFuzzParseNode> nlist, BioFuzzQuery q,TraceType type) {
		List<BioFuzzParseNode> res = new Vector<BioFuzzParseNode>();
		assert(nlist != null);
		
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
	
	
	public boolean traceAndReplaceAll(BioFuzzParseTree tree, BioFuzzQuery q, List<String> replacements,TraceType type)  {
		assert(tree != null);
		assert(q != null);
		assert(replacements != null);
		assert(replacements.size() > 0);
		
		boolean ret = false;
		
		
		List<BioFuzzQuery> ql = new Vector<BioFuzzQuery>();
		
		ql.add(q);
		
		ql.add(	new BioFuzzQuery() {
			public Boolean condition(BioFuzzParseNode node) {
				return node.getAtagType() == TagType.TERMINAL || node.getAtagType() == TagType.REGEXP;
		};});
		
		List<BioFuzzParseNode> nl = traceAll(tree,ql,type);
		
		if(nl == null || nl.size() <= 0)
			return false;
		
		for(BioFuzzParseNode n : nl) {
			assert(n.getAtagType() == TagType.TERMINAL || n.getAtagType() == TagType.REGEXP );
			logger.debug("replace");
			//tree.getTokLst().replace(n.getTokIdx(), replacement);
			ret = true;
		}
		return ret;
		
	}
	
	
	public boolean traceAndReplaceAll(BioFuzzParseTree tree, BioFuzzQuery q, String replacement,TraceType type)  {
		boolean ret = false;
		List<BioFuzzQuery> ql = new Vector<BioFuzzQuery>();
		
		ql.add(q);
		
		ql.add(	new BioFuzzQuery() {
			public Boolean condition(BioFuzzParseNode node) {
				return node.getAtagType() == TagType.TERMINAL || node.getAtagType() == TagType.REGEXP;
		};});
		
		List<BioFuzzParseNode> nl = traceAll(tree,ql,type);
		
		if(nl == null || nl.size() <= 0)
			return false;
		
		for(BioFuzzParseNode n : nl) {
			assert(n.getAtagType() == TagType.TERMINAL || n.getAtagType() == TagType.REGEXP );
			logger.debug("replace");
			tree.getTokLst().replace(n.getTokIdx(), replacement);
			ret = true;
		}
		return ret;
		
	}
	
	@Override
	public String toString() {
		String s = "BioFuzzMgr: \n";
		s += this.mgr.toString();
		return s;
	}
	

}
