/**
 * Project: Xtream
 * Module: Queue
 * Task: Queue for ITuples
 * Last Modify: May 15,2013
 * Created: 2007
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
package xtream.structures;

import java.util.*;

import xtream.Globals;
import xtream.interfaces.IQueue;
import xtream.interfaces.ITuple;

/**
 * A simple implementation for IQueue NOTE: This structure does not care about
 * probability threshold (only simple set/get)
 * 
 * @author ghalambor
 * 
 */
public class TupleQueue implements IQueue {
	protected int tupleSize; // size of tuples in this queue (byte)
	protected int maxBufferSize; // max number of tuples in queue, 0:unlimited
	protected int allocatedMem; // amount of memory that has been allocated to
								// Queue (in byte)
	protected Vector<ITuple> buffer; // buffer of tuples
	protected double currentPT;

	/**
	 * Constructor
	 * 
	 * @param tsize
	 *            tuple size
	 * @param initBufSize
	 *            initial size of queue
	 * @param maxBufSize
	 *            max queue size (number of tuples) 0:unlimited
	 */
	public TupleQueue(int tsize, int initBufSize, int maxBufSize) {
		tupleSize = tsize;
		allocatedMem = initBufSize * tsize;
		maxBufferSize = maxBufSize;
		buffer = new Vector<ITuple>(initBufSize);
	} // constructor

	/***************************
	 * GetTuple target: to return a tuple from head of queue and remove it from
	 * buffer
	 * 
	 * @return Tuple (should be casted to proper tuple type)
	 */
	public synchronized ITuple GetTuple() {
		if (GetCount() > 0) {
			ITuple tp = (ITuple) buffer.remove(0);
			return tp;
		} else { // if buf is not empty
			return null;
		}
	} // GetTuple

	/***************************
	 * TopTuple target: to return a tuple from head of queue without remove it
	 * from buffer
	 * 
	 * @return Tuple (should be casted to proper tuple type)
	 */
	public synchronized ITuple TopTuple() {
		if (GetCount() > 0) {
			ITuple tp = (ITuple) buffer.firstElement();
			return tp;
		} else { // if buf is not empty
			return null;
		}
	} // TopTuple

	/***************************
	 * PutTuple target: to add a new tuple at the end of buffer
	 * 
	 * @param i
	 *            DO NOT CARE
	 * @param tp
	 *            tuple to add at the end of buffer
	 * @return number of added tuples
	 * @throws RuntimeException
	 *             if no capacity
	 */
	public synchronized void PutTuple(ITuple tp, int i) throws RuntimeException {
		if (maxBufferSize == 0 || buffer.size() < maxBufferSize)
			buffer.add(tp);
		else
			throw new RuntimeException(
					"ERROR: Queue is full! Can not PutTuple!");

	} // PutTuple

	/***************************
	 * PutTuple target: to add a new tuple at the end of buffer
	 * 
	 * @param tp
	 *            tuple to add at the end of buffer
	 * @return number of added tuples
	 * @throws RuntimeException
	 *             if no capacity
	 */
	public synchronized void PutTuple(ITuple tp) throws RuntimeException {
		PutTuple(tp, 0);
	} // PutTuple

	/*****************************
	 * GetSize
	 * 
	 * @return size of queue (bytes)
	 */
	public synchronized long GetSize() {
		if (tupleSize > 0)
			return (GetCount() * tupleSize);
		else {
			int sumSize = 0;
			for (ITuple tpl : buffer)
				sumSize += tpl.GetSize();
			return sumSize;
		}
	} // GetSize

	/*****************************
	 * Get Count
	 * 
	 * @return num of tuples in buffer
	 */
	public synchronized long GetCount() {
		return buffer.size();
	} // GetCount

	/*****************************
	 * Get Capacity
	 * 
	 * @return max num of tuples possible in buffer, 0:unlimited
	 */
	public synchronized long GetCapacity() {
		return maxBufferSize;
	} // GetCapacity

	@Override
	public void Open() {
	}

	@Override
	public void Close() {
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public boolean hasTuple() {
		return (GetCount() > 0);
	}

	@Override
	public ITuple nextTuple() {
		return GetTuple();
	}

	@Override
	public boolean isUnary() {
		return true;
	}

	@Override
	public double SetPT(double newPT) {
		currentPT = newPT;
		RemoveInvalidTuples();
		return currentPT;
	}

	@Override
	public double GetPT() {
		return currentPT;
	}

	/**
	 * To remove invalid tuples specially after increasing probability-threshold (SetPT)
	 */
	protected void RemoveInvalidTuples() {
		long curTime = Globals.core.GetSysCurTime();
		int i = 0;
		while (i < buffer.size()) {
			if (buffer.get(i).GetConf() < currentPT
					|| buffer.get(i).GetTimestamp()[1] < curTime)
				buffer.remove(i);
			else
				i++;
		}
	}
}
