/**
 * Project: Xtream
 * Module:
 * Task:
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
package xtream.interfaces;

import java.io.IOException;

/**
 * @author ghalambor
 *
 */
public interface IOutPort {


	/**
	 * @return true: port is opean false: port is closed
	 */
	public boolean isOpen();

    /********************************
     * Open
     * to open the output file
     */
    public void Open();


    /********************************
     * Close
     * to close the output file
     */
    public void Close();


    /**********************************
     * WriteTuple
     * @param tp tuple to be written in file
     * @param i index for input (for operators/ports with multiple inputs)
     * @throws IOException if can not pu t tuple (e.g. no capacity)
     */
    public void PutTuple(ITuple tp, int i) throws IOException;
    
	/**
	 * @return true: this is a unary operator/port (e.g. select,project,agg) false: binary or more (e.g. join)
	 */
	public boolean isUnary();

}
