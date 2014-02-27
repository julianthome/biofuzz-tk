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

package org.biofuzztk.cfg;

import org.w3c.dom.Node;

/**
 * 
 * A tag is assigned to each node of a CFG-graph. This tag
 * stores meta information about the tag itself. This is also referenced
 * in the parse tree. It maps the XML node of the configuration to
 * the corresponding node in the CFG-graph and to a node of the resulting
 * parse tree.
 * 
 * @author julian
 *
 */
public class BioFuzzAttackTag {
	
	/**
	 * 
	 * Different node types.
	 * 
	 * @author julian
	 *
	 */
	public enum TagType {
		/**
		 * a root node (of a parse tree).
		 */
		ROOT("ROOT",-1),
		
		/**
		 * start meta node of a production rule.
		 */
		START("START",-2), 
		
		/**
		 * stop meta node of a production rule.
		 */
		STOP("STOP",-3), 
		
		/**
		 * non-terminal node.
		 */
		NON_TERMINAL("NON_TERMINAL",-4), 
		
		/**
		 * terminal node.
		 */
		TERMINAL("TERMINAL",-5), 
		
		
		/**
		 * regular expression node.
		 */
		REGEXP("REGEXP",-6),
		
		/**
		 * meta tag for general parsing error.
		 */
		ERROR("ERROR", -7),
		
		/**
		 * meta tag for general parsing error - invalid non-terminal.
		 */
		NTERROR("NTERROR", -8),
		
		/**
		 * meta tag for general parsing error - invalid terminal.
		 */
		TERROR("TERROR", -9),
		
		/**
		 * meta tag for rolling back stack of the pushdown automaton.
		 */
		ROLLBACK("ROLLBACK", -10),

		/**
		 * the same like Terminal but only visible for the token generator (not for the parser).
		 */
		TOK_TERMINAL("TOK_TERMINAL",-5);
		
		private String desc;
		private int value;
		TagType(String desc, int value) {
			this.desc = desc;
			this.value = value;
		}
		
		public int getValue() {
			return this.value;
		}
		
		@Override
		public String toString() {
			return this.desc + " val:" + this.value;
		}
	};
	
	private String name;
	private TagType tagType;
	private int coord;
	private Node node;
	

	public BioFuzzAttackTag(Node node,String name, TagType tagType, int coord) {
		this.name = name;
		this.tagType = tagType;
		this.coord = coord;
		this.node = node;
	}
	
	public BioFuzzAttackTag(Node node,String name, TagType tagType) {
		this.name = name;
		this.tagType = tagType;
		this.coord = 0;
		this.node = node;
	}	
	
	public BioFuzzAttackTag() {
		this.name = "ROOT";
		this.tagType = TagType.ROOT;
		this.coord = 0;
		this.node = null;
	}	
	
	public BioFuzzAttackTag(BioFuzzAttackTag atag) {
		this.name = atag.name;
		this.tagType = atag.tagType;
		this.coord = atag.coord;
		this.node = atag.node;
	}
	
	/**
	 * 
	 * Returns the corresponding XML node.
	 * 
	 * @return XML node.
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 
	 * Set the corresponding XML node.
	 * 
	 * @return XML node.
	 */
	public void setNode(Node node) {
		this.node = node;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TagType getTagType() {
		return tagType;
	}

	public void setTagType(TagType tagType) {
		this.tagType = tagType;
	}

	public int getCoord() {
		return coord;
	}

	public void setCoord(int coord) {
		this.coord = coord;
	}
	
	@Override
	public String toString() {
		return "[" + this.name + "]";
	}
	

}
