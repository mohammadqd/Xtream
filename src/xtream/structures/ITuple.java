/**
 * Project: Xtream
 * Module: ITuple
 * Task: represents a stream tuple
 * Last Modify: May 2013
 * Created: 2007
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

public interface ITuple extends Comparable<ITuple> {

	/**
	 * @return size of tuple in bytes
	 */
	public long GetSize();

	/**
	 * to check equality of two tuples (all fields except timestamp)
	 * 
	 * @param tp tuple to compare this tuple with
	 * @return true: two tuples are equal (except timestamp)
	 */
	public boolean EqualValue(ITuple tp);

	/**
	 * to check equality of two tuples (all fields including timestamp)
	 * 
	 * @param tp
	 *            tuple to compare this tuple with
	 * @return true: two tuples are equal (including timestamp)
	 */
	public boolean FullyEqual(ITuple tp);

	/**
	 * @return string representation of tuple
	 */
	public String toString();

	/**
	 * @return an index for tuple
	 */

	public int GetIndex();

	/**
	 * @return type of tuple
	 */
	public int GetType();

	/**
	 * @return tuple timestamps first element is starting timestamp and second
	 *         element is expiration timestamp
	 */
	public long[] GetTimestamp();

	/**
	 * @return confidence of tuple
	 */
	public double GetConf();

	public long GetResponseTime();

	/**
	 * @param rt
	 *            exact response time to set (must be computed regarding current
	 *            time and start time stamp)
	 */
	public void SetResponseTime(long rt);

	public ITuple Clone();
}
