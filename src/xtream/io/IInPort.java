/**
 * Project: Xtream
 * Module: In Port Interface
 * Task: InPorts should extend this interface
 * Last Modify: 
 * Created: May 2013
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

import xtream.structures.ITuple;

/**
 * @author ghalambor
 *
 */
public interface IInPort {

	/**
	 * @return true: port is opean false: closed
	 */
	public boolean isOpen();

	/********************************
	 * Open to open the inport
	 */
	public void Open();

	/********************************
	 * Close to close the inport
	 */
	public void Close();

	/**
	 * @return true if there is some tuples to get
	 */
	public boolean hasTuple();

	/**
	 * @return next tuple and also remove it from the port
	 * @throws IOException when io exception happens
	 */
	public ITuple nextTuple() throws IOException;

	/**
	 * To set probability threshold
	 * 
	 * @param newPT
	 *            new probability threshold to set in [0,1]
	 * @return new set probability threshold (equal of bigger than newPT)
	 */
	public double SetPT(double newPT);

	/**
	 * @return current probability threshold in [0,1]
	 */
	public double GetPT();

}
