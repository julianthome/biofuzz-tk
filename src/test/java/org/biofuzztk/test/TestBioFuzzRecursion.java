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


public class TestBioFuzzRecursion {

	private static BioFuzzMgr mgr;
	final static Logger logger = LoggerFactory.getLogger(TestBioFuzzModifier.class);

	static List<BioFuzzParseTree> tLst0 = null;
	
	@BeforeClass
	public static void testParser() {
		
		
		BioFuzzTokenizer tokenizer = new BioFuzzTokenizer() {

			@Override
			public String[] tokenize(String s) {
				s = s.replaceAll("a","a\n");
				
				return s.split("\n");
			}
			
		};

		mgr = new BioFuzzMgr("src/main/resources/recursion.xml", tokenizer);
		assert(mgr != null);
	}
	
	@Test
	public void testCrossOver() {

		logger.debug(">> Tree 0 creation");
		
		tLst0 = mgr.buildTrees("aaaaaa");
		
		logger.debug(">> check");
		assert(tLst0 != null);
		logger.debug("List length: " + tLst0.get(0).toString());
			
	}


}
