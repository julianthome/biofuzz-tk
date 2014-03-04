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

package org.biofuzztk.components.modifier.mutators;

import java.util.Random;
import org.biofuzztk.components.modifier.BioFuzzMutationType;
import org.biofuzztk.components.modifier.BioFuzzMutator;
import org.biofuzztk.ptree.BioFuzzTokLst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Mutator that changes the case of a terminal node:
 * 	- uppercase
 * 	- lowercase
 * 	- mixed case
 * 
 * @author julian
 *
 */
public class BioFuzzCaseMutator implements BioFuzzMutator {

	public enum MutationType implements BioFuzzMutationType {
		UPPERCASE("UPPERCASE"), 
		LOWERCASE("LOWERCASE"), 
		MIXED("MIXED");
		
		private String desc;
		MutationType(String desc) {
			this.desc = desc;
		}
		
		@Override
		public String toString() {
			return this.desc;
		}
	};
	
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzCaseMutator.class);
	
	@Override
	public void mutate(BioFuzzTokLst tokLst, int tidx) {
		MutationType [] type = {MutationType.LOWERCASE, MutationType.UPPERCASE, MutationType.MIXED};
		
		//int tokIdx = tokLst.getSize()-2;
		int tokIdx = tidx;
		String s = tokLst.get(tokIdx);

		
		Random rand = new Random();
		int idx = rand.nextInt(type.length-1);

		logger.debug("Mutation Type that is being applied: " + type[idx]);
		
		switch(type[idx]) {
		case LOWERCASE:
			s = s.toLowerCase();
			break;
		case UPPERCASE:
			s = s.toUpperCase();
			break;
		case MIXED:
			String mixed = "";
			for (int i = 0; i < s.length(); i++) {
				if (rand.nextBoolean()) {
					mixed += String.valueOf(s.charAt(i)).toUpperCase();
				} else {
					mixed += String.valueOf(s.charAt(i)).toLowerCase();
				}
			}
			s = mixed;
			break;
		}
		
		tokLst.remove(tokIdx);
		
		if(tokIdx > 0) {
			tokLst.insert(tokIdx, s);
		} else {
			tokLst.insert(0,s);
		}
		
	}

	@Override
	public String getName() {
		return "Case Mutator";
	}

	@Override
	public boolean matches(String s) {
		if(s.matches("[a-zA-z]+"))
			return true;
		else
			return false;
	}
	
	

	

}
