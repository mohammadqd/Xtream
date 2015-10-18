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

import xtream.structures.AAggregation;
import xtream.structures.AggTuple;

/**
 * to Count tuples of tumbling time windows\n 
 * this is a holistic implementation
 * and not efficient as it stores all tuples of a window
 * 
 * @author ghalambor
 * 
 */
public class TumblingTimeWindowCount extends ATumblingWindowAggregation {

	/**
	 * @param timeWindowSize time window size
	 * @param tupleWindowSize tuple-based window size
	 * @param opName operator unique name
	 * @see ATumblingWindowAggregation
	 */
	public TumblingTimeWindowCount(long timeWindowSize,long tupleWindowSize, String opName,IQuery parentQuery) {
		super(timeWindowSize,tupleWindowSize, opName,parentQuery);

		agg = new AAggregation() {

			@Override
			public Object ComputeAggregation(Object... tuples) {
				AggTuple aggtpl = new AggTuple(new Long(tuples.length), 1);
				return aggtpl;
			}
		};
	}

}
