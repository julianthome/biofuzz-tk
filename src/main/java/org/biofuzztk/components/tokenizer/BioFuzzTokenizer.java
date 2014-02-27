package org.biofuzztk.components.tokenizer;

/**
 * 
 * Interface that all tokenizers have to implement.
 * 
 * @author julian
 *
 */
public interface BioFuzzTokenizer {

	/**
	 * 
	 * Interface function to tokenize a string.
	 * 
	 * @param s a string.
	 * @return the tokenized string where each array field corresponds to a token.
	 * 
	 */
	public String[] tokenize(String s);

}
