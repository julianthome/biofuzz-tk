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

public class BioFuzzAttackCfgMgr {
	
	private Map<String,BioFuzzAttackCfg> cfgMap;
	
	public BioFuzzAttackCfgMgr() {
		this.cfgMap = new HashMap<String,BioFuzzAttackCfg>();
	}
	
	
	public BioFuzzAttackCfg createAttackCfg(String name) {
		BioFuzzAttackCfg cfg = new BioFuzzAttackCfg();
		cfgMap.put(name, cfg);
		return cfg;
	}
	
	
	
	public Set<String> getKeys() {
		return this.cfgMap.keySet();
	}

	public BioFuzzAttackCfg getAttackCfgByKey(String key) {
		return this.cfgMap.get(key);
	}

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
