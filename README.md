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
