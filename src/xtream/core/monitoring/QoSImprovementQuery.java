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
package xtream.core.monitoring;

import xtream.Globals;
import xtream.core.User;
import xtream.interfaces.IAggTuple;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.query.ABinaryJoin;
import xtream.query.AQuery;
import xtream.query.BinarySlope;
import xtream.query.MaxWindowAgg;
import xtream.query.Project;
import xtream.query.Select;
import xtream.structures.ABooleanPredicate;
import xtream.structures.AFuzzyPredicate;
import xtream.structures.AProjection;
import xtream.structures.AggTuple;
import xtream.structures.FuzzyVectorSweepArea;
import xtream.structures.JointTuples;
import xtream.structures.QueryStatisticsTuple;
import xtream.structures.STDOutPort;
import xtream.structures.TupleQueue;
import xtream.structures.TxtFileOutPort;

/**
 * @author ghalambor
 * 
 */
public class QoSImprovementQuery extends AQuery {

	// *
	// * S1(MemInfoSnapghot)....S2(QueryStatisticsTuples)
	// * ........|..................|....................
	// * ........V..................V....................
	// * ......(PRJ1).............(SEL2).................
	// * ......./...\...............V....................
	// * ...(SEL1) (AGG1)........(AGG2)..................
	// * .......|.....|.............|....................
	// * .......|.....V.............|....................
	// * .......|..(SEL3)...........|....................
	// * .......|...|...............|....................
	// * .......V...V...............V....................
	// * ......(JOIN1)==========>(JOIN2).................
	// * ...........................|....................
	// * ...........................V....................
	// * .........................(PRJ2).................
	// * ...........................|....................
	// * ...........................V....................
	// * .........................RESULT.................
	// * ------------------------------
	// * PRJ1: to change memsnapghots into AggTuple format
	// * PRJ2: to compute final result and invoke QoS improver
	// * SEL1: to check if used_mem < M_low (MEMORY_NORMAL_USE_THRESHOLD)
	// * SEL2: to check if query_qos_weight*(1-QQoS) > Q_DMG
	// * SEL3: to check if mslp < MSLP_LOW  (slope of used mem is lower than a threshold)
	// * AGG1: to find the slope of used_mem
	// * AGG2: to find the query witch has worst effect of TQoS 
	// * JOIN1,JOIN2: time-based binary joins (trivial)
	
	/**
	 * @param name
	 * @param qosWeight
	 * @param owner
	 */
	public QoSImprovementQuery(String name, double qosWeight, User owner) {
		super(name, qosWeight, owner);

		// ////////////////////////
		// /// SELECTs
		// ////////////////////////

		Select sel1 = new Select(new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) {
				if (((IAggTuple) tpls[0]).getValue().longValue() < Globals.MEMORY_NORMAL_USE_THRESHOLD) 
				{
					// DEBUG - SHOW
//					 System.out.println("\n NEW MEM SIZE ALERT: "
//					 + ((IAggTuple) tpls[0]).getValue().longValue());
					return true;
				} else
					return false;
			}
		}, name + "_SEL1", this);

		Select sel2 = new Select(new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) {
				if (((IAggTuple) tpls[0]).getValue().doubleValue() > Globals.Q_DMG) 
				{
					// DEBUG - SHOW
//					 System.out.println("\n NEW Q_DMG ALERT: "
//					 + ((IAggTuple) tpls[0]).getValue().doubleValue());
					return true;
				} else
					return false;
			}
		}, name + "_SEL2", this);

		Select sel3 = new Select(new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) {
				if (((IAggTuple) tpls[0]).getValue().doubleValue() < Globals.MSLP_LOW) {
					// DEBUG - SHOW
//					 System.out.println("\n NEW MEM SLOPE ALERT: "
//					 + ((IAggTuple) tpls[0]).getValue().doubleValue());
					return true;
				} else
					return false;
			}
		}, name + "_SEL3", this);

		// ////////////////////////
		// /// AGGREGATE
		// ////////////////////////
		BinarySlope agg1 = new BinarySlope(name + "_AGG1", this);

		MaxWindowAgg agg2 = new MaxWindowAgg(Globals.OVERLOAD_CHECKING_TIME_PERIOD,
				Globals.MONITORING_TUPLE_PERIOD, name + "_AGG2", this);

		// ////////////////////////
		// /// JOIN 1
		// ////////////////////////

		ABinaryJoin join1 = new ABinaryJoin(Globals.OVERLOAD_CHECKING_TIME_PERIOD/2,
				name + "_JOIN1", this) {

			@Override
			protected void CreateSynopses() {
				synopses[0] = new FuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						return 1;
					}
				}, DefaultRemovePredicate, null);

				synopses[1] = new FuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						return 1;
					}
				}, DefaultRemovePredicate, null);

			}
		};

		// ////////////////////////
		// /// JOIN 2
		// ////////////////////////

		ABinaryJoin join2 = new ABinaryJoin(Globals.OVERLOAD_CHECKING_TIME_PERIOD/2,
				name + "_JOIN2", this) {

			@Override
			protected void CreateSynopses() {
				synopses[0] = new FuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						return 1;
					}
				}, DefaultRemovePredicate, null);

				synopses[1] = new FuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						return 1;
					}
				}, DefaultRemovePredicate, null);

			}
		};

		// ////////////////////////
		// /// PROJECTs
		// ////////////////////////

		Project prj1 = new Project(new AProjection() { // MemInfoSnapshot =>
														// AggTuple
					public ITuple ProjectComputation(ITuple tpl) {
						MemInfoSnapshot tp = (MemInfoSnapshot) tpl;
						AggTuple aggTpl = new AggTuple(tp.usedMem, tp.GetConf());
						// DEBUG
						// System.out.println("\n"+aggTpl);
						return aggTpl;
					}
				}, name + "_PRJ1", this);

		Project prj2 = new Project(new AProjection() {
			public ITuple ProjectComputation(ITuple tpl) {
				QueryStatisticsTuple queryToImprove;
				JointTuples jtpl = (JointTuples) tpl;
				if (jtpl.tuples[0] instanceof JointTuples) {
					queryToImprove = ((QueryStatisticsTuple)jtpl.tuples[1]);
				} else {
					queryToImprove = ((QueryStatisticsTuple)jtpl.tuples[0]);
				}				
				System.out.println("\nRequest for improving: "+queryToImprove);
				Globals.core.GetLoadShedder().QueryQoSImprove(queryToImprove.getQuery());
				return tpl;
			}
		}, name + "_PRJ2", this);

		// ////////////////////////
		// /// CONNECTIONs
		// ////////////////////////

		TupleQueue[] inQueue = new TupleQueue[2];
		inQueue[0] = new TupleQueue(MemInfoSnapshot.SIZE(), 10, 0);
		inQueue[1] = new TupleQueue(0, 10, 0);
		
		TxtFileOutPort finalResults = new TxtFileOutPort(name
				+ "_QoS_Improvement.txt");
		
		// DEBUG
//		agg2.AddOutPort(new STDOutPort());

		sel1.AddInPort(prj1);
		sel1.AddOutPort(join1, 0);
		sel2.AddInPort(inQueue[1]);
		sel2.AddOutPort(agg2);
		sel3.AddInPort(agg1);
		sel3.AddOutPort(join1, 1);

		prj1.AddInPort(inQueue[0]);
		prj1.AddOutPort(sel1);
		prj1.AddOutPort(agg1);
		prj2.AddInPort(join2);
		prj2.AddOutPort(finalResults);

		join1.AddOutPort(join2, 0);
		join1.AddInPort(sel1, 0);
		join1.AddInPort(sel3, 1);
		join2.AddOutPort(prj2);
		join2.AddInPort(join1, 0);
		join2.AddInPort(agg2, 1);

		agg1.AddInPort(prj1);
		agg1.AddOutPort(sel3);
		agg2.AddInPort(sel2);
		agg2.AddOutPort(join2, 1);

		AddInPort(inQueue[0], 0);
		AddInPort(inQueue[1], 1);
		AddOperators(sel1, sel2, sel3, prj1, prj2, join1, join2, agg1, agg2);
		AddOutPort(finalResults, 0);
		SetRootOperator(prj2);
		AddLeafOperators(prj1, sel2);

		SetPT(0);
	}

}
