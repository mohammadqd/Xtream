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
package xtream.plr;

import xtream.core.User;
import xtream.io.TxtFileOutPort;
import xtream.query.AQuery;
import xtream.query.Project;
import xtream.query.Select;
import xtream.structures.AProjection;
import xtream.structures.ABooleanPredicate;
import xtream.structures.ITuple;
import xtream.structures.TupleQueue;

/**
 * @author ghalambor
 * 
 */
public class Query1 extends AQuery {

	/**
	 * Pos => (SELECT) => (PROJECT)=>
	 * 
	 * @param qosWeight
	 *            QoS weight of query (>0)
	 * @param owner
	 *            owner of query
	 * */
	public Query1(String name, double qosWeight, User owner) {
		super(name,qosWeight, owner);

		// ========== CREATING Query Plans =====
		Select sel1 = new Select(new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) {
				int vid = ((PPos) tpls[0]).vid;
				return true;// (vid < 5);
			}
		}, name + "SEL1",this);

		Project prj1 = new Project(new AProjection() {
			public ITuple ProjectComputation(ITuple tpl) {
				PPos oldtpl = (PPos) tpl;
				PPos newtp = new PPos(oldtpl.sysInTS) {
					public String toString() {
						String outstr = "VID=" + vid;
						return outstr;
					}
				};
				newtp.vid = oldtpl.vid;
				return oldtpl; // TODO DEBUG
			}
		}, name + "PRJ1",this);

		TupleQueue inQueue = new TupleQueue(PPos.GetPPosSize(), 10, 0);
		TxtFileOutPort finalResults = new TxtFileOutPort("Q_" + name
				+ "_finalResults.txt");
		prj1.AddOutPort(finalResults, 0);
		prj1.AddInPort(sel1, 0);
		sel1.AddOutPort(prj1, 0);
		sel1.AddInPort(inQueue, 0);

		AddInPort(inQueue, 0);
		AddOperators(sel1, prj1);
		AddOutPort(finalResults, 0);
		SetRootOperator(prj1);
		AddLeafOperators(sel1);

	}

}
