package org.biofuzztk.test;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biofuzztk.components.BioFuzzMgr;
import org.biofuzztk.components.modifier.BioFuzzMutator;
import org.biofuzztk.components.tokenizer.BioFuzzTokenizer;
import org.biofuzztk.ptree.BioFuzzParseTree;
import org.biofuzztk.ptree.BioFuzzTokLst;
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
		
		
		BioFuzzMutator mut = new BioFuzzMutator() {
			
			@Override
			public void mutate(BioFuzzTokLst lst, int idx) {

				int num = Integer.parseInt(lst.get(idx));
				num += 5;
				lst.remove(idx);
				lst.insert(idx, String.valueOf(num));
				
			}

			@Override
			public String getName() {
				return "Adder";
			}

			@Override
			public boolean matches(String s) {
				if(s.matches("[0-9]+"))
					return true;
				else
					return false;
			}

		};
		
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
		
		List<BioFuzzMutator> lmut = new Vector<BioFuzzMutator>();
		lmut.add(mut);

		mgr = new BioFuzzMgr("src/main/resources/math.xml", tokenizer, lmut);
		assert(mgr != null);
	}
	
	//@Test
	public void testStrManipulation() {

		logger.debug(">> Tree 0 creation");
		
		tLst0 = mgr.buildTrees("1+4*(5+2)/10-4+");
		mgr.validate(tLst0.get(0));

		mgr.extend(tLst0.get(0));

		


		logger.debug("List length: " + tLst0.get(0).toString());
		
//		BioFuzzParseTree t = mgr.getNewParseTree();
//		while(!t.getVal()) {
//			mgr.extend(t);
//		}
//		logger.debug("new tree: " + t.toString());
//		
//		
//		logger.debug(">> check");
//		assert(tLst0 != null);
//		logger.debug("List length: " + tLst0.get(0).toString());
			
	}

	
	@Test
	public void testCrossOver() {

		logger.debug(">> Tree 0 creation");
		
		tLst0 = mgr.buildTrees("1+4*(5+2)/10-4");

		BioFuzzParseTree tree = tLst0.get(0);
		logger.debug(">> Before " + tree.getTokLst().toString());
		
		mgr.mutate(tree,0,tree.getTokLstLen()-2);
		
		logger.debug(">> After " + tree.getTokLst().toString());
		

		
	}

}
