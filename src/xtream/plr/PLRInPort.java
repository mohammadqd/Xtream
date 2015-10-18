/**
 * Project: Xtream
 * Module: PLR In Port
 * Task:
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
package xtream.plr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import xtream.Globals;
import xtream.core.Core.ExecutionState;
import xtream.core.commonconfig.CommonConfig;
import xtream.experiments.ASyntheticInputDelayGenerator;
import xtream.io.AggOutPort;
import xtream.io.IInPort;
import xtream.io.IOutPort;
import xtream.structures.ITuple;
import xtream.structures.PeriodicStatistics;
import xtream.structures.TupleQueue;

/**
 * Push-based implementation (can be upgraded to pull-based)
 * 
 * @author ghalambor
 * 
 */
public class PLRInPort extends Thread implements IInPort {

	protected BufferedReader inStream;
	protected String fileName; // name of file to read data
	protected long startTime; // start time of beginning read data
								// (millisecond). tuple t1 should be inserted in
								// time: t1.timestamp + startTime
	protected String inName = "UNKNOWN FileInPort";
	protected long totalReadTuples; // total number of read tuples

	public long getTotalReadTuples() {
		return totalReadTuples;
	}

	public class OutChannel {
		public IOutPort outPort;
		public int index; // input index for out port
	}

	protected Vector<OutChannel> outChannels;

	protected TupleQueue buffer; // read tuples waiting to push/pull

	protected AggOutPort inpAgg; // to put statistics about input
	protected boolean isOpen;
	protected double currentPT; // probability threshold
	protected ASyntheticInputDelayGenerator syntheticDelayGen; // synthetic
																// delay
																// generator
	protected FileInputStream inpFile;

	// protected PeriodicStatistics inputStatistics;

	/*********************************
	 * Constructor
	 * 
	 * @param name
	 *            name of inport
	 * @param nameOfFile
	 *            name of input file
	 */
	public PLRInPort(String name, String nameOfFile, int rndEngSeed) {
		super(name);
		setName("PLR_INPORT");
		outChannels = new Vector<OutChannel>();
		buffer = new TupleQueue(PPos.GetPPosSize(), 1, 0);
		inName = name;
		fileName = nameOfFile;
		totalReadTuples = 0;
		isOpen = false;
		currentPT = 0;
		try {
			inpFile = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// inputStatistics = new
		// PeriodicStatistics(Globals.MONITORING_TIME_PERIOD);
	} // Constructor

	/**
	 * @param op
	 *            out port to add
	 * @param index
	 *            input index for outport
	 * @return ref to added out port
	 */
	public IOutPort AddOutPort(IOutPort op, int index) {
		OutChannel ch = new OutChannel();
		ch.outPort = op;
		ch.index = index;
		outChannels.add(ch);
		return (outChannels.get(outChannels.indexOf(ch)).outPort);
	}

	/**
	 * This method is basically for testing and evaluations
	 * 
	 * @param outp
	 *            output port
	 */
	public void AddAggOutPort(AggOutPort outp) {
		this.inpAgg = outp;
	}

	/*****************************
	 * invoked by Java Garbage Collector
	 */
	public void finalize() {
		try {
			isOpen = false;
			inStream.close();
			if (inpAgg != null) {
				inpAgg.Close();
				System.out.println("\n PLRInPort: Total Read PPos = "
						+ totalReadTuples);
			}
			for (OutChannel outch : outChannels) {
				outch.outPort.Close();
			}
		} catch (java.io.IOException exp) // try
		{
			exp.printStackTrace();
		} // catch
	} // finalize

	/**********************************
	 * run to run it as a thread
	 */
	public void run() {
		startTime = System.currentTimeMillis();
		if (!isOpen)
			Open();
		try {
			while (isOpen && Globals.core.ExecState() == ExecutionState.RUNNING) {
				String nextTupleStr = inStream.readLine();
				if (nextTupleStr == null) {
					inpFile.getChannel().position(0);
					inStream = new BufferedReader(new InputStreamReader(inpFile));					
					nextTupleStr = inStream.readLine();
					System.out.println("\n[[[[[ RESET IN INPORT ]]]]]");
					// return; // end of file
				}
				PPos nextTuple = new PPos(Globals.core.GetSysCurTime());
				nextTuple.DispatchString(nextTupleStr);
				long nextTime = startTime + nextTuple.time; // arrival time of
															// next tuple
				// deltaTime presents delay before next tuple
				long deltaTime = 0;
				if (Globals.SYNTHETIC_INPUT_RATE)
					deltaTime = syntheticDelayGen
							.nextDelay((double) Globals.core.GetSysCurTime()
									/ CommonConfig.GetConfigIntItem("TOTAL_RUNTIME"));
				else
					deltaTime = nextTime - System.currentTimeMillis();
				if (deltaTime > 0)
					sleep(deltaTime); // sleep till input time of next tuple
				nextTuple.sysInTS = System.currentTimeMillis();
				Insert(nextTuple); // add nextTuple to queues
				if (((Globals.MAX_READ_TUPLES != 0) && (totalReadTuples > Globals.MAX_READ_TUPLES))
						|| (nextTuple.time >= Globals.MAX_TOTAL_MINUTE * 60000)) {
					return; // sufficient read
				}
			} // while true
		} catch (java.io.EOFException exp) // try, end of input file
		{
			System.out.println("End of input: " + inName);
			return;
		} catch (Throwable exp) // catch1
		{
			// exp.printStackTrace();
			Globals.core.Exception(exp);
		} // catch2
		finally {
			Close();
		}
	} // run

	/************************************
	 * insert new tuple to all registered queues (it does shadow copy to all
	 * queues not deep copy)
	 * 
	 * @param tp
	 *            tuple to add
	 * @todo if is not necessary and should be removed
	 */
	private void Insert(ITuple tp) {
		try {
			if ((!Globals.ADAPTIVE_FLS || tp.GetConf() >= currentPT) && !Globals.ADMISSION_CTRL_BLOCKINPUT) {
				Globals.core.NewTuple(1);
				// inputStatistics.newValue(tp.GetConf());
				if (inpAgg != null)
					inpAgg.WriteAggValue(tp.GetTimestamp()[0], 1); // input
																	// statistics
				for (OutChannel och : outChannels) {
					och.outPort.PutTuple(tp.Clone(), och.index);
				}
				totalReadTuples++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	} // Insert

	@Override
	public void Open() {
		if (!isOpen) {
			inStream = new BufferedReader(new InputStreamReader(inpFile));
			if (inpAgg != null)
				inpAgg.Open();
			for (OutChannel och : outChannels) {
				och.outPort.Open();
			}
			isOpen = true;
		}
	}

	@Override
	public void Close() {
		if (isOpen) {
			finalize();
			isOpen = false;
		}
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public boolean hasTuple() { // if it is open then there are tuples
		return isOpen;
	}

	@Override
	public ITuple nextTuple() throws IOException {
		throw new IOException(
				"ERROR: trying to use PLRInPort in an unimplemented pull-based way!");
	}

	@Override
	public synchronized double SetPT(double newPT) {
		currentPT = newPT;
		return currentPT;
	}

	@Override
	public synchronized double GetPT() {
		return currentPT;
	}

	/**
	 * @param syntheticDelayGen
	 *            the syntheticDelayGen to set
	 */
	public synchronized void setSyntheticDelayGen(
			ASyntheticInputDelayGenerator syntheticDelayGen) {
		this.syntheticDelayGen = syntheticDelayGen;
	}

}
