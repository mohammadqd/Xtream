/**
 * Project: Xtream
 * Module:
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

package xtream.structures;

import java.io.*;

import xtream.Globals;
import xtream.interfaces.IOutPort;
import xtream.interfaces.ITuple;

/**
 * This is out port for text files
 * 
 * @author ghalambor
 * @see TxtFileOutPort
 * 
 */
public class TxtFileOutPort implements IOutPort {
	private FileWriter fout; // file to save output
	private String fileName; // name of output file
	protected boolean isOpen;

	/*********************************
	 * Constructor
	 * 
	 * @param fname
	 *            name of output file
	 */
	public TxtFileOutPort(String fname) {
		if (fname != null)
			fileName = Globals.OUTPUT_FILES_PREFIX + fname;
		else
			fileName = null;
		isOpen = false;
	}// OutPort

	/********************************
	 * Open to open the output file
	 */
	public synchronized void Open() {
		try {
			if (!isOpen) {
				if (fileName != null)
					fout = new FileWriter(fileName);
				isOpen = true;
			}
		}// try
		catch (IOException err) {
			Globals.core.Exception(err);
		}// catch
	}// Open

	/********************************
	 * Close to close the output file
	 */
	public synchronized void Close() {
		try {
			if (isOpen) {
				if (fout != null)
					fout.close();
				isOpen = false;
			}
		} catch (IOException ex) {
			Globals.core.Exception(ex);
		}
	}// Close

	/**********************************
	 * WriteTuple
	 * 
	 * @param i
	 *            index of input
	 * @param tp
	 *            tuple to be written in file
	 * @throws IOException
	 */
	public synchronized void PutTuple(ITuple tp, int i) throws IOException {
		if (isOpen && fout != null) {
			fout.write(/*
						 * "Index: " + i + ", " + Globals.core.GetSysCurTime() +
						 * "," +
						 */tp.toString() + "\n");
		}
		// else
		// throw new IOException(
		// "ERROR: Putting tuple in a closed TxtFileOutPort!");
		Globals.core.Exception(new IOException("ERROR: Putting tuple in a closed TxtFileOutPort!"));

	}// WriteTuple

	/**********************************
	 * WriteStr
	 * 
	 * @param str
	 *            string to be written in file
	 */
	public synchronized void WriteStr(String str) {
		try {
			if (isOpen && fout != null)
				fout.write(str + "\n");
		}// try
		catch (IOException ex) {
			Globals.core.Exception(ex);
		}// catch
	}// WriteStr

	@Override
	public synchronized boolean isOpen() {
		return isOpen;
	}

	@Override
	public synchronized boolean isUnary() {
		return true;
	}

}// OutPort Class
