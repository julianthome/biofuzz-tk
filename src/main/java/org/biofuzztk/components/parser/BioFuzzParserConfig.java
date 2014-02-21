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


public class BioFuzzParserConfig {

	private BioFuzzParsingStatus minQual = BioFuzzParsingStatus.INVALID;
	private int maxIter = 0;
	private int maxSsize = 0;
	
	public BioFuzzParserConfig(int maxSsize, BioFuzzParsingStatus minQuality, int maxIter) {

		this.minQual = minQuality;
		this.maxSsize = maxSsize;
		this.maxIter = maxIter;
	}

	public BioFuzzParsingStatus getMinQual() {
		return minQual;
	}

	public void setMinQual(BioFuzzParsingStatus minQual) {
		this.minQual = minQual;
	}

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

	public int getMaxSsize() {
		return maxSsize;
	}

	public void setMaxSsize(int maxSsize) {
		this.maxSsize = maxSsize;
	}
	
	

	
	
}
