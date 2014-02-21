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

import java.util.List;
import java.util.Random;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.automaton.RegExp;

public class BioFuzzRexpGen {

    private final Automaton sm;
    private final Random rgen;

    public BioFuzzRexpGen(String regex) {
        this.sm = new RegExp(regex).toAutomaton();
        this.rgen = new Random();
    }

    public String doGenerate() {
        StringBuilder builder = new StringBuilder();
        generate(builder, sm.getInitialState());
        return builder.toString();
    }

    private void generate(StringBuilder sb, State s) {
    	
        List<Transition> transitions = s.getSortedTransitions(true);
        
        if (transitions.size() == 0) {
            assert s.isAccept();
            return;
        }
        
        int choices = s.isAccept() ? transitions.size() : transitions.size() - 1;
        int choice = getRandomInt(0, choices, rgen);
        
        
        if (s.isAccept() && choice == 0)
            return;
        
        Transition t = transitions.get(choice - (s.isAccept() ? 1 : 0));
        
        sb.append((char) getRandomInt(t.getMin(), t.getMax(), rgen));
        
        generate(sb, t.getDest());
    }

    
    public final static int getRandomInt(int min, int max, Random random) {
        return (min + Math.round(random.nextFloat() * (max - min)));
    }

}
