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
import java.util.Vector;

import org.biofuzztk.ptree.BioFuzzParseTree;


/**
 * 
 * The stack manager can be seen as a set of push-down automatons. 
 * 
 * @author julian
 *
 */
public class BioFuzzStackMgr {
	
	private List<BioFuzzTupleStack> stackLst;
	private int progressCnt;
	private int finishCnt;
	private int validCnt;
	
	public BioFuzzStackMgr() {
		this.stackLst = new Vector<BioFuzzTupleStack>();
		this.progressCnt = 0;
		this.finishCnt = 0;
		this.validCnt = 0;
		
	}
	
	/**
	 * 
	 * Creates a new stack, registers and returns it.
	 * 
	 * @param key the name of the corresponding attack-tag.
	 * @param cur the token index of the current string in the token list.
	 * @return a new tuple stack.
	 * 
	 */
	public BioFuzzTupleStack createAndGetStack(String key, int cur) {
		BioFuzzTupleStack tupStack = new BioFuzzTupleStack(key, cur);
		this.stackLst.add(tupStack);
		return tupStack;
	}
	
	/**
	 * 
	 * Creates a copy of the tuple stack and returns it.
	 * 
	 * @param t a tuple stack.
	 * @return a copy of the tuple stack t.
	 */
	public BioFuzzTupleStack copyAndGetStack(BioFuzzTupleStack t) {
		BioFuzzTupleStack tupStack = new BioFuzzTupleStack(t);
		this.stackLst.add(tupStack);
		return tupStack;
	}
	
	
	/**
	 * 
	 * Getter.
	 * 
	 * @return  the amount of push-down automatons that the stack
	 * manager keeps track of.
	 */
	public int getSize() {
		return stackLst.size();
	}
	
	/**
	 * 
	 * Getter.
	 * 
	 * @param idx index of the tuple stack to return.
	 * @return the tuple stack with index idx.
	 */
	public BioFuzzTupleStack getTupleStack(int idx) {
		return stackLst.get(idx);
	}
	
	/**
	 * 
	 * The reduce method removes all push-down automatons with
	 * status INVALID from the stack manager.
	 * 
	 */
	public void reduce() {
		List<BioFuzzTupleStack> stackLstTmp = new Vector<BioFuzzTupleStack>();
		
		this.progressCnt = 0;
		this.finishCnt = 0;
		this.validCnt = 0;
		
		for(int i = 0; i < this.stackLst.size(); i++) {
			BioFuzzTupleStack tstack = this.stackLst.get(i);
			
			switch(tstack.getStatus()) {
				case IN_PROGRESS:
					this.progressCnt++;
					stackLstTmp.add(tstack);
					break;
				case VALID:
					this.validCnt ++;
					stackLstTmp.add(tstack);
					break;
				case FINISHED:
					this.finishCnt ++;
					stackLstTmp.add(tstack);
					break;
				case INVALID:
					break;
			}
		}
		
		this.stackLst = stackLstTmp;
		

	}
	
	/**
	 * 
	 * Getter.
	 * 
	 * @return the amount of push-down automatons that are IN_PROGRESS.
	 * 
	 */
	public int getInProgress() {
		return this.progressCnt;
	}
	
	/**
	 * 
	 * Getter.
	 * 
	 * @param minQual all parse trees with this parsing status are considered.
	 * @return final list of parse trees.
	 * 
	 */
	public List<BioFuzzParseTree> getGeneratedTrees(BioFuzzParsingStatus minQual) {
		
		List<BioFuzzParseTree> trees = new Vector<BioFuzzParseTree>();
		
		for(int i = 0; i < this.stackLst.size(); i++) {
			BioFuzzTupleStack tstack = this.stackLst.get(i);
			assert(tstack != null);
			if(tstack.getStatus().ordinal() >= minQual.ordinal()) {
				assert(tstack.getParseTree() != null);
				trees.add(tstack.getParseTree());
			}
		}
		
		return trees;
	}
	
	@Override 
	public String toString() {
		String s = "STACK LIST\n";
		
		for(int i = 0; i < this.stackLst.size(); i++) {
			BioFuzzTupleStack tstack = this.stackLst.get(i);
			s += "############################### i:" + i + "\n";
			s += tstack.toString();
			s += "#####################################\n";
			s += "in Progress: " + this.progressCnt + "\n";
			s += "valid: " + this.validCnt + "\n";
			s += "finished: " + this.finishCnt + "\n";
		}
		return s;
	}

}

