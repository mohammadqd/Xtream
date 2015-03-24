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
package xtream.query;

import java.io.IOException;

import xtream.Globals;
import xtream.core.Core.ExecutionState;
import xtream.interfaces.IInPort;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.structures.ABooleanPredicate;

/**
 * PUSH based Operator
 * 
 * @author ghalambor
 * 
 */
public class Select extends AOperator {

	protected ABooleanPredicate prediction;

	public Select(ABooleanPredicate prediction, String opName,
			IQuery parentQuery) {
		super(opName, parentQuery);
		this.prediction = prediction;
	}

	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		if (tp != null) {
			long startTime = System.currentTimeMillis();
			if (!isOpen())
				throw new IOException(
						"ERROR: Trying to put tuples in a closed SELECT operator");
			else {
				if ((!Globals.ADAPTIVE_FLS || tp.GetConf() >= GetPT()) && prediction.Predicate(tp)) { // if
																			// tuple
																			// pass
																			// prediction
					for (OutChannel o : outChannels) { // send it to all out
														// ports
														// (including
														// next operators)
						ITuple nextTpl = tp.Clone();
						if (isRootOP())
							GetQuery().CheckResultTuple(nextTpl);
						o.outPort.PutTuple(nextTpl, o.index);
					}
					rt.AddValue(tp.GetConf(), System.currentTimeMillis()
							- startTime);
				}
			}
		}
	}

	@Override
	public void run(long ts) { // do not care about timeslice
		IInPort inPort = inPorts.elementAt(0);
		while (inPort.hasTuple()
				&& Globals.core.ExecState() == ExecutionState.RUNNING) {
			try {
				PutTuple(inPort.nextTuple(), 1);
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#hasTuple() It returns true if there is
	 * something to process and practically there may be no result so you may
	 * run it and get true but after running nextTuple() get null!!
	 */
	@Override
	public boolean hasTuple() {
		assert false : "Not Implemented!";
		return false;
	}

	@Override
	public ITuple nextTuple() throws IOException {
		// if (!isOpen())
		// throw new RuntimeException(
		// "ERROR: Trying to get tuples from a closed SELECT operator");
		// else {
		// IInPort inPort = inPorts.elementAt(0);
		// while (inPort.hasTuple()) {
		// ITuple tp = inPort.nextTuple();
		// if (prediction.Predicate(tp)) { // if tuple pass prediction
		// return tp;
		// }
		// }
		// return null;
		// }
		assert false : "Not Implemented!";
		return null;
	}

	@Override
	public boolean isUnary() {
		return true;
	}

}
