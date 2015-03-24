/**
 * Project: Xtream
 * Module: IQueue
 * Task: A general interface for queues for tuples
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

public interface IQueue extends IIOPort {
    /***************************
     * GetTuple
     * target: to return a tuple from head of queue and remove it from buffer
     * @return Tuple (should be casted to proper tuple type)
     */
    public ITuple GetTuple();

    /***************************
     * TopTuple
     * target: to return a tuple from head of queue without remove it from buffer
     * @return Tuple (should be casted to proper tuple type)
     */
    public ITuple TopTuple();

    /*****************************
     * GetSize
     * @return size of queue (bytes)
     */
    public long GetSize();

    /*****************************
     * Get Count
     * @return num of tuples in buffer
     */
    public long GetCount();

    /*****************************
     * Get Capacity
     * @return max num of tuples possible in buffer, -1: unlimited
     */
    public long GetCapacity();
}
