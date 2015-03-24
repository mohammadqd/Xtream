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
package xtream.experiments;

/**
 * @author ghalambor
 *
 */
public class LinearTupleDelay extends ASyntheticInputDelayGenerator {
	
	protected long totalTuples; 
	protected long tupleCounter;
	protected long startDelay;
	protected long endDelay;

	/**
	 * 
	 */
	public LinearTupleDelay(long totalTuples,long startDelay,long endDelay) {
		super();
		this.totalTuples = totalTuples;
		this.startDelay = startDelay;
		this.endDelay = endDelay;
		this.tupleCounter = 0;
	}

	/* (non-Javadoc)
	 * @see xtream.experiments.ASyntheticInputDelayGenerator#nextDelay()
	 */
	@Override
	public long nextDelay(double progress) {
		long result =  Math.round(((double)endDelay-startDelay)*(((double)tupleCounter)/totalTuples)+startDelay);
		tupleCounter++;
//		System.out.println("DELAY:("+result+")");
		return result;
	}
}
