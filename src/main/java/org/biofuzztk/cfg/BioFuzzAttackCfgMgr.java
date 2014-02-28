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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 
 * This is a class that resembles a whole context-free grammar (CFG). A
 * grammar is represented as a set of CFG-graphs (one for each production
 * rule). 
 * 
 * @author julian
 *
 */
public class BioFuzzAttackCfgMgr {
	/**
	 * 
	 * Maps the name of a production rule to its definition.
	 * 
	 */
	private Map<String,BioFuzzAttackCfg> cfgMap;
	
	public BioFuzzAttackCfgMgr() {
		this.cfgMap = new HashMap<String,BioFuzzAttackCfg>();
	}
	
	/**
	 * 
	 * Add a new production rule.
	 * 
	 * @param name name of the production rule.
	 * @return return the corresponding definition.
	 * 
	 */
	public BioFuzzAttackCfg createAttackCfg(String name) {
		BioFuzzAttackCfg cfg = new BioFuzzAttackCfg();
		cfgMap.put(name, cfg);
		return cfg;
	}
	
	
	/**
	 * 
	 * Returns all production rule name.
	 * 
	 * @return set of production rules.
	 * 
	 */
	public Set<String> getKeys() {
		return this.cfgMap.keySet();
	}

	/**
	 * 
	 * Returns the definition of a given production rule.
	 * 
	 * @param key name of the production rule.
	 * @return the corresponding defintion of the production rule.
	 * 
	 */
	public BioFuzzAttackCfg getAttackCfgByKey(String key) {
		return this.cfgMap.get(key);
	}

	/**
	 * 
	 * A setter for cfgMap.
	 * 
	 * @param cfgMap
	 * 
	 */
	public void setCfgMap(HashMap<String, BioFuzzAttackCfg> cfgMap) {
		this.cfgMap = cfgMap;
	}

	@Override
	public String toString() {
		

		String s = "";
		for (Map.Entry<String, BioFuzzAttackCfg> entry : this.cfgMap.entrySet()) {
			String name = entry.getKey();
			BioFuzzAttackCfg cfg = entry.getValue();
			
			s += "Name: " + name + "\n" + cfg.toString() + "\n";
			
		}

		return "BioFuzzAttackCfgMgr:\n" +
				"Configurations: " + this.cfgMap.size() + "\n" + s + "\n";
	}
	

}
