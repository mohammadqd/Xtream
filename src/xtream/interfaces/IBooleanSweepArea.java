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

import java.util.Iterator;

/**
 * @author ghalambor
 *
 */
public interface IBooleanSweepArea extends ISweepArea {
	
	/**
	 * to run a query and return results
	 * 
	 * @param tpl
	 *            tuple as a parameter for query
	 * @param j
	 *            j is 1 or 2 defining order of parameters
	 * @return iterator to results
	 */
	public Iterator<ITuple> Query(ITuple tpl, int j);

}
