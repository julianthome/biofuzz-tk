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


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackCfg;
import org.biofuzztk.cfg.BioFuzzAttackCfgMgr;
import org.biofuzztk.cfg.BioFuzzAttackTag;
import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;
import org.biofuzztk.components.tokenizer.BioFuzzTokenizer;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.biofuzztk.ptree.BioFuzzTokLst;

/**
 * 
 * The parser that takes generates a parse-tree from a string
 * based on a context-free grammar (CFG).
 * 
 * @author julian
 *
 */
public class BioFuzzParser {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzParser.class);
	
	private BioFuzzAttackCfgMgr mgr;
	
	private BioFuzzParsingStatus minQual;
	private int maxIter;
	private int maxSSize;
	private BioFuzzTokenizer tokenizer;
	
	BioFuzzParserConfig config = null;
	
	/**
	 * 
	 * Constructor.
	 * 
	 * @param mgr the CFG-graph.
	 * @param config the parser configuration.
	 * @param tokenizer the tokenizer to tokenize the string before parsing it.
	 * 
	 */
	public BioFuzzParser(BioFuzzAttackCfgMgr mgr, BioFuzzParserConfig config, BioFuzzTokenizer tokenizer) {
		this.mgr = mgr;
		this.config = config;
		
		
		this.minQual = config.getMinQual();
		this.maxSSize = config.getMaxSsize();
		this.maxIter = config.getMaxIter();
		this.tokenizer = tokenizer;
		
		logger.debug("Parser Config :" + this.config.toString());
		//logger.debug(this.mgr.toString());	
	}
	
	/**
	 * 
	 * Tokenize the given string.
	 * 
	 * @param s the string to tokenize.
	 * @return an array of string where each field contains a token.
	 * 
	 */
	public String[] tokenize(String s) {
		
		logger.debug(s);
		
		return this.tokenizer.tokenize(s);
	}
	
	/**
	 * 
	 * Takes a string and creates a list of parse-trees from it. It might
	 * be that there are ambiguities, i.e. there are different rules in
	 * the CFG that produce the same string. This is the reason why this
	 * function returns a list of parse-trees.
	 * 
	 * @param s the string to parse.
	 * @return a list of parse-trees that represent the string s.
	 * 
	 */
	public List<BioFuzzParseTree> buildTrees(String s) {
		String tokLst[] = tokenize(s);
		assert(tokLst.length > 0);
		logger.debug("build trees: " + s);
		//logger.debug("tokLst: " + BioFuzzUtils.strArrayToStr(tokLst));
		
		return intialize(tokLst);
	}
	
	/**
	 * 
	 * Adds the initial production rule S on a tuple stack and 
	 * then analyzes all paths through the CFG that can produce
	 * tokLst.
	 * 
	 * @param tokLst the token list to check.
	 * @return a list of parse-trees that produce tokLst.
	 * 
	 */
	private List<BioFuzzParseTree> intialize(String[] tokLst) {
		BioFuzzStackMgr smgr = null;
		smgr = new BioFuzzStackMgr();
		// Iterate over all configurations
		
		//for ( String key: this.mgr.getKeys()) {
			//logger.debug("buildTrees - Check Rule: " + key);
			
		//	BioFuzzAttackCfg cfg = this.mgr.getAttackCfgByKey(key);
			
		//	BioFuzzTupleStack tstack = smgr.createAndGetStack(key, 0);
			
		//	tstack.pushTuple(cfg, TagType.ROOT, 0);
			

		//}
		
		// Intead of iterating over all configuration - just look at the start symbol.
		// That speeds up the parsing process
		
		assert(this.mgr.getKeys().contains("S"));
		BioFuzzAttackCfg cfg = this.mgr.getAttackCfgByKey("S");
		BioFuzzTupleStack tstack = smgr.createAndGetStack("S", 0);
		tstack.pushTuple(cfg, TagType.ROOT, 0);
		
		
		
		BioFuzzTokLst btokLst = new BioFuzzTokLst(tokLst);
		
		traversePaths(smgr, btokLst);
		
		List<BioFuzzParseTree> list =  smgr.getGeneratedTrees(this.minQual);
		
		if(list == null || list.size() <= 0) { 
			logger.debug("list is null");
			return null;
		}
		
		assert(list != null);
		
		return list;
	}
	
	/**
	 * 
	 * Manages the set of the push-down automatons.
	 * 
	 * @param smgr the set of stacks, each of them representing a different ruleset that might produce tokLst.
	 * @param tokLst the token list.
	 * @return type of the lastly added tag.
	 * 
	 */
	private TagType traversePaths(BioFuzzStackMgr smgr, BioFuzzTokLst tokLst) {
		
		logger.debug("TokLst: " + tokLst.toString());
		logger.debug("TokLst cursor: " + tokLst.getCursor());
		
		//logger.debug(smgr.toString());
		int iter = 0;

		while(smgr.getSize() > 0) {
			//logger.debug(">>" + smgr.getSize());
			//logger.debug("TokLst: " + tokLst.toString());
			iter ++;
			int ub = smgr.getSize();
			//logger.debug("START=======================================================");
			for (int i = 0; i < ub; i++ ) {
				int ret = 0;
				BioFuzzTupleStack tstack = smgr.getTupleStack(i);
				assert(tstack != null);
				
				if(tstack.getCur() <= tokLst.getSize() - 2) {
					//logger.debug("match");
					ret = match(tokLst, smgr, tstack);
				} else if (tstack.getStatus() == BioFuzzParsingStatus.IN_PROGRESS) {
					tstack.changeStatus(BioFuzzParsingStatus.FINISHED);
					tstack.setTokLst(tokLst);
				}
				
				
				// stack succesfully reduced - word is valid according to the grammar
				if(tstack.getStatus() == BioFuzzParsingStatus.FINISHED && tstack.getSize() == 0) {
					tstack.changeStatus(BioFuzzParsingStatus.VALID);
				}

					
				//logger.debug("Return value: " + ret);
				
				if((ret == 0 || tstack.getSize() <= 0) ||  tstack.getSize() > this.maxSSize) {
					//logger.debug("invalidate");
					//logger.debug("invalidate :" + tstack.toString());
					if(tstack.getStatus() != BioFuzzParsingStatus.FINISHED && 
							tstack.getStatus() != BioFuzzParsingStatus.VALID) {
						tstack.changeStatus(BioFuzzParsingStatus.INVALID);
					}
				}

				
			}
			
			//logger.debug(smgr.toString());
			
			// reduce the search space
			smgr.reduce();
			
			if(iter > this.maxIter) {
				logger.debug("too many iterations");
				break;
			}
			
			if(smgr.getInProgress() <= 0) {
				logger.debug("no stacks in progress left");
				break;
			}
			
//			if (iter == 32)
//				break;
			//logger.debug("END========================================================");
			
			//logger.debug("reduce");

		}
		logger.debug("show" );
		//logger.debug(smgr.toString());
		//logger.debug("size: " + smgr.getSize());
		
		logger.debug(" -------------------------------------------------------------------\n");
		
		return TagType.STOP;
		
	}

	/**
	 * 
	 * Creates a copy of tstack and adds it to the stack manager. This function is called
	 * to create a fork, i.e. to follow different paths through the CFG from a given 
	 * node.
	 * 
	 * @param smgr stack manager that contains all push-down automatons.
	 * @param tstack current stack.
	 * @param fork if true a fork is created.
	 * @return a copy of tstack or tstack itself.
	 * 
	 */
	private BioFuzzTupleStack forkTupleStack(BioFuzzStackMgr smgr, BioFuzzTupleStack tstack, boolean fork) {
		
		return fork ? smgr.copyAndGetStack(tstack) : tstack;
		
	}
	
	/**
	 * 
	 * This function matches tokens to the CFG-definition.
	 * 
	 * @param tokLst the token-list.
	 * @param smgr the stack manager.
	 * @param tstack the currently active stack.
	 * @return the number of matches.
	 * 
	 */
	@SuppressWarnings("incomplete-switch")
	private int match(BioFuzzTokLst tokLst, BioFuzzStackMgr smgr, BioFuzzTupleStack tstack) {
		
		int matchCnt = 0;

		BioFuzzTupleStack myTstack = null;
		
		BioFuzzParsingTuple tup = tstack.getLastTuple();
		if(tup == null)
			return 0;

		
		BioFuzzAttackCfg cfg = tup.getCfg();
		assert(cfg != null);
		int lfr = tup.getLfr();
		
		//logger.debug("LFR is " + lfr);
		
		int cur = tstack.getCur();
		
		//logger.debug("CUR is " + cur);
		
		List<Number> choices = cfg.getChoicesByIdx(lfr);
		
		//logger.debug("CHOICES: " + choices);
		
		assert(choices != null);
		
		for(int i = 0; i < choices.size(); i++)  {
			myTstack = null;
			int choice = choices.get(i).intValue();
			
			BioFuzzAttackTag atag = cfg.getAtagByIdx(choice);
			
			//logger.debug(">>>>>>>>>> " + choice + " << " + tokLst.get(cur) + " vs. " + atag.getName() + " " + atag.getTagType());
			
			switch(atag.getTagType()) {
			
				case NON_TERMINAL: {
					BioFuzzAttackCfg ntCfg = this.mgr.getAttackCfgByKey(atag.getName());
					
					
					// this block avoid loops - it avoids that the same non-terminal is
					// being added to the stack over and over again
					if(tstack != null && tstack.getSize() > 2) {
						BioFuzzParsingTuple lastTup = tstack.getTuple(tstack.getSize()-2);
						if(tstack.getTuple(tstack.getSize()-1).getLfr() == 0 
								&& lastTup.getLfr() == choice &&
								lastTup.getCfg() == ntCfg) {
							//logger.debug("loop detected");
							continue;
						}
					}
					
					assert(ntCfg != null);
					myTstack = forkTupleStack(smgr, tstack, true);
					assert(myTstack != null);
					myTstack.pushTuple(cfg, TagType.NON_TERMINAL, choice);
					myTstack.pushTuple(ntCfg, TagType.START, 0);
					matchCnt++;
					//logger.debug("NON-TERMINAL detected " + atag.getName());
					continue;
				}
				
				case TERMINAL: {
					
					String as = atag.getName().toLowerCase();
					String tok = tokLst.get(cur).toLowerCase();
					//logger.debug("Terminal match : " + as + " vs. " + tok);
					if(as.equals(tok)) {
						myTstack = forkTupleStack(smgr, tstack, true);
						assert(myTstack != null);
						myTstack.pushTuple(cfg, TagType.TERMINAL, choice);
						myTstack.nxtCur();
	
						matchCnt+=1;
						//logger.debug("Terminal match " + atag.getName());
						//logger.debug("matchCnt " + matchCnt);
						//logger.debug(cfg.toString());
						continue;
					} else {
						continue;
					}
				}
				
				case REGEXP : {
					String rexp = atag.getName();
					String tok = tokLst.get(cur);
					
					if(tok.matches(rexp)) {
						myTstack = forkTupleStack(smgr, tstack, true);
						assert(myTstack != null);
						assert(myTstack != null);
						myTstack.pushTuple(cfg,TagType.REGEXP, choice);
						myTstack.nxtCur();
						
						matchCnt+=1;
						
						//logger.debug("REGEXP match " + atag.getName());
						continue;
					} else {
						continue;
					}
					
				}
				
				case STOP : {
					//logger.debug("STOP detected");
					//assert(choices.size() == 1);
					myTstack = forkTupleStack(smgr, tstack, true);
					assert(myTstack != null);
					myTstack.rollback();
					//logger.debug(myTstack.toString());
					
					matchCnt++;
					continue;
					
				}
			
			}
			
		}
		tstack.changeStatus(BioFuzzParsingStatus.INVALID);
		

		return matchCnt;
		
		
	}
	
	
}
