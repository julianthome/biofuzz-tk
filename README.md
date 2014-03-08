# biofuzz-tk

Jump to ...

[Introductory Example](#introductory-example)

[String Parsing](#parsing)

[Generation](#string-generation)

[Extension](#string-extension)

[Crossover](#crossover)

[Mutation](#mutation)

[Tracing](#tracing)

## What is biofuzz-tk

BioFuzz Toolkit helps you to parse strings and to perform operations (modification/extension) on the resulting parse trees. It is suited to programs that leverage genetic programming (GP) or similar techniques. The parsing and token-generation happens based on a context-free grammar description. Further, it supports the two well-known GP operations: Mutation and Crossover.

## How to use it

The following examples give you an intuition how the BioFuzz Toolkit can be used.

### Introductory Example

Suppose you want to parse the string *aaaaaa* matches the grammar definition
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

If you want to parse the string *aaaaaa*, you can use the following instruction:

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

### Example 2: Mathematical Expressions

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

BioFuzzMgr mgr = new BioFuzzMgr("cfg.xml", tokenizer);
List<BioFuzzParseTree> tLst0 = mgr.buildTrees("1+4*(5+2)/10-4");
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

If you want to generate a string from scratch you can do the following:

``` java
// The tokenizer is an optional parameter to the BioFuzzMgr since it is only
// used for parsing and not for string generation or extension
BioFuzzMgr myMgr = new BioFuzzMgr("src/main/resources/math.xml");
BioFuzzParseTree t = mgr.getNewParseTree();
while(!t.getVal()) {
	mgr.extend(t);
}
```

This will generate a random string that matches the CFG definition. The loop repeats until the parse-tree is complete according to the CFG. This might produce an expression like ``( 330 ) + ( ( 291107 ) - 1 + ( ( 4 / ( ( 4 ) ) - 2 - ( ( 04 / ( 5 ) + ( ( 4 ) ) + 9 - 5 / ( ( ( 3 ) ) ) ) ) + 40 ) / 23526 / 9902 ) * 7 ) - ( ( 8 ) )``


#### String extension

The BioFuzz toolkit has the capability of extending Strings on a context-free grammar. 

``` java
List<BioFuzzParseTree> tLst0 = mgr.buildTrees("1+4*(5+2)/10-4+");
mgr.validate(tLst0.get(0));
mgr.extend(tLst0.get(0));
```

The Java code above might yield the new expression ``1 + 4 * ( 5 + 2 ) / 10 - 4 + 6``. The extend function generates a single new terminal (and non-terminals associated with that particular termina) for each call. The BioFuzz toolkit scans the parse-tree for so called extension points. An extension point is a non-terminal where new terminal child nodes can be appended to. Another feature that you can see in the code above is partial parsing. The BioFuzz  toolkit has the capability of parsing strings that yield incomplete parse trees. These parse trees can be extended with the string extension capabilities.

#### Crossover

Crossover can be seen as the exchange of semantically equal subtrees between two individuals (represented as parse-trees). The Java code for performing Crossover looks as follows:

``` java
List<BioFuzzParseTree> tLst0 = mgr.buildTrees("1+4*(5+2)/10-4");
List<BioFuzzParseTree> tLst1 = mgr.buildTrees("5-2/(1+3)-4");
		
BioFuzzParseTree I_A = tLst0.get(0);
BioFuzzParseTree I_B = tLst1.get(0);
		
BioFuzzParseTree I_C = mgr.crossover(I_A, I_B);
```

In the example above we have two parent individuals *I_A* and *I_B*. The crossover-function yields a new parse-tree *I_C* .  The function scans both trees for semantically equivalent parse-trees. It takes a non-terminal *NT_B* from *I_B* and exchanges the non-terminal *NT_A* which is semantically equivalent to *NT_B* with *NT_B*.  After that call, you might get a new child individual *I_C* that looks as follows:

		* [ ROOT(S): validity:(true)]
		|--(tok_idx: 0 descIdx: 1 id:1)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		 |--(tok_idx: 0 descIdx: 5 id:2)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		  |--(tok_idx: 0 descIdx: 1 id:3)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		   |--(tok_idx: 0 descIdx: 1 id:4)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
		    |--(tok_idx: 0 descIdx: 1 id:4)[Tok: 1(REGEXP val:-6)(true)]
		 |--(tok_idx: 1 descIdx: 6 id:5)[AttackTag: operator(NON_TERMINAL val:-4)(true)]
		  |--(tok_idx: 1 descIdx: 1 id:6)[AttackTag: +(TERMINAL val:-5)(true)]
		   |--(tok_idx: 1 descIdx: 1 id:6)[Tok: +(TERMINAL val:-5)(true)]
		 |--(tok_idx: 2 descIdx: 7 id:14)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		  |--(tok_idx: 2 descIdx: 2 id:15)[AttackTag: ((TERMINAL val:-5)(true)]
		   |--(tok_idx: 2 descIdx: 2 id:15)[Tok: ((TERMINAL val:-5)(true)]
		  |--(tok_idx: 3 descIdx: 3 id:16)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		   |--(tok_idx: 3 descIdx: 5 id:17)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		    |--(tok_idx: 3 descIdx: 1 id:18)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		     |--(tok_idx: 3 descIdx: 1 id:19)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
		      |--(tok_idx: 3 descIdx: 1 id:19)[Tok: 1(REGEXP val:-6)(true)]
		   |--(tok_idx: 4 descIdx: 6 id:20)[AttackTag: operator(NON_TERMINAL val:-4)(true)]
		    |--(tok_idx: 4 descIdx: 1 id:21)[AttackTag: +(TERMINAL val:-5)(true)]
		     |--(tok_idx: 4 descIdx: 1 id:21)[Tok: +(TERMINAL val:-5)(true)]
		   |--(tok_idx: 5 descIdx: 7 id:22)[AttackTag: term(NON_TERMINAL val:-4)(true)]
		    |--(tok_idx: 5 descIdx: 1 id:23)[AttackTag: number(NON_TERMINAL val:-4)(true)]
		     |--(tok_idx: 5 descIdx: 1 id:24)[AttackTag: [0-9]{1,10}(REGEXP val:-6)(true)]
		      |--(tok_idx: 5 descIdx: 1 id:24)[Tok: 3(REGEXP val:-6)(true)]
		  |--(tok_idx: 6 descIdx: 4 id:25)[AttackTag: )(TERMINAL val:-5)(true)]
		   |--(tok_idx: 6 descIdx: 4 id:25)[Tok: )(TERMINAL val:-5)(true)]

		#Nodes: 19
		#NTs: 3
		NTs: [term, number, operator]
		NTUbound: [term, number, operator]
		Last NT: (tok_idx: 12 descIdx: 1 id:35)
		TokLst: [1][+][(][1][+][3][)]<$>
		Checkpoints: []
		Length: 8

		TokLstStr: 1 + ( 1 + 3 )
		Pfx Barrier: 0

#### Mutation

Mutation is an operation that changes the representation of a single terminal node. The modification of non-terminals (or a set of terminals that are subordinate to a non-terminal) is not yet implemented.  You can implement your own mutators. An example of a mutator implementation is given below:

``` java
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
```

The interface *BioFuzzMutator* has three methods that one has to implement. The *mutate* method takes a list of tokens and an index, that refers to the token in *lst* that is about to be mutated. The method *getName* just returns the name of the mutator and the method *matches* is a constraint on the token. A mutator is only called if this constraint is true.

A list of mutator can be passed as a parameter to the BioFuzz Manager.

``` java
List<BioFuzzMutator> lmut = new Vector<BioFuzzMutator>();
lmut.add(mut);
BioFuzzManager mgr = new BioFuzzMgr("src/main/resources/math.xml", tokenizer, mut);
```

This is due to the fact that users normally define multiple mutators. The toolkit automatically determines which mutators are applicable for a given token by looking at the constraint that is given by the *matches* function of the BioFuzzMutator interface, and randomly picks one of them. The following code will parse a mathematical expression, validate the resulting parse-tree, and mutate a token that is picked randomly from the range with the lower bound *0* (inclusively) and tree.getTokLstLen()-2 (which is the index of the last added terminal node) as the upper bound (inclusively). 


``` java
List<BioFuzzParseTree> tLst0 = mgr.buildTrees("1+4*(5+2)/10-4");
BioFuzzParseTree tree = tLst0.get(0);
mgr.validate(tree);
mgr.mutate(tree,0,tree.getTokLstLen()-2);
```

Given the BioFuzzMutator implementation above, the expression ``1+4*(5+2)/10-4`` might be mutated into ``6+4*(5+2)/10-4``

#### Tracing

The BioFuzz Toolkit incorporates a tracer that can be used in order to search for elements within a parse tree easily. The first thing
to do, is to define an object that implements the *BioFuzzQuery* interface:


``` java
BioFuzzQuery q = new BioFuzzQuery() {
@Override
	public Boolean condition(BioFuzzParseNode node) {
		if(node != null && node.getParent() != null) {
			if ( node.getAtagType() == TagType.TERMINAL && node.getParent().getAtagName().equals("operator")) {
				return true;
			}
		}
		return false;
};
List<BioFuzzParseNode> nodesBfs = mgr.trace(tree, q, TraceType.BFS);
```

The tracer traverses each node of the parse-tree and whenever the return value of  the member-function *condition* is true,
the corresponding node will be added to the return set of nodes. The BioFuzz Manager contains a member function *trace* 
that calls the tracer with the parse tree to search, the query and the algorithm to use. For now, the two methodes breadth-first
search (BFS) and depth-first search are implemented. Dependent on the algorithm, the nodes are added in-order to the resulting
list. Given the code above, the list *nodeBfs* contains the following elements (in the given order): ``[+][*][+][/][-]``.



