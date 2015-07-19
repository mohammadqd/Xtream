/**
 * Project: Xtream
 * Module: binary slope (compute slope based on two values)
 * Task: Computes slope of two consequent <i>IAggTuples</i> based on their values and start timestamps
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

import xtream.interfaces.IAggTuple;
import xtream.interfaces.IQuery;
import xtream.structures.AAggregation;
import xtream.structures.AggTuple;

/**
 * Computes slope of two consequent <i>IAggTuples</i> based on their values and start timestamps
 * @author ghalambor
 * 
 */
public class BinarySlope extends ADistributiveAggregation {

	/**
	 * NOTE: Confidences of the results are computed based on intentional semantics 
	 * @param opName operator name
	 * @param parentQuery parent query
	 */
	public BinarySlope(String opName,IQuery parentQuery) {
		super(opName,parentQuery);

		//---------------------------------
		// Create Main Aggregation Function
		//---------------------------------
		aggResult = new AAggregation() {

			@Override
			public Object ComputeAggregation(Object... tuples) {
				if (tuples.length == 2 && tuples[0] != null
						&& tuples[1] != null) {
					IAggTuple[] tpls = { (IAggTuple) tuples[0],
							(IAggTuple) tuples[1] };
					AggTuple aggtpl = new AggTuple(new Double(
							(tpls[1].getValue().doubleValue() - tpls[0]
									.getValue().doubleValue())
									/ (tpls[1].GetTimestamp()[0] - tpls[0]
											.GetTimestamp()[0])), tpls[1].GetTimestamp()[0],tpls[0].GetConf()*tpls[1].GetConf());
					return aggtpl;
				} else
					return null; // no tuple to aggregate!
			}
		};

		//---------------------------------
		// Create Function to update value
		//---------------------------------
		newValue = new AAggregation() {

			@Override
			public Object ComputeAggregation(Object... tuples) {
				assert tuples.length == 2;
				return tuples[1]; // always new value will be kept
			}
		};
	}

}
