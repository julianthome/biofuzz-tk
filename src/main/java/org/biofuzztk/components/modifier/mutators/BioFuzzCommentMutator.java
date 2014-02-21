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

import org.biofuzztk.components.modifier.BioFuzzMutator;
import org.biofuzztk.ptree.BioFuzzTokLst;

public class BioFuzzCommentMutator implements BioFuzzMutator {
	
	@Override
	public void mutate(BioFuzzTokLst lst) {

		lst.setDel("/**/");
	}

	@Override
	public String getName() {
		return "BioFuzzCommentMutator";
	}

}
