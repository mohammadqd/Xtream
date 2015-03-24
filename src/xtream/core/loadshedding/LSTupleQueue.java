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

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import xtream.Globals;
import xtream.Globals.Monitoring_Modes;
import xtream.interfaces.IInPort;
import xtream.interfaces.ILSStore;
import xtream.interfaces.IOperator;
import xtream.interfaces.IQuery;
import xtream.interfaces.IQueue;
import xtream.interfaces.ITuple;

/**
 * Tuple Priority Queue + Load Shedding Support
 * 
 * @author ghalambor
 * 
 */
public class LSTupleQueue implements IQueue, ILSStore, IOperator, IInPort {

	protected LSBSTStructure buffer;
	protected boolean isRootOP;
	protected IQuery parentQuery;
	protected Vector<IInPort> inPorts;


	/**
	 * 
	 */
	public LSTupleQueue(IQuery parentQuery) {
		buffer = new LSBSTStructure(Globals.PER_OPERATOR_LS_OFFERS_COUNT);
		this.parentQuery = parentQuery;
		isRootOP = false;
		inPorts = new Vector<IInPort>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#Open()
	 */
	@Override
	public void Open() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#Close()
	 */
	@Override
	public void Close() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#hasTuple()
	 */
	@Override
	public synchronized boolean hasTuple() {
		return (buffer.size() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#nextTuple()
	 */
	@Override
	public synchronized ITuple nextTuple() throws IOException {
		return buffer.pollValidTuple();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#PutTuple(xtream.interfaces.ITuple, int)
	 */
	@Override
	public synchronized void PutTuple(ITuple tp, int i) throws IOException {
		buffer.add(tp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#isUnary()
	 */
	@Override
	public boolean isUnary() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQueue#GetTuple()
	 */
	@Override
	public synchronized ITuple GetTuple() {
		return buffer.pollValidTuple();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQueue#TopTuple()
	 */
	@Override
	public synchronized ITuple TopTuple() {
		return buffer.peek();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQueue#GetSize()
	 */
	@Override
	public synchronized long GetSize() {
		ITuple top = buffer.peek();
		if (top == null)
			return 0;
		else
			return buffer.size() * top.GetSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQueue#GetCount()
	 */
	@Override
	public long GetCount() {
		return buffer.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQueue#GetCapacity()
	 */
	@Override
	public long GetCapacity() {
		return -1; // unlimited
	}

	@Override
	public synchronized void LSCommand(LSOffer offer) {
		buffer.LSCommand(offer);
	}

	@Override
	public double SetPT(double newPT) {
//		if (Globals.MONITORING_MODE == Monitoring_Modes.Full) // DEBUG
//			System.out.format("\nMON_MSG: Set PT of LSQueue to %f\n", newPT);
		return buffer.SetPT(newPT);
	}

	@Override
	public double GetPT() {
		return buffer.GetPT();
	}

	@Override
	public void run(long ts) {
		assert false : "Wrong Method Call!";
	}

	@Override
	public void SetAsRootOP() {
		isRootOP = true;
	}

	@Override
	public boolean isRootOP() {
		return isRootOP;
	}

	@Override
	public IQuery GetQuery() {
		return parentQuery;
	}
	
	/**
	 * To add an InPort without index (Only for unary operators)
	 * 
	 * @param ip
	 *            in port to add
	 * @return ref to added in port
	 */
	public IInPort AddInPort(IInPort ip) {
		inPorts.add(ip);
		return (ip);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILSStore#getLSOffers(double[])
	 */
	@Override
	public LSOffer[] getLSOffers(double[] newPTs) {
		Arrays.sort(newPTs);
		assert (newPTs[newPTs.length - 1] <= 1d && newPTs[0] >= 0d) : "Wrong Input!";
		LSOffer[] offers = buffer.getLSOffers(newPTs);
		
		// Augment current offers with offers from children operators
		LSOffer[] childrenOffers;
		for (IInPort in:inPorts) {
			if (in instanceof ILSStore) {
				childrenOffers = ((ILSStore)(in)).getLSOffers(newPTs);
				for (int i = 0; i < offers.length; i++) {
					offers[i].AugmentWith(childrenOffers[i]);
				}

			}
		}
		return offers;
	}
}
