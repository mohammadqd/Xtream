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
package xtream.plr;

import xtream.Globals;
import xtream.structures.IQoS;

/** 
 * 2D QoS based on Probability-Threshold and Response-time for PLR
 * @author ghalambor
 *
 */
public class PTRTQoS implements IQoS {
	
	protected double ART; // average response time
	protected double PTR; // probability threshold in [0,1]
	protected double weight; // weight of related query among all queries (>0)

	/**
	 * @param aRT average response time
	 * @param pTR probability threshold in [0,1]
	 * @param weight weight of related query among all queries (>0)
	 */
	public PTRTQoS(double aRT, double pTR, double weight) {
		ART = aRT;
		PTR = pTR;
		this.weight = weight;
	}


	/* (non-Javadoc)
	 * @see xtream.interfaces.IQoS#GetQoS()
	 */
	@Override
	public double GetQoS() {
		return GetQoS(ART, PTR);
	}
	
	public double GetTQoS() {
		return GetTQoS(ART, PTR, weight);
	}
	
	public double GetWeight() {
		return weight;
	}
	
	public static double GetQoS(double ART, double PTR) {
		double ARTQuality = Math.max(1 - (ART / Globals.MAX_ACCEPTABLE_ART) , 0);
		double PTQuality = Math.max(1-PTR, 0);
		return (PTQuality * Globals.PTR_WEIGHT_IN_QOS + ARTQuality * (1-Globals.PTR_WEIGHT_IN_QOS));
	}
	
	public static double GetTQoS(double ART, double PTR, double weight) {
		double ARTQuality = Math.max(1 - (ART / Globals.MAX_ACCEPTABLE_ART) , 0);
		double PTQuality = Math.max(1-PTR, 0);
		return (weight*(PTQuality * Globals.PTR_WEIGHT_IN_QOS + ARTQuality * (1-Globals.PTR_WEIGHT_IN_QOS)));
	}
	
	/**
	 * @param newPT 
	 * @param newRT
	 * @return the lost QoS caused by this switch
	 */
	public double getSwitchCost(double newPT,double rtRelease) {
		return (GetQoS(ART,PTR)-GetQoS(ART-rtRelease,newPT));
	}

}
