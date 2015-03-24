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

import xtream.Globals;
import xtream.Globals.LSRMType;
import xtream.core.User;
import xtream.core.loadshedding.LSFuzzyVectorSweepArea;
import xtream.core.loadshedding.LSTupleQueue;
import xtream.interfaces.IInPort;
import xtream.interfaces.IOperator;
import xtream.interfaces.IQueue;
import xtream.interfaces.ITuple;
import xtream.lsrm.ILSRMOP;
import xtream.lsrm.RandomDrop;
import xtream.query.ABinaryJoin;
import xtream.query.AOperator;
import xtream.query.AQuery;
import xtream.query.ASelfJoin;
import xtream.query.ProbCounter;
import xtream.query.Project;
import xtream.query.Select;
import xtream.structures.ABooleanPredicate;
import xtream.structures.AFuzzyPredicate;
import xtream.structures.AProjection;
import xtream.structures.JointTuples;
import xtream.structures.TxtFileOutPort;

/**
 * @author ghalambor
 * 
 */
public class Query3 extends AQuery {

	// *
	// * .....S1(PPos).....S2(PPos).................
	// * ........|.............|....................
	// * ........V.............V....................
	// * ......(SEL1)........(SEL2).................
	// * ........|.............|....................
	// * ........V.............V....................
	// * ......(PRJ1)........(PRJ2).................
	// * ........|.............|....................
	// * ........V.............V....................
	// * ......(JOIN1).......(JOIN2)................
	// * ........\............./....................
	// * .........\.........../.....................
	// * ..........\........./......................
	// * ............(JOIN3)........................
	// * ...............|...........................
	// * ...............V...........................
	// * .............RESULT........................
	// * -------------------------------------------
	// * PRJ1: to remove extra load
	// * PRJ2: to remove extra load
	// * SEL1: to check dir % 2 == 0
	// * SEL2: to check dir % 2 != 0
	// * JOIN1,JOIN2: self-joins based on position similarity of different
	// vehicles
	// * JOIN3: binary-joins based on position similarity of different vehicles

	/**
	 * @param name
	 *            query name => (Position SELF-JOIN) =>
	 * @param qosWeight
	 *            QoS weight of query (>0)
	 * @param owner
	 *            owner of query
	 */
	public Query3(String name, double qosWeight, User owner) {
		super(name, qosWeight, owner);

		// ////////////////////////
		// /// JOIN 1
		// ////////////////////////

		ASelfJoin join1 = new ASelfJoin(Globals.MAX_JOIN_TIME_DIFFERENCE, name
				+ "_JOIN1", this) {

			@Override
			protected void CreateSynopsis() {
				synopsis = new LSFuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						if (((PPos) tpls[0]).vid == ((PPos) tpls[1]).vid)
							return 0f;
						else
							return (((PPos) tpls[0]).position.Similiar(
									((PPos) tpls[1]).position,
									Globals.DEFAULT_POS_RESOLUTION));
					}
				}, DefaultRemovePredicate, null);
			}
		};

		// ////////////////////////
		// /// JOIN 2
		// ////////////////////////

		ASelfJoin join2 = new ASelfJoin(Globals.MAX_JOIN_TIME_DIFFERENCE, name
				+ "_JOIN2", this) {

			@Override
			protected void CreateSynopsis() {
				synopsis = new LSFuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						if (((PPos) tpls[0]).vid == ((PPos) tpls[1]).vid)
							return 0f;
						else
							return (((PPos) tpls[0]).position.Similiar(
									((PPos) tpls[1]).position,
									Globals.DEFAULT_POS_RESOLUTION));
					}
				}, DefaultRemovePredicate, null);
			}
		};

		// ////////////////////////
		// /// JOIN 3
		// ////////////////////////

		ABinaryJoin join3 = new ABinaryJoin(Globals.MAX_JOIN_TIME_DIFFERENCE,
				name + "_JOIN3", this) {

			@Override
			protected void CreateSynopses() {
				synopses[0] = new LSFuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						double resolution = Globals.DEFAULT_POS_RESOLUTION;
						PPos[][] poses = new PPos[2][2];
						poses[0][0] = (PPos) ((JointTuples) tpls[0]).tuples[0];
						poses[0][1] = (PPos) ((JointTuples) tpls[0]).tuples[1];
						poses[1][0] = (PPos) ((JointTuples) tpls[1]).tuples[0];
						poses[1][1] = (PPos) ((JointTuples) tpls[1]).tuples[1];
						double[] confs = { 0d, 0d, 0d, 0d };
						confs[0] = poses[0][0].position.Similiar(
								poses[1][0].position, resolution);
						confs[1] = poses[0][0].position.Similiar(
								poses[1][1].position, resolution);
						confs[2] = poses[0][1].position.Similiar(
								poses[1][0].position, resolution);
						confs[3] = poses[0][1].position.Similiar(
								poses[1][1].position, resolution);
						return (Math.max(Math.max(confs[0], confs[1]),
								Math.max(confs[2], confs[3])));
					}
				}, DefaultRemovePredicate, null);

				synopses[1] = new LSFuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						double resolution = Globals.DEFAULT_POS_RESOLUTION;
						PPos[][] poses = new PPos[2][2];
						poses[0][0] = (PPos) ((JointTuples) tpls[0]).tuples[0];
						poses[0][1] = (PPos) ((JointTuples) tpls[0]).tuples[1];
						poses[1][0] = (PPos) ((JointTuples) tpls[1]).tuples[0];
						poses[1][1] = (PPos) ((JointTuples) tpls[1]).tuples[1];
						double[] confs = { 0d, 0d, 0d, 0d };
						confs[0] = poses[0][0].position.Similiar(
								poses[1][0].position, resolution);
						confs[1] = poses[0][0].position.Similiar(
								poses[1][1].position, resolution);
						confs[2] = poses[0][1].position.Similiar(
								poses[1][0].position, resolution);
						confs[3] = poses[0][1].position.Similiar(
								poses[1][1].position, resolution);
						return (Math.max(Math.max(confs[0], confs[1]),
								Math.max(confs[2], confs[3])));
					}
				}, DefaultRemovePredicate, null);

			}
		};

		// ////////////////////////
		// /// SELECTs
		// ////////////////////////

		Select sel1 = new Select(new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) {
				// int vid = ((PPos) tpls[0]).vid;
				int dir = ((PPos) tpls[0]).dir;
				return (dir % 2 == 0);
			}
		}, name + "SEL1", this);

		Select sel2 = new Select(new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) {
				// int vid = ((PPos) tpls[0]).vid;
				int dir = ((PPos) tpls[0]).dir;
				return (dir % 2 == 1);
			}
		}, name + "SEL2", this);

		// ////////////////////////
		// /// PROJECTs
		// ////////////////////////

		Project prj1 = new Project(new AProjection() { // removes extraLoad
					public ITuple ProjectComputation(ITuple tpl) {
						// PPos newtp = (PPos) tpl.Clone();
						// newtp.extraLoad = null; // release extra load
						// return newtp;
						return tpl;
					}
				}, name + "PRJ1", this);

		Project prj2 = new Project(new AProjection() { // removes extraLoad
					public ITuple ProjectComputation(ITuple tpl) {
						// PPos newtp = (PPos) tpl.Clone();
						// newtp.extraLoad = null; // release extra load
						// return newtp;
						return tpl;
					}

				}, name + "PRJ2", this);

		// ////////////////////////
		// /// LSRM DROP OPERATORS
		// ////////////////////////
		AOperator[] dropOP = new AOperator[2];
		if (Globals.LSRM_TYPE == LSRMType.Random) {
			dropOP[0] = new RandomDrop(name+"_RndDrop1", this, 0);
			dropOP[1] = new RandomDrop(name+"_RndDrop2", this, 0);
			Globals.dropOperators.add((ILSRMOP)dropOP[0]);
			Globals.dropOperators.add((ILSRMOP)dropOP[1]);
		}

		// ////////////////////////
		// /// CONNECTIONs
		// ////////////////////////

		LSTupleQueue[] inQueue = new LSTupleQueue[2];
		inQueue[0] = new LSTupleQueue(this);
		inQueue[1] = new LSTupleQueue(this);

		ProbCounter prbcnt = new ProbCounter(name + "_PRBCNT", this);

		TxtFileOutPort finalResults = new TxtFileOutPort(null);//name
//				+ "_finalResults.txt");

		sel1.AddInPort(inQueue[0], 0);
		sel2.AddInPort(inQueue[1], 0);
		
		if (Globals.LSRM_TYPE == LSRMType.Disable) {
			sel1.AddOutPort(prj1, 0);
			sel2.AddOutPort(prj2, 0);
			prj1.AddInPort(sel1, 0);
			prj2.AddInPort(sel2, 0);

		} else { // active LSRM
			sel1.AddOutPort(dropOP[0], 0);
			sel2.AddOutPort(dropOP[1], 0);
			prj1.AddInPort(dropOP[0], 0);
			prj2.AddInPort(dropOP[1], 0);
			dropOP[0].AddInPort(sel1, 0);
			dropOP[0].AddOutPort(prj1, 0);
			dropOP[1].AddInPort(sel2, 0);
			dropOP[1].AddOutPort(prj2, 0);
			AddOperators(dropOP[0],dropOP[1]);
		}

		prj1.AddOutPort(join1, 0);
		prj2.AddOutPort(join2, 0);
		join1.AddOutPort(prbcnt);

		join1.AddOutPort(join3, 0);
		join1.AddInPort(prj1, 0);
		join2.AddOutPort(join3, 1);
		join2.AddInPort(prj2, 0);
		join3.AddOutPort(finalResults, 0);
		join3.AddInPort(join1, 0);
		join3.AddInPort(join2, 1);
		
		join1.AddOutPort(prbcnt);

		AddInPort(inQueue[0], 0);
		AddInPort(inQueue[1], 1);
		AddOperators(sel1, sel2, prj1, prj2, join1, join2, join3, prbcnt);
		AddOutPort(finalResults, 0);
		SetRootOperator(join3);
		AddLeafOperators(sel1, sel2);

		SetPT(Globals.PROBABILITY_THRESHOLD);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.query.AQuery#Close()
	 */
	@Override
	public void Close() {
		for (IInPort inp : inPorts) {
			IQueue q = (IQueue) inp;
			if (q.GetCount() > 0)
				System.out.println("\n Tuples in input queue of Query3: "
						+ q.GetCount());

		}
		super.Close();
	}

}
