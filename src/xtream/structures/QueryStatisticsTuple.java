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
import xtream.query.IQuery;

/**
 * This tuple includes query statistics including QoS
 * 
 * @author ghalambor
 * 
 */
public class QueryStatisticsTuple implements IAggTuple {

	protected IQuery query; // related query
	protected IQoS qos;

	// General fields
	/**
	 * confidence
	 */
	public double conf;
	protected long startTime;
	protected long expirationTime;
	protected long responseTime;

	/**
	 * @param query
	 *            related query
	 * @param timestamp
	 *            timestamp of tuple
	 * @param qos
	 *            related QoS
	 * @param conf
	 *            confidence value
	 */
	public QueryStatisticsTuple(IQuery query, IQoS qos, long timestamp,
			double conf) {
		this.query = query;
		this.qos = qos;
		this.conf = conf;
		responseTime = -1; // not valid
		this.startTime = timestamp;
		this.expirationTime = startTime + Globals.DEFAULT_AGG_TUPLE_EXPIRATION;
	}

	/**
	 * @see xtream.structures.QueryStatisticsTuple#QueryStatisticsTuple(IQuery,
	 *      IQoS, long, double)
	 */
	public QueryStatisticsTuple(IQuery query, IQoS qos, double conf) {
		this(query, qos, Globals.core.GetSysCurTime(), conf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetSize()
	 */
	@Override
	public long GetSize() {
		return 5 * Double.SIZE / 8;
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
		QueryStatisticsTuple newtpl = new QueryStatisticsTuple(query, qos, conf);
		newtpl.startTime = startTime;
		newtpl.expirationTime = expirationTime;
		newtpl.responseTime = responseTime;
		return newtpl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// DEBUG
		// System.out.println("(Query_Statistics QName: "
		// + ((query != null) ? query.GetName() : "Unknown!") + " QoS: "
		// + qos.GetQoS() + " Time: " + startTime + ") ");
		return "(Query_Statistics QName: "
				+ ((query != null) ? query.GetName() : "Unknown!") + " QoS: "
				+ qos.GetQoS() + " Time: " + startTime + ") ";
	}

	@Override
	public Number getValue() {
		return (1d - qos.GetQoS()) * query.GetQoSWeight();
	}

	@Override
	public void setValue(Number value) {
		assert false : "Not Implemented";
	}

	@Override
	public int compareTo(ITuple o) {
		return (new Long(GetTimestamp()[0])).compareTo(new Long(o
				.GetTimestamp()[0]));
	}

	/**
	 * @return related query
	 */
	public IQuery getQuery() {
		return query;
	}
}