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

package org.biofuzztk.components.modifier;

import org.biofuzztk.ptree.BioFuzzTokLst;

/**
 * 
 * Interface that has to implemented by any BioFuzz mutator.
 * 
 * @author julian
 *
 */
public interface BioFuzzMutator {
	
	/**
	 * 
	 * The mutation function that takes a token list and applies a
	 * mutation on it. 
	 * 
	 * @param lst the token list.
	 * @param idx the index of the token to mutate.
	 * 
	 */
	public void mutate(BioFuzzTokLst lst, int idx);
	
	
	/**
	 * 
	 * This is a constraint function - mutation is only applied if this
	 * function returns true.
	 * 
	 * @param s the string to match.
	 * @return true if string s matches the defined constraint.
	 */
	public boolean matches(String s);
	
	/**
	 * 
	 * Returns the name of the mutator.
	 * 
	 * @return the name of the mutator.
	 * 
	 */
	public String getName();

}
