package org.biofuzztk.test;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biofuzztk.components.BioFuzzMgr;
import org.biofuzztk.components.tokenizer.BioFuzzTokenizer;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestBioFuzzMath {

	private static BioFuzzMgr mgr;
	final static Logger logger = LoggerFactory.getLogger(TestBioFuzzModifier.class);

	static List<BioFuzzParseTree> tLst0 = null;
	static List<BioFuzzParseTree> tLst1 = null;
	
	@BeforeClass
	public static void testParser() {
		
		
		BioFuzzTokenizer tokenizer = new BioFuzzTokenizer() {

			@Override
			public String[] tokenize(String s) {
				List<String> matches = new ArrayList<String>();
				Pattern pattern = Pattern.compile("((\\d*\\.\\d+)|(\\d+)|([\\+\\-\\*/\\(\\)]))");
				Matcher m = pattern.matcher(s);
				
				while (m.find()) {
				   matches.add(m.group());
				}
				
				String [] ret = matches.toArray(new String[matches.size()+1]);
				ret[ret.length-1] = "$";
				return ret;
			}
			
		};

		mgr = new BioFuzzMgr("src/main/resources/math.xml", tokenizer);
		assert(mgr != null);
	}
	
	@Test
	public void testCrossOver() {

		logger.debug(">> Tree 0 creation");
		
		tLst0 = mgr.buildTrees("1+4*(5+2)/10-4");
		mgr.validate(tLst0.get(0));
		
		BioFuzzParseTree t = mgr.getNewParseTree();
		while(!t.getVal()) {
			mgr.extend(t);
		}
		logger.debug("new tree: " + t.toString());
		
		
		logger.debug(">> check");
		assert(tLst0 != null);
		logger.debug("List length: " + tLst0.get(0).toString());
			
	}


}
