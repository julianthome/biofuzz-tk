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
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * 
 * A token list that manages access to the tokens.
 * 
 * @author julian
 *
 */
public class BioFuzzTokLst {
	
	private List<String> tokLst;
	private int cursor;
	private Stack<Number> lfr;
	private Stack<Number> cstack;
	private Stack<Number> ack; 
	
	private Set<Integer> checkpoints;
	private String del;
	
	public BioFuzzTokLst(String [] tokLst) {
		this.cursor = 0;
		this.tokLst = new ArrayList<String>(Arrays.asList(tokLst));
		normalize(this.tokLst);
		this.lfr = new Stack<Number>();
		this.cstack = new Stack<Number>();
		this.ack = new Stack<Number>();
		this.checkpoints = new HashSet<Integer>();
		this.del = " ";
	}
	
	public BioFuzzTokLst() {
		this.cursor = 0;
		this.tokLst = new ArrayList<String>();
		this.tokLst.add("$");
		normalize(this.tokLst);
		this.lfr = new Stack<Number>();
		this.cstack = new Stack<Number>();
		this.ack = new Stack<Number>();
		this.checkpoints = new HashSet<Integer>();
		this.del = " ";
	}
	
	public BioFuzzTokLst(BioFuzzTokLst t) {
		this.cursor = t.cursor;
		this.lfr = new Stack<Number>();
		this.cstack = new Stack<Number>();
		this.ack = new Stack<Number>();
		
		this.tokLst = new ArrayList<String>();
		this.tokLst.addAll(t.tokLst);
		
		// leave the list of checkpoints as they are
		this.checkpoints = new HashSet<Integer>();
		this.del = t.del;
	}
	
	public String getDel() {
		return del;
	}

	public void setDel(String del) {
		this.del = del;
	}

	public Integer [] getCheckpoints() {
		return this.checkpoints.toArray(new Integer[this.checkpoints.size()]);
	}
	
	public List<String> getTokLst() {
		return tokLst;
	}

	public void addCheckpoint(int bp) {
		if(!this.checkpoints.contains(bp))
			this.checkpoints.add(bp);
	}
	
	public int getCheckpointCnt() {
		return this.checkpoints.size();
	}
	
	public void setTokLst(List<String> tokLst) {
		this.tokLst = tokLst;
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public int getCstackTop() {
		return this.cstack.lastElement().intValue();
	}
	
	public int getAckStackTop() {
		return this.ack.lastElement().intValue();
	}
	
	public int popCPos() {
		if(this.cstack.size() > 0)
			return this.cstack.pop().intValue();
		else
			return 0;
	}
	
	public int popLfr() {
		//popCPos();
		int ret = 0;
		try {
			ret = this.lfr.pop().intValue();
		} catch (EmptyStackException e) {
			return 0;
		}
		return ret;
	}

	public void pushLfr(int lfr) {
		//this.cstack.push(this.cursor);
		this.lfr.push(lfr);
	}
	
	public int popAck() {
		if(this.ack.size() > 0)
			return this.ack.pop().intValue();
		else 
			return 0;
	}

	public void pushAck(int idx) {
		this.ack.push(idx);
	}
	
	public int getLfr() {
		if (this.lfr.size() > 0)
			return this.lfr.get(this.lfr.size()-1).intValue();
		else
			return 0;
	}
	
	public Boolean hasNext() {
		return (this.cursor < (tokLst.size() - 1));	
	}
	
	public String next() {
		
		String s = tokLst.get(cursor);
		
		if (cursor < (tokLst.size() - 1))
			cursor ++;
		
		return s;
	}
	
	public String current() {
		return tokLst.get(cursor);
	}
	
	public Boolean isFinished() {
		// Keep in mind that there is a redundant end element
		return (this.cursor >= this.tokLst.size() - 1);
	}
	
	public String get(int idx) {
		return tokLst.get(idx);
	}
	
	public Boolean finished() {
		return (cursor == this.tokLst.size()-1);
	}
	
	public int getSize() {
		return tokLst.size();
	}
	
	public void flushLfr() {
		this.lfr.clear();
	}
	
	public void append(String tok) {
		assert(this.tokLst.size() > this.cursor);
		this.tokLst.add(this.cursor++, tok);
	}
	
	public String remove (int idx) {
		String s = this.tokLst.remove(idx);
		if (s != null && idx <= cursor) {
			this.cursor--;
		}
		return s;
	}
	
	
	public void insert (int idx, String elem) {
		this.tokLst.add(idx, elem);
		if ( idx <= cursor ) {
			this.cursor ++;
		}
	}
	
	public void replace (int idx, String elem) {
		assert(idx < this.tokLst.size());
		this.tokLst.remove(idx);
		this.tokLst.add(idx,elem);
		
	}
	
	private static void normalize(List<String> lst) {
		Iterator<String> iter = lst.iterator();
		
		while (iter.hasNext()) {
			String item = (String) iter.next();
			item = item.trim();
			if(item.length() <= 0){
				iter.remove();
			}
		}
	}
	
	public String getStrFromTokens() {
		String str = "";
		for(int i = 0; i < this.tokLst.size()-1; i++) {
			str += tokLst.get(i);
			if(i < this.tokLst.size()-2 )
				str += this.del;
		}
		return str;
	}
	
	public int getLength() {
		return this.tokLst.size();
	}
	
	public int getLfrSize() {
		return this.lfr.size();
	}
	
	@Override
	public String toString() {
		String s = "";
		for(int i = 0; i < this.tokLst.size(); i++) {
			
			if (this.checkpoints.contains(i) && i != this.cursor) {
				s += "{" + this.tokLst.get(i) + "}";
			}  else if (this.checkpoints.contains(i) && i == this.cursor) {
				s +="<{" + this.tokLst.get(i) + "}>";
			} else if (i == this.cursor) {
				s +="<" + this.tokLst.get(i) + ">";
			} else {
				s +="[" + this.tokLst.get(i) + "]";
			}
		}
		
		s = s + "\nCheckpoints: " + this.checkpoints.toString() + "\n";
		s = s + "Length: " + this.tokLst.size() + "\n";
		return s;
	}

}
