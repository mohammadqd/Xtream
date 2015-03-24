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

import java.util.Comparator;

import xtream.interfaces.IBooleanSweepArea;
import xtream.interfaces.ITuple;

/**
 * @author ghalambor
 *
 */
public abstract class ABooleanSweepArea implements IBooleanSweepArea {

	protected ABooleanPredicate queryPredicate;
	protected ABooleanPredicate removePredicate;
	protected Comparator<ITuple> order;
	
	/**
	 * @param queryPredicate
	 * @param removePredicate
	 * @param order to check order of tuples (usually by timestamp)
	 */
	public ABooleanSweepArea(ABooleanPredicate queryPredicate,
			ABooleanPredicate removePredicate, Comparator<ITuple> order) {
		this.removePredicate = removePredicate;
		this.order = order;		
		this.queryPredicate = queryPredicate;
	}

}
