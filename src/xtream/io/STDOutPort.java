/**
 * Project: Xtream
 * Module:
 * Task:
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
package xtream.io;

import java.io.IOException;

import xtream.structures.ITuple;

/**
 * Simple Out Port to print tuples on stdout
 * 
 * @author ghalambor
 * 
 */
public class STDOutPort implements IOutPort {

	/**
	 * 
	 */
	public STDOutPort() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#Open()
	 */
	@Override
	public void Open() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IInPort#Close()
	 */
	@Override
	public void Close() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#PutTuple(xtream.interfaces.ITuple, int)
	 */
	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		System.out.println(tp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.IOutPort#isUnary()
	 */
	@Override
	public boolean isUnary() {
		return true;
	}

}
