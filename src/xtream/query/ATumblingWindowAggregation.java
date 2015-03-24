package xtream.query;

import java.io.IOException;
import java.util.Vector;

import xtream.Globals;
import xtream.core.Core.ExecutionState;
import xtream.interfaces.IInPort;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.structures.AAggregation;

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
	 *            size of time window (millisec), 0 to make it inactive
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
