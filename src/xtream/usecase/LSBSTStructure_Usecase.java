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
package xtream.usecase;

import xtream.Globals;
import xtream.core.Core;
import xtream.core.loadshedding.LSBSTStructure;
import xtream.core.loadshedding.LSOffer;
import xtream.structures.AggTuple;
import xtream.structures.ITuple;

/**
 * @author ghalambor
 *
 */
public class LSBSTStructure_Usecase {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Globals.core = new Core();
		
		// Parameters
		int leaves = 8;
		int totalTuples = 1000000;
		double[] proposedOffers = {0.1,0.6,0.3,1};


		System.out.println("\nTesting LSBSTStructur...\n");
		LSBSTStructure tree = new LSBSTStructure(leaves);
		for (int i = 0; i < totalTuples; i++) {
			AggTuple newTpl = new AggTuple(new Double(i),i,i*1d/totalTuples);
			tree.add(newTpl);
//			System.out.println("\nTREE STEP: "+i);
//			System.out.println("\n"+tree);
		}
		
		
		System.out.println("\nLEVEL 1\n");
//		System.out.println("\n"+tree);
		
		LSOffer[] offers = tree.getLSOffers(proposedOffers);
		for (LSOffer of:offers) {
			System.out.println(of);
		}
		
//		tree.LSCommand(offers[2]);
		tree.SetPT(0.4);
		
		System.out.println("\nLEVEL 2\n");
//		System.out.println("\n"+tree);
		
		offers = tree.getLSOffers(proposedOffers);
		for (LSOffer of:offers) {
			System.out.println(of);
		}
		
		
		System.out.println("\nLEVEL 3 - POLLS \n");
		ITuple tpl = tree.pollValidTuple();
		while (tpl != null) {
//			System.out.println("\n"+tpl);
			tpl = tree.pollValidTuple();
		}
		



	}

}
