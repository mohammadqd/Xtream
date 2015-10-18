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

import java.util.Iterator;

/**
 * @author ghalambor
 * 
 */
public interface ISweepArea {

	/**
	 * @param tpl
	 *            insert tuple to sweeparea
	 */
	public void Insert(ITuple tpl);

	/**
	 * replaces first occurrence of oldtuple with newtuple
	 * 
	 * @param oldTpl
	 *            old tuple to be replaced by new tuple
	 * @param newTpl
	 *            new tuple to be replace for old tuple
	 */
	public void Replace(ITuple oldTpl, ITuple newTpl);

	/**
	 * @return iterator to scan all tuples
	 */
	public Iterator<ITuple> GetIterator();

	/**
	 * to extract tuples which should be removed Whenever the iterator returns
	 * an element, this element is removed from the SweepArea
	 * 
	 * @param tpl
	 *            tuple as a parameter for query
	 * @param j
	 *            j is 1 or 2 defining order of parameters
	 * @return iterator to results (tuples which should be removed)
	 */
	public Iterator<ITuple> ExtractElements(ITuple tpl, int j);

	/**
	 * to purge unused tuples
	 * 
	 * @param tpl
	 *            tuple as a parameter for query
	 * @param j
	 *            j is 1 or 2 defining order of parameters
	 * @return number of removed tuples
	 */
	public int PurgeElements(ITuple tpl, int j);

	/**
	 * @return number of tuples in sweeparea
	 */
	public int GetCount();

}
