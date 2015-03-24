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

import java.util.HashSet;
import java.util.Set;

import xtream.Globals;

/**
 * @author mohammad
 * 
 */
public class FairThief {

	/**
	 * 
	 */
	public FairThief() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param V min mem to release (byte)
	 * @param k offers per query
	 * @param n queries (offer makers)
	 * @param offers array of offers
	 * @return array of best offers, null: No Possible Result
	 */
	public static LSOffer[] FindBestOffers(int _V, int k, int n,
			LSOffer[][] offers) {
		long unit = 1048576;
		int V = _V / (int)unit;
		Set<LSOffer>[][] Sp = new Set[n + 1][V + 1];
		int[][] Sv = new int[n + 1][V + 1];
		double[][] Sw = new double[n + 1][V + 1];
		for (int vt = 0; vt <= V; vt++) {
			Sp[0][vt] = new HashSet<LSOffer>();
			Sv[0][vt] = 0;
			Sw[0][vt] = 0;
		}
		for (int i = 1; i <= n; i++) { // for all group
			for (int j = 0; j <= V; j++) { // for all values
				int deltav = j - Sv[i - 1][j]; // find the difference between
												// ideal and computed value of
												// upper cell
				
				if (deltav > 0) {
					double minWeight = Double.MAX_VALUE;
					int bestChoice = -1;
					for (int m = 0; m < k; m++) {
						if (Sv[i - 1][(int) Math.max(0, j
								- (offers[i - 1][m].memRelease/unit))]
								+ (offers[i - 1][m].memRelease/unit) >= j) {
							double newWeight = Sw[i - 1][(int) Math.max(0, j
									- (offers[i - 1][m].memRelease/unit))]
									+ offers[i - 1][m].totalCost;
							if (newWeight < minWeight) {
								minWeight = newWeight;
								bestChoice = m;
							}
						}
					}
					if (bestChoice > -1) { // if exists a possible solution
						// Save the best solution
						Sp[i][j] = new HashSet<LSOffer>(n);
						Sp[i][j].addAll(Sp[i - 1][(int) Math.max(0, j
								- (offers[i - 1][bestChoice].memRelease/unit))]);
						Sp[i][j].add(offers[i - 1][bestChoice]);
						Sv[i][j] = Sv[i - 1][(int) Math.max(0, j
								- (offers[i - 1][bestChoice].memRelease/unit))]
								+ (int) (offers[i - 1][bestChoice].memRelease/unit);
						Sw[i][j] = Sw[i - 1][(int) Math.max(0, j
								- (offers[i - 1][bestChoice].memRelease/unit))]
								+ offers[i - 1][bestChoice].totalCost;
					} else { // no solution
						for (int p = j; p <= V; p++) {
							Sp[i][p] = null;
							Sv[i][p] = 0;
							Sw[i][p] = 0;
						}
						break; // break j
					}
				} else { // if deltav <= 0 (there is an optinal solution in the
							// upper cell)
					double minWeight = Double.MAX_VALUE;
					int bestChoice = -1;
					for (int m = 0; m < k; m++) {
						if ((Sv[i - 1][(int) Math.max(0, j
								- (offers[i - 1][m].memRelease/unit))]
								+ (offers[i - 1][m].memRelease/unit) >= j) && (Sw[i - 1][(int) Math.max(0, j
								- (offers[i - 1][m].memRelease/unit))]
								+ offers[i - 1][m].totalCost < Sw[i - 1][j])) {
							double newWeight = Sw[i - 1][(int) Math.max(0, j
									- (offers[i - 1][m].memRelease/unit))]
									+ offers[i - 1][m].totalCost;
							if (newWeight < minWeight) {
								minWeight = newWeight;
								bestChoice = m;
							}
						}
					}
					if (bestChoice > -1) { // if exists a possible solution
						// Save the best solution
						Sp[i][j] = new HashSet<LSOffer>(n);
						Sp[i][j].addAll(Sp[i - 1][(int) Math.max(0, j
								- (offers[i - 1][bestChoice].memRelease/unit))]);
						Sp[i][j].add(offers[i - 1][bestChoice]);
						Sv[i][j] = Sv[i - 1][(int) Math.max(0, j
								- (offers[i - 1][bestChoice].memRelease/unit))]
								+ (int) (offers[i - 1][bestChoice].memRelease/unit);
						Sw[i][j] = Sw[i - 1][(int) Math.max(0, j
								- (offers[i - 1][bestChoice].memRelease/unit))]
								+ offers[i - 1][bestChoice].totalCost;
					} else { // no solution
						Sp[i][j] = new HashSet<LSOffer>(n);
						Sp[i][j].addAll(Sp[i - 1][j]);
						Sv[i][j] = Sv[i - 1][j];
						Sw[i][j] = Sw[i - 1][j];
					}
				}
			}
			// DEBUG
//			System.out.println("\n****************************************");
//			System.out.println("\n I="+i);
//			System.out.println("\n--- Sv ---");
//			PrintIntArray(Sv, n, V);
//			System.out.println("\n--- Sw ---");
//			PrintDoubleArray(Sw, n, V);
		}
//		// TEST
//		long totalSize = 0;
//		for (int i=0; i<V; i++) {
//			for (int j=0; j<n; j++) {
//				totalSize += Sp[j][i].size() * LSOffer.getSize();
//			}
//		}
//		Globals.totalSize += totalSize;
		
		if (Sp[n][V] != null)
			return Sp[n][V].toArray(new LSOffer[0]);
		else { // no result
			return null;
		}
		

	}
	
	public static void PrintIntArray(int[][] a,int n,int v) {
		for (int i=0; i<=n; i++) {
			System.out.format("\n%3d",i);
			for (int j=0; j<=v; j++) {
				System.out.format("%3d",a[i][j]);
			}
		}
	}
	
	public static void PrintDoubleArray(double[][] a,int n,int v) {
		for (int i=0; i<=n; i++) {
			System.out.format("\n%3d",i);
			for (int j=0; j<=v; j++) {
				System.out.format(" %5.3f",a[i][j]);
			}
		}
	}

}
