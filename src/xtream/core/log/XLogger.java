/** * Project: Xtream
 * Module: Log Manager
 *
 * Task: Log internal events
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

package xtream.core.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import xtream.Globals;

/**
 * Main Logger Class
 */
public class XLogger {

	/**
	 * Severity levels of log records
	 */
	public enum SeverityLevel {
		ERROR, WARNING, INFO, DEBUG
	}

	private static FileHandler fileTxt;
	private static SimpleFormatter defaultJavaFormatter;
	private static LogSimpleFormatter logSimpleFormatter;
	private static Logger logger;
	private volatile static boolean initialized = false;

	/**
	 * To determine the minimum level of records to log (It uses default logging
	 * level from Globals)
	 * 
	 * @see xtream.Globals#DefaultLoggingLevel
	 */
	public static synchronized void setup() throws IOException {
		setup(Globals.DefaultLoggingLevel);
	}

	/**
	 * To determine the minimum level of records to log e.g. Level.FINEST
	 * 
	 * @param _logMinLevel
	 *            minimum level of logging (default is set in Globals)
	 */

	public static synchronized void setup(java.util.logging.Level _logMinLevel)
			throws IOException {
		if (!initialized) {
			// get the global logger to configure it
			logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
			logger.setLevel(_logMinLevel);
			fileTxt = new FileHandler("Xtream.log.txt");

			// create a TXT formatter
			// java.util.logging.SimpleFormatter.format="%4$s: %5$s [%1$tc]%n";
			defaultJavaFormatter = new SimpleFormatter();
			logSimpleFormatter = new LogSimpleFormatter();
			fileTxt.setFormatter(logSimpleFormatter);
			logger.addHandler(fileTxt);
			initialized = true;
		}
	}

	public static synchronized void Log(String submodule, String message,
			SeverityLevel level) {
		try {
			if (!initialized)
				setup();
			if (initialized && logger != null) {
				String event = String.format("[%s] [%s]", submodule, message);
				switch (level) {
				case ERROR:
					logger.severe(event);
					break;
				case WARNING:
					logger.warning(event);
					break;
				case INFO:
					logger.info(event);
					break;
				case DEBUG:
					logger.fine(event);
					break;
				default:
					logger.finest(event);
				}
			}
		}	catch (Exception e)
		{
			System.err.println("Exception in Xlogger.Log(): " + e.getMessage());
		}
	}
}
