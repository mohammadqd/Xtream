/**
 * Project: Xtream
 * Module: Abstract Operator
 * Task: base for other operators
 * Last Modify: 2013
 * Created: 2003
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
package xtream.query;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import xtream.Globals;
import xtream.Globals.Monitoring_Modes;
import xtream.core.loadshedding.ILSStore;
import xtream.core.loadshedding.LSOffer;
import xtream.core.loadshedding.LSResponseTimeContainer;
import xtream.io.IInPort;
import xtream.io.IOutPort;
import xtream.structures.ITuple;

/**
 * base for other operators
 * 
 */
public abstract class AOperator implements IOperator, ILSStore {

	protected boolean isRootOP;
	protected double currentPT; // probability-threshold for results
	/**
	 * unique name for operator
	 */
	public final String opName;
	protected IQuery parentQuery; // query which has this operator
	protected LSResponseTimeContainer rt; // to maintain response-time for PT
											// buckets (must be updated by
											// implementing classes)

	public class OutChannel {
		public IOutPort outPort;
		public int index; // input index for out port
	}

	protected Vector<OutChannel> outChannels;
	protected Vector<IInPort> inPorts;
	protected boolean isOpen; // because operators are also outports

	/**
	 * @param opName
	 *            operator unique name
	 * @param parentQuery
	 *            query which contains this operator
	 */
	public AOperator(String opName, IQuery parentQuery) {
		outChannels = new Vector<OutChannel>();
		inPorts = new Vector<IInPort>();
		isOpen = false;
		isRootOP = false;
		currentPT = 0;
		this.opName = opName;
		this.parentQuery = parentQuery;
		rt = new LSResponseTimeContainer(Globals.PER_OPERATOR_LS_OFFERS_COUNT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#Open()
	 */
	@Override
	public void Open() {
		// for (IInPort ip : inPorts) {
		// ip.Open();
		// }
		isOpen = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#Close()
	 */
	@Override
	public void Close() {
		// for (IInPort ip : inPorts) {
		// ip.Close();
		// }
		isOpen = false;
	}

	/**
	 * @param op
	 *            out port to add
	 * @param index
	 *            input index for outport (no need to be unique, will be used
	 *            when putting tuples into outports (index parameter of PutTuple
	 *            method))
	 * @return ref to added out port
	 */
	public IOutPort AddOutPort(IOutPort op, int index) {
		OutChannel ch = new OutChannel();
		ch.outPort = op;
		ch.index = index;
		outChannels.add(ch);
		return (outChannels.get(outChannels.indexOf(ch)).outPort);
	}

	/**
	 * Only use this when op is a unary operator (index is 0)
	 * 
	 * @param op
	 *            out port to add
	 * @return ref to added out port
	 */
	public IOutPort AddOutPort(IOutPort op) {
		assert (op.isUnary()) : "You should not use this method for non-unary operators/ports";
		return (AddOutPort(op, 0));
	}

	/**
	 * @param ip
	 *            in port to add
	 * @param i
	 *            index of in port (should be unique in operator)
	 * @return ref to added in port
	 */
	public IInPort AddInPort(IInPort ip, int i) {
		inPorts.add(i, ip);
		return (inPorts.get(inPorts.indexOf(ip)));
	}

	/**
	 * To add an InPort without index (Only for unary operators)
	 * 
	 * @param ip
	 *            in port to add
	 * @return ref to added in port
	 */
	public IInPort AddInPort(IInPort ip) {
		assert (this.isUnary()) : "Only use this method for Unary operators/ports";
		inPorts.add(ip);
		return (ip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#PutTuple(xtream.interfaces.ITuple)
	 */
	@Override
	public abstract void PutTuple(ITuple tp, int i) throws IOException;

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOperator#SetAsRootOP()
	 */
	@Override
	public void SetAsRootOP() {
		isRootOP = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOperator#isRootOP()
	 */
	@Override
	public boolean isRootOP() {
		return isRootOP;
	}

	@Override
	public double GetPT() {
		return currentPT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOperator#GetQuery()
	 */
	@Override
	public IQuery GetQuery() {
		return parentQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILSStore#LSCommand(xtream.structures.LSOffer)
	 */
	@Override
	public void LSCommand(LSOffer offer) {
		SetPT(offer.getNewPT());
	}

	/*
	 * This is a simple implementation and should be overridden for some
	 * operators (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILSStore#getLSOffers(double[])
	 */
	@Override
	public LSOffer[] getLSOffers(double[] newPTs) {
		Arrays.sort(newPTs);
		assert (newPTs[newPTs.length - 1] <= 1d && newPTs[0] >= 0d) : "Wrong Input!";
		LSOffer[] offers = getLocalLSOffers(newPTs);

		// Augment current offers with offers from children operators
		LSOffer[] childrenOffers;
		for (IInPort in : inPorts) {
			if (in instanceof ILSStore) {
				childrenOffers = ((ILSStore) (in)).getLSOffers(newPTs);
				for (int i = 0; i < offers.length; i++) {
					offers[i].AugmentWith(childrenOffers[i]);
				}

			}
		}
		return offers;
	}

	/**
	 * <p>
	 * This method generates local (<i>per operator</i>) <b>LSOffers</b> so
	 * getLSOffers() can combine them with upstream offers.
	 * </p>
	 * <p>
	 * Should be implemented in implementing classes (operators) which are
	 * stateful operators.
	 * </p>
	 * 
	 * @param newPTs
	 * @return local (per operator) LSOffers
	 */
	protected LSOffer[] getLocalLSOffers(double[] newPTs) {
		// System.out.println("\n LS Local Offer invocation of AOperator for "+opName);
		LSOffer[] offers = new LSOffer[newPTs.length];
		for (int i = 0; i < offers.length; i++) {
			offers[i] = new LSOffer(parentQuery, newPTs[i],
					rt.getRTRelease(newPTs[i]), 0);
		}
		return offers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#SetPT(double)
	 */
	@Override
	public double SetPT(double newPT) {
		assert newPT >= 0 && newPT <= 1;
		if (currentPT != newPT) {
			double oldPT = newPT;
			double finalPT = newPT;
			do {
				oldPT = finalPT;
				for (IInPort ip : inPorts) {
					finalPT = Math.max((ip).SetPT(finalPT), finalPT);
				}
			} while (finalPT != oldPT);
			currentPT = finalPT;
			// if (Globals.MONITORING_MODE == Monitoring_Modes.Full) // DEBUG
			// System.out.format("\nMON_MSG: Set PT of %s to %f",
			// opName,currentPT);
			rt.SetPT(currentPT);
		}
		return currentPT;
	}
}
