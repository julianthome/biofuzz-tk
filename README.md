# biofuzz-tk

## What is biofuzz-tk

BioFuzz Toolkit helps you to parse strings and to perform operations (modification/extension) on the resulting parse trees. It is suited to programs that leverage genetic programming (GP) or similar techniques. The parsing and token-generation happens based on a context-free grammar description. Further, it supports the two well-known GP operations: Mutation and Crossover.

## How to use it

biofuzz-tk works in two steps: first you have to tokenize your string and then you have to parse it. Both, the tokenizing and parsing are configurable.

### Example: Easy string parsing

Suppose you want to parse the string **aaaaa** matches the grammar definition
	S ->  a {S}

You can define a CFG configuration for biofuzz-tk that looks as follows:

```
<attackcfg>
        <rule>
	        <key label="S"/>	
	        <val>
	        	<start/>
	        		<const label="a"/>
	        		<oneof>
			        	<var label="S"/>
					<stop/>
		        	</oneof>
		        <stop/>
	        </val>
        </rule>
</attackcfg>
```

The BioFuzz Manager is the object that manages everything operation that can be executed on a parse-tree including the parse-tree generation itself. To read in the configuration you habe to create a Manager object and pass the path to the configuration file as a paramter. Besides that you have to implement a tokenizer as well and pass it to the manager. Before the parsing takes place, a string has to be split it tokens. The interface-method    tokenize from the interface    BioFuzzTokenizer should return an array of tokens. An appropriate tokenizer implementation might looks as follows:

```
BioFuzzTokenizer tokenizer = new BioFuzzTokenizer() {
	@Override
	public String[] tokenize(String s) {
		s = s.replaceAll("a","a\n");			
		return s.split("\n");
	}		
};
```





