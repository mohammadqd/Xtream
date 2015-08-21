/**
 * Project: Xtream
 * Module: GLOBALS
 * Task: Provide system with global parameters and access to core
 * Last Modify: May 2013
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
package xtream;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;

import cern.jet.random.engine.MersenneTwister;
import xtream.core.Core;
import xtream.lsrm.ILSRMOP;
import xtream.query.IQuery;

/**
 * This class provides Xtream codes with access to global parameters and also
 * references to main global modules like core
 * 
 * @author ghalambor
 */
public class Globals {

	public static Thread inport;

	public static long totalSize = 0;

	public static long DEBUG_COUNTER1;

	public enum FLSMonitoringType {
		Continuous, Periodic, Disable
	}

	public enum LSRMType {
		Random, Semantic, Disable
	}

	public enum AdmissionControl {
		Random, // random reject
		ConfThreshold, // confidence threshold
		Disable // no admission control
	};

	public enum LSOfferSelectionMethod {
		SamePT, // one new pt for all queries (Heuristic)
		FairThief, // fair thief optimal solution
		Greedy, // greedy algorithm (sort by mem release)
		Greedy2 // greedy algorithm (sort by gain/loss i.e. mem_release/QoS
				// loss)
	};

	public enum Monitoring_Modes {
		No, // No need to monitoring
		Partial, // partial monitoring (low overhead)
		Full // full monitoring (more overhead)
	};

	// GENERAL TOOLS
	// public static Instrumentation instrumentation; // useful to get object
	// size

	
	/**
	 * Default Logging level for Xtream 
	 */
	public static java.util.logging.Level DefaultLoggingLevel = Level.FINEST;
	
	/**
	 *prefix for output files, will be set in Main
	 */
	public static String OUTPUT_FILES_PREFIX = "";

	// Configuration
	// [MIGRATED TO XConfig] public static final int TOTAL_RUNTIME = 1 * 60 * 1000; // system run time
															// (msec)
//	public static final int NUM_OF_XWAYS = 1;
//	public static final int TIME_THRESHOLD = 30000;
	// [MIGRATED TO XConfig] public static final double DEFAULT_PROBABILITY_THRESHOLD = 0.0;

	// -------- PLR ------------
	public static final boolean SYNTHETIC_INPUT_RATE = true; // false:
																// PLRInPort
																// uses real PLR
																// input rate,
																// true: uses
																// synthetic
																// rate
																// generators
	public static final double DEFAULT_QUERY_QOS_WEIGHT = 1; // default query
																// qos weight
																// (>0)
	public static final int DEFAULT_TUPLE_EXPIRATION = 30000; // for input
																// tuples
	public static final int DEFAULT_AGG_TUPLE_EXPIRATION = 10000; // for
																	// aggregation
																	// tuples
	public static final double MAX_ACCEPTABLE_ART = DEFAULT_TUPLE_EXPIRATION; // max
																				// acceptable
																				// average
																				// response
																				// time
																				// for
																				// QoS
	public static final double PTR_WEIGHT_IN_QOS = 0.5; // weight of probability
														// threshold in QoS,
														// must be in [0,1], the
														// other weight will be
														// for AVR

	public static final int INPUT_TIME_GRANULARITY = 1000; // timestamp of input
															// tuples (in
															// millisec) will be
															// input time *
															// granularity
	public static final int TUPLE_EXTRA_SIZE = 10000;// * 1024; // extra size
														// for tuples
														// (making bigger tuples
														// for
														// experiments)
	public static final double DEFAULT_POS_RESOLUTION = 50; // for
															// resolution-based
															// equality based of
															// position
	public static final int PositionReport_Type = 0;
	public static final int TravelTimeQuery_Type = 4;
	public static final double POS_ERROR_BOUND = 10;
	public static final double SPD_ERROR_BOUND = 5;
	public static final double MIN_INPUT_CONFIDENCE = 0.0;
	public static final double POS_STD = 5;
	public static final long MAX_JOIN_TIME_DIFFERENCE = Math.min(30000,
			DEFAULT_TUPLE_EXPIRATION);

	public static final int INIT_SYNOPSIS_SIZE = 100;
	public static final int NUM_OF_QUERY_REPLICATES = 10;

	public static int OpExeTimeSlice = 300;

	public static long MAX_READ_TUPLES = 0;
	public static int MAX_TOTAL_MINUTE = 120; // total minute of simulation
	public static int MAX_CLUSTER_DIFFER_SPEED = 10;
	public static int MAX_VERTICAL_MERGE_COST = 10;
	public static int MAX_INCONSISTENCY_PERCENT = 5;

	public static Core core = null; // a reference to system core

	public static MersenneTwister rndEngine = new MersenneTwister(5456465); // random
																			// generation
																			// engine
	public static MersenneTwister inputConfidenceRandEngine = new MersenneTwister(
			4546); // random engine
	// for
	// confidence of
	// input PPos
	// tuples

	// --------------------------------------------------------------------
	// ---------------- RESOURCE MANAGEMENT/MONITORING --------------------
	// --------------------------------------------------------------------
	public static boolean ADAPTIVE_FLS = true; // if true: load shedding + adaptivity
	public static final FLSMonitoringType FEDERAL_MONITORING = FLSMonitoringType.Disable;
	public static boolean FEDERAL_LOADSHEDDING_IS_ACTIVE = false;
	public static AdmissionControl ADMISSION_CTRL_TYPE = AdmissionControl.Disable;// AdmissionControl.Random;
	public static boolean ADMISSION_CTRL_BLOCKINPUT = false; // inputs will be
																// blocked while
																// this is true
																// (DO NOT
																// CHANGE IT
																// MANUALLY)
	public static LSOfferSelectionMethod LOADSHEDDING_OFFERSELECTION_METHOD = LSOfferSelectionMethod.FairThief;
	public static final Monitoring_Modes MONITORING_MODE = Monitoring_Modes.Full;
	// [MIGRATED TO XConfig] public static final long MONITORING_TIME_PERIOD = 15000; // time period for
																// monitoring

	public static final long SYNTHETIC_INPUT_TIME_PERIOD = 15000; // synthetic
																	// input
																	// period
	public static final long OVERLOAD_CHECKING_TIME_PERIOD = 5000; // for
																	// ADMISSION
																	// CTRL,
																	// LSRM,
																	// FLS(PERIODIC)
	public static final long MONITORING_TUPLE_PERIOD = 50; // monitoring will
															// be activated
															// after this
															// number of
															// input tuples
	public static long MEMORY_MAX_ALLOWABLE = Math.min(Long.MAX_VALUE, Runtime
			.getRuntime().maxMemory()); // max mem allowable for Xtream (bytes)

	public static long MIN_DELAY_BETWEEN_LOADSHEDDINGS = 500; // millisec
	public static long MEMORY_USE_ALERT_THRESHOLD = Math
			.round(0.85 * MEMORY_MAX_ALLOWABLE); // passing this threshold
													// activates load shedding
													// mechanisms
	public static long ADMISSION_MEMORY_USE_ALERT_THRESHOLD = Math
			.round(0.70 * MEMORY_MAX_ALLOWABLE); // passing this threshold
												// activates Admission ctrl.
	public static double MEMORY_MSLP_ALERT_THRESHOLD = 1000d; // used in MTR
																// function
	public static double MEMORY_ISLP_ALERT_THRESHOLD = 0.0; // used in MTR
															// function

	public static long MEMORY_NORMAL_USE_THRESHOLD = Math
			.round(0.7 * MEMORY_MAX_ALLOWABLE);
	// //
	// when
	// used
	// memory
	// is
	// lower
	// than this value, we have
	// no memory problem and can
	// improve QoS

	public static double MSLP_LOW = 3000;// 0; // slope threshold for QoS
											// improvement
	public static double Q_DMG = 0.1; // is a threshold for min damage
										// of QQoS
										// in TQoS to make a candidate for QQoS
										// Improvement
	public static long PER_OPERATOR_LS_OFFERS_COUNT = (!FEDERAL_LOADSHEDDING_IS_ACTIVE)?1:Math
			.round(Math.pow(2, 3)); // number of ls offers for operator
									// (granularity), should be power of 2


	// --------------------------------------------------------------------
	// ---------------- LSRM - JUST FOR MY THESIS -------------------------
	// --------------------------------------------------------------------
	public static Vector<ILSRMOP> dropOperators = new Vector<ILSRMOP>();
	public static LSRMType LSRM_TYPE = LSRMType.Disable;
	public static long LSRM_MEMORY_USE_ALERT_THRESHOLD = Math
			.round(0.70 * MEMORY_MAX_ALLOWABLE);


	public Globals() {
	}

	/**
	 * To check validity of config (consistency)
	 */
	public static void CheckConfigValidity() { // Check FederalLoadShedding XOR
												// (LSRM OR ADMISSION_CTRL)
		if (FEDERAL_LOADSHEDDING_IS_ACTIVE) {
			assert LSRM_TYPE == LSRMType.Disable;
			assert ADMISSION_CTRL_TYPE == AdmissionControl.Disable;
		} else {
			assert (LSRM_TYPE == LSRMType.Disable || ADMISSION_CTRL_TYPE == AdmissionControl.Disable); // check
																										// LSRM
																										// XOR
																										// ADMISSION_CTRL
			assert (FEDERAL_MONITORING == FLSMonitoringType.Periodic);
		}
	}

	public static <T extends Comparable<T>> T max(T a, T b) {
		if (a == null) {
			if (b == null)
				return a;
			else
				return b;
		}
		if (b == null)
			return a;
		return a.compareTo(b) > 0 ? a : b;
	}
}
