/**
 * Project: Xtream
 * Module: Core
 * Task: Core of the Xtream (mainly for executing threads)
 * Last Modify: Jul 13 2015 (Xlog and CommonConfig and more comments)
 * Modify: 2013 (Threading and Statistics support)
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
import xtream.core.commonconfig.CommonConfig;
import xtream.core.loadshedding.FederalLoadShedder;
import xtream.core.log.XLogger;
import xtream.core.log.XLogger.SeverityLevel;
import xtream.interfaces.ILoadShedder;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.plr.PTRTQoS;
import xtream.structures.AggOutPort;
import xtream.structures.TxtFileOutPort;

/**
 * Core of running Xtream
 */
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
	// [DEPRECATED] public TxtFileOutPort traceCore = new TxtFileOutPort("TraceCore.txt");
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
		XLogger.Log("CORE", "Creating Xtream Core...", SeverityLevel.INFO);
		execState = ExecutionState.BEFORE_RUN;
		systemStartTime = System.currentTimeMillis();
		users = new Vector<User>();
		periodicRTStat = new AggOutPort("Periodic_RESULTS_RT.txt",
				CommonConfig.GetConfigIntItem("MONITORING_TIME_PERIOD"));
		periodicConfStat = new AggOutPort("Periodic_RESULTS_CONF.txt",
				CommonConfig.GetConfigIntItem("MONITORING_TIME_PERIOD"));
		periodicTQoSStat = new AggOutPort("Periodic_RESULTS_TQoS.txt",
				CommonConfig.GetConfigIntItem("MONITORING_TIME_PERIOD"));
		Open(); // open core (io ports)
	}// constructor

	/**
	 * to add a thread to be run by core
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

	/**
	 * Run Core!
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
			systemStartTime = System.currentTimeMillis();
			XLogger.Log("CORE", "Start Running Core", SeverityLevel.INFO);

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
			XLogger.Log("CORE", "Exception in Core.Run(): " + e.getMessage(), SeverityLevel.ERROR);
			Finish(true); // immediately
		}
	}// run

	/**
	 * to finish core (non-preemptive)
	 * 
	 * @param immediately
	 *            true:immediately false: with delay
	 */
	public synchronized void Finish(boolean immediately) {
		XLogger.Log("CORE", "Total Results: "+ totalResults
				+ " Total Inputs: " + totalInputs, SeverityLevel.INFO);
		XLogger.Log("CORE", "Core Finished "
				+ ((immediately) ? "Immediately" : "NotImmediately")
				+ " Runtime(sec):" + (GetSysCurTime() / 1000) + " @ "
				+ (new Date()),SeverityLevel.INFO);
		if (immediately) {
			threadExecutor.shutdownNow();
			execState = ExecutionState.END;
		} else {
			threadExecutor.shutdown();
			execState = ExecutionState.FINISHING;
		}
		Close(); // close core (io ports)
	}

	public long GetSysStartTime() {
		return systemStartTime;
	}

	/**
	 * @return execution time (duration of execution)
	 */
	public long GetSysCurTime() {
		return (System.currentTimeMillis() - systemStartTime);
	}// runtime

	/**
	 * 
	 * @return execution state 0: Before Run 1: Running 2: Finished but waiting
	 *         for threads to finish 3: All done and finished
	 * 
	 */
	public ExecutionState ExecState() {
		return execState;
	}

	/**
	 * To increase statistics about total number of tuples
	 * @param cnt count of new tuples
	 * @return total number of tuples
	 */
	public long NewTuple(int cnt) {
		synchronized (activeTplsSynch) {
			totalInputs += cnt;
			return totalInputs;
		}
	}

	/**
	 * @return total number of tuples
	 */
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

	/**
	 * To report exceptions (even from other classes)
	 * @param e exception
	 */
	public synchronized void Exception(Throwable e) {
		// Print
		System.out.println("\n************************************");
		System.out.println(" Exception: " + e.getClass());
		System.out.println("************************************");
		e.printStackTrace();
		XLogger.Log("CORE", "Exception/Error Report by class: "+e.getClass()+" Message: "+ e.getMessage(), SeverityLevel.ERROR);
		// Process
		Finish(true);

	}

	/**
	 * to open Core (i.e. core io ports)
	 */
	public void Open() {
		if (!isOpen) {
			periodicConfStat.Open();
			periodicRTStat.Open();
			periodicTQoSStat.Open();
			isOpen = true;
		}
	}

	/**
	 * to close Core (i.e. core io ports)
	 */
	public void Close() {
		if (isOpen) {
			periodicConfStat.Close();
			periodicRTStat.Close();
			periodicTQoSStat.Close();
			loadShedder.Close();
			isOpen = false;
		}
	}
	
	/**
	 * @return all registered queries
	 */
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
