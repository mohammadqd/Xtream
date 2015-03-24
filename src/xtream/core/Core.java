/**
 * Project: Xtream
 * Module: Core
 * Task: Core of the Xtream (mainly for executing threads)
 * Last Modify: May 2013
 * Created: 2007
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
package xtream.core;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xtream.Globals;
import xtream.Globals.Monitoring_Modes;
import xtream.core.loadshedding.FederalLoadShedder;
import xtream.interfaces.ILoadShedder;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.plr.PTRTQoS;
import xtream.structures.AggOutPort;
import xtream.structures.TxtFileOutPort;

public class Core {

	public enum ExecutionState {
		BEFORE_RUN, RUNNING, FINISHING, // Finished but waiting for threads to
										// finish
		END // All done and finished
	};

	protected AggOutPort periodicRTStat; // periodic RT statistics for total
											// results
	protected AggOutPort periodicConfStat; // periodic Conf statistics for total
											// results
	protected AggOutPort periodicTQoSStat; // periodic TQoS statistics for total
	// results
	protected boolean isOpen;
	protected long systemStartTime;
	protected long totalInputs; // to maintain statistics
	protected long totalResults;// to maintain statistics
	protected Object activeTplsSynch = new Object();
	protected Object resultsCounterSynch = new Object();
	public TxtFileOutPort traceCore = new TxtFileOutPort("TraceCore.txt");
	protected ExecutorService threadExecutor;
	protected Vector<Runnable> threadsToRun;
	protected ExecutionState execState;
	protected ILoadShedder loadShedder; // main load shedder (set in
										// RunPreprocessings())
	protected Vector<User> users; // all querying users
	protected Vector<IQuery> queries; // ref to all queries (set in
										// RunPreprocessings())
	protected long periodicExtraLoad; // byte

	/*****************************
	 * Constructor
	 */
	public Core() {
		threadsToRun = new Vector<Runnable>();
		threadExecutor = Executors.newCachedThreadPool();
		traceCore.WriteStr("CORE: CREATED");
		execState = ExecutionState.BEFORE_RUN;
		systemStartTime = System.currentTimeMillis();
		users = new Vector<User>();
		periodicRTStat = new AggOutPort("Periodic_RESULTS_RT.txt",
				Globals.MONITORING_TIME_PERIOD);
		periodicConfStat = new AggOutPort("Periodic_RESULTS_CONF.txt",
				Globals.MONITORING_TIME_PERIOD);
		periodicTQoSStat = new AggOutPort("Periodic_RESULTS_TQoS.txt",
				Globals.MONITORING_TIME_PERIOD);
		Open();
	}// constructor

	/**
	 * @param thrd
	 *            thread to run
	 * @return true:success false:failed
	 */
	public synchronized boolean AddRunnable(Runnable thrd) {
		ExecutionState state = execState;
		if (state == ExecutionState.BEFORE_RUN) // before run
			threadsToRun.add(thrd); // add it to pool
		else if (state == ExecutionState.RUNNING) // running mode
			threadExecutor.execute(thrd); // run it
		else if (state == ExecutionState.FINISHING
				|| state == ExecutionState.END)
			return false;
		return true;
	}

	/*********************************
	 * StartSimulation
	 * 
	 * @param runtime
	 *            runtime of execution (milliseconds)
	 * @param immediately
	 *            true:immdeiately finish all threads after time out false: tell
	 *            the threads to finish but let them finish themselves
	 *            (non-preemptive)
	 */
	public void Run(long runtime, boolean immediately) {
		try {
			// systemStartTime = System.currentTimeMillis();
			traceCore.WriteStr("CORE: Start Run @ " + (new Date()));
			RunPreprocessings();
			execState = ExecutionState.RUNNING;
			for (Runnable r : threadsToRun) {
				
				threadExecutor.execute(r);
			}
//			Globals.inport.run();
			while (GetSysCurTime() < runtime
					&& execState == ExecutionState.RUNNING)
				Thread.sleep(1000);
			Finish(immediately);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Finish(true); // immediately
		}
	}// startsimulation

	/**
	 * to finish core (non-preemptive)
	 * 
	 * @param immediately
	 *            true:immediately false: with delay
	 */
	public synchronized void Finish(boolean immediately) {
		traceCore.WriteStr("CORE: Total Results: " + totalResults
				+ " Total Inputs: " + totalInputs);
		traceCore.WriteStr("CORE: Finished "
				+ ((immediately) ? "Immediately" : "NotImmediately")
				+ " Runtime(sec):" + (GetSysCurTime() / 1000) + " @ "
				+ (new Date()));
		if (immediately) {
			threadExecutor.shutdownNow();
			execState = ExecutionState.END;
		} else {
			threadExecutor.shutdown();
			execState = ExecutionState.FINISHING;
		}
		Close();
	}

	public long GetSysStartTime() {
		return systemStartTime;
	}

	/*********************************
	 * RunTime get clock of simulator (from start of simulation how many
	 * milliseconds)
	 */
	public long GetSysCurTime() {
		return (System.currentTimeMillis() - systemStartTime);
	}// runtime

	/************************************
	 * 
	 * @return execution state 0: Before Run 1: Running 2: Finished but waiting
	 *         for threads to finish 3: All done and finished
	 * 
	 */
	public ExecutionState ExecState() {
		return execState;
	}

	public long NewTuple(int cnt) {
		synchronized (activeTplsSynch) {
			totalInputs += cnt;
			return totalInputs;
		}
	}

	public long GetTuplesCount() {
		return totalInputs;
	}

	/**
	 * Statistical Function for new query results
	 * 
	 * @param result
	 *            result tuple
	 * @param query
	 *            related query
	 */
	public void NewResult(ITuple result, IQuery query) {
		synchronized (resultsCounterSynch) {
			long curTime = GetSysCurTime();
			totalResults += 1;
			periodicConfStat.WriteAggValue(curTime, result.GetConf(),
					query.GetRelativeQoSWeight());
			periodicRTStat.WriteAggValue(curTime, result.GetResponseTime(),
					query.GetRelativeQoSWeight());
			periodicTQoSStat.WriteAggValue(curTime, PTRTQoS.GetTQoS(
					result.GetResponseTime(), result.GetConf(),
					query.GetRelativeQoSWeight()), 1);

		}
	}

	/**
	 * To do some preprocessings before running Xtream
	 */
	protected void RunPreprocessings() {
		queries = new Vector<IQuery>();
		for (User u : users) {
			queries.addAll(u.queries);
		}
		loadShedder = new FederalLoadShedder(
				(IQuery[]) (queries.toArray(new IQuery[0])),
				(int) Globals.PER_OPERATOR_LS_OFFERS_COUNT);

	}

	/**
	 * @param users
	 *            querying users to add
	 */
	public void AddUser(User... users) {
		for (User user : users) {
			this.users.add(user);
		}
	}

	public ILoadShedder GetLoadShedder() {
		return loadShedder;
	}

	/**
	 * To set new probability threshold for all registered queries
	 * 
	 * @param newPT
	 */
	public void SetPT(double newPT) {
		if (Globals.MONITORING_MODE == Monitoring_Modes.Partial
				|| Globals.MONITORING_MODE == Monitoring_Modes.Full)
			System.out.println("\n CORE: Set PT to " + newPT);
		for (IQuery q : queries)
			q.SetPT(newPT);
	}

	public synchronized void Exception(Throwable e) {
		// Print
		System.out.println("\n************************************");
		System.out.println(" Exception: " + e.getClass());
		System.out.println("************************************");
		e.printStackTrace();
		traceCore.WriteStr("CORE: Exception/Error: " + e.getClass() + " @ "
				+ (new Date()));

		// Process
		Finish(true);

	}

	public void Open() {
		if (!isOpen) {
			periodicConfStat.Open();
			periodicRTStat.Open();
			periodicTQoSStat.Open();
			traceCore.Open();
			isOpen = true;
		}
	}

	public void Close() {
		if (isOpen) {
			periodicConfStat.Close();
			periodicRTStat.Close();
			periodicTQoSStat.Close();
			traceCore.Close();
			loadShedder.Close();
			isOpen = false;
		}
	}
	
	public Vector<IQuery> getQueries() {
		return queries;
	}

	/**
	 * @return the periodicExtraLoad (byte)
	 */
	public synchronized long getPeriodicExtraLoad() {
		return periodicExtraLoad;
	}

	/**
	 * @param periodicExtraLoad(byte) the periodicExtraLoad to set
	 */
	public synchronized void setPeriodicExtraLoad(long periodicExtraLoad) {
		this.periodicExtraLoad = periodicExtraLoad;
	}
}
