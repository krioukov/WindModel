import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;

public class DataReaderNREL {

	public boolean verbose;

	public WindDataStorage storage;

	// public Hashtable table;

	public DataReaderNREL(WindDataStorage storage) {

		this.verbose = false;

		// this.table = table;

		this.storage = storage;

	}

	public Calendar parseTimestamp(String timestamp) throws Exception {
		/*
		 * * we specify Locale.US since months are in english
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US);
		Date d = sdf.parse(timestamp);
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		return cal;
	}

	public void parseResultLineKey(String line, int rowCounter, String[] table)
			throws Exception {

		// System.out.println(line);

		StringTokenizer token = new StringTokenizer(line, ",");

		String currentToken = token.nextToken();
		String date = currentToken.trim();

		currentToken = token.nextToken();
		double windSpeed = Double.parseDouble(currentToken.trim());

		currentToken = token.nextToken();
		double ratedPower = Double.parseDouble(currentToken.trim());

		currentToken = token.nextToken();
		double scoreLight = Double.parseDouble(currentToken.trim());

		currentToken = token.nextToken();
		double correctedScore = Double.parseDouble(currentToken.trim());

		if (this.verbose) {

			// System.out.println("Date: " + date);
			// System.out.println("windSpeed: " + windSpeed);
			// System.out.println("ratedPower: " + ratedPower);
			// System.out.println("scoreLight: " + scoreLight);
			// System.out.println("correctedScore: " + correctedScore);

		}

		table[rowCounter] = date;

	}

	/*
	 * Method for parsing one line of a NREL wind measurement file and write the
	 * measurement value into the array provided as argument.
	 */
	public void parseResultLineValue(String line, int rowCounter, double[] table)
			throws Exception {

		// System.out.println(line);

		StringTokenizer token = new StringTokenizer(line, ",");

		String currentToken = token.nextToken();
		String date = currentToken.trim();

		currentToken = token.nextToken();
		double windSpeed = Double.parseDouble(currentToken.trim());

		currentToken = token.nextToken();
		double ratedPower = Double.parseDouble(currentToken.trim());

		currentToken = token.nextToken();
		double scoreLight = Double.parseDouble(currentToken.trim());

		currentToken = token.nextToken();
		double correctedScore = Double.parseDouble(currentToken.trim());

		if (this.verbose) {

			// System.out.println("Date: " + date);
			// System.out.println("windSpeed: " + windSpeed);
			// System.out.println("ratedPower: " + ratedPower);
			// System.out.println("scoreLight: " + scoreLight);
			// System.out.println("correctedScore: " + correctedScore);

		}

		table[rowCounter] = correctedScore;

	}

	public void readFileValue(String fileName, double[] table) throws Exception {

		if (this.verbose) {

			System.out.println("Starting to read data " + fileName);

		}

		File file = new File(fileName);

		if (file.exists()) {

			// row counter
			int rowCounter = 0;

			// ...checks on aFile are elided
			StringBuffer contents = new StringBuffer();

			// declared here only to make visible to finally clause
			BufferedReader input = null;
			try {
				// use buffering, reading one line at a time
				// FileReader always assumes default encoding is OK!
				input = new BufferedReader(new FileReader(file));
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) {

					if (line.charAt(0) == 'D') {

						// Header or footer, do nothing.

					} else {

						parseResultLineValue(line, rowCounter - 1, table);

					}

					if (this.verbose) {

						if (rowCounter % 1000 == 0) {

							System.out.println(rowCounter + " lines read.");

						}

					}

					rowCounter++;

				}

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (input != null) {
						// flush and close both "input" and its underlying
						// FileReader
						input.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		} else {

			System.out.println("File missing. Exiting.");

		}

	}

	public void readFileKey(String fileName, String[] table) throws Exception {

		if (this.verbose) {

			System.out.println("Starting to read data " + fileName);

		}

		File file = new File(fileName);

		if (file.exists()) {

			// row counter
			int rowCounter = 0;

			// ...checks on aFile are elided
			StringBuffer contents = new StringBuffer();

			// declared here only to make visible to finally clause
			BufferedReader input = null;
			try {
				// use buffering, reading one line at a time
				// FileReader always assumes default encoding is OK!
				input = new BufferedReader(new FileReader(file));
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) {

					if (line.charAt(0) == 'D') {

						// Header or footer, do nothing.

					} else {

						parseResultLineKey(line, rowCounter - 1, table);

					}

					if (this.verbose) {

						if (rowCounter % 1000 == 0) {

							System.out.println(rowCounter + " lines read.");

						}

					}

					rowCounter++;

				}

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (input != null) {
						// flush and close both "input" and its underlying
						// FileReader
						input.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		} else {

			System.out.println("File missing. Exiting.");

		}

	}

	/*
	 * Row counter.
	 */
	public int readFileRowCounter(String fileName) throws Exception {

		// row counter
		int rowCounter = 0;

		if (this.verbose) {

			System.out.println("Starting to read data " + fileName);

		}

		// Hashtable table = null;
		//
		// if (fileName.equals("tehachapi")) {
		// table = this.storage.tehachapi;
		// } else if (fileName.equals("clark")) {
		// table = this.storage.clark;
		// } else if (fileName.equals("solano")) {
		// table = this.storage.solano;
		// } else if (fileName.equals("sanGorgonio")) {
		// table = this.storage.sanGorgonio;
		// } else if (fileName.equals("sanDiego")) {
		// table = this.storage.sanDiego;
		// } else if (fileName.equals("humboldt")) {
		// table = this.storage.humboldt;
		// } else if (fileName.equals("imperial")) {
		// table = this.storage.imperial;
		// } else if (fileName.equals("altamont")) {
		// table = this.storage.altamont;
		// } else if (fileName.equals("monterey")) {
		// table = this.storage.monterey;
		// } else if (fileName.equals("pacheco")) {
		// table = this.storage.pacheco;
		// }

		File file = new File(fileName);

		if (file.exists()) {

			// ...checks on aFile are elided
			StringBuffer contents = new StringBuffer();

			// declared here only to make visible to finally clause
			BufferedReader input = null;
			try {
				// use buffering, reading one line at a time
				// FileReader always assumes default encoding is OK!
				input = new BufferedReader(new FileReader(file));
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) {

					if (line.charAt(0) == 'D') {

						// Header or footer, do nothing.

					} else {

					}

					if (this.verbose) {

						if (rowCounter % 1000 == 0) {

							System.out.println(rowCounter + " lines read.");

						}

					}

					rowCounter++;

				}

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (input != null) {
						// flush and close both "input" and its underlying
						// FileReader
						input.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		} else {

			System.out.println("File missing. Exiting.");

		}

		return (rowCounter - 1);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
