/**
 * Project: Xtream
 * Module: ASimpleInPort
 * Task: A Simple port to extend and use
 * Last Modify: Sep 10, 2015
 * Created: Sep 10, 2015
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

package xtream.io;

import java.io.IOException;
import java.util.Vector;

import xtream.Globals;
import xtream.plr.PLRInPort.OutChannel;
import xtream.structures.ITuple;
import xtream.core.Core.ExecutionState;
import xtream.io.AggOutPort;
import xtream.io.IInPort;
import xtream.io.IOutPort;

public abstract class ASimpleInPort extends Thread implements IInPort {

	public class OutChannel {
		public IOutPort outPort;
		public int index; // input index for out port
	}

	protected boolean isOpen; // is port open?
	protected AggOutPort inpAgg; // to put statistics about input (OPTIONAL)
	protected Vector<OutChannel> outChannels;
	protected double currentPT; // probability threshold
	protected long startTime; // start time of beginning read data
	// (millisecond). tuple t1 should be inserted in
	// time: t1.timestamp + startTime
	protected long totalReadTuples; // total number of read tuples

	public ASimpleInPort(String name) {
		super(name);
		setName(name);
		outChannels = new Vector<OutChannel>();
		totalReadTuples = 0;
		isOpen = false;
		currentPT = 0;
	}
	
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.io.IInPort#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return isOpen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.io.IInPort#Open()
	 */
	@Override
	public void Open() {
		if (!isOpen) {
			if (inpAgg != null)
				inpAgg.Open();
			for (OutChannel och : outChannels) {
				och.outPort.Open();
			}
			isOpen = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.io.IInPort#Close()
	 */
	@Override
	public void Close() {
		if (isOpen) {
			finalize();
			isOpen = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.io.IInPort#hasTuple()
	 */
	@Override
	public boolean hasTuple() { // if it is open then there are tuples
		return isOpen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.io.IInPort#nextTuple()
	 */
	@Override
	public ITuple nextTuple() throws IOException {
		throw new IOException(
				"ERROR: trying to use InPort in an unimplemented pull-based way!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.io.IInPort#SetPT(double)
	 */
	@Override
	public synchronized double SetPT(double newPT) {
		currentPT = newPT;
		return currentPT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.io.IInPort#GetPT()
	 */
	@Override
	public synchronized double GetPT() {
		return currentPT;
	}

	/*****************************
	 * invoked by Java Garbage Collector
	 */
	public void finalize() {
		isOpen = false;
		if (inpAgg != null) {
			inpAgg.Close();
		}
		for (OutChannel outch : outChannels) {
			outch.outPort.Close();
		}
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
				long nextDelay = GetNextDelay();
				if (nextDelay > 0)
					sleep(nextDelay); // sleep before sending next tuple
				ITuple nextTuple = ImportNextTuple();
				Insert(nextTuple); // add nextTuple to queues
			} // while true
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
			if ((!Globals.ADAPTIVE_FLS || tp.GetConf() >= currentPT)
					&& !Globals.ADMISSION_CTRL_BLOCKINPUT) {
				Globals.core.NewTuple(1);
				if (inpAgg != null)
					inpAgg.WriteAggValue(tp.GetTimestamp()[0], 1); // input
																	// statistics
				for (OutChannel och : outChannels) {
					och.outPort.PutTuple(tp.Clone(), och.index);
				}
				totalReadTuples++;
			}
		} catch (IOException e) {
			Globals.core.Exception(e);
		}

	} // Insert

	/**
	 * To get next input (generated or imported) tuple Must be implemented by
	 * derived classes
	 * 
	 * @return imported tuple
	 */
	protected abstract ITuple ImportNextTuple();

	/**
	 * To get next delay before sending next tuple to outputs
	 * 
	 * @return next delay before sending tuple to output (msec)
	 */
	protected abstract long GetNextDelay();

}
