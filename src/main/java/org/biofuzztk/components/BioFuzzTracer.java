/** 
 *  The BioFuzz Toolkit for input parsing/generation/modification of
 *  structured input.
 *  
 *  Copyright (C) 2014 Julian Thome (frostisch@yahoo.de)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.biofuzztk.components;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.biofuzztk.ptree.BioFuzzParseNode;
import org.biofuzztk.ptree.BioFuzzParseTree;

public class BioFuzzTracer {

	public enum TraceType {
		DFS("Depth-First-Search"), 
		BFS("Breadth-First-Search");

		private String desc;
		TraceType(String desc) {
			this.desc = desc;
		}

		@Override
		public String toString() {
			return this.desc ;
		}
	};


	final static Logger logger = LoggerFactory.getLogger(BioFuzzTracer.class);

	public interface BioFuzzQuery {
		public Boolean condition(BioFuzzParseNode node);
	}

	public BioFuzzTracer() {

	}

	public List<BioFuzzParseNode> doTraceSubNodes(BioFuzzParseNode node, BioFuzzQuery q, TraceType type) {


		List<BioFuzzParseNode> ret = new Vector<BioFuzzParseNode>();

		if(!node.hasChildren())
			return null;

		for(BioFuzzParseNode child: node.getChildren()) {
			if(type == TraceType.BFS) {
				//logger.debug(" " + node.getAtagType());
				// add first element just for bfs
				if(q.condition(child)){
					//logger.debug("aldd");
					ret.add(child);
				}
				
				traceBFS(child, q, ret);
			} else {
				traceDFS(child, q, ret);
			}
		}

		if(ret.size() > 0) {
			return ret;
		}

		return null;
	}


	public List<BioFuzzParseNode> doTrace(BioFuzzParseTree tree, BioFuzzQuery q, TraceType type) {


		List<BioFuzzParseNode> ret = new Vector<BioFuzzParseNode>();

		assert(tree != null);
		assert(q != null);

		if(type == TraceType.BFS) {
			
			// add first element just for bfs
			if(q.condition(tree.getRootNode())){
				ret.add(tree.getRootNode());
			}
			
			traceBFS(tree.getRootNode(), q, ret);
		} else {
			traceDFS(tree.getRootNode(), q, ret);
		}

		if(ret.size() > 0)
			return ret;

		return null;
	}

	private void traceDFS(BioFuzzParseNode node,BioFuzzQuery q,List<BioFuzzParseNode> l) {

		if(node == null)
			return;
		
		if(q.condition(node)){
			l.add(node);
		}
		
		//logger.debug("(DFS):" + node.getAtagName());

		if(node.hasChildren()) {
			for(BioFuzzParseNode child: node.getChildren()) {
				traceDFS(child, q, l);
			}
		}

	}


	private void traceBFS(BioFuzzParseNode node,BioFuzzQuery q,List<BioFuzzParseNode> l) {
		
		Queue<BioFuzzParseNode> queue = new LinkedList<BioFuzzParseNode>();
		
		if(node == null)
			return;

		//logger.debug("(BFS):" + node.getAtagName() + " " + node.getAtagType());

		if(node.hasChildren()) {
			for(BioFuzzParseNode child: node.getChildren()) {
				queue.offer(child);
				
				if(q.condition(child)){
					l.add(child);
				}
			}
		}

		while(!queue.isEmpty()) {
			traceBFS(queue.poll(),q,l);
		}
	
	}


}
