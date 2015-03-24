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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;

import xtream.Globals;
import xtream.interfaces.ILSStore;
import xtream.interfaces.ITuple;

/**
 * This is a Load Shedding Capable Priority Queue This structure gurantees
 * output order (Ascending order of tuples based on their compareTo
 * implementation)
 * 
 * @author ghalambor
 * 
 */
public class LSBSTStructure implements ILSStore {

	protected LSBSTNode root;
	protected long totalOperations; // number of all done operations (add,poll)
	protected LinkedList<LSBSTNode> leaves; // leaf nodes
	protected double currentPT; // probability-threshold for results

	/**
	 * 
	 */
	public LSBSTStructure(long leavesCount) {
		leaves = new LinkedList<LSBSTNode>();
		root = CreateLSBST(leavesCount);
		totalOperations = 0;

	}

	protected void RecursiveLSBSTCreate(long levelsRemained, LSBSTNode parent) {
		if (levelsRemained > 0) {
			// Left Child
			parent.leftChild = new LSBSTNode(parent.leftBound, parent.key,
					false, this);
			RecursiveLSBSTCreate(levelsRemained - 1, parent.leftChild);
			// Right Child
			parent.rightChild = new LSBSTNode(parent.key, parent.rightBound,
					false, this);
			RecursiveLSBSTCreate(levelsRemained - 1, parent.rightChild);
		} else {
			// Left Child
			parent.leftChild = new LSBSTNode(parent.leftBound, parent.key,
					true, this);
			parent.leftChild.buffer = new PriorityQueue<ITuple>();

			// Right Child
			parent.rightChild = new LSBSTNode(parent.key, parent.rightBound,
					true, this);
			parent.rightChild.buffer = new PriorityQueue<ITuple>();
		}
	}

	/**
	 * @param leavesCount
	 *            number of buffers (leaves)
	 * @return root of the created tree
	 */
	public LSBSTNode CreateLSBST(long leavesCount) {
		if (leavesCount <= 0)
			return null;
		else if (leavesCount == 1) {
			LSBSTNode root = new LSBSTNode(0, 1, true, this);
			root.buffer = new PriorityQueue<ITuple>();
			return root;
		} else { // leavesCount > 1
			long levelsCount = Math
					.round((Math.log((double) leavesCount) / Math.log(2)));
			LSBSTNode root = new LSBSTNode(0, 1, false, this);
			RecursiveLSBSTCreate(levelsCount - 1, root);
			return root;
		}
	}

	/**
	 * Insert to tree
	 * 
	 * @param newTpl
	 *            new tuple to insert
	 */
	public void add(ITuple newTpl) {
		assert root != null;
		if (root != null && (!Globals.ADAPTIVE_FLS || newTpl.GetConf() >= GetPT())) {
			totalOperations++;
			root.add(newTpl);
		}
	}

	/**
	 * @return Retrieves, but does not remove, the head of this queue, or
	 *         returns null if this queue is empty.
	 */
	public ITuple peek() {
		assert root != null;
		if (root != null)
			return root.peek();
		else
			return null;
	}

	/**
	 * @return Retrieves and removes the head of this queue, or returns null if
	 *         this queue is empty.
	 */
	public ITuple poll() {
		assert root != null;
		if (root != null) {
			totalOperations++;
			return root.poll();
		} else
			return null;
	}

	/**
	 * @return Retrieves and removes the head of this queue (the first
	 *         non-expired head with high enough confidence), or returns null if
	 *         this queue is empty.
	 */
	public ITuple pollValidTuple() {
		ITuple nextTpl = poll();
		long curSysTime = Globals.core.GetSysCurTime();
		while (nextTpl != null
				&& (nextTpl.GetTimestamp()[1] < curSysTime || (Globals.ADAPTIVE_FLS && nextTpl.GetConf() < GetPT())))
			nextTpl = poll();
		return nextTpl;
	}

	/**
	 * To clear tuples of a subtree
	 */
	public void clear() {
		assert root != null;
		if (root != null)
			root.clear();
	}

	/**
	 * @return the root
	 */
	public synchronized LSBSTNode getRoot() {
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILSCStore#LSCommand(xtream.structures.LSOffer)
	 */
	public void LSCommand(LSOffer offer) {
		SetPT(offer.newPT);
	}

	/**
	 * @return the leaves
	 */
	public synchronized LinkedList<LSBSTNode> getLeaves() {
		return leaves;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return root.toString();
	}

	public double GetPT() {
		return currentPT;
	}

	public double SetPT(double newPT) {
		Collections.sort(leaves);
		int index = 0;
		while (leaves.get(index).rightBound < newPT && index < leaves.size()) {
			leaves.get(index).clear();
			index++;
		}
		root.Refresh(); // refresh nodes
		currentPT = newPT;
		return currentPT;
	}

	/**
	 * @return number of tuples in this structure
	 */
	public long size() {
		if (root == null)
			return 0;
		else
			return root.childrenCount;
	}

	@Override
	public LSOffer[] getLSOffers(double[] newPTs) {
		Arrays.sort(newPTs);
		assert (newPTs[newPTs.length - 1] <= 1d && newPTs[0] >= 0d) : "Wrong Input!";
		Collections.sort(leaves);
		LSOffer[] offers = new LSOffer[newPTs.length];
		double oldRT = (double) root.totalDelay / ((totalOperations>0)?totalOperations:1);
		double delay = (double) root.totalDelay;
		long memRelease = 0;
		int i = 0; // leaf index
		int j = 0; // offer index
		for (double pt : newPTs) {
			while (i < leaves.size() && leaves.get(i).rightBound <= pt) {
				memRelease += leaves.get(i).getSize();
				delay -= leaves.get(i).totalDelay;
				i++;
			}
			offers[j] = new LSOffer(null, pt,
					oldRT - (delay) / ((totalOperations>0)?totalOperations:1), memRelease);
			j++;
		}
		return offers;
	}

}
