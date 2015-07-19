/**
 * Project: Xtream
 * Module: Bounded Normal Distro
 * Task: To model bounded normal distribution
 * Last Modify: Jul 19 2015 (revise)
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
package xtream.types;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;

/**
 * Bounded Normal Distribution
 * @author ghalambor
 * 
 */
public class BoundedNormal {

	protected double l, u; // low and up bounds of distribution
	protected double mean, std; // mean and standard deviation
	protected MersenneTwister rndeng; // random engine
	protected Normal dist;

	public synchronized double getMean() {
		return mean;
	}

	public synchronized void setMean(double mean) {
		this.mean = mean;
		dist = new Normal(mean, std, rndeng);
	}

	/**
	 * @return standard deviation
	 */
	public synchronized double getStd() {
		return std;
	}

	/**
	 * @param std set standard deviation
	 */
	public synchronized void setStd(double std) {
		this.std = std;
		dist = new Normal(mean, std, rndeng);
	}

	/**
	 * @return the l low bound of distribution
	 */
	public synchronized double getL() {
		return l;
	}

	/**
	 * @param l
	 *            to set low bound of distribution
	 */
	public synchronized void setL(double l) {
		this.l = l;
	}

	/**
	 * @return the u up bound of distribution
	 */
	public synchronized double getU() {
		return u;
	}

	/**
	 * @param u
	 *            to set up bound of distribution
	 */
	public synchronized void setU(double u) {
		this.u = u;
	}

	/**
	 * @return the random generation engine
	 */
	public synchronized MersenneTwister getRndeng() {
		return rndeng;
	}

	/**
	 * @param l
	 *            low bound
	 * @param u
	 *            up bound
	 * @param mean
	 *            mean
	 * @param std
	 *            standard deviation
	 * @param rndEng
	 *            random generation engine
	 */
	public BoundedNormal(double l, double u, double mean, double std,
			MersenneTwister rndEng) {
		this.rndeng = rndEng;
		this.l = l;
		this.u = u;
		this.mean = mean;
		this.std = std;
		dist = new Normal(mean, std, rndEng);
	}

	/**
	 * @param l
	 *            lower bound
	 * @param u
	 *            upper bound
	 * @return probability for the random variable being in (l,u) interval
	 */
	public double Prob(double l, double u) {
		if (!Overlap(l, u)) // if no overlap
			return 0;
		else {
			double newu = Math.min(this.u, u);
			double newl = Math.max(this.l, l);
			return (dist.cdf(newu) - dist.cdf(newl));
		}
	}

	/**
	 * @param l
	 *            low bound
	 * @param u
	 *            up bound
	 * @return true: overlap between interval of the random variable and (l,u)
	 */
	public boolean Overlap(double l, double u) {
		if (u <= this.l || l >= this.u)
			return false;
		else {
			return true;
		}
	}

	/**
	 * @param other
	 *            other bounded normal to check equality with this one
	 * @param resolution
	 *            equality resolution
	 * @return Probability( |(this)-(other)| < resolution)
	 */
	public double Similiar(BoundedNormal other, double resolution) {
		if (!Overlap(other.l, other.u))
			return 0;
		else {
			// make diffDost as a new random var presenting (this)-(other)
			double diffDist_u = this.u - other.l;
			double diffDist_l = this.l - other.u;
			BoundedNormal diffDist = new BoundedNormal(diffDist_l, diffDist_u,
					this.mean - other.mean, this.std + other.std, rndeng);
			return (diffDist.Prob(resolution * -1, resolution));
		}
	}

	public boolean Equal(BoundedNormal other) {
		if (this.mean == other.mean && this.std == other.std
				&& this.l == other.l && this.u == other.u)
			return true;
		else
			return false;
	}

}
