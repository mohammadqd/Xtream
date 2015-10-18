/**
 * Project: Xtream
 * Module: IOperator
 * Task: A general interface for operators
 * Last Modify: May 15,2013
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

package xtream.query;

import xtream.io.IIOPort;

public interface IOperator extends IIOPort {

	/**
	 * @param ts
	 *            timeslice to run (millisec)
	 */
	public void run(long ts);

	/**
	 * To set this operator as root operator (esp. for FLS)
	 */
	public void SetAsRootOP();

	/**
	 * @return if this OP is a root OP
	 */
	public boolean isRootOP();

	/**
	 * @return query which contains this operator, may return null for orphan
	 *         operators
	 */
	public IQuery GetQuery();
}
