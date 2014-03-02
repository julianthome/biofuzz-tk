# biofuzz-tk

## What is biofuzz-tk

BioFuzz Toolkit helps you to parse strings and to perform operations (modification/extension) on the resulting parse trees. It is suited to programs that leverage genetic programming (GP) or similar techniques. The parsing and token-generation happens based on a context-free grammar description. Further, it supports the two well-known GP operations: Mutation and Crossover.

## How to use it

biofuzz-tk works in two steps: first you have to tokenize your string and then you have to parse it. Both, the tokenizing and parsing are configurable.

### Example 1: Easy string parsing

Suppose you want to parse the string **aaaaaa** matches the grammar definition
	S ->  a {S}

You can define a CFG configuration *cfg.xml* for biofuzz-tk that looks as follows:

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

The whole configuration is embedded in the *attackcfg*-tag. This tag contains one or more *rule*-tags. A *rule*-tag represents a production rule. The *key*-tag defines the name of the non-terminal *S* (left-hand side of a production rule) which is expected to be present, since it is the starting-symbol. The *val*-tag contains the actual production rule definition. The meta-tags *start* and *stop* are used to indicate the starting/stopping terminals/non-terminals. The *const*-tag is used to define terminals whereas the *var*-tag references non-terminal definitions. When you are writing your own configuration file, please make sure, that all the non-terminals that are references do have their own rule-definition. The biofuzz-tk definition is EBNF-alike, so we have plenty of different XML-tags that can be used:


| biofuzz-tk        | meaning | 
| ------------- |:-------------:| 
|   oneof    |  alternative |
|  zormore     |  optional repeat |
|  zorone    |   optional |
|  group |  grouping  |


The BioFuzz Manager is the object that manages everything operation that can be executed on a parse-tree including the parse-tree generation itself. To read in the configuration you habe to create a Manager object and pass the path to the configuration file as a paramter. Besides that you have to implement a tokenizer as well and pass it to the manager. Before the parsing takes place, a string has to be split it tokens. The interface-method *tokenize* from the interface *BioFuzzTokenizer* should return an array of tokens. An appropriate tokenizer implementation might looks as follows:

``` java
BioFuzzTokenizer tokenizer = new BioFuzzTokenizer() {
	@Override
	public String[] tokenize(String s) {
		s = s.replaceAll("a","a\n");			
		return s.split("\n");
	}		
};
```

The next important step is the creation of a BioFuzz Manager object itself by typing:


``` java
BioFuzzMgr mgr = new BioFuzzMgr("cfg.xml", tokenizer); ;
```

If you want to parse the string **aaaaaa**, you can use the following instruction:

``` java
List<BioFuzzParseTree> tLst = mgr.buildTrees("aaaaaa");
BioFuzzParseTree tree = tLst.get(0);
mgr.validate(tree);
```

The BioFuzz Manager will now tokenize the string, parse it and return al list of parse-trees, i.e. all parse-trees that resemble all possible paths through the CFG that can be taken in order to generate the string *aaaaaa*. The second line just shows how to access the first parse-tree of the list. The third line shows the validation method that is incorporated in the manger. It just traverses the tree and checks whether it is complete according to the CFG or not. This information is important for the tokenizer which we will discuss later. The resulting parse tree might look as follows (*toString()* method is implemented).

	* [ ROOT(S): validity:(true)]
	|--(tok_idx: 0 descIdx: 1 id:1)[AttackTag: a(TERMINAL val:-5)(true)]
	 |--(tok_idx: 0 descIdx: 1 id:1)[Tok: a(TERMINAL val:-5)(true)]
	|--(tok_idx: 1 descIdx: 2 id:2)[AttackTag: S(NON_TERMINAL val:-4)(true)]
	 |--(tok_idx: 1 descIdx: 1 id:3)[AttackTag: a(TERMINAL val:-5)(true)]
	  |--(tok_idx: 1 descIdx: 1 id:3)[Tok: a(TERMINAL val:-5)(true)]
	 |--(tok_idx: 2 descIdx: 2 id:4)[AttackTag: S(NON_TERMINAL val:-4)(true)]
	  |--(tok_idx: 2 descIdx: 1 id:5)[AttackTag: a(TERMINAL val:-5)(true)]
	   |--(tok_idx: 2 descIdx: 1 id:5)[Tok: a(TERMINAL val:-5)(true)]
	  |--(tok_idx: 3 descIdx: 2 id:6)[AttackTag: S(NON_TERMINAL val:-4)(true)]
	   |--(tok_idx: 3 descIdx: 1 id:7)[AttackTag: a(TERMINAL val:-5)(true)]
	    |--(tok_idx: 3 descIdx: 1 id:7)[Tok: a(TERMINAL val:-5)(true)]
	   |--(tok_idx: 4 descIdx: 2 id:8)[AttackTag: S(NON_TERMINAL val:-4)(true)]
	    |--(tok_idx: 4 descIdx: 1 id:9)[AttackTag: a(TERMINAL val:-5)(true)]
	     |--(tok_idx: 4 descIdx: 1 id:9)[Tok: a(TERMINAL val:-5)(true)]

	#Nodes: 10
	#NTs: 1
	NTs: [S]
	NTUbound: [S]
	Last NT: (tok_idx: 4 descIdx: 2 id:8)
	TokLst: [a][a][a][a][a]<a>
	Checkpoints: []
	Length: 6

	TokLstStr: a a a a a

### Example 1: Mathematical Expressions



