/**
 * Project: Xtream
 * Module: ???
 * Task: ???
 * Last Modify: Sep 10, 2015
 * Created: Sep 10, 2015
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

import xtream.core.User;
import xtream.io.IInPort;
import xtream.io.IOutPort;
import xtream.structures.ABooleanPredicate;
import xtream.structures.TupleQueue;

public class SimpleSelectQuery extends AQuery {

	public SimpleSelectQuery(String qname, double qosWeight, User owner, ABooleanPredicate selectPredicate, IOutPort... outPorts) {
		super(qname, qosWeight, owner);

		// ========== CREATING Query Plans =====
		Select selectOP = new Select(selectPredicate, qname + "_SELECTOP",this);

		TupleQueue inQueue = new TupleQueue(100, 10, 0);
		selectOP.AddInPort(inQueue, 0);
		int i = 0;
		for (IOutPort outp : outPorts) 
		{
			selectOP.AddOutPort(outp, 0);
			AddOutPort(outp, i);
			i++;
		}
		AddInPort(inQueue, 0);
		AddOperators(selectOP);
		SetRootOperator(selectOP);
		AddLeafOperators(selectOP);
	}

}
