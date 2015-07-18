/**
 * Project: Xtream
 * Module: XLogger usecase
 * Task: usecase
 * Last Modify: Mar 29, 2015
 * Created: Mar 29, 2015
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

import java.io.IOException;

import xtream.core.log.*;
import xtream.core.log.XLogger.SeverityLevel;

public final class XLogger_Usecase {

	public XLogger_Usecase() {
	}

	public static void main(String[] args) {
		System.out.println("Testing XLog:");

		try {
			XLogger.setup();
			XLogger.Log("mod1", "Message 1", SeverityLevel.ERROR);
			XLogger.Log("mod2", "Message 2", SeverityLevel.WARNING);
			XLogger.Log("mod3", "Message 3", SeverityLevel.INFO);
			XLogger.Log("mod4", "Message 4", SeverityLevel.DEBUG);
			System.out.println("Press Enter to finish:");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
