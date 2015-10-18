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
import xtream.io.TxtFileOutPort;
import xtream.query.ABinaryJoin;
import xtream.query.AQuery;
import xtream.query.BinarySlope;
import xtream.query.Project;
import xtream.query.Select;
import xtream.query.TumblingTimeWindowCount;
import xtream.structures.ABooleanPredicate;
import xtream.structures.AFuzzyPredicate;
import xtream.structures.AProjection;
import xtream.structures.AggTuple;
import xtream.structures.FuzzyVectorSweepArea;
import xtream.structures.IAggTuple;
import xtream.structures.ITuple;
import xtream.structures.JointTuples;
import xtream.structures.TupleQueue;

/**
 * @author ghalambor
 * 
 */
public class MemoryOverloadQuery extends AQuery {

	// *
	// * S1(MemInfoSnapghot)....S2(PPos)
	// * ..........|................|
	// * ..........V................V
	// * .......(IN_Q0)..........(PRJ4)
	// * ...........................V
	// * ........................(IN_Q1)
	// * ...........................V
	// * ......(PRJ1).<==(PRJ2)<==(AGG3)
	// * ......./...\...............V
	// * ...(SEL1) (AGG1)........(AGG2)
	// * ......\...../..............V
	// * ......(JOIN1)==========>(JOIN2)
	// * ...........................|
	// * ...........................V
	// * .........................(PRJ3)
	// * ...........................|
	// * ...........................V
	// * .........................RESULT
	// * ------------------------------
	// * IN_Q0: input queue 0 for S1
	// * IN_Q1: input queue 1 for output of PRJ4 (S2)
	// * PRJ1: to change memsnapghots into AggTuple format
	// * PRJ2: to send memsnapshots to PRJ1, triggered by a time/tuple based
	// window of AGG3
	// * PRJ3: to compute and generate (OVERLOAD_TO_SHED) value
	// * PRJ4: to replace heavy PPos tuples with light AggTuples to reduce mem
	// usage
	// * SEL1: to check overload of memory based on a predefined threshold
	// * AGG1: to find the slope of used_mem
	// * AGG2: to find the slope of incoming_tuples
	// * AGG3: a time/tuple -based window aggregation to find Count(incoming
	// PPos tuples)
	// * JOIN1,JOIN2: time-based binary joins (trivial)

	/**
	 * @param name
	 *            query name
	 * @param qosWeight
	 *            QoS weight of query (>0)
	 * @param owner
	 *            owner of query
	 */
	public MemoryOverloadQuery(String name, double qosWeight, User owner) {
		super(name, qosWeight, owner);

		// ////////////////////////
		// /// SELECTs
		// ////////////////////////

		Select sel1 = new Select(new ABooleanPredicate() {
			public boolean Predicate(ITuple... tpls) {
				if (((IAggTuple) tpls[0]).getValue().longValue() > Globals.MEMORY_USE_ALERT_THRESHOLD) {
					// DEBUG - SHOW
					// System.out.println("\n NEW MEM ALERT: "
					// + ((IAggTuple) tpls[0]).getValue().longValue());
					return true;
				} else
					return false;
			}
		}, name + "_SEL1", this);

		// ////////////////////////
		// /// AGGREGATE
		// ////////////////////////
		BinarySlope agg1 = new BinarySlope(name + "_AGG1", this);

		BinarySlope agg2 = new BinarySlope(name + "_AGG2", this);

		TumblingTimeWindowCount agg3 = new TumblingTimeWindowCount(
				Globals.OVERLOAD_CHECKING_TIME_PERIOD,
				Globals.MONITORING_TUPLE_PERIOD, name + "_AGG3", this);

		// ////////////////////////
		// /// JOIN 1
		// ////////////////////////

		ABinaryJoin join1 = new ABinaryJoin(
				1 * Globals.OVERLOAD_CHECKING_TIME_PERIOD, name + "_JOIN1",
				this) {

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

		ABinaryJoin join2 = new ABinaryJoin(
				1 * Globals.OVERLOAD_CHECKING_TIME_PERIOD, name + "_JOIN2",
				this) {

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

		Project prj2 = new Project(new AProjection() { // MemInfoSnapshot =>
					// AggTuple
					public ITuple ProjectComputation(ITuple tpl) {
						MemInfoSnapshot tp = new MemInfoSnapshot();
						return tp;
					}
				}, name + "_PRJ2", this);

		Project prj3 = new Project(new AProjection() { // Final overload
					// computation
					public ITuple ProjectComputation(ITuple tpl) {
						// System.out.println("\n MemOverloadQy Output: " +
						// tpl);
						double mslp, islp;
						long um;
						JointTuples jtpl = (JointTuples) tpl;
						if (jtpl.tuples[0] instanceof JointTuples) {
							if (((AggTuple) ((JointTuples) jtpl.tuples[0]).tuples[0])
									.getValue() instanceof Double) {
								mslp = ((AggTuple) ((JointTuples) jtpl.tuples[0]).tuples[0])
										.getValue().doubleValue();
								um = ((AggTuple) ((JointTuples) jtpl.tuples[0]).tuples[1])
										.getValue().longValue();
							} else {
								mslp = ((AggTuple) ((JointTuples) jtpl.tuples[0]).tuples[1])
										.getValue().doubleValue();
								um = ((AggTuple) ((JointTuples) jtpl.tuples[0]).tuples[0])
										.getValue().longValue();
							}
							islp = ((AggTuple) jtpl.tuples[1]).getValue()
									.doubleValue();
						} else {
							if (((AggTuple) ((JointTuples) jtpl.tuples[1]).tuples[0])
									.getValue() instanceof Double) {
								mslp = ((AggTuple) ((JointTuples) jtpl.tuples[1]).tuples[0])
										.getValue().doubleValue();
								um = ((AggTuple) ((JointTuples) jtpl.tuples[1]).tuples[1])
										.getValue().longValue();
							} else {
								mslp = ((AggTuple) ((JointTuples) jtpl.tuples[1]).tuples[1])
										.getValue().doubleValue();
								um = ((AggTuple) ((JointTuples) jtpl.tuples[1]).tuples[0])
										.getValue().longValue();
							}
							islp = ((AggTuple) jtpl.tuples[0]).getValue()
									.doubleValue();
						}
						// System.out.println("MemOverloadQy Output UM: " +
						// um+" MSLP: "+mslp+" ISLP: "+islp);
						if (mslp > Globals.MEMORY_MSLP_ALERT_THRESHOLD
						/* && islp > Globals.MEMORY_ISLP_ALERT_THRESHOLD */) {
							long mtr = Globals.core.GetLoadShedder().MTR(um,
									mslp, islp);
							System.out.println("MemOverloadQy MTR: " + mtr);
							if (Globals.FEDERAL_LOADSHEDDING_IS_ACTIVE
									&& ((JointTuples) tpl).GetOldestTimestamp() > Globals.core
											.GetLoadShedder()
											.GetLastLoadSheddingTime()) {
								Globals.core.GetLoadShedder()
										.ReleaseMemory(mtr);
							}
						}
						return tpl;
					}
				}, name + "_PRJ3", this);

		Project prj4 = new Project(new AProjection() { // PPos --> AggTuple
					// computation
					public ITuple ProjectComputation(ITuple tpl) {
						return new AggTuple(new Integer(1),
								tpl.GetTimestamp()[0], 1);
						// return tpl;
					}
				}, name + "_PRJ4", this);

		// ////////////////////////
		// /// CONNECTIONs
		// ////////////////////////

		TupleQueue[] inQueue = new TupleQueue[2];
		inQueue[0] = new TupleQueue(MemInfoSnapshot.SIZE(), 10, 0);
		inQueue[1] = new TupleQueue(AggTuple.SIZE(), 10, 0);

		TxtFileOutPort finalResults = new TxtFileOutPort(name
				+ "_Detected_Overload.txt");

		sel1.AddInPort(prj1);
		sel1.AddOutPort(join1, 0);

		prj1.AddInPort(inQueue[0]);
		prj1.AddInPort(prj2);
		prj1.AddOutPort(sel1);
		prj1.AddOutPort(agg1);
		prj2.AddInPort(agg3);
		prj2.AddOutPort(prj1);
		prj3.AddInPort(join2);
		prj3.AddOutPort(finalResults);
		prj4.AddOutPort(inQueue[1]);

		join1.AddOutPort(join2, 0);
		join1.AddInPort(sel1, 0);
		join1.AddInPort(agg1, 1);
		join2.AddOutPort(prj3);
		join2.AddInPort(join1, 0);
		join2.AddInPort(agg2, 1);

		agg1.AddInPort(prj1);
		agg1.AddOutPort(join1, 1);
		agg3.AddInPort(inQueue[1]);
		agg3.AddOutPort(agg2);
		agg3.AddOutPort(prj2);
		agg2.AddInPort(agg3);
		agg2.AddOutPort(join2, 1);

		AddInPort(inQueue[0], 0);
		// AddInPort(inQueue[1], 1);
		AddInPort(prj4, 1);
		AddOperators(sel1, prj1, prj2, prj3, prj4, join1, join2, agg1, agg2,
				agg3);
		AddOutPort(finalResults, 0);
		SetRootOperator(prj3);
		AddLeafOperators(/* prj1, */agg3);

		root.SetPT(0);
	}
}
