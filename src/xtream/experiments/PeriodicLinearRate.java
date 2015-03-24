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
public class PeriodicLinearRate extends ASyntheticInputDelayGenerator {

	protected long totalPeriods; 
	protected long startRate; // rate = no. of tuples per period
	protected long endRate;
	protected double periodSize; // in [0,1]
	protected long periodTime; // length of period (millisec)

	/**
	 * 
	 */
	public PeriodicLinearRate(double periodSize,long periodTime,long startRate,long endRate) {
		super();
		this.periodSize = periodSize;
		this.periodTime = periodTime;
		this.totalPeriods = (long)(1d / periodSize);
		this.startRate = startRate;
		this.endRate = endRate;
	}

	/* (non-Javadoc)
	 * @see xtream.experiments.ASyntheticInputDelayGenerator#nextDelay()
	 */
	@Override
	public long nextDelay(double progress) {
		long curPeriod = (long)(progress/periodSize);
		long result =  Math.round((double)periodTime/(((double)endRate-startRate)*(((double)curPeriod)/totalPeriods)+startRate));
//		System.out.println("DELAY:("+result+")");
		return result;
	}

}
