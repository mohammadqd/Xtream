/**
 * Project: Xtream
 * Module: out port for aggregations
 * Task: 
 * Last Modify: May 2013
 * Created: 2011
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
package xtream.io;


/**
 * @author Mohammad to generate aggregational output (esp. statistics)
 */
public class AggOutPort extends TxtFileOutPort {

	protected double sum; // sum of values of each period
	protected double totalSum; // sum for all periods together
	protected long cnt; // count of values of each period
	protected double wght;
	protected long totalCnt; // cnt for all periods together
	protected double totalWeight;
	protected long lastTime; // last seen time (e.g. 0,5000,10000,...)
	protected long periodSize; // e.g. 5000 to have results each 5 seconds

	/**
	 * @param fname
	 * @param mainCore
	 * @param periodSize
	 *            size of periods to send out results
	 */
	public AggOutPort(String fname, long periodSize) {
		super(fname);
		this.periodSize = periodSize;
	}

	/**
	 * @param time
	 *            (e.g. ideal time)
	 * @param value
	 *            (e.g. response time)
	 * @param weight
	 *            weight of value
	 */
	public synchronized void WriteAggValue(long time, double value,
			double weight) {
		// System.out.print(","+time); //DBG
		totalSum += value * weight;
		totalCnt++;
		totalWeight += weight;
		if (time < lastTime + periodSize) {
			sum += value * weight;
			cnt++;
			wght += weight;
		} else {
			WriteStr(lastTime + "," + ((wght > 0) ? (sum / wght) : 0) + ","
					+ sum + "," + wght + "," + cnt);
			lastTime = (time / periodSize) * periodSize;
			sum = value * weight;
			cnt = 1;
			wght = weight;
		}
	}

	public synchronized void WriteAggValue(long time, double value) {
		WriteAggValue(time, value, 1);
	}

	@Override
	public synchronized void Close() {
		WriteStr(lastTime + "," + ((wght > 0) ? (sum / wght) : 0) + "," + wght
				+ "," + cnt);
		WriteStr("[TOTAL MEAN]," + (totalSum / totalWeight) + ", Total Sum,"
				+ totalSum + ", Total Weight," + totalWeight
				+ ", Total Count: " + totalCnt);
		// WriteStr("[START_TIME_OF_PERIOD] , [PERIODIC_WEIGHTED_MEAN] , [PERIODIC_SUM], [PERIODIC_TOTAL_WEIGHT] , [PERIODIC_COUNT]");
		super.Close();
	}

}
