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


package org.biofuzztk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BioFuzzLevenshtein {
	final static Logger logger = LoggerFactory.getLogger(BioFuzzLevenshtein.class);


	private BioFuzzLevenshtein(String s1, String s2) {
	}


	public static int getLdist(String s1, String s2) {
		//s1 = s1.toLowerCase();
		//s2 = s2.toLowerCase();

		int [] dist = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					dist[j] = j;
				else {
					if (j > 0) {
						int newValue = dist[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), dist[j]) + 1;
						dist[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				dist[s2.length()] = lastValue;
		}
		return dist[s2.length()];
	}

	
	public static double getNormalizedLdist(String s1, String s2) {
		//s1 = s1.toLowerCase();
		//s2 = s2.toLowerCase();

		double ldist = getLdist(s1,s2);
		
		double mlen = s1.length() > s2.length() ? s1.length() : s2.length();
		
		assert(mlen != 0.0);
		
		return ldist/mlen;
		
	}


}
