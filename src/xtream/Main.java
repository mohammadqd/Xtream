/**
 * Project: Xtream
 * Module: Main
 * Task: Test and Evaluations
 * Last Modify: Jul 13, 2015 (Adding Log and CommonConfig Support)
 * Created: May 2013
 * Developer: Mohammad Ghalambor Dezfuli (mghalambor@iust.ac.ir & @gmail.com)
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
package xtream;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import xtream.Globals.AdmissionControl;
import xtream.Globals.FLSMonitoringType;
import xtream.Globals.LSRMType;
import xtream.core.*;
import xtream.core.commonconfig.CommonConfig;
import xtream.core.log.XLogger;
import xtream.core.log.XLogger.SeverityLevel;
import xtream.core.monitoring.MemoryMonitor;
import xtream.core.monitoring.MemoryOverloadQuery;
import xtream.core.monitoring.OverloadMonitor;
import xtream.core.monitoring.QoSImprovementQuery;
import xtream.experiments.LinearTimeDelay;
import xtream.experiments.LinearTupleDelay;
import xtream.experiments.PeriodicConstantRate;
import xtream.experiments.PeriodicDiracDeltaRate;
import xtream.experiments.PeriodicLinearRate;
import xtream.experiments.PeriodicTrapezoidalRate;
import xtream.experiments.UltimateRate;
import xtream.interfaces.IOutPort;
import xtream.plr.*;
import xtream.query.AQuery;
import xtream.structures.*;

/**
 * Main class uses a Xtream with a predefined scenario. It is also a good sample
 * for using Xtream.
 */
public class Main {

	/**
	 * main method to run Xtream scenario
	 * 
	 * @param args
	 *            if no arg, results go to output folder, if 1 arg, results go
	 *            to EXPR_DEFAULT/args[0] and if 2 args, results go to
	 *            EXPR_args[0]/args[1]/
	 */
	public static void main(String args[]) {
		try {
			// SETUP LOG SYSTEM
			XLogger.setup();
			XLogger.Log("MAIN", "Starting Main", SeverityLevel.INFO);
			// SETUP CommonConfig
			CommonConfig.Initialize("XConfig.txt");

			// ========================
			// ========== INIT ==========
			// ========================
			// find and print the number of cpu cores
			int processors = Runtime.getRuntime().availableProcessors();
			System.out.println("Xtream with " + processors
					+ " processors: Starting ...");
			// check args to set out path
			if (args.length == 2) {
				Globals.OUTPUT_FILES_PREFIX = "EXPR_" + args[0] + "/" + args[1]
						+ "/";
			} else if (args.length == 1) {
				Globals.OUTPUT_FILES_PREFIX = "EXPR_DEFAULT/" + args[0] + "/";
			} else if (args.length == 0) {
				Globals.OUTPUT_FILES_PREFIX = "output/";
			}
			// make proper folder for results
			XLogger.Log("MAIN", "Creating Output Folder(s)", SeverityLevel.INFO);
			File dir = new File(Globals.OUTPUT_FILES_PREFIX);
			dir.mkdirs();
			// create system CORE
			XLogger.Log("MAIN", "Creating Core!", SeverityLevel.INFO);
			Core c = new Core();
			XLogger.Log("MAIN", "Checking Config Validity...", SeverityLevel.INFO);
			Globals.CheckConfigValidity();
			Globals.core = c;
			XLogger.Log("MAIN", "Creating System Users", SeverityLevel.INFO);
			User systemUser = new User("system", true); // system user (not real
														// user)
			User admin = new User("admin");
			c.AddUser(admin);

			// ================================
			// ========= CREATING Query Plans
			// ================================
			XLogger.Log("MAIN", "Creating Query Plans", SeverityLevel.INFO);
			TxtFileOutPort queryStatisticsResutls = new TxtFileOutPort(
					"Queries_Statistics_Results.txt");
			for (int i = 0; i < Globals.NUM_OF_QUERY_REPLICATES; i++) {
				AQuery q = new Query3("Q_" + i,
						Globals.DEFAULT_QUERY_QOS_WEIGHT + (5 * i), admin);
				q.AddQueryStatisticsOutPort(queryStatisticsResutls);
				admin.addQuery(q);
				q.Open();
				c.AddRunnable(q);
			}

			// ================================
			// ========== CREATING THREADS =====
			// ================================
			XLogger.Log("MAIN", "Creating Threads", SeverityLevel.INFO);
			AggOutPort inputAgg = new AggOutPort("InputAgg.txt",
					CommonConfig.GetConfigIntItem("MONITORING_TIME_PERIOD")); // to save statistics about
														// input
			// stream
			// PLRInPort rport = new
			// PLRInPort("RoadPort","data/datafile30min_1XW_modified.dat", 154);
			// //
			// 154: random
			// PLRInPort rport = new PLRInPort("RoadPort",
			// "data/datafile3hours_modified.dat", 154); // 154: random
			PLRInPort rport = new PLRInPort("RoadPort",
					"data/datafile5min_1XW_modified.dat", 154); // 154: random
			rport.AddAggOutPort(inputAgg);
			// rport.setSyntheticDelayGen(new
			// PeriodicLinearRate((double)Globals.SYNTHETIC_INPUT_TIME_PERIOD/Globals.TOTAL_RUNTIME,Globals.SYNTHETIC_INPUT_TIME_PERIOD,
			// 200, 1000));
			rport.setSyntheticDelayGen(new PeriodicConstantRate(150,
					Globals.SYNTHETIC_INPUT_TIME_PERIOD));
			// rport.setSyntheticDelayGen(new
			// PeriodicTrapezoidalRate(Globals.TOTAL_RUNTIME,Globals.SYNTHETIC_INPUT_TIME_PERIOD,0.34,0.33,1000,200));
			// rport.setSyntheticDelayGen(new
			// PeriodicDiracDeltaRate(Globals.SYNTHETIC_INPUT_TIME_PERIOD,200,2000,0.05,0.25,0.50,0.75));//17000
			// rport.setSyntheticDelayGen(new UltimateRate());
			for (int i = 0; i < admin.GetQueriesCount(); i++)
				// for all admin queries
				for (int j = 0; j < admin.getQuery(i).InPortsCount(); j++)
					rport.AddOutPort((IOutPort) admin.getQuery(i).GetInPort(j),
							i);
			rport.setPriority(Thread.MAX_PRIORITY);
			// Globals.inport = rport;
			c.AddRunnable(rport);

			// MONITORING THREAD
			MemoryMonitor sysMon = new MemoryMonitor(
					CommonConfig.GetConfigIntItem("MONITORING_TIME_PERIOD"), true);
			sysMon.setPriority(Thread.NORM_PRIORITY);
			c.AddRunnable(sysMon);

			// Overload Monitor
			if (Globals.ADMISSION_CTRL_TYPE != AdmissionControl.Disable
					|| Globals.LSRM_TYPE != LSRMType.Disable
					|| (Globals.FEDERAL_MONITORING == FLSMonitoringType.Periodic && Globals.FEDERAL_LOADSHEDDING_IS_ACTIVE)) {
				XLogger.Log("MAIN", "Activating Overload Monitoring Thread", SeverityLevel.INFO);
				OverloadMonitor overMon = new OverloadMonitor(
						Globals.OVERLOAD_CHECKING_TIME_PERIOD, false);
				sysMon.setPriority(Thread.NORM_PRIORITY);
				c.AddRunnable(overMon);
			}
			else {
				XLogger.Log("MAIN", "Overload Monitoring Thread is DISABLED!", SeverityLevel.INFO);
			}

			if (Globals.FEDERAL_MONITORING == FLSMonitoringType.Continuous) {
				// QoS Monitoring Query
				XLogger.Log("MAIN", "Activating Continuous QoS Improvement Thread", SeverityLevel.INFO);
				QoSImprovementQuery QQoSQ = new QoSImprovementQuery(
						"QQoS_Monitoring_Query",
						Globals.DEFAULT_QUERY_QOS_WEIGHT, systemUser);
				sysMon.AddOutPort((IOutPort) QQoSQ.GetInPort(0), 0);
				for (int i = 0; i < admin.GetQueriesCount(); i++)
					// for all admin queries
					admin.getQuery(i).AddQueryStatisticsOutPort(
							(IOutPort) QQoSQ.GetInPort(1)); // register all queries to send their statistics to QoSImprovementQuery
				QQoSQ.setPriority(Thread.NORM_PRIORITY);
				c.AddRunnable(QQoSQ);

				// Memory Overloading Query
				MemoryOverloadQuery memQ = new MemoryOverloadQuery(
						"MemOverloadQuery", Globals.DEFAULT_QUERY_QOS_WEIGHT,
						systemUser);
				// sysMon.AddOutPort(memQ.GetInPort(0), 0);
				rport.AddOutPort((IOutPort) memQ.GetInPort(1), 1);
				memQ.setPriority(Thread.NORM_PRIORITY);
				c.AddRunnable(memQ);
			}
			
			// ================================
			// ========== RUNNING ==============
			// ================================
			
			XLogger.Log("MAIN", "Running Core...", SeverityLevel.INFO);
			c.Run(CommonConfig.GetConfigIntItem("TOTAL_RUNTIME"), false);
			System.out.print("\nXtream: Finished!");
		} catch (OutOfMemoryError e) {
			XLogger.Log("MAIN", "ERROR: Outta Memory!!!.", SeverityLevel.ERROR);
			System.out
					.println("++++++++++++++++++++++++++++++++++++ Outta Memo ");
		}
		catch (IOException e) {
			XLogger.Log("MAIN", "ERROR: IOException: " + e.getMessage(), SeverityLevel.ERROR);
			e.printStackTrace();
		}
		catch (Exception e) {
			XLogger.Log("MAIN", "ERROR: Exception: " + e.getMessage(), SeverityLevel.ERROR);
			e.printStackTrace();
		}
	}
}
