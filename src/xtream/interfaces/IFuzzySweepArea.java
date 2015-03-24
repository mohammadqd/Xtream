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

import xtream.structures.FuzzyQueryResult;

/**
 * @author ghalambor
 *
 */
public interface IFuzzySweepArea extends ISweepArea {
	
	/**
	 * to run a threshold-based query and return results
	 * 
	 * @param tpl
	 *            tuple as a parameter for query
	 * @param j
	 *            j is 1 or 2 defining order of parameters
	 * @param threshold threshold for threshold-based queries in [0,1]
	 * @return iterator to results
	 */
	public Iterator<FuzzyQueryResult> FQuery(ITuple tpl, int j, double threshold);
	
	/**
	 * Set probability threshold for outputs in [0,1]
	 * @param pt new probability threshold for outputs in [0,1]
	 * @return guaranteed probability threshold (may be more than pt)
	 */
	public double SetPT(double pt);
	
	/**
	 * @return probability threshold for outputs in [0,1]
	 */
	public double GetPT();

}
