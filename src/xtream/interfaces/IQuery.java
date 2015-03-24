/**
 * Project: Xtream
 * Module: IQuery
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

import xtream.core.User;

/**
 * This is an interface for query plans
 * @author ghalambor
 *
 */
public interface IQuery {

	/**
	 * root operator is the last operator in query which makes results. some recursive
	 * commands go to this operator and then to its preceding operators. This method only
	 * set the operator as root and it should have been added before by AddOperator() method.
	 * 
	 * @param op root operator
	 * @return ref to root operator
	 */
	public IOperator SetRootOperator(IOperator op);

	/**
	 * @param op reference to new operator to add
	 */
	public void AddOperators(IOperator... op);


	/**
	 * @param p port to all
	 * @param index index for port
	 */
	public void AddOutPort(IOutPort p, int index);
	
	/**
	 * @param p port to all
	 * @param index index for port
	 */
	public void AddInPort(IInPort p, int index);
	
	/**
	 * @param op operator to add as a leaf operator
	 */
	public void AddLeafOperators(IOperator... op);
	
	/**
	 * to unlimitedly run query
	 */
	public void run();
	
	/**
	 * @param index index of port
	 * @return ref to port
	 */
	public IInPort GetInPort(int index);
	
	/**
	 * @param index index of port
	 * @return ref to port
	 */
	public IOutPort GetOutPort(int index);
	
	public void Open();
	
	public void Close();
	
	/**
	 * @return num of input ports
	 */
	public int InPortsCount();
	
	/**
	 * @param pt probability threshold in [0,1]
	 * @return new finalized probability threshold (>= pt)
	 */
	public double SetPT(double pt);
	
	/**
	 * @return current probability threshold in [0,1]
	 */
	public double GetPT();
	

	/**
	 * @param art last computed Average Response Time for query
	 * @return art
	 */
	public double SetART(double art);
	
	/**
	 * @return last computed Average Response Time for query
	 */
	public double GetART();
	
	/**
	 * @return last computed QoS for current query
	 */
	public IQoS GetQoS();
	
	/**
	 * @return QoS Weight of query (>0)
	 */
	public double GetQoSWeight();
	
	/**
	 * @return Relative (per user) QoS Weight of query in [0,1]
	 */
	public double GetRelativeQoSWeight();
	
	/**
	 * @return query owner
	 */
	public User GetOwner();
	
	/**
	 * Add ports to send query statistics
	 * @param p port to all
	 */
	public void AddQueryStatisticsOutPort(IOutPort p);
	
	/**
	 * @return query name
	 */
	public String GetName();
	
	/**
	 * All result tuples of queries should be checked (to update query
	 * statistics) when ready to send out
	 * 
	 * @param tpl
	 *            result tuple to check
	 */
	public void CheckResultTuple(ITuple tpl);

}
