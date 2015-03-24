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

import xtream.interfaces.IAggTuple;
import xtream.interfaces.IQuery;
import xtream.structures.AAggregation;

/**
 * This is a Tumbling Time/Tuple Window Aggregate to retrieve a tuple with max
 * value among <i>IAggTuples</i>
 * 
 * @author ghalambor
 * 
 */
public class MaxWindowAgg extends ATumblingWindowAggregation {

	/**
	 * @param timeWindowSize
	 *            size of time window (millisec), 0 to make it inactive
	 * @param tupleWindowSize
	 *            size of tuple-based window, 0 to make it inactive
	 * @param opName unique name for operator
	 * @param parentQuery ref to parent query
	 */
	public MaxWindowAgg(long timeWindowSize, long tupleWindowSize,
			String opName, IQuery parentQuery) {
		super(timeWindowSize, tupleWindowSize, opName, parentQuery);

		agg = new AAggregation() {

			@Override
			public Object ComputeAggregation(Object... tuples) {
				if (tuples.length > 0) {
					Number maxValue = ((IAggTuple) tuples[0]).getValue();
					int maxValueIndex = 0;
					for (int i = 1; i < tuples.length; i++) {
						IAggTuple tpl = (IAggTuple) tuples[i];
						if (tpl.getValue().doubleValue() > maxValue
								.doubleValue()) {
							maxValue = tpl.getValue();
							maxValueIndex = i;
						}
					}
					return tuples[maxValueIndex];
				} else
					return null; // no tuple to aggregate!
			}
		};
	}

}
