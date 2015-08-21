/**
 * Project: Xtream
 * Module: Distributive Aggregation
 * Task: maintain and update single value aggregations (e.g. MIN,MAX,SUM,COUNT)
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
import xtream.structures.AAggregation;
import xtream.structures.ITuple;

/**
 * This kind of aggregate only stores one value/tuple and each time after
 * receiving a new tuple computes a new aggregation value and may update its
 * only stored value It is good for MIN,MAX,SUM,COUNT and BINARY_SLOPE
 * aggregations
 * 
 * @author ghalambor
 * 
 */
public class ADistributiveAggregation extends AOperator {

	protected Object value; // the only stored value
	protected AAggregation aggResult; // aggregation function for result (must
										// be created in implementing classes)
	protected AAggregation newValue; // aggregation function to compute new
										// value (must be created in
										// implementing classes)

	/**
	 * @param opName name of operator
	 * @param parentQuery link to parent query
	 */
	public ADistributiveAggregation(String opName, IQuery parentQuery) {
		super(opName, parentQuery);
		value = null;
		aggResult = null;
		newValue = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOperator#run(long)
	 */
	@Override
	public void run(long ts) {
		assert false : "NOT IMPLEMENTED!";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#hasTuple()
	 */
	@Override
	public boolean hasTuple() {
		assert false : "NOT IMPLEMENTED!";
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#nextTuple()
	 */
	@Override
	public ITuple nextTuple() throws IOException {
		assert false : "NOT IMPLEMENTED!";
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.AOperator#PutTuple(xtream.interfaces.ITuple, int)
	 */
	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		assert aggResult != null : "agg Not Set!";
		assert newValue != null : "agg Not Set!";
		long startTime = System.currentTimeMillis();
		ITuple result = (ITuple) (aggResult.ComputeAggregation(value, tp));
		if (result != null
				&& (!Globals.ADAPTIVE_FLS || result.GetConf() >= GetPT())) {
			for (OutChannel o : outChannels) { // send it to all out ports
				// (including
				// next operators)
				ITuple nextTpl = result.Clone();
				if (isRootOP())
					GetQuery().CheckResultTuple(nextTpl);
				o.outPort.PutTuple(nextTpl, o.index);
			}
			rt.AddValue(result.GetConf(), System.currentTimeMillis()
					- startTime);
		}
		value = newValue.ComputeAggregation(value, tp);
	}

	@Override
	public boolean isUnary() {
		return true;
	}

}
