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
package xtream.interfaces;

import xtream.Globals;

/**
 * @author ghalambor
 *
 */
public interface ILoadShedder {
	
	/**
	 * @param memToRelease
	 *            mem to release (byte)
	 * @return released mem (byte)
	 */
	public long ReleaseMemory(long memToRelease);
	
	/**
	 * @param um used mem (bytes)
	 * @param mslp used mem slope 
	 * @param islp input rate slope
	 * @return memory size to release (bytes)
	 */
	public long MTR(long um, double mslp, double islp);
	
	/**
	 * @param q query to improve its PT one step
	 */
	public void QueryQoSImprove(IQuery q);
	
	/**
	 * @return system time (millisec) of last load shedding
	 */
	public long GetLastLoadSheddingTime();
	
	public void Close();

}
