/**
 * Project: Xtream
 * Module:
 * Task:
 * Last Modify:
 * Created:
 * Developer: Mohammad Ghalambor Dezfuli (mghalambor@iust.ac.ir & @ gmail.com)
 *
 * LICENSE:
 *    
 * This file is part of the Xtream project.
 *
 * Xtream is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xtream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xtream.  If not, see <http://www.gnu.org/licenses/>.
 */
package xtream.core.loadshedding;

import java.util.PriorityQueue;
import xtream.interfaces.ITuple;

/**
 * Node to create load shedding capable multi queues of tuples
 * 
 * @author ghalambor
 * 
 */
public class LSBSTNode implements Comparable<LSBSTNode> {

	// -------------------------------------------------
	// NOTE: Do Not Use value of Leaf Nodes!!!
	// -------------------------------------------------

	public LSBSTNode leftChild, rightChild;
	public double key; // confidence value as BST key
	public ITuple value; // value is a tuple, only for non-leaves
	public double leftBound, rightBound; // interval
	public boolean isLeaf;
	public PriorityQueue<ITuple> buffer; // only for leaves
	public long childrenCount; // tuples in children
	public long totalDelay; // total delay of all operations
	protected LSBSTStructure tree; // ref to related tree

	public LSBSTNode(double leftBound, double rightBound, boolean isLeaf,
			LSBSTStructure tree) {
		this.tree = tree;
		this.key = (leftBound + rightBound) / 2;
		this.value = null;
		this.leftBound = leftBound;
		this.rightBound = rightBound;
		this.isLeaf = isLeaf;
		this.childrenCount = 0;
		if (isLeaf)
			tree.getLeaves().add(this);
	}

	/**
	 * Insert to tree
	 * 
	 * @param newTpl
	 *            new tuple to insert
	 */
	public void add(ITuple newTpl) {
		long startTime = System.currentTimeMillis();
		double newConf = newTpl.GetConf();
		if (newConf >= leftBound && newConf <= rightBound) {
			if (isLeaf) {
				buffer.add(newTpl);
			} else if (value == null) {
				value = newTpl;
			} else {
				if (value.compareTo(newTpl) > 0) { // if value should be
													// replaced by new tuple
					ITuple oldValue = value;
					value = newTpl;
					if (oldValue.GetConf() < key)
						leftChild.add(oldValue);
					else
						rightChild.add(oldValue);
				} else {
					if (newConf < key) {
						leftChild.add(newTpl);
					} else {
						rightChild.add(newTpl);
					}
				}
			}
			childrenCount++;
		}
		totalDelay += System.currentTimeMillis() - startTime;
	}

	/**
	 * @return Retrieves, but does not remove, the head of this queue, or
	 *         returns null if this queue is empty.
	 */
	public ITuple peek() {
		if (isLeaf)
			return buffer.peek();
		else
			return value;
	}

	/**
	 * @return Retrieves and removes the head of this queue, or returns null if
	 *         this queue is empty.
	 */
	public ITuple poll() {
		long startTime = System.currentTimeMillis();
		if (isLeaf) {
			childrenCount--;
			totalDelay += System.currentTimeMillis() - startTime;
			return (buffer.poll());
		} else if (value != null) {
			ITuple oldValue = value;
			if (leftChild.peek() != null && rightChild.peek() == null) {
				value = leftChild.poll();
			} else if (leftChild.peek() == null && rightChild.peek() != null) {
				value = rightChild.poll();
			} else if (leftChild.peek() != null && rightChild.peek() != null) { // none
																				// of
																				// the
																				// children
																				// is
																				// null
				switch (leftChild.peek().compareTo(rightChild.peek())) {
				case -1: // left < right
				case 0: // left == right
					value = leftChild.poll();
					break;
				case 1: // left > right
					value = rightChild.poll();
				}
			} else { // both children are null
				value = null;
			}
			childrenCount--;
			totalDelay += System.currentTimeMillis() - startTime;
			return oldValue;
		} else {
			return null;
		}
	}

	/**
	 * To clear tuples of a subtree
	 */
	public void clear() {
		if (isLeaf) {
			buffer = new PriorityQueue<ITuple>();
		} else {
			leftChild.clear();
			rightChild.clear();
		}
		childrenCount = 0;
		totalDelay = 0;
		value = null;
	}

	@Override
	public int compareTo(LSBSTNode arg0) {
		if (key < arg0.key)
			return -1;
		else if (key > arg0.key)
			return 1;
		else
			return 0;
	}

	/**
	 * @return size of buffers of leaf nodes (byte), 0 for non-leaf nodes
	 */
	public long getSize() {
		if (!isLeaf)
			return 0;
		else {
			long bufSize = buffer.size();
			if (bufSize > 0) {
				return bufSize * buffer.peek().GetSize();
			} else
				return 0;
		}
	}

	public void Refresh() {
		if (!isLeaf) {
			rightChild.Refresh();
			leftChild.Refresh();
			this.childrenCount = rightChild.childrenCount
					+ leftChild.childrenCount + ((value != null) ? 1 : 0);
			this.totalDelay = rightChild.totalDelay + leftChild.totalDelay;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = new String("\n(LSBSTNode: "
				+ ((isLeaf) ? "Leaf" : "Not-Leaf") + " Interval: (" + leftBound
				+ "," + rightBound + ") Value:"
				+ ((value == null) ? "NULL" : value) + " ChildCount: "
				+ childrenCount + " Delay: " + totalDelay + " BufCount: "
				+ ((buffer == null) ? 0 : buffer.size()) + ")");
		if (isLeaf) {
			result += "\nBUFFER:\n";
			for (ITuple it : buffer) {
				result += "\n" + it;
			}
		} else {
			result += "\nLEFT CHILD:\n";
			result += leftChild;
			result += "\nRIGHT CHILD:\n";
			result += rightChild;

		}
		return result;
	}

}
