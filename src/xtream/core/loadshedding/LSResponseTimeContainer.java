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
package xtream.core.loadshedding;

import java.util.Arrays;

import xtream.Globals;

/**
 * To maintain response times for federal load shedding
 * 
 * @author ghalambor
 * 
 */
public class LSResponseTimeContainer {

	protected long leavesCount;
	protected long totalOperations;
	protected long[] delays;
	protected double currentPT; 

	/**
	 * @param leavesCount
	 * @see Globals.PER_OPERATOR_LS_OFFERS_COUNT
	 */
	public LSResponseTimeContainer(long leavesCount) {
		delays = new long[(int) leavesCount];
		this.leavesCount = leavesCount;
	}
	
	public LSResponseTimeContainer() {
		this(Globals.PER_OPERATOR_LS_OFFERS_COUNT);
	}

	/**
	 * @param conf confidence value of new operation to add
	 * @param delay delay of new operation
	 */
	public void AddValue(double conf, long delay) {
		totalOperations++;
		long index = Math.min(Math.round(conf * leavesCount),
				leavesCount - 1);
		delays[(int) index] += delay;
	}
	
	/**
	 * @param pt probability threshold
	 * @return released response-time if we change prob. threshold to <i>pt</i>
	 */
	public double getRTRelease(double pt) {
		int i = (int)Math.min(Math.round(currentPT * leavesCount),
				leavesCount - 1);		
		long delayReleased = 0;
		while (i < leavesCount && ((double) i + 1) / leavesCount <= pt) {
			delayReleased += delays[i];
			i++;
		}
		return (double)delayReleased/((totalOperations>0)?totalOperations:1);
	}
	
	public void SetPT(double newPT) {
		currentPT = newPT;
	}
	
	public double GetPT() {
		return currentPT;
	}
	
	/**
	 * @return general response-time for this operation
	 */
	public double GetRT() {
		long totalDelay = 0;
		for (long delay:delays) {
			totalDelay += delay;
		}
		return (double)totalDelay/((totalOperations>0)?totalOperations:1);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String thresholds = new String();
		for (int i=0; i<leavesCount; i++) {
			thresholds += "(" + (((double) i) / leavesCount) + ","+(((double) i + 1) / leavesCount)+") "; 
		}
		return "LSResponseTimeContainer [leavesCount=" + leavesCount
				+ ", totalOperations=" + totalOperations + ", Buckets: "+thresholds+", delays="
				+ Arrays.toString(delays) + ", currentPT=" + currentPT + "]";
	}

}
