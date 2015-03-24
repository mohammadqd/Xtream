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
package xtream.structures;

import xtream.Globals;
import xtream.interfaces.ITuple;

/**
 * @author ghalambor
 * 
 */
public class JointTuples implements ITuple {

	// ------------------------------------------------------------
	// NOTE: On changing fields, update constructor & clone methods
	// ------------------------------------------------------------

	public ITuple[] tuples; // array of join tuples
	protected long time;
	protected long expirationTime;
	public double conf; // confidence
	protected long responseTime;

	/**
	 * 
	 */
	protected JointTuples() {
	}

	/**
	 * @param time
	 * @param expirationTime
	 * @param tples
	 *            tuples to join
	 */
	public JointTuples(long time, long expirationTime, double conf,
			ITuple... tples) {
		this.time = time;
		this.expirationTime = expirationTime;
		this.conf = conf;
		tuples = new ITuple[tples.length];
		for (int i = 0; i < tples.length; i++) {
			tuples[i] = tples[i];
		}
		responseTime = -1; // Not Valid
	}

	/**
	 * @param tples
	 *            tuples to join
	 */
	public JointTuples(double conf, ITuple... tples) {
		time = 0;
		expirationTime = Long.MAX_VALUE;
		this.conf = conf;
		tuples = new ITuple[tples.length];
		for (int i = 0; i < tples.length; i++) {
			tuples[i] = tples[i];
			long[] ts = tples[i].GetTimestamp();
			time = Math.max(time, ts[0]);
			expirationTime = Math.min(expirationTime, ts[1]);
		}
		responseTime = -1; // Not Valid
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetSize()
	 */
	@Override
	public long GetSize() {
		long totalSize = 0;
		for (int i = 0; i < tuples.length; i++)
			totalSize += tuples[i].GetSize();
		return totalSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#EqualValue(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean EqualValue(ITuple tp) {
		// TODO NOT IMPLEMENTED
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#FullyEqual(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean FullyEqual(ITuple tp) {
		// TODO NOT IMPLEMENTED
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetIndex()
	 */
	@Override
	public int GetIndex() {
		// TODO NOT IMPLEMENTED
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetType()
	 */
	@Override
	public int GetType() {
		// TODO NOT IMPLEMENTED
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetTimestamp()
	 */
	@Override
	public long[] GetTimestamp() {
		long[] timestamp = new long[2];
		timestamp[0] = time;
		timestamp[1] = expirationTime;
		return timestamp;
	}

	/**
	 * @return true if expirationTime > time (valid timestamps)
	 */
	public boolean isValid() {
		return true;
		// TODO ACTIVE IT
		// return (expirationTime > time);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "@@@ JOINT(";
		for (int i = 0; i < tuples.length; i++)
			result += tuples[i].toString();
		result += ") [CONF:" + conf + "]\n";
		return result;
	}

	@Override
	public double GetConf() {
		return conf;
	}

	@Override
	public long GetResponseTime() {
		assert responseTime >= 0 : "Getting response time of an incomplete tuple!";
		if (responseTime < 0) // responseTime is not set
			return (Globals.core.GetSysCurTime() - time); // calculate and
															// return a
															// temporal
															// response time
		else
			return responseTime;
	}

	@Override
	public void SetResponseTime(long rt) {
		responseTime = rt;
	}

	@Override
	public ITuple Clone() {
		JointTuples newTpl = new JointTuples();
		newTpl.time = time;
		newTpl.expirationTime = expirationTime;
		newTpl.responseTime = responseTime;
		newTpl.conf = conf;
		newTpl.tuples = new ITuple[tuples.length];
		for (int i = 0; i < tuples.length; i++) {
			newTpl.tuples[i] = tuples[i].Clone();
		}
		return newTpl;
	}

	@Override
	public int compareTo(ITuple o) {
		return (new Long(GetTimestamp()[0])).compareTo(new Long(o
				.GetTimestamp()[0]));
	}

	/**
	 * @return oldest timestamp (start time) of all contained tuples
	 */
	public long GetOldestTimestamp() {
		long oldestTS = GetTimestamp()[0];
		for (ITuple tp : tuples) {
			if (tp instanceof JointTuples) {
				oldestTS = Math.min(oldestTS,
						((JointTuples) tp).GetOldestTimestamp());
			} else {
				oldestTS = Math.min(oldestTS, tp.GetTimestamp()[0]);
			}
		}
		return oldestTS;
	}

}
