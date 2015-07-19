/**
 * Project: Xtream
 * Module: MeanRT Aggregate
 * Task: to produce mean response time aggregation for tuples
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

import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.structures.AAggregation;
import xtream.structures.AggTuple;

/**
 * to produce mean response time aggregation for tuples
 * 
 * @author ghalambor
 * @see xtream.query.ATumblingWindowAggregation
 */
public class MeanRTAgg extends ATumblingWindowAggregation {

	/**
	 * @see xtream.query.ATumblingWindowAggregation#ATumblingWindowAggregation(long, long, String, IQuery)
	 */
	public MeanRTAgg(long timeWindowSize, long tupleWindowSize, String opName,IQuery parentQuery) {
		super(timeWindowSize,tupleWindowSize, opName,parentQuery);
		agg = new AAggregation() {

			@Override
			public Object ComputeAggregation(Object... tuples) {
				double value = 0;
				for (int i = 0; i < tuples.length; i++) {
					ITuple tpl = (ITuple) tuples[i];
					value += tpl.GetResponseTime();
				}
				if (tuples.length > 0) {
					AggTuple aggtpl = new AggTuple(new Double(value / tuples.length), 1);
					return aggtpl;
				} else
					return null; // no tuple to aggregate!
			}
		};
	}

}
