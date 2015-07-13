package xtream.plr;

import xtream.Globals;
import xtream.core.User;
import xtream.core.commonconfig.CommonConfig;
import xtream.interfaces.ITuple;
import xtream.query.AQuery;
import xtream.query.ASelfJoin;
import xtream.structures.ABooleanPredicate;
import xtream.structures.AFuzzyPredicate;
import xtream.structures.TupleQueue;
import xtream.structures.TxtFileOutPort;
import xtream.structures.FuzzyVectorSweepArea;

/**
 * @author ghalambor
 * 
 */
public class Query2 extends AQuery {

	/**
	 * @param name query name
	 * => (Position SELF-JOIN) =>
	 * @param qosWeight QoS weight of query (>0)
	 * @param owner owner of query
	 */
	public Query2(String name, double qosWeight, User owner) {
		super(name,qosWeight,owner);
		ASelfJoin join1 = new ASelfJoin(Globals.MAX_JOIN_TIME_DIFFERENCE,name+"Join",this) {

			@Override
			protected void CreateSynopsis() {
				synopsis = new FuzzyVectorSweepArea(new AFuzzyPredicate() {
					public double Predicate(ITuple... tpls) {
						if (((PPos) tpls[0]).vid != ((PPos) tpls[1]).vid)
							return 0f;
						else
							return (((PPos) tpls[0]).position.Similiar(
									((PPos) tpls[1]).position,
									Globals.DEFAULT_POS_RESOLUTION));
					}
				}, new ABooleanPredicate() {
					public boolean Predicate(ITuple... tpls) { // remove older
																// tuples
																// tpls[1] is
																// new tuple
						if (((PPos) tpls[1]).time - ((PPos) tpls[0]).time > timeWindowSize)
							return true;
						else
							return false;
					}
				}, null);
			}
		};

		TupleQueue inQueue = new TupleQueue(PPos.GetPPosSize(), 10, 0);
		TxtFileOutPort finalResults = new TxtFileOutPort("Q_" + name
				+ "_finalResults.txt");
		join1.AddOutPort(finalResults, 0);
		join1.AddInPort(inQueue, 0);

		AddInPort(inQueue, 0);
		AddOperators(join1);
		AddOutPort(finalResults, 0);
		SetRootOperator(join1);
		AddLeafOperators(join1);
		
		root.SetPT(CommonConfig.GetConfigDoubleItem("DEFAULT_PROBABILITY_THRESHOLD"));
	}

}
