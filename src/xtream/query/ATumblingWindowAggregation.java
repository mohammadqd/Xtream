/**
 * Project: Xtream
 * Module: tumbling time/tuple window aggregation
 * Task: tumbling time/tuple window aggregation (ref. DSMS Literature)
 * Last Modify: Jul 19, 2015
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
import java.util.Vector;

import xtream.Globals;
import xtream.core.Core.ExecutionState;
import xtream.io.IInPort;
import xtream.structures.AAggregation;
import xtream.structures.ITuple;

/**
 * <p>
 * This is a tumbling time/tuple window aggregation time window and tuple count
 * based window are both supported in tandem
 * </p>
 * <p>
 * A window will be closed when time length or tuple count exceeds from the
 * given parameters. Also it is possible to set time/tuple window size as 0 to
 * make it ignorable. It is not possible to set both parameters as 0!
 * </p>
 * 
 * @author ghalambor
 * 
 */
public class ATumblingWindowAggregation extends AOperator {

	protected Vector<Object> window;
	protected long timeWindowSize;
	protected long tupleWindowSize;
	protected long windowStartTime; // start time of the current window
	protected AAggregation agg; // aggregation function

	/**
	 * PUSH based aggregation
	 * 
	 * @param timeWindowSize
	 *            size of time window (msec), 0 to make it inactive
	 * @param tupleWindowSize
	 *            size of tuple-based window, 0 to make it inactive
	 */
	public ATumblingWindowAggregation(long timeWindowSize,
			long tupleWindowSize, String opName, IQuery parentQuery) {
		super(opName, parentQuery);
		assert (timeWindowSize != 0 || tupleWindowSize != 0) : "Wrong Setting!";
		this.timeWindowSize = timeWindowSize;
		this.tupleWindowSize = tupleWindowSize;
		window = new Vector<Object>();
		windowStartTime = System.currentTimeMillis();
	}

	@Override
	public void run(long ts) {
		IInPort inPort = inPorts.elementAt(0);
		while (inPort.hasTuple()
				&& Globals.core.ExecState() == ExecutionState.RUNNING) {
			try {
				PutTuple(inPort.nextTuple(), 1);
			} catch (RuntimeException e) {
				Globals.core.Exception(e);
			} catch (IOException e) {
				Globals.core.Exception(e);
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

	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		assert agg != null : "agg Not Set!";
		long startTime = System.currentTimeMillis();
		if ((timeWindowSize != 0 && (startTime - windowStartTime > timeWindowSize)) // checking
																					// time
																					// window
				|| (tupleWindowSize != 0 && window.size() > tupleWindowSize)) { // checking
																				// tuple
																				// window
			Object[] tuples = new Object[window.size()]; // temp array
			window.copyInto(tuples);
			ITuple result = (ITuple) (agg.ComputeAggregation(tuples));
			if (result != null && (!Globals.ADAPTIVE_FLS || result.GetConf() >= GetPT())) {
				for (OutChannel o : outChannels) { // send it to all out ports
					// (including
					// next operators)
					ITuple nextTpl = result.Clone();
					if (isRootOP())
						GetQuery().CheckResultTuple(nextTpl);
					o.outPort.PutTuple(nextTpl, o.index);
				}
				rt.AddValue(result.GetConf(),System.currentTimeMillis() - startTime);
			}
			window = new Vector<Object>(); // create new window
			windowStartTime = System.currentTimeMillis();
		}
		window.add(tp);

	}

	@Override
	public boolean isUnary() {
		return true;
	}

}
