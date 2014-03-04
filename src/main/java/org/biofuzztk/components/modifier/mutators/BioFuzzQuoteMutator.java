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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.components.modifier.BioFuzzMutationType;
import org.biofuzztk.components.modifier.BioFuzzMutator;
import org.biofuzztk.ptree.BioFuzzTokLst;

/**
 * 
 * Mutator that changes the quote representation.
 * 
 * @author julian
 *
 */
public class BioFuzzQuoteMutator implements BioFuzzMutator {

	public enum MutationType implements BioFuzzMutationType {
		GBK("GBK");
		
		private String desc;
		MutationType(String desc) {
			this.desc = desc;
		}
		
		@Override
		public String toString() {
			return this.desc ;
		}
	};
	
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzQuoteMutator.class);
	
	@Override
	public void mutate(BioFuzzTokLst tokLst, int tidx) {
		
		MutationType [] type = {MutationType.GBK};
		
		//int tokIdx = tokLst.getSize()-2;
		
		int tokIdx = tidx;
		String s = tokLst.get(tokIdx);

		Random rand = new Random();
		int idx = rand.nextInt(type.length);

		logger.debug("Mutation Type that is being applied: " + type[idx]);
		
		switch(type[idx]) {
		case GBK:
			char a = (char)191;
			char b = (char)27;
			s = String.format("%c%c", a,b);
			s = s.toLowerCase();
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
		if(s.matches("[\"']") && s.length() == 1)
			return true;
		else
			return false;
	}
	
	

}
