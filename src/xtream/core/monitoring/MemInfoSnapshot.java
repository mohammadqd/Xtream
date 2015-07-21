/**
 * Project: Xtream
 * Module:
 * Task: This is a tuple for measuring memory (for monitoring system)
 * Last Modify: Jul 21 2015
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
package xtream.core.monitoring;

import xtream.Globals;
import xtream.interfaces.ITuple;

/**
 * This is a tuple for measuring memory (for monitoring system)
 * 
 * @author ghalambor
 * 
 */
public class MemInfoSnapshot implements ITuple {
	
	// ------------------------------------------------------------
	// NOTE: On changing fields, update constructor & clone methods
	// ------------------------------------------------------------

	/**
	 * used memory (consumed + free)
	 */
	public long assignedMem; 
	/**
	 * used(full) mem out of assigned mem
	 */
	public long usedMem; 
	/**
	 *  free mem out of assigned mem
	 */
	public long freeMem; 
	/**
	 * max possible/allowable mem to assign
	 */
	public long maxMem;  
	/**
	 * timestamp
	 */
	public long startTime;  
	/**
	 * expiration timestamp
	 */
	public long endTime;  

	/**
	 * @return size of a MemInfoSnapshot object (fixed size)
	 */
	public static int SIZE() {
		return 6 * Long.SIZE / 8;
	}

	/**
	 * @param assignedMem used memory (consumed + free)
	 * @param usedMem used(full) mem out of assigned mem
	 * @param freeMem free mem out of assigned mem
	 * @param maxMem max possible/allowable mem to assign
	 * @param startTime timestamp
	 * @param endTime expiration timestamp
	 */
	public MemInfoSnapshot(long assignedMem, long usedMem, long freeMem,
			long maxMem, long startTime, long endTime) {
		this.assignedMem = assignedMem;
		this.usedMem = usedMem;
		this.freeMem = freeMem;
		this.maxMem = maxMem;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Receives data parameters from running JVM
	 */
	public MemInfoSnapshot() {
		long curTime = Globals.core.GetSysCurTime();
		long freemem, totalmem, maxmem;
		maxmem = Runtime.getRuntime().maxMemory();
		freemem = Runtime.getRuntime().freeMemory();
		totalmem = Runtime.getRuntime().totalMemory();
		
		this.assignedMem = totalmem;
		this.usedMem = totalmem - freemem;
		this.freeMem = freemem;
		this.maxMem = maxmem;
		this.startTime = curTime;
		this.endTime = curTime + Globals.DEFAULT_AGG_TUPLE_EXPIRATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetSize()
	 */
	@Override
	public long GetSize() {
		return SIZE();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#EqualValue(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean EqualValue(ITuple tp) {
		// TODO Auto-generated method stub
		assert false : "NOT IMPLEMENTED";
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#FullyEqual(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean FullyEqual(ITuple tp) {
		// TODO Auto-generated method stub
		assert false : "NOT IMPLEMENTED";
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetIndex()
	 */
	@Override
	public int GetIndex() {
		// TODO Auto-generated method stub
		assert false : "NOT IMPLEMENTED";
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetType()
	 */
	@Override
	public int GetType() {
		// TODO Auto-generated method stub
		assert false : "NOT IMPLEMENTED";
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetTimestamp()
	 */
	@Override
	public long[] GetTimestamp() {
		long[] ts = { startTime, endTime };
		return ts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetConf()
	 */
	@Override
	public double GetConf() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#GetResponseTime()
	 */
	@Override
	public long GetResponseTime() {
		// TODO Auto-generated method stub
		assert false : "NOT IMPLEMENTED";
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#SetResponseTime(long)
	 */
	@Override
	public void SetResponseTime(long rt) {
		// TODO Auto-generated method stub
		assert false : "NOT IMPLEMENTED";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#Clone()
	 */
	@Override
	public ITuple Clone() {
		MemInfoSnapshot clne = new MemInfoSnapshot(assignedMem, usedMem,
				freeMem, maxMem, startTime, endTime);
		return clne;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * based on timestamp
	 */
	@Override
	public int compareTo(ITuple o) {
		return (new Long(startTime)).compareTo(new Long(o.GetTimestamp()[0]));
	}

}
