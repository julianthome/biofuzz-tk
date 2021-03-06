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

/**
 * 
 * The different parsing statuses. 
 * 
 * @author julian
 *
 */
public enum BioFuzzParsingStatus {
	
	/**
	 * string is does not match CFG.
	 */
	INVALID("INVALID"),
	
	/**
	 * parse-tree for string is still under construction.
	 */
	IN_PROGRESS("IN_PROGRESS"), 
	
	/**
	 * all terminal nodes are consumed.
	 */
	FINISHED("FINISHED"),
	
	/**
	 * all terminal nodes are consumed and parse tree is complete.
	 */
	VALID("VALID");
	
	private String desc;

	BioFuzzParsingStatus(String desc) {
		this.desc = desc;
	}

	
	@Override
	public String toString() {
		return this.desc;
	}
};
