package net.campanalbero;

import java.io.IOException;

import net.campanalbero.csv2kml.Csv2Kml;

public class Main {
	public static void main(String[] args) {
		try {
			Csv2Kml c2k = new Csv2Kml(args[0], args[1]);
			c2k.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
