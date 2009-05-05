/* MultiplePairsPartition.java Copyright 2006-2007 by J. Falkink-Pol
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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * DiagramPanel section containing multiple pair segments.
 * 
 * @author J. Falkink-Pol
 * 
 */
public abstract class MultiplePairsPartition extends MultipleThreadsPartition {

    private List<Partition> partitions = new java.util.Vector<Partition>();

    public List<Partition> getPartitions() {
        return partitions;
    }

    public Bounds<ThreadSegment> getBounds() {
        return super.getBounds();
    }

    /**
     * Searches for a <code>Cross</code> or <code>Twist</code> with the
     * coordinates within its bounds. Zooms in on diagram sections as long as
     * the specified position is within the bounds of the section.
     * 
     * 
     * @param x
     *            position along the x-axis, zero is left, minus is out of sight
     * @param y
     *            position along the y-axis, zero is top, minus is out of sight
     * 
     * @return null or a <code>Cross</code> or <code>Twist</code> with the
     *         coordinates within its bounds.
     * 
     * @see nl.BobbinWork.diagram.model.Connectors#getBounds
     */
    public MultipleThreadsPartition getSwitchAt(int x, int y) {
        List<Partition> v = getPartitions();
        for (int i = v.size() - 1; i >= 0; i--) {
            Partition p = v.get(i);
            if (p instanceof MultipleThreadsPartition) {
                MultipleThreadsPartition n = (MultipleThreadsPartition) p;
                if (n.getBounds().contains(x, y)) {
                    if (n instanceof Switch) {
                        return n;
                    } else if (n instanceof MultiplePairsPartition) {
                        return ((MultiplePairsPartition) n).getSwitchAt(x, y);
                    }
                }
            }
        }
        return null;
    }

    public ThreadSegment getThreadAt(int x, int y) {
        Switch s = (Switch) getSwitchAt(x, y);
        if (s != null) {
        	return s.getFront(); 
        } else {
            return null;
        }

    }

    /** Range of pairs of the parent used in this Group/Stitch. */
    private Range pairRange = null;

    Range getPairRange() {
        return pairRange;
    }

    void setPairRange(Range pairs) {
        this.pairRange = pairs;
    }

    /** Pairs going into and coming out of a Group/Stitch. */
    private Connectors<PairSegment> pairConnectors = null;

    Connectors<PairSegment> getPairConnectors() {
        return pairConnectors;
    }

    void setPairConnectors(Connectors<PairSegment> pairEnds) {
        this.pairConnectors = pairEnds;
    }

    MultiplePairsPartition() {}

	Iterator<Drawable> threadIterator () {
		if ( ! isVisible() ) return new Vector<Drawable>().iterator();
		return new It(partitions.iterator()){
			void nextSibling() {
				current = siblings.next().threadIterator();
			}
		};
	}

	Iterator<Drawable> pairIterator () {
		if ( ! isVisible() ) return new Vector<Drawable>().iterator();
		return new It(partitions.iterator()){
			void nextSibling() {
				current = siblings.next().pairIterator();
			}
		};
	}
	
	Iterator<Drawable> pinIterator () {
		if ( ! isVisible() ) return new Vector<Drawable>().iterator();
		return new It(partitions.iterator()){
			void nextSibling() {
				current = siblings.next().pinIterator();
			}
		};
	}
	
	private abstract static class It implements Iterator<Drawable>{
		
		Iterator<Partition> siblings;
		Iterator<Drawable> current = null;

		public It(Iterator<Partition> siblings) {
			this.siblings = siblings;
			if ( siblings.hasNext() ) {
				nextSibling(); 
			} else {
				current = new Vector<Drawable>().iterator();
			}
		}

		abstract void nextSibling();

		public boolean hasNext() {
			if ( ! current.hasNext() ) {
				if ( siblings.hasNext() ) {
					nextSibling();
				}
			}
			return current.hasNext();
		}
		
		public Drawable next() {
			return current.next();
		}
		
		public void remove() {
			throw new UnsupportedOperationException ();
		}
	}
}
