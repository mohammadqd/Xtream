/**
 * Project: Xtream
 * Module: Abstract Self Join in SPJ queries
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
package xtream.query;

import java.io.IOException;
import java.util.Iterator;

import xtream.core.loadshedding.LSOffer;
import xtream.interfaces.IFuzzySweepArea;
import xtream.interfaces.IInPort;
import xtream.interfaces.ILSStore;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.structures.ABooleanPredicate;
import xtream.structures.FuzzyQueryResult;
import xtream.structures.JointTuples;
import xtream.usecase.VectorSweepArea_Usecase;

/**
 * PUSH based Operator
 * Abstract Self Join in SPJ queries
 * @author ghalambor
 * 
 */
public abstract class ASelfJoin extends AOperator {

	/**
	 * synopsis should be created in realized versions
	 */
	public IFuzzySweepArea synopsis;

	// classes
	protected long timeWindowSize; // size of time window (msec)
	protected ABooleanPredicate DefaultRemovePredicate; // default remove
														// predicate removes
														// expired tuples based
														// on their timestamp
														// and confidence

	/**
	 * @param timeWindowSize
	 *            size of time window (msec) default is in
	 *            xtream.Globals#OVERLOAD_CHECKING_TIME_PERIOD
	 * @param opName
	 *            operator name
	 * @param parentQuery
	 *            link to parent query
	 * @see xtream.query.ABinaryJoin#ABinaryJoin(long, String, IQuery)
	 */
	public ASelfJoin(final long timeWindowSize, String opName,
			IQuery parentQuery) {
		super(opName, parentQuery);
		this.timeWindowSize = timeWindowSize;
		DefaultRemovePredicate = new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) { // remove older
														// tuples
														// tpls[1] is
														// new tuple
				if (tpls[1].GetTimestamp()[0] - tpls[0].GetTimestamp()[0] > timeWindowSize) // checking
					// timestamp
					return true;
				else
					return false;
			}
		};
		CreateSynopsis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#PutTuple(xtream.interfaces.ITuple)
	 */
	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		if (!isOpen())
			throw new IOException(
					"ERROR: Trying to put tuples in a closed SelfJoin operator");

		Iterator<FuzzyQueryResult> it; // iterator

		// purge expired tuples
		synopsis.PurgeElements(tp, 1);

		// Query
		it = synopsis.FQuery(tp, 1, GetPT());
		while (it.hasNext()) {
			FuzzyQueryResult newMatch = it.next();
			JointTuples result = new JointTuples(newMatch.conf, tp,
					newMatch.tpl);
			if (result.isValid()) {
				for (OutChannel o : outChannels) { // for all out ports
													// ports
					ITuple nextTpl = result.Clone();
					if (isRootOP())
						GetQuery().CheckResultTuple(nextTpl);
					o.outPort.PutTuple(nextTpl, o.index); // to out
				}
			}
		}

		// Insert new tuple
		synopsis.Insert(tp);
	}

	/**
	 * to create synopsis with query and removal predicates
	 * 
	 * @param _synopsis
	 *            synopsis to create
	 * @see VectorSweepArea_Usecase
	 */
	protected abstract void CreateSynopsis();

	@Override
	public void run(long ts) {
		IInPort inPort = inPorts.elementAt(0);
		long startTime = System.currentTimeMillis();
		while (inPort.hasTuple()
				&& (System.currentTimeMillis() - startTime) < ts) {
			try {
				PutTuple(inPort.nextTuple(), 1);
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean hasTuple() {
		assert false : "Not Implemented!";
		return false;
	}

	@Override
	public ITuple nextTuple() throws IOException {
		assert false : "Not Implemented!";
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.AOperator#SetPT(double)
	 */
	@Override
	public double SetPT(double pt) {
		double oldPT = GetPT();
		double newPT = super.SetPT(pt);
		if (oldPT != newPT) {
			synopsis.SetPT(newPT);
		}
		return newPT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOperator#isUnaryOP()
	 */
	@Override
	public boolean isUnary() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.query.AOperator#getLocalLSOffers(double[])
	 */
	@Override
	protected LSOffer[] getLocalLSOffers(double[] newPTs) {
		if (synopsis instanceof ILSStore) {
			return ((ILSStore) synopsis).getLSOffers(newPTs);
		} else
			return super.getLocalLSOffers(newPTs);
	}

}
