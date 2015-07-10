/**
 * Project: Xtream
 * Module: Simple Log Formatter
 * Task: Format log text
 * Last Modify: Mar 31, 2015
 * Created: Mar 31, 2015
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogSimpleFormatter extends Formatter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	// this method is called for every log records
	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);

		// Append Log Level
		int recLevel = rec.getLevel().intValue();
		if (recLevel == Level.SEVERE.intValue())
			buf.append("[ERROR]");
		else if (recLevel == Level.WARNING.intValue())
			buf.append("[WARNING]");
		else if (recLevel == Level.INFO.intValue())
			buf.append("[INFO]");
		else if (recLevel == Level.FINE.intValue())
			buf.append("[DEBUG]");
		else
			buf.append("[UNKNOWN]");

		// Append Log Time
		buf.append(calcDate(rec.getMillis()));

		// Append Log Message
		buf.append(formatMessage(rec));

		// Append Next Line
		buf.append("\n");

		return buf.toString();
	}

	private String calcDate(long millisecs) {
		SimpleDateFormat date_format = new SimpleDateFormat(" MMM dd,yyyy HH:mm ");
		Date resultdate = new Date(millisecs);
		return date_format.format(resultdate);
	}

}
