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

import xtream.Globals;
import xtream.Globals.LSOfferSelectionMethod;
import xtream.interfaces.IAggTuple;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.plr.PTRTQoS;

/**
 * Load Shedding Offer
 * 
 * @author ghalambor
 * 
 */
public class LSOffer implements IAggTuple {

	// ------------------------------------------------------------
	// NOTE: On changing fields, update constructor & clone methods
	// ------------------------------------------------------------

	public IQuery query; // related query
	// protected double oldPT; // old probability threshold
	public double newPT; // new probability threshold
	// protected double oldRT; // old response time
	// protected double newRT; // new response time
	public long memRelease; // memory release after executing this offer
							// (byte)
	public double rtRelease; // decrease in response time

	public double conf;
	public long startTime;
	public long expirationTime;
	public long responseTime;
	public double cost; // cost of this offer (lost QoS)
	public double totalCost; // total cost (lost TQoS)

	/**
	 * @param query
	 *            related query
	 * @param newPT
	 *            new probability threshold
	 * @param memRelease
	 *            memory release after executing this offer (byte)
	 */
	public LSOffer(IQuery query, double newPT, double rtRelease, long memRelease) {
		this.query = query;
		this.newPT = newPT;
		this.memRelease = memRelease;
		this.rtRelease = rtRelease;
		this.conf = 1;
		responseTime = -1; // not valid
		this.startTime = Globals.core.GetSysCurTime();
		this.expirationTime = startTime + Globals.DEFAULT_AGG_TUPLE_EXPIRATION;
		if (query != null) {
			cost = ((PTRTQoS) query.GetQoS()).getSwitchCost(newPT, rtRelease);
			totalCost = cost * query.GetRelativeQoSWeight();
		}
	}

	public void SetCosts() {
		cost = ((PTRTQoS) query.GetQoS()).getSwitchCost(newPT, rtRelease);
		totalCost = cost * query.GetRelativeQoSWeight();
	}

	public LSOffer() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetSize()
	 */
	@Override
	public long GetSize() {
		return getSize();
	}
	
	public static long getSize() {
		return 9 * Double.SIZE / 8;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#EqualValue(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean EqualValue(ITuple tp) {
		assert false : "Not Implemented";
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#FullyEqual(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean FullyEqual(ITuple tp) {
		assert false : "Not Implemented";
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetIndex()
	 */
	@Override
	public int GetIndex() {
		assert false : "Not Implemented";
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetType()
	 */
	@Override
	public int GetType() {
		assert false : "Not Implemented";
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetTimestamp()
	 */
	@Override
	public long[] GetTimestamp() {
		long[] ts = { startTime, expirationTime };
		return ts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetConf()
	 */
	@Override
	public double GetConf() {
		return conf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetResponseTime()
	 */
	@Override
	public long GetResponseTime() {
		assert responseTime >= 0 : "Getting unready response time";
		return responseTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#SetResponseTime(long)
	 */
	@Override
	public void SetResponseTime(long rt) {
		responseTime = rt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#Clone()
	 */
	@Override
	public ITuple Clone() {
		LSOffer newtpl = new LSOffer(query, newPT, rtRelease, memRelease);
		newtpl.startTime = startTime;
		newtpl.expirationTime = expirationTime;
		newtpl.responseTime = responseTime;
		newtpl.conf = conf;
		return newtpl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IAggTuple#getValue()
	 */
	@Override
	public Number getValue() {
		return newPT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IAggTuple#setValue(java.lang.Number)
	 */
	@Override
	public void setValue(Number value) {
		assert false : "Not Implemented";
	}

	/**
	 * @return the newPT
	 */
	public synchronized double getNewPT() {
		return newPT;
	}

	/**
	 * @param newPT
	 *            the newPT to set
	 */
	public synchronized void setNewPT(double newPT) {
		this.newPT = newPT;
	}

	@Override
	public int compareTo(ITuple o) {
		if (Globals.LOADSHEDDING_OFFERSELECTION_METHOD == LSOfferSelectionMethod.Greedy) {
		// GREEDY 1 (gain)
		return (new Long(((LSOffer) o).memRelease)).compareTo(new Long(
				(memRelease)));
		} else { 		// GREEDY 2 gain/loss
		return (new Double((double)(((LSOffer) o).memRelease)/((LSOffer) o).totalCost)).compareTo(new Double(
				((double)memRelease/totalCost))); }

	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String("(LSOffer NEW_PT: " + newPT + " RT_RELEASE: "
				+ rtRelease + " MEM_RELEASE: " + memRelease + " CONF: " + conf
				+ " ST_TS: " + startTime + " EX_TS: " + expirationTime + ") ");
	}

	/**
	 * @param otherOffer
	 * @return this offer while rtRelease and memRelease of the otherOffer is
	 *         added to it
	 */
	public LSOffer AugmentWith(LSOffer otherOffer) {
		this.memRelease += otherOffer.memRelease;
		this.rtRelease += otherOffer.rtRelease;
		this.conf *= otherOffer.conf;
		this.expirationTime = Math.min(this.expirationTime,
				otherOffer.expirationTime);
		return this;
	}

	/**
	 * @param otherOffer
	 * @return true if both offers offer the same new probability threshold
	 */
	public boolean SimilarTo(LSOffer otherOffer) {
		return (this.newPT == otherOffer.newPT);
	}
}
