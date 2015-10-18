/**
 * Project: Xtream
 * Module: Common Config
 * Task: To access config parameres transparently
 * Last Modify: Jul 1, 2015
 * Created: Jul 1, 2015
 * Developer: Mohammad Ghalambor Dezfuli (mghalambor@iust.ac.ir & @ gmail.com)
 * NOTE: Main source of this code is from: http://www.mkyong.com/java/java-properties-file-examples/ 
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

package xtream.core.commonconfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import xtream.core.log.XLogger;
import xtream.core.log.XLogger.SeverityLevel;

/**
 * This class provides us with a simple transparent mechanism to access config
 * file
 */
public class CommonConfig {

	protected static Properties prop; // config file will be loaded here
	protected static InputStream input = null; // to read input file
	protected static boolean initialized = false; // true: config is initialized
	protected static final String defaultConfigName = "XConfig.txt"; // default config file name

	
	/**
	 * @return initialized status
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * To initialize CommonConfig using defaultConfigName 
	 * 
	 * @return true: initialized false: not initialized
	 */
	public static boolean Initialize()
	{
		return Initialize(defaultConfigName);
	}
	/**
	 * To initialize CommonConfig
	 * 
	 * @param _configFileName
	 *            config file addr and name (default addr is project root)
	 * @return true: initialized false: not initialized
	 */
	public static boolean Initialize(String _configFileName) {
		XLogger.Log("CommonConfig", "Initializing Config from "
				+ _configFileName, SeverityLevel.INFO);
		prop = new Properties();
		input = null;
		initialized = false;
		try {
			input = new FileInputStream(_configFileName);
			// ACTIVATE IN CASE OF CLASSPATH USING
			// input =
			// CommonConfig.class.getClassLoader().getResourceAsStream(configFileName);
			if (input == null) {
				XLogger.Log("CommonConfig",
						"Initialization Failed! Unable to find config file: "
								+ _configFileName, SeverityLevel.ERROR);
				return initialized;
			}
			// load a properties file
			prop.load(input);
			initialized = true;
		} catch (IOException ex) {
			XLogger.Log("CommonConfig", "Initialization Failed! Exception: "
					+ ex.getMessage(), SeverityLevel.ERROR);
		}
		return initialized;
	}

	/**
	 * To retrieve an double config item from config file
	 * @see GetConfigStrItem
	 * @param itemName name of config item to retrieve
	 * @return on success: value of requested item on failure: 0 (+ log entry)
	 */	
	public static double GetConfigDoubleItem(String itemName)
	{
		try
		{
			return Double.parseDouble(GetConfigStrItem(itemName));
		}
		catch (NumberFormatException ex)
		{
			XLogger.Log("CommonConfig", "Illegal Double Config Item: " + itemName
					+ " Please check config file.",
					SeverityLevel.WARNING);
			return 0;			
		}
	}
	
	/**
	 * To retrieve an int config item from config file
	 * @see GetConfigStrItem
	 * @param itemName name of config item to retrieve
	 * @return on success: value of requested item on failure: 0 (+ log entry)
	 */	
	public static int GetConfigIntItem(String itemName)
	{
		try
		{
			return Integer.parseInt(GetConfigStrItem(itemName));
		}
		catch (NumberFormatException ex)
		{
			XLogger.Log("CommonConfig", "Illegal Int Config Item: " + itemName
					+ " Please check config file.",
					SeverityLevel.WARNING);
			return 0;			
		}
	}
	/**
	 * To retrieve a string config item from config file
	 * @param itemName name of config item to retrieve
	 * @return on success: value of requested item on failure: empty string (i.e. "") (+ log entry)
	 */
	public static String GetConfigStrItem(String itemName) {
		if (initialized) {
			try {
				String itemValue = prop.getProperty(itemName);
				if (itemValue != null)
					return itemValue;
				else {
					XLogger.Log("CommonConfig", "Null Config Item: " + itemName
							+ " Please check config file.",
							SeverityLevel.WARNING);
					return "";
				}
			} catch (Exception ex) {
				XLogger.Log("CommonConfig", "Error in reading Config Item: "
						+ itemName + " Please check config file.",
						SeverityLevel.ERROR);
				return "";
			}
		} else
			XLogger.Log("CommonConfig", "Reading Config Item: " + itemName
					+ " while CommonConfig is not initialized!!",
					SeverityLevel.WARNING);
		return "";
	}

}
