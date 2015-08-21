/**
 * Project: Xtream
 * Module: User
 * Task: To model users' information and queries
 * Last Modify: Jul 13, 2015 (more comments)
 * Created: 2013
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

import xtream.query.IQuery;

/**
 * To model users' information and queries
 */
/**
 *
 */
public class User {

	protected Vector<IQuery> queries; // contains user's queries
	protected double totalQoSWeight;
	protected String userID;
	protected boolean systemUser; // system user is different from normal users

	/**
	 * Constructor
	 * @param id user id
	 * @param systemUser true: power user false: limited user
	 */
	public User(String id,boolean systemUser) {
		this.systemUser = systemUser;
		userID = id;
		totalQoSWeight = 0;
		queries = new Vector<IQuery>();
	}
	
	/**
	 * Constructor for limited users
	 * @param id user id
	 */
	public User(String id) {
		this(id,false);
	}

	/**
	 * Register a query for the current user
	 * @param q query to be registered
	 */
	public synchronized void addQuery(IQuery q) {
		queries.add(q);
		totalQoSWeight += q.GetQoSWeight();
	}

	/**
	 * get a query by its index
	 * @return demanded query
	 */
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
