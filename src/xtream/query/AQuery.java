/**
 * Project: Xtream
 * Module: Abstract Query
 * Task: base for CQueries
 * Last Modify: Jul 18, 2015 (revise and documentation)
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
package xtream.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import xtream.Globals;
import xtream.Globals.Monitoring_Modes;
import xtream.core.Core.ExecutionState;
import xtream.core.loadshedding.ILSStore;
import xtream.core.loadshedding.LSOffer;
import xtream.core.log.XLogger;
import xtream.core.log.XLogger.SeverityLevel;
import xtream.core.User;
import xtream.io.IInPort;
import xtream.io.IOutPort;
import xtream.plr.PTRTQoS;
import xtream.structures.AProjection;
import xtream.structures.AggTuple;
import xtream.structures.IQoS;
import xtream.structures.ITuple;
import xtream.structures.QueryStatisticsTuple;

/**
 * @author ghalambor
 * 
 */
public abstract class AQuery extends Thread implements IQuery, ILSStore {

	protected IOperator root; // query root operator (top and last operator)
	protected List<IOperator> operators; // all query operators
	protected Vector<IOutPort> outPorts; // out ports for final results
	protected Vector<IOutPort> outStatisticsPorts; // out ports for statistical
													// results
	protected Vector<IInPort> inPorts; // in ports to get input tuples
	protected Vector<IOperator> leafOperators; // leaf operators which firstly
												// get input tuples
	protected boolean isOpen;
	protected double currentART; // last computed average response time
	protected double currentPT; // last set probability-threshold
	protected double qosWeight; // QoS weight of query (>0)
	protected User owner;
	protected String qname; // query name
	protected MeanRTAgg rtAgg; // to compute response-time aggregate of output
								// tuples
	protected Project statisticsPrj; // project operator to compute internal
										// aggregates
	protected long totalResults; // to hold total number of results (for log)

	/**
	 * @param qosWeight
	 *            QoS weight of query (>0)
	 * @param owner
	 *            owner of query
	 * @param qname
	 *            query name
	 */
	public AQuery(String qname, double qosWeight, User owner) {
		this.qname = qname;
		operators = new ArrayList<IOperator>(1);
		outPorts = new Vector<IOutPort>(1);
		outStatisticsPorts = new Vector<IOutPort>();
		inPorts = new Vector<IInPort>(1);
		leafOperators = new Vector<IOperator>(1);
		isOpen = false;
		currentART = 0;
		currentPT = 0;
		this.qosWeight = qosWeight;
		this.owner = owner;
		totalResults = 0;

		// Creating Statistics Internal Query

		// ////////////////////////
		// /// AGGREGATE
		// ////////////////////////
		rtAgg = new MeanRTAgg(Globals.OVERLOAD_CHECKING_TIME_PERIOD,
				Globals.MONITORING_TUPLE_PERIOD, qname + "_RTAGG(internal)",
				this);

		// ////////////////////////
		// /// PROJECTs
		// ////////////////////////

		statisticsPrj = new Project(new AProjection() {
			public ITuple ProjectComputation(ITuple tpl) {
				AggTuple tp = (AggTuple) tpl;
				SetART(tp.getValue().doubleValue());
				PTRTQoS newQoS = new PTRTQoS(GetART(), GetPT(), GetQoSWeight());
				QueryStatisticsTuple qst = new QueryStatisticsTuple(GetQuery(),
						newQoS, 1.0);
				return qst;
			}
		}, qname + "_STATISTICSPRJ(internal)", this);

		// ////////////////////////
		// /// CONNECTIONs
		// ////////////////////////

		rtAgg.AddOutPort(statisticsPrj, 0);
		statisticsPrj.AddInPort(rtAgg);
		for (IOutPort p : outStatisticsPorts)
			statisticsPrj.AddOutPort(p);
		AddOperators(statisticsPrj, rtAgg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * xtream.interfaces.IQuery#SetRootOperator(xtream.interfaces.IOperator)
	 */
	@Override
	public IOperator SetRootOperator(IOperator op) {
		root = op;
		op.SetAsRootOP();
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#AddOperator(xtream.interfaces.IOperator)
	 */
	@Override
	public void AddOperators(IOperator... op) {
		for (int i = 0; i < op.length; i++) {
			operators.add(op[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#AddOutPort(xtream.interfaces.IOutPort, int)
	 */
	@Override
	public void AddOutPort(IOutPort p, int index) {
		outPorts.add(index, p);
	}

	@Override
	public void AddInPort(IInPort p, int index) {
		inPorts.add(index, p);
	}

	@Override
	public void AddLeafOperators(IOperator... op) {
		for (int i = 0; i < op.length; i++)
			leafOperators.add(op[i]);
	}

	@Override
	public void run() {
		try {
			if (!isOpen)
				Open();
			while (Globals.core.ExecState() == ExecutionState.RUNNING) { // while
																			// running
				for (IOperator op : leafOperators) {
					op.run(Globals.OpExeTimeSlice);
				}
				sleep(Globals.OpExeTimeSlice); // OPTIONAL to prevent running
												// empty loops (very effective!)
			}
		} catch (InterruptedException e) {
			// do nothing
		} catch (Throwable e) {
			Globals.core.Exception(e);
		} finally {
			Close();
		}
	}

	@Override
	public IInPort GetInPort(int index) {
		return inPorts.get(index);
	}

	@Override
	public IOutPort GetOutPort(int index) {
		return outPorts.get(index);
	}

	@Override
	public void Open() {
		if (!isOpen) {
			for (IOutPort it : outStatisticsPorts)
				it.Open();
			for (IOutPort it : outPorts)
				it.Open();
			for (IInPort it : inPorts)
				it.Open();
			for (IOperator it : operators)
				it.Open();
			// root.Open();
			isOpen = true;
		}
	}

	@Override
	public void Close() {
		if (isOpen) {
			for (IOutPort it : outStatisticsPorts)
				it.Close();
			for (IOutPort it : outPorts)
				it.Close();
			for (IInPort it : inPorts)
				it.Close();
			for (IOperator it : operators)
				it.Close();
			// root.Close();
			isOpen = false;
			// DEBUG
			XLogger.Log("QUERY", "Query: " + qname + " Total Results: "
					+ totalResults, SeverityLevel.DEBUG);
		}
	}

	@Override
	public int InPortsCount() {
		return inPorts.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#SetPT(double)
	 */
	@Override
	public double SetPT(double pt) {
		assert pt >= 0 && pt <= 1;
		if (root != null) {
			currentPT = root.SetPT(pt);
		} else
			currentPT = pt;
		return currentPT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#GetPT()
	 */
	@Override
	public double GetPT() {
		return currentPT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#SetART(double)
	 */
	@Override
	public double SetART(double art) {
		assert art >= 0;
		currentART = art;
		return currentART;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#GetART()
	 */
	@Override
	public double GetART() {
		return currentART;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#GetQoS()
	 */
	@Override
	public IQoS GetQoS() {
		PTRTQoS qos = new PTRTQoS(currentART, currentPT, qosWeight);
		return qos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#GetWeight()
	 */
	@Override
	public double GetQoSWeight() {
		return qosWeight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#GetRelativeQoSWeight()
	 */
	@Override
	public double GetRelativeQoSWeight() {
		return qosWeight / owner.GetTotalQoSWeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#GetOwner()
	 */
	@Override
	public User GetOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * xtream.interfaces.IQuery#AddQueryStatisticsOutPort(xtream.interfaces.
	 * IIOPort, int)
	 */
	@Override
	public void AddQueryStatisticsOutPort(IOutPort p) {
		outStatisticsPorts.add(p);
		statisticsPrj.AddOutPort(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IQuery#GetName()
	 */
	@Override
	public String GetName() {
		return qname;
	}

	/**
	 * All result tuples of queries should be checked (to update query
	 * statistics) when ready to send out
	 * 
	 * @param tpl
	 *            result tuple to check
	 */
	public void CheckResultTuple(ITuple tpl) {
		try {
			tpl.SetResponseTime(Globals.core.GetSysCurTime()
					- tpl.GetTimestamp()[0]);
			rtAgg.PutTuple(tpl, 0);
			totalResults++;
			if (!owner.isSystemUser())
				Globals.core.NewResult(tpl, this);
		} catch (IOException e) {
			XLogger.Log(
					"AQuery",
					"Exception in xtream.query.AQuery.CheckResultTuple: "
							+ e.getMessage(), SeverityLevel.ERROR);
		}
	}

	/**
	 * @return ref to current query
	 */
	public IQuery GetQuery() {
		return this;
	}

	public void LSCommand(LSOffer offer) {
		SetPT(offer.getNewPT());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILSStore#getLSOffers(double[])
	 */
	@Override
	public LSOffer[] getLSOffers(double[] newPTs) {
		if (root != null && root instanceof ILSStore) {
			LSOffer[] offers = ((ILSStore) root).getLSOffers(newPTs);
			for (LSOffer offer : offers) {
				offer.SetCosts();
			}
			return offers;
		} else
			return null;
	}
}
