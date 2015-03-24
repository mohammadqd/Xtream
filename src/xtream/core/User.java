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
package xtream.core;

import java.util.Iterator;
import java.util.Vector;

import xtream.interfaces.IQuery;

/**
 * @author ghalambor
 * 
 */
public class User {

	protected Vector<IQuery> queries;
	protected double totalQoSWeight;
	protected String userID;
	protected boolean systemUser; // system user is different from normal users

	/**
	 * 
	 */
	public User(String id,boolean systemUser) {
		this.systemUser = systemUser;
		userID = id;
		totalQoSWeight = 0;
		queries = new Vector<IQuery>();
	}
	
	public User(String id) {
		this(id,false);
	}

	public synchronized void addQuery(IQuery q) {
		queries.add(q);
		totalQoSWeight += q.GetQoSWeight();
	}

	public synchronized IQuery getQuery(int index) {
		return queries.get(index);
	}

	public synchronized Iterator<IQuery> getAllQueries() {
		return queries.iterator();
	}
	
	public synchronized int GetQueriesCount() {
		return queries.size();
	}

	public synchronized boolean isSystemUser() {
		return systemUser;
	}
	
	public synchronized double GetTotalQoSWeight() {
		return totalQoSWeight;
	}
}
