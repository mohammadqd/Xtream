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
package xtream.core.loadshedding;


/**
 * Interface for Load Shedding Capable Stores
 * 
 * @author ghalambor
 * 
 */
public interface ILSStore {

	/**
	 * to create LSOffers
	 * 
	 * @param newPTs
	 *            array of new probability-threshold, so offers will be made
	 *            based on these PTs
	 * @return array of Load Shedding Offers
	 */
	public LSOffer[] getLSOffers(double[] newPTs);

	/**
	 * to execute LSCommands
	 * 
	 * @param offer
	 *            load shedding offer to perform
	 */
	public void LSCommand(LSOffer offer);

}
