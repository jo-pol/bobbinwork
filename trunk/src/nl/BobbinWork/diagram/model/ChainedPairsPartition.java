/* Group.java Copyright 2006-2007 by J. Falkink-Pol
 *
 * This file is part of BobbinWork.
 *
 * BobbinWork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BobbinWork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BobbinWork.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.BobbinWork.diagram.model;

import java.util.Vector;

import nl.BobbinWork.diagram.xml.ElementType;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author J. Falkink-Pol
 */
abstract class ChainedPairsPartition extends MultiplePairsPartition {

    /**
     * Adds a new child to the list and connects the thread/pair Ends's.
     * 
     * @param element
     *            a group or stitch that should become part of the group/pattern
     */
    void addChild(MultiplePairsPartition child) {
        getPartitions().add(child);
        connectChild(child);
    }

    /**
     * Creates a new (section of the) tree of Partition's.
     * 
     * @param element
     *            XML element &lt;pattern&gt; or &lt;group&gt;
     * @param range TODO
     */
    ChainedPairsPartition(org.w3c.dom.Element element, Range range) {
        
    	super(element);
        
    	setPairRange(range);
        initEnds();
        
        for //
        (Node child = element.getFirstChild() //
        ; child != null //
        ; child = child.getNextSibling()) //
        {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                ElementType childType = ElementType.valueOf(child.getNodeName());
                Element childElement = (Element) child;
				if (childType == ElementType.group) {
                    addChild(new Group(childElement));
                } else if (childType == ElementType.stitch) {
                    addChild(Builder.createStitch(childElement));
                } else if (childType == ElementType.pin) {
                    getPartitions().add(Builder.createPin(childElement));
                }
            }
        }
    }

    abstract void initEnds();
    abstract void connectChild(MultiplePairsPartition child);

    public ChainedPairsPartition(//
    		Range range, //
    		Vector<MultiplePairsPartition> newParts, //
			Vector<Pin> pins) {
    	
    	setPairRange(range);
    	initEnds();
    	
    	Vector<Partition> parts = getPartitions();
    	for (Pin pin:pins) parts.add(pin);
    	for (MultiplePairsPartition newPart:newParts) {
    		parts.add(newPart);
    		connectChild(newPart); 
    	}
	}
}
