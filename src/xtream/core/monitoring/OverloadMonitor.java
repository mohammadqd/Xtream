package xtream.core.monitoring;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import xtream.Globals;
import xtream.Globals.AdmissionControl;
import xtream.Globals.FLSMonitoringType;
import xtream.Globals.LSRMType;
import xtream.core.Core.ExecutionState;
import xtream.interfaces.IInPort;
import xtream.interfaces.IOutPort;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;
import xtream.lsrm.ILSRMOP;
import xtream.structures.AggOutPort;
import xtream.structures.JointTuples;
import xtream.structures.TxtFileOutPort;

public class OverloadMonitor extends Thread implements IInPort {

	protected long periodTime;
	protected boolean screenShow; // to print results online (to screen)
	protected int counter; // counter for output
	protected boolean isOpen; // port status

	public class OutChannel {
		public IOutPort outPort;
		public int index; // input index for out port
	}

	protected Vector<OutChannel> outChannels;

	public OverloadMonitor(long periodTime, boolean screenShow) {
		super("PDSMS_Monitor");
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

				usedmem = Runtime.getRuntime().totalMemory() - freemem; // input
				// statistics
				if (lastUsedMem < 0)
					lastUsedMem = usedmem;

				tplcnt = Globals.core.GetTuplesCount(); // input

				// -----------------------
				// Federal Load Shedder
				// -----------------------

				if (Globals.FEDERAL_MONITORING == FLSMonitoringType.Periodic
						&& Globals.FEDERAL_LOADSHEDDING_IS_ACTIVE) {
					if (usedmem > Globals.MEMORY_USE_ALERT_THRESHOLD) {
						long mtr = Globals.core.GetLoadShedder().MTR(usedmem,
								0, 0);
						System.out.println("MemOverloadQy MTR: " + mtr);
						Globals.core.GetLoadShedder().ReleaseMemory(mtr);
					}
				}

				// -----------------------
				// ADMISSION CTRL
				// -----------------------
				if (Globals.ADMISSION_CTRL_TYPE != AdmissionControl.Disable) {
					if (usedmem > Globals.ADMISSION_MEMORY_USE_ALERT_THRESHOLD) { // overload
						Globals.ADMISSION_CTRL_BLOCKINPUT = true;
					} else {
						Globals.ADMISSION_CTRL_BLOCKINPUT = false;
					}
				}

				// -----------------------
				// LSRM
				// -----------------------
				if (Globals.LSRM_TYPE != LSRMType.Disable) {
					Collections.sort(Globals.dropOperators,
							Collections.reverseOrder());
					Iterator<ILSRMOP> it = Globals.dropOperators.iterator();
					if (usedmem > Globals.LSRM_MEMORY_USE_ALERT_THRESHOLD
							&& (usedmem - lastUsedMem) > 0) { // if overload
						long memToRelease = usedmem - lastUsedMem;
						long releasedMem = 0;
						// System.out.println("\n---------------- LSRM Set ---------------");
						while (releasedMem < memToRelease && it.hasNext()) {
							ILSRMOP nextOP = it.next();
							if (nextOP.getGain() < (memToRelease - releasedMem)) {
								nextOP.ChangeDropRatio(1d);
								// System.out
								// .println(nextOP
								// + " DropRatio Changed To: 1");
							} else {
								nextOP.ChangeDropRatio(((double) (memToRelease - releasedMem))
										/ (nextOP.getGain() + 1));
								// System.out
								// .println(nextOP
								// + " DropRatio Changed To: "
								// + (((double) (memToRelease - releasedMem)) /
								// (nextOP
								// .getGain()+1)));
							}

							releasedMem += nextOP.getGain();
						}
					} else { // no overload
						// System.out.println("\n---------------- LSRM Set Back ---------------");
						while (it.hasNext()) {
							ILSRMOP nextOP = it.next();
							nextOP.ChangeDropRatio(0d);
							// System.out.println(nextOP+" DropRatio Changed To: 0");
						}
					}

				}

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
			for (OutChannel och : outChannels)
				och.outPort.Open();
			isOpen = true;
		}
	}

	@Override
	public void Close() {
		if (isOpen) {
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

	@Override
	public double SetPT(double newPT) {
		return 1;
	}

	@Override
	public double GetPT() {
		return 1;
	}
}
