/**
 * Project: Xtream
 * Module: Project operator for DB SPJ Queries
 * Task: schema filtering
 * Last Modify:
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

import xtream.Globals;
import xtream.core.Core.ExecutionState;
import xtream.io.IInPort;
import xtream.structures.AProjection;
import xtream.structures.ITuple;

/**
 * PULL/PUSH based Operator
 * 
 * @author ghalambor
 * 
 */
public class Project extends AOperator {

	protected AProjection prj; // projecting function

	/**
	 * @param prj projecting function
	 * @param opName operator's name
	 * @param parentQuery link to parent query
	 */
	public Project(AProjection prj, String opName, IQuery parentQuery) {
		super(opName, parentQuery);
		this.prj = prj;

	}

	/* (non-Javadoc)
	 * @see xtream.query.AOperator#PutTuple(xtream.interfaces.ITuple, int)
	 */
	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		long startTime = System.currentTimeMillis();
		if (isOpen())
		{
			ITuple newtp = prj.ProjectComputation(tp);
			if ((!Globals.ADAPTIVE_FLS || newtp.GetConf() >= GetPT())) { // check probability threshold
				for (OutChannel o : outChannels) { // send it to all out ports
													// (including
													// next operators)
					ITuple nextTpl = newtp.Clone();
					if (isRootOP())
						GetQuery().CheckResultTuple(nextTpl);
					o.outPort.PutTuple(nextTpl, o.index);
				}
			}
			rt.AddValue(newtp.GetConf(), System.currentTimeMillis() - startTime);
		}
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IOperator#run(long)
	 */
	@Override
	public void run(long ts) { // does not care about timeslice
		IInPort inPort = inPorts.elementAt(0);
		while (hasTuple() && Globals.core.ExecState() == ExecutionState.RUNNING) {
			try {
				PutTuple(inPort.nextTuple(), 1);
			} catch (RuntimeException e) {
				Globals.core.Exception(e);
			} catch (IOException e) {
				Globals.core.Exception(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IInPort#hasTuple()
	 */
	@Override
	public boolean hasTuple() {
		return inPorts.elementAt(0).hasTuple();
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IInPort#nextTuple()
	 */
	@Override
	public ITuple nextTuple() throws IOException {
		if (!isOpen())
			throw new RuntimeException(
					"ERROR: Trying to get tuples from a closed PROJECT operator");
		else {
			IInPort inPort = inPorts.elementAt(0);
			if (hasTuple()) {
				ITuple newtp = prj.ProjectComputation(inPort.nextTuple());
				if ((!Globals.ADAPTIVE_FLS || newtp.GetConf() >= GetPT())) { // check probability threshold
					return newtp;
				}
			}
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IOutPort#isUnary()
	 */
	@Override
	public boolean isUnary() {
		return true;
	}

}
