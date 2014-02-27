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

package org.biofuzztk.components.tokenizer;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BioFuzzSQLTokenizer implements BioFuzzTokenizer {
	
	final static Logger logger = LoggerFactory.getLogger(BioFuzzSQLTokenizer.class);
	
	private static ArrayList<String> tokens = null;
	//private static BioFuzzTokenizer tokenizer = null;

	private static final String grpStr = "([\"'`][^\"'`]*[\"'`])";
	
	public BioFuzzSQLTokenizer() {
		tokens = new ArrayList<String>();
	}
	
	
	private void applyRegexp(String rx, String rep) {
		for(int i = 0; i < tokens.size(); i++) {
			String s = tokens.get(i);
			
			if(s.matches(grpStr)) {
				//logger.debug("MATCH" + s);
				
				continue;
			}
			//logger.debug("replace " + rx + " by " +rep + " for " + s);
			s = s.replaceAll(rx, rep).trim();
			
			//logger.debug("done" + s);
			
			tokens.set(i, s);
		}
	}
	
	
	private void complete() {
		for(int i = 0; i < tokens.size(); i++) {
			String s = tokens.get(i);
			s = s.replaceAll("\'", "\n\'\n").trim();
			s = s.replaceAll("\"", "\n\"\n").trim();
			s = s.replaceAll("`", "\n`\n").trim();
			tokens.set(i, s);
		}
	}
	
	
	private void delimit() {
		for(int i = 0; i < tokens.size(); i++) {
			String s = tokens.get(i);

			if(s.matches(grpStr)) {
				//logger.debug("MATCH" + s);
				continue;
			}
			s = s.replaceAll(", *","\n,\n");
			s = s.replaceAll(" ","\n");
			tokens.set(i, s);
		}
	}
	
	@Override 
	public String toString() {
		String s = "";
		
		for(int i = 0; i < tokens.size(); i++) {
			s += "["+ tokens.get(i) +"]\n";
		}
		
		return s;
		
	}
	
	public String generateResult() {
		String s = "";
		
		for(int i = 0; i < tokens.size(); i++) {
			s += tokens.get(i).trim() + "\n";
		}
		
		return s;
		
	}

	public void log() {
		
		for(int i = 0; i < tokens.size(); i++) {
			logger.debug("i: " + tokens.get(i).trim());
		}
		
		
	}

	
	public String[] tokenize(String s) {
		tokens.clear();
		s = s.replaceAll(grpStr,"\n$1\n");
		
		
		logger.debug(" >> " + s);
		
		String [] sl = s.split("\n");

		
		for(int i = 0; i < sl.length; i++) {
			logger.debug("token add " + sl[i]);
			tokens.add(sl[i]);
		}
		
		delimit();
		
		
		
		applyRegexp("[ \n]*\\([ \n]*", "\n\\(\n");
		applyRegexp("[ \n]*\\)[ \n]*", "\n\\)\n");
		
		//logger.debug(" # " + this.tokens.toString());

		applyRegexp("[ \n]*=[ \n]*", "\n=\n");
		applyRegexp("[ \n]*>[ \n]*", "\n>\n");
		applyRegexp("[ \n]*<[ \n]*", "\n<\n");

		applyRegexp(" *[Nn][Oo][Ww][ \n]*\\([ \n]*\\)[ +\n]", "\nNOW\\(\\)\n");
		
		applyRegexp(" *[Cc][Hh][Aa][Rr][\n]*\\([\n]*([0-9]*)[\n]*\\)", "\nCHAR\\($1\\)\n");
		applyRegexp(" *[Cc][Oo][Nn][Cc][Aa][Tt][ \n]*\\([ \n]", "\nCONCAT\\(\n");
		applyRegexp(" *[Mm][Dd]5[ \n]*\\([ \n]", "\nMD5\\(\n");
		
		applyRegexp("\n<\n+>\n", "\n<>\n");
		applyRegexp("[ +\n][Cc][Oo][Uu][Nn][Tt][ \n]*\\([ \n]*\\*[ \n]\\)[ +\n]", "\nCOUNT\\(\\*\\)\n");
		applyRegexp("<[\n ]*>", "\n<>\n");
		applyRegexp("[ \n]*[Oo][Rr][Dd][Ee][Rr][\n ]*[Bb][Yy][ \n]*", "\nORDER BY\n");
		applyRegexp(" *[Dd][Ee][Ss][Cc] *", "\nDESC\n");
		
		applyRegexp(" *", "");
		applyRegexp(" *\n+", "\n");
		applyRegexp(" *\n *", "\n");
		applyRegexp(", \n", ",");
		applyRegexp(";", "\n;\n");
		
		
		complete();
		
		String ret = this.generateResult();
		ret += "\n$";
		logger.debug("RETURN : "+ ret);
		return ret.split("\n");
	}
	
}
