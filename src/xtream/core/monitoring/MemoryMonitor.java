/**
 * Project: Xtream
 * Module: System Monitor
 * Task: 
 * Last Modify: May 2013
 * Created: 2011
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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import xtream.Globals;
import xtream.Globals.AdmissionControl;
import xtream.Globals.LSRMType;
import xtream.core.Core.ExecutionState;
import xtream.core.loadshedding.FederalLoadShedder;
import xtream.core.loadshedding.LSOffer;
import xtream.interfaces.IInPort;
import xtream.interfaces.ILSStore;
import xtream.interfaces.IOutPort;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.lsrm.ILSRMOP;
import xtream.structures.AggOutPort;
import xtream.structures.TxtFileOutPort;

/**
 * @author Mohammad
 * 
 */
public class MemoryMonitor extends Thread implements IInPort {

	protected TxtFileOutPort freeMemoryStat; // free memory statistics (percent)
	protected TxtFileOutPort usedMemoryStat; // memory usage statistics (percent)
	protected AggOutPort totalTQoSStat; // TQoS statistic
	protected AggOutPort totalRTStat; // RT statistic
	protected AggOutPort totalPTStat; // PT statistic

	protected long periodTime;
	protected boolean screenShow; // to print results online (to screen)
	protected int counter; // counter for output
	protected boolean isOpen; // port status

	public class OutChannel {
		public IOutPort outPort;
		public int index; // input index for out port
	}

	protected Vector<OutChannel> outChannels;

	public MemoryMonitor(long periodTime, boolean screenShow) {
		super("PDSMS_Monitor");
		this.freeMemoryStat = new TxtFileOutPort("FreeMemoryStat.txt");
		this.usedMemoryStat = new TxtFileOutPort("UsedMemoryStat.txt");
		this.totalTQoSStat = new AggOutPort("TQoSStat.txt", periodTime);
		this.totalRTStat = new AggOutPort("RTStat.txt", periodTime);
		this.totalPTStat = new AggOutPort("PTStat.txt", periodTime);
		this.periodTime = periodTime;
		this.screenShow = screenShow;
		counter = 0;
		isOpen = false;
		outChannels = new Vector<OutChannel>();
	}

	/**********************************
	 * run to run it as a thread
	 */
	public void run() {
		Open();
		try {
			long lastUsedMem = -1; // used mem of last period
			while (Globals.core.ExecState() == ExecutionState.RUNNING
					|| Globals.core.ExecState() == ExecutionState.BEFORE_RUN) {
				long curTime = Globals.core.GetSysCurTime();
				long freemem, usedmem, maxmem, tplcnt, tplmaxcnt;
				maxmem = Runtime.getRuntime().maxMemory();
				freemem = Runtime.getRuntime().freeMemory(); // input statistics
				freeMemoryStat.WriteStr("" + counter + ","
						+ ((double)freemem / maxmem));
				usedmem = Runtime.getRuntime().totalMemory() - freemem; // input
				// statistics
				if (lastUsedMem < 0)
					lastUsedMem = usedmem;
				usedMemoryStat.WriteStr("" + counter + ","
						+ ((double)usedmem / maxmem));
				tplcnt = Globals.core.GetTuplesCount(); // input

				for (IQuery q : Globals.core.getQueries()) {
					if (!q.GetOwner().isSystemUser())
						totalTQoSStat.WriteAggValue(curTime, q.GetQoS()
								.GetQoS(), q.GetRelativeQoSWeight());
					totalRTStat.WriteAggValue(curTime, q.GetART(),
							q.GetRelativeQoSWeight());
					totalPTStat.WriteAggValue(curTime, q.GetPT(),
							q.GetRelativeQoSWeight());
				}
				// -----------------------
				// Screen Show of results
				// -----------------------

				if (screenShow) {
					System.out
							.format("\n%d:MONITOR: TS: %d FreeMem: %d MB UsedMem: %d MB CurrentCap: %d MB MaxCap: %d MB TotalInput: %d\n",
									counter, Globals.core.GetSysCurTime(),
									freemem / 1048576, usedmem / 1048576,
									(usedmem + freemem) / 1048576,
									maxmem / 1048576, tplcnt);
					counter++;
				}

				// -----------------------
				// Send results to monitoring queries
				// -----------------------

				MemInfoSnapshot memSnap = new MemInfoSnapshot(
						usedmem + freemem, usedmem, freemem, maxmem, curTime,
						curTime + periodTime);
				SendOutTuple(memSnap);

				lastUsedMem = usedmem;
				sleep(periodTime); // sleep till input time of next tuple
			} // while true

		} catch (Throwable exp) // catch1
		{
			Globals.core.Exception(exp);
			// exp.printStackTrace();
		} // catch
		finally {
			Close();
		}
	} // run

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void Open() {
		if (!isOpen) {
			freeMemoryStat.Open();
			usedMemoryStat.Open();
			totalTQoSStat.Open();
			totalRTStat.Open();
			totalPTStat.Open();
			for (OutChannel och : outChannels)
				och.outPort.Open();
			isOpen = true;
		}
	}

	@Override
	public void Close() {
		if (isOpen) {
			freeMemoryStat.Close();
			usedMemoryStat.Close();
			totalTQoSStat.Close();
			totalRTStat.Close();
			totalPTStat.Close();
			for (OutChannel och : outChannels)
				och.outPort.Close();
			isOpen = false;
		}
	}

	@Override
	public boolean hasTuple() {
		assert false : "Not Implemented";
		return false;
	}

	@Override
	public ITuple nextTuple() throws IOException {
		assert false : "Not Implemented";
		return null;
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

	/************************************
	 * insert new tuple to all registered queues (it does shadow copy to all
	 * queues not deep copy)
	 * 
	 * @param tp
	 *            tuple to add
	 * @todo if is not necessary and should be removed
	 */
	private void SendOutTuple(ITuple tp) {
		try {
			for (OutChannel ch : outChannels) {
				ch.outPort.PutTuple(tp, ch.index);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	} // Insert

	@Override
	public double SetPT(double newPT) {
		return 1;
	}

	@Override
	public double GetPT() {
		return 1;
	}
}
