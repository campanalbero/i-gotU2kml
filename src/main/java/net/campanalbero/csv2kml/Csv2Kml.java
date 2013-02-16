package net.campanalbero.csv2kml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Csv2Kml {
	private static final String ICON_URL = "http://campanalbero.net/icon/";
	private final BufferedWriter writer;
	private final BufferedReader reader;

	public Csv2Kml(String input, String output) throws IOException {
		reader = new BufferedReader(new FileReader(input));
		writer = new BufferedWriter(new FileWriter(output));
	}

	private String getIconStyle(String hour, String minute) {
		return "<Style id=\"" + hour + ":" + minute + "\"><IconStyle><Icon><href>" + ICON_URL + hour + minute +".png</href></Icon></IconStyle></Style>";
	}

	/**
	 * e.g. 
	 * <pre>
	 * <Style id=\"09:00\"><IconStyle><Icon><href>http://campanalbero.net/icon/0900.png</href></Icon></IconStyle></Style>
	 * </pre>
	 * @return expresions that are 0 - 23 o'clock
	 */
	private String[] getIconExpressions() {
		String[] icon = new String[24];
		for (int i = 0; i < 24; i++) {
			if (i < 10) {
				icon[i] = getIconStyle("0" + i, "00");
			} else {
				icon[i] = getIconStyle("" + i, "00");
			}
		}
		return icon;
	}

	private void writeIcon(String hh, String latitude, String longitude) throws IOException {
		writer.write("<Placemark>\n");
		writer.write("<styleUrl>#" + hh + ":00</styleUrl>\n");
		writer.write("<name>" + hh + ":00</name>\n");
		writer.write("<Point>\n");
		writer.write("<coordinates>\n");
		writer.write(latitude + "," + longitude + ",0.0\n");
		writer.write("</coordinates>\n");
		writer.write("</Point>\n");
		writer.write("</Placemark>\n\n");
	}

	private void writeStart() throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");
		writer.write("<Document>\n");
	}

	private void writeEnd() throws IOException {
		writer.write("</Document>\n</kml>\n");
	}

	private void writeIconStyle() throws IOException {
		String[] icons = getIconExpressions();
		for (String icon : icons) {
			writer.write(icon + "\n");
		}
		writer.write("\n");
	}

	private String getHH(String hhmmss) {
		return hhmmss.split(":")[0].trim();
	}

	private void writePlacemarkStart(String hh) throws IOException {
		writer.write("<Placemark>\n");
		writer.write("<name>" + hh + ":00 - " + hh + ":59</name>\n");
		writer.write("<LineString>\n");
		writer.write("<coordinates>\n");
	}

	private void writePlacemarkEnd() throws IOException {
		writer.write("</coordinates>\n");
		writer.write("</LineString>\n");
		writer.write("</Placemark>\n\n");
	}

	public void run() throws IOException {
		try {
			writeStart();
			writeIconStyle();

			reader.readLine(); // skip 1st line
			String str = reader.readLine();
			String[] parsed = str.split(",");
			String[] parsedTime = parsed[1].split(":");
			int lastHour = Integer.parseInt(parsedTime[0].trim());
			int lastMinute = Integer.parseInt(parsedTime[1].trim());
			
			String hh = getHH(parsed[1]);
			writeIcon(hh, parsed[3], parsed[2]);
			writePlacemarkStart(hh);
			writer.write(parsed[3] + "," + parsed[2] + ", 0.0\n");
			
			while (str != null) {
				parsed = str.split(",");
				parsedTime = parsed[1].split(":");
				int hour = Integer.parseInt(parsedTime[0].trim());
				int minute = Integer.parseInt(parsedTime[1].trim());
				if (hour == lastHour && minute == lastMinute) {
					// do nothing
				} else if (hour == lastHour && minute != lastMinute) {
					// write normal
					writer.write(parsed[3] + "," + parsed[2] + ", 0.0\n");
				} else {
					// write icon and normal
					writer.write(parsed[3] + "," + parsed[2] + ", 0.0\n");
					writePlacemarkEnd();
					
					hh = getHH(parsed[1]);
					writeIcon(hh, parsed[3], parsed[2]);
					writePlacemarkStart(hh);
					writer.write(parsed[3] + "," + parsed[2] + ", 0.0\n");
				}
				
				// update last hour
				lastHour = hour;
				lastMinute = minute;
				str = reader.readLine();
			}
			
			writer.write(parsed[3] + "," + parsed[2] + ", 0.0\n");
			writePlacemarkEnd();

			// last icon and line don't fit
			writeIcon(String.valueOf(Integer.parseInt(getHH(parsed[1])) + 1), parsed[3], parsed[2]);
			writeEnd();
			writer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} finally {
					if (writer != null) {
						writer.close();
					}
				}
			} else {
				if (writer != null) {
					writer.close();
				}
			}
		}
	}
}
