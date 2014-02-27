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

package org.biofuzztk.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.cfg.BioFuzzAttackTag.TagType;

/**
 * 
 * This is a class that resembles one single production rule of a context-free 
 * grammar as a graph (CFG-graph). The implementation uses the simplest possible 
 * representation of a graph, i.e. an adjacency list.
 * 
 * @author julian
 *
 */
public class BioFuzzAttackCfg {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzAttackCfg.class);
	
	private Vector<BioFuzzAttackTag> matDesc;
	private HashMap<Number,List<Number>> matCoord;
	
	public BioFuzzAttackCfg() {
		this.matDesc = new Vector<BioFuzzAttackTag>();
		this.matCoord = new HashMap<Number,List<Number>>();
	}

	/**
	 * 
	 * @param matDesc a vector of nodes of the graph.
	 */
	public void setMatDesc(Vector<BioFuzzAttackTag> matDesc) {
		this.matDesc = matDesc;
	}

	/**
	 * 
	 * Adds a new pair of node and its successor to the adjacency
	 * list.
	 * 
	 * @param a a node in the CFG-graph (node A)
	 * @param b a node in the CFG-graph (node A's successor)
	 */
	public void addPoint(int a, int b) {
		
		if (!this.matCoord.containsKey(a)){
			List<Number> l = new ArrayList<Number>();
			l.add(b);
			this.matCoord.put(a, l);
		} else {
			// key already present
			if(!this.matCoord.get(a).contains(b)) {
				Boolean ret = this.matCoord.get(a).add(b);
				assert(ret == true);
			}
		}
		
	}
	
	/**
	 * 
	 * Returns a list of successors for a given node.
	 * 
	 * @param idx identifies a node (node A).
	 * @return a list of indices that identify all successors from node A. 
	 */
	public List<Number> getChoicesByIdx(int idx) {
		assert(this.matCoord != null);
		//logger.debug("get idx " + idx);
		return this.matCoord.get(idx);
	}
	
	/**
	 * 
	 * Returns the number of nodes.
	 * 
	 * @return the number of nodes of the CFG-graph.
	 */
	public int getDescNrs() {
		return this.matDesc.size();
	}
	
	/**
	 * 
	 * Adds a node to the CFG-graph. 
	 * 
	 * @param atag a node of the CFG-graph.
	 * 
	 */
	public void addAtag(BioFuzzAttackTag atag) {
		this.matDesc.add(atag);
	}
	
	/**
	 * 
	 * Extends the CFG-graph after creation. This is useful if
	 * you want to modify production rules that are already present.
	 * 
	 * @param atag a node of the CFG-graph.
	 * 
	 */
	public void appendAtag(BioFuzzAttackTag atag) {
		assert(this.matDesc.size() > 0);
		
		int oldStopIdx = this.matDesc.size()-1;
		
		BioFuzzAttackTag stop = this.matDesc.get(oldStopIdx);
		
		assert(stop.getTagType() == TagType.STOP);
		
		int coord = stop.getCoord();
		
		stop.setCoord(coord+1);
		
		atag.setCoord(oldStopIdx);
		
		this.matDesc.insertElementAt(atag,oldStopIdx);
		
		int toRm = -1;
		
		for(Number n : this.matCoord.keySet() ) {
			List<Number> ln = this.matCoord.get(n);
			
			for(int i = 0; i < ln.size(); i++) {
				if(ln.get(i).intValue() == coord) {
					toRm = i;
					break;
				}
			}
			if(toRm != -1) {
				ln.remove(toRm);
				ln.add(coord + 1);
			}
		}
		
		addPoint(0,coord);
		addPoint(coord,coord+1);
	}
	
	/**
	 * 
	 * Translates a node index to the 'real' node.
	 * 
	 * @param descIdx
	 * @return a node of the CFG-graph.
	 */
	public BioFuzzAttackTag getAtagByIdx(int descIdx) {
		if ( descIdx >= 0 && this.matDesc.size() > descIdx ) {
			return this.matDesc.get(descIdx);
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		
		Iterator<BioFuzzAttackTag> iter = this.matDesc.iterator();
		String s = "";
		
		int idx = 0;
		while(iter.hasNext()) {
			s += "[<" + idx++ +"> "  + ((BioFuzzAttackTag)iter.next()).getName() +"]";
		}
		
		return "BioFuzzAttackCfg" +  "\nMatcoord:" +
				this.matCoord + "\nMatdesc:" +
				s + "\n";			
	}
	
}
