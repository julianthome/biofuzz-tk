# biofuzz-tk

[Easy string parsing](###Example-1:-Easy-string-parsing)

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

The whole configuration is embedded in the *attackcfg*-tag. This tag contains one or more *rule*-tags. A *rule*-tag represents a production rule. The *key*-tag defines the name of the non-terminal *S* (left-hand side of a production rule) which is expected to be present, since it is the starting-symbol. The *val*-tag contains the actual production rule definition. The meta-tags *start* and *stop* are used to indicate the starting/stopping terminals/non-terminals. The *const*-tag is used to define terminals whereas the *var*-tag references non-terminal definitions. When you are writing your own configuration file, please make sure, that all the non-terminals that are references do have their own rule-definition. The biofuzz-tk definition is EBNF-alike. The following XML-tags can be used.


| biofuzz-tk        | meaning | 
| ------------- |:-------------:| 
|   oneof    |  alternative |
|  zormore     |  optional repeat |
|  zorone    |   optional |
|  group |  grouping  |
|  regexp |  to match regular expressions that are defined via the label-attribute.  |


The BioFuzz Manager is the object that manages every operation that can be executed on a parse-tree including the parse-tree generation itself. To read in the configuration you habe to create a Manager object and pass the path to the configuration file as a paramter. Besides that you have to implement a tokenizer as well and pass it to the manager. Before the parsing takes place, a string has to be split it tokens. The interface-method *tokenize* from the interface *BioFuzzTokenizer* should return an array of tokens. An appropriate tokenizer implementation might looks as follows:

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

#### Parsing
A CFG configuration for mathematical expressions might look as follows.

```
<attackcfg>
        <rule>
	        <key label="S"/>	
	        <val>
	        	<start/>
					<var label="term"/>
	        	<stop/>
	        </val>
        </rule>
        <rule>
	        <key label="number"/>	
	        <val>
	        	<start/>
					<regexp label="[0-9]{1,10}"/>
	        	<stop/>
	        </val>
        </rule>
         <rule>
	        <key label="operator"/>	
	        <val>
	        	<start/>
					<oneof>
						<const label="+"/>
						<const label="-"/>
						<const label="*"/>
						<const label="/"/>
					</oneof>
	        	<stop/>
	        </val>
        </rule>
        <rule>
	        <key label="term"/>	
	        <val>
	        	<start/>
	        		<oneof>
	        			<var label="number"/>
	        			<grp>
							<const label="("/>
							<var label="term"/>
							<const label=")"/>
					</grp>
		        		<grp>
							<var label="term"/>
							<var label="operator"/>
							<var label="term"/>
						</grp>
				</oneof>
	        	<stop/>
	        </val>
        </rule>
</attackcfg>
```

Lets suppose you want to parse the expression *1+4*(5+2)/10-4*. The following code will do that for you:

``` java
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

mgr = new BioFuzzMgr("cfg.xml", tokenizer);
tLst0 = mgr.buildTrees("1+4*(5+2)/10-4");
mgr.validate(tLst0.get(0));
```
The resulting parse-tree looks as follows.


		* [ ROOT(S): validity:(true)]
		|--(tok_idx: 0 descIdx: 1 id:1)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		 |--(tok_idx: 0 descIdx: 5 id:2)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		  |--(tok_idx: 0 descIdx: 1 id:3)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		   |--(tok_idx: 0 descIdx: 1 id:4)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
		    |--(tok_idx: 0 descIdx: 1 id:4)[Tok: 1(REGEXP val:-6)(true)]
		 |--(tok_idx: 1 descIdx: 6 id:5)[AttackTag: operator(NON_TERMINAL val:-4)(true)]
		  |--(tok_idx: 1 descIdx: 1 id:6)[AttackTag: +(TERMINAL val:-5)(true)]
		   |--(tok_idx: 1 descIdx: 1 id:6)[Tok: +(TERMINAL val:-5)(true)]
		 |--(tok_idx: 2 descIdx: 7 id:7)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		  |--(tok_idx: 2 descIdx: 5 id:8)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		   |--(tok_idx: 2 descIdx: 1 id:9)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		    |--(tok_idx: 2 descIdx: 1 id:10)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
		     |--(tok_idx: 2 descIdx: 1 id:10)[Tok: 4(REGEXP val:-6)(true)]
		  |--(tok_idx: 3 descIdx: 6 id:11)[AttackTag: operator(NON_TERMINAL val:-4)(true)]
		   |--(tok_idx: 3 descIdx: 3 id:12)[AttackTag: *(TERMINAL val:-5)(true)]
		    |--(tok_idx: 3 descIdx: 3 id:12)[Tok: *(TERMINAL val:-5)(true)]
		  |--(tok_idx: 4 descIdx: 7 id:13)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		   |--(tok_idx: 4 descIdx: 5 id:14)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		    |--(tok_idx: 4 descIdx: 2 id:15)[AttackTag: ((TERMINAL val:-5)(true)]
		     |--(tok_idx: 4 descIdx: 2 id:15)[Tok: ((TERMINAL val:-5)(true)]
		    |--(tok_idx: 5 descIdx: 3 id:16)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		     |--(tok_idx: 5 descIdx: 5 id:17)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		      |--(tok_idx: 5 descIdx: 1 id:18)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		       |--(tok_idx: 5 descIdx: 1 id:19)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
			|--(tok_idx: 5 descIdx: 1 id:19)[Tok: 5(REGEXP val:-6)(true)]
		     |--(tok_idx: 6 descIdx: 6 id:20)[AttackTag: operator(NON_TERMINAL val:-4)(true)]
		      |--(tok_idx: 6 descIdx: 1 id:21)[AttackTag: +(TERMINAL val:-5)(true)]
		       |--(tok_idx: 6 descIdx: 1 id:21)[Tok: +(TERMINAL val:-5)(true)]
		     |--(tok_idx: 7 descIdx: 7 id:22)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		      |--(tok_idx: 7 descIdx: 1 id:23)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		       |--(tok_idx: 7 descIdx: 1 id:24)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
			|--(tok_idx: 7 descIdx: 1 id:24)[Tok: 2(REGEXP val:-6)(true)]
		    |--(tok_idx: 8 descIdx: 4 id:25)[AttackTag: )(TERMINAL val:-5)(true)]
		     |--(tok_idx: 8 descIdx: 4 id:25)[Tok: )(TERMINAL val:-5)(true)]
		   |--(tok_idx: 9 descIdx: 6 id:26)[AttackTag: operator(NON_TERMINAL val:-4)(true)]
		    |--(tok_idx: 9 descIdx: 4 id:27)[AttackTag: /(TERMINAL val:-5)(true)]
		     |--(tok_idx: 9 descIdx: 4 id:27)[Tok: /(TERMINAL val:-5)(true)]
		   |--(tok_idx: 10 descIdx: 7 id:28)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		    |--(tok_idx: 10 descIdx: 5 id:29)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		     |--(tok_idx: 10 descIdx: 1 id:30)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		      |--(tok_idx: 10 descIdx: 1 id:31)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
		       |--(tok_idx: 10 descIdx: 1 id:31)[Tok: 10(REGEXP val:-6)(true)]
		    |--(tok_idx: 11 descIdx: 6 id:32)[AttackTag: operator(NON_TERMINAL val:-4)(true)]
		     |--(tok_idx: 11 descIdx: 2 id:33)[AttackTag: -(TERMINAL val:-5)(true)]
		      |--(tok_idx: 11 descIdx: 2 id:33)[Tok: -(TERMINAL val:-5)(true)]
		    |--(tok_idx: 12 descIdx: 7 id:34)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		     |--(tok_idx: 12 descIdx: 1 id:35)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		      |--(tok_idx: 12 descIdx: 1 id:36)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
		       |--(tok_idx: 12 descIdx: 1 id:36)[Tok: 4(REGEXP val:-6)(true)]

		#Nodes: 37
		#NTs: 3
		NTs: [term, number, operator]
		NTUbound: [term, number, operator]
		Last NT: (tok_idx: 12 descIdx: 1 id:35)
		TokLst: [1][+][4][*][(][5][+][2][)][/][10][-][4]<$>
		Checkpoints: []
		Length: 14

		TokLstStr: 1 + 4 * ( 5 + 2 ) / 10 - 4

#### String generation

t.b.c

