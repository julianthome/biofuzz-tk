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

	static List<BioFuzzParseTree> tLst = null;
	
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
	public void test() {

		logger.debug(">> Create a tree");
		
		tLst = mgr.buildTrees("aaaaaa");
		assert(tLst != null);
		logger.debug(">> Tree successfully build");
		
		logger.debug(">> get tree");
		BioFuzzParseTree tree = tLst.get(0);
		
		mgr.validate(tree);
		logger.debug("List length: " + tree.toString());
			
	}


}
