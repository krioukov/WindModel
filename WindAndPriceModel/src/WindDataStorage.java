import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WindDataStorage {

	// Debugging variable.
	public static boolean verbose = false;

	/*
	 * +++++++++++++++++++++ WIND DATA +++++++++++++++++++++
	 */

	/*
	 * Parameters specific to NREL wind data. Measurement points correspond to
	 * 30MW of installed wind capacity each.
	 */
	public static double capacityPerMeasurement = 30;

	// Reader class for NREL wind data.
	public DataReaderNREL rNREL;

	// Arrays for dates.
	public String[] dates_10_min;
	public String[] dates_60_min;

	// Arrays for locational wind power measurements. 10 minute measurements.
	public double[] tehachapi;
	public double[] clark;
	public double[] solano;
	public double[] sanGorgonio;
	public double[] sanDiego;
	public double[] humboldt;
	public double[] imperial;
	public double[] altamont;
	public double[] monterey;
	public double[] pacheco;

	// Total wind power in California now and in deep integration scenario. 10
	// minute measurements.
	public double[] california_now;
	public double[] california_deep;

	// Total wind power in California now and in deep integration scenario. 60
	// minute means.
	public double[] california_now_60_mean;
	public double[] california_deep_60_mean;

	// Total wind power in California now and in deep integration scenario. 60
	// minute means for each hour and each season.
	public double[] california_now_60_mean_spring;
	public double[] california_now_60_mean_summer;
	public double[] california_now_60_mean_fall;
	public double[] california_now_60_mean_winter;
	public double[] california_deep_60_mean_spring;
	public double[] california_deep_60_mean_summer;
	public double[] california_deep_60_mean_fall;
	public double[] california_deep_60_mean_winter;

	// Wind power installed capacity now and deep integration scenario (33%).
	public static double tehachapi_now = 722;
	public static double tehachapi_deep = 6459;
	public static double clark_now = 0;
	public static double clark_deep = 1500;
	public static double solano_now = 327;
	public static double solano_deep = 583.45;
	public static double sanGorgonio_now = 624;
	public static double sanGorgonio_deep = 528;
	public static double sanDiego_now = 0;
	public static double sanDiego_deep = 1527;
	public static double humboldt_now = 0;
	public static double humboldt_deep = 218.2;
	public static double imperial_now = 0;
	public static double imperial_deep = 547.9;
	public static double altamont_now = 954;
	public static double altamont_deep = 14;
	public static double monterey_now = 118;
	public static double monterey_deep = 0;
	public static double pacheco_now = 21;
	public static double pacheco_deep = 0;

	public static double capacity_now = 2766;
	public static double capacity_deep = 11377.55;

	public WindDataStorage() {

		this.rNREL = new DataReaderNREL(this);

	}

	/*
	 * Method for initializing data storage.
	 */
	public void init() throws Exception {

		/*
		 * Create raw wind database.
		 */
		this.createWindDatabase();

		/*
		 * Average wind data across locations.
		 */
		this.computeCaliforniaWindData();

		/*
		 * Average wind data across hours.
		 */
		this.computeHourlyMeans();

		/*
		 * Average wind data to obtain hourly means for different seasons.
		 */
		this.computeSeasonalHourlyMeans();

		// this.printWindHourlySeasonalMeans();

	}

	/*
	 * WIND DATA METHOD; Method for computing hourly means for California data.
	 */
	public void computeHourlyMeans() {

		// System.out.println(this.california_now.length);

		int numberOfMeasurementsPerHour = 6;

		this.california_now_60_mean = new double[this.dates_10_min.length
				/ numberOfMeasurementsPerHour];

		this.california_deep_60_mean = new double[this.dates_10_min.length
				/ numberOfMeasurementsPerHour];

		double currentMean_now = 0;

		double currentMean_deep = 0;

		for (int i = 0; i < this.dates_10_min.length; i++) {

			currentMean_now += this.california_now[i];

			currentMean_deep += this.california_deep[i];

			/*
			 * Sum up 10 minutes values and compute hourly average.
			 */
			if ((i + 1) % numberOfMeasurementsPerHour == 0) {

				// System.out.println((i + 1) / numberOfMeasurementsPerHour -
				// 1);
				//
				// System.out.println(this.dates_10_min[i
				// - (numberOfMeasurementsPerHour - 1)]);

				/*
				 * Get hourly dates from 10 min array.
				 */
				this.dates_60_min[((i + 1) / numberOfMeasurementsPerHour) - 1] = this.dates_10_min[i
						- (numberOfMeasurementsPerHour - 1)];

				/*
				 * Store hourly averages in corresponding arrays.
				 */

				this.california_now_60_mean[((i + 1) / numberOfMeasurementsPerHour) - 1] = currentMean_now
						/ (double) numberOfMeasurementsPerHour;

				this.california_deep_60_mean[((i + 1) / numberOfMeasurementsPerHour) - 1] = currentMean_deep
						/ (double) numberOfMeasurementsPerHour;

				currentMean_now = 0;

				currentMean_deep = 0;

			}

		}

	}

	/*
	 * WIND DATA METHOD; Method for inferring the actual amount of generated
	 * wind power in California based on the measurements and installed
	 * capacity.
	 */
	public void computeCaliforniaWindData() {

		if (verbose) {

			System.out
					.println("Starting to populate California wind database...");

		}

		for (int i = 0; i < this.dates_10_min.length; i++) {

			this.california_now[i] = (this.tehachapi_now * tehachapi[i] / this.capacityPerMeasurement)
					+ (this.clark_now * clark[i] / this.capacityPerMeasurement)
					+ (this.solano_now * solano[i] / this.capacityPerMeasurement)
					+ (this.sanGorgonio_now * sanGorgonio[i] / this.capacityPerMeasurement)
					+ (this.sanDiego_now * sanDiego[i] / this.capacityPerMeasurement)
					+ (this.humboldt_now * humboldt[i] / this.capacityPerMeasurement)
					+ (this.imperial_now * imperial[i] / this.capacityPerMeasurement)
					+ (this.altamont_now * altamont[i] / this.capacityPerMeasurement)
					+ (this.monterey_now * monterey[i] / this.capacityPerMeasurement)
					+ (this.pacheco_now * pacheco[i] / this.capacityPerMeasurement);

			this.california_deep[i] = (this.tehachapi_deep * tehachapi[i] / this.capacityPerMeasurement)
					+ (this.clark_deep * clark[i] / this.capacityPerMeasurement)
					+ (this.solano_deep * solano[i] / this.capacityPerMeasurement)
					+ (this.sanGorgonio_deep * sanGorgonio[i] / this.capacityPerMeasurement)
					+ (this.sanDiego_deep * sanDiego[i] / this.capacityPerMeasurement)
					+ (this.humboldt_deep * humboldt[i] / this.capacityPerMeasurement)
					+ (this.imperial_deep * imperial[i] / this.capacityPerMeasurement)
					+ (this.altamont_deep * altamont[i] / this.capacityPerMeasurement)
					+ (this.monterey_deep * monterey[i] / this.capacityPerMeasurement)
					+ (this.pacheco_deep * pacheco[i] / this.capacityPerMeasurement);

			if (verbose) {

				if (i % 1000 == 0) {

					System.out.println(i + " lines written.");

				}

			}

		}

	}

	/*
	 * WIND DATA METHOD; Method for printing array of 10 minute measurements.
	 */
	public void printWindMeasurementData_10_min(double[] values) {

		for (int i = 0; i < this.dates_10_min.length; i++) {

			System.out.println(this.dates_10_min[i] + " " + values[i]);

		}

	}

	/*
	 * WIND DATA METHOD; Method for printing array of 60 minute measurements.
	 */
	public void printWindMeasurementData_60_min(double[] values) {

		for (int i = 0; i < this.dates_60_min.length; i++) {

			System.out.println(this.dates_60_min[i] + " " + values[i]);

		}

	}

	/*
	 * WIND DATA METHOD; Method for printing hourly seasonal means.
	 */
	public void printWindHourlySeasonalMeans() {

		System.out.println("Wind output now");

		System.out.print("Spring \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_now_60_mean_spring[i] + "\t");

		}

		System.out.print("\n");

		System.out.print("Summer \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_now_60_mean_summer[i] + "\t");

		}

		System.out.print("\n");

		System.out.print("Fall \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_now_60_mean_fall[i] + "\t");

		}

		System.out.print("\n");

		System.out.print("Winter \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_now_60_mean_winter[i] + "\t");

		}

		System.out.print("\n");

		System.out.println("Wind output with deep integration");

		System.out.print("Spring \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_deep_60_mean_spring[i] + "\t");

		}

		System.out.print("\n");

		System.out.print("Summer \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_deep_60_mean_summer[i] + "\t");

		}

		System.out.print("\n");

		System.out.print("Fall \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_deep_60_mean_fall[i] + "\t");

		}

		System.out.print("\n");

		System.out.print("Winter \t");

		for (int i = 0; i < 24; i++) {

			System.out.print(this.california_deep_60_mean_winter[i] + "\t");

		}

		System.out.print("\n");

	}

	public Calendar parseTimestamp(String timestamp) {

		timestamp = timestamp.trim();

		Calendar cal = Calendar.getInstance();

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.US);

			Date d;

			d = sdf.parse(timestamp);

			// Calendar cal = Calendar.getInstance();
			cal.setTime(d);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.print("xxx");

			e.printStackTrace();

		}

		return cal;

	}

	/*
	 * WIND DATA METHOD; Method for getting the row count of the NREL wind
	 * database of 2006.
	 */
	public int getNRELRowCount() throws Exception {

		return this.rNREL.readFileRowCounter("wind data/tehachapi.csv");

	}

	/*
	 * WIND DATA METHOD; Method for loading the wind data from different
	 * locations into memory.
	 */
	public void createWindDatabase() {

		int numRows = -1;

		try {
			numRows = this.getNRELRowCount();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (numRows != -1) {

			this.dates_10_min = new String[numRows];
			this.dates_60_min = new String[numRows / 6];

			this.tehachapi = new double[numRows];
			this.clark = new double[numRows];
			this.solano = new double[numRows];
			this.sanGorgonio = new double[numRows];
			this.sanDiego = new double[numRows];
			this.humboldt = new double[numRows];
			this.imperial = new double[numRows];
			this.altamont = new double[numRows];
			this.monterey = new double[numRows];
			this.pacheco = new double[numRows];

			this.california_now = new double[numRows];
			this.california_deep = new double[numRows];

		} else {

			System.out.println("ERROR in method createWindDatabase.");

		}

		try {

			this.rNREL
					.readFileKey("wind data/tehachapi.csv", this.dates_10_min);
			this.rNREL.readFileValue("wind data/tehachapi.csv", this.tehachapi);
			this.rNREL.readFileValue("wind data/clark.csv", this.clark);
			this.rNREL.readFileValue("wind data/solano.csv", this.solano);
			this.rNREL.readFileValue("wind data/san gorgonio.csv",
					this.sanGorgonio);
			this.rNREL.readFileValue("wind data/san diego.csv", this.sanDiego);
			this.rNREL.readFileValue("wind data/humboldt.csv", this.humboldt);
			this.rNREL.readFileValue("wind data/imperial.csv", this.imperial);
			this.rNREL.readFileValue("wind data/altamont.csv", this.altamont);
			this.rNREL.readFileValue("wind data/monterey.csv", this.monterey);
			this.rNREL.readFileValue("wind data/pacheco.csv", this.pacheco);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * WIND DATA METHOD;Method for computing hourly means for every day of every
	 * season.
	 */
	public void computeSeasonalHourlyMeans() throws Exception {

		this.california_now_60_mean_spring = new double[24];
		this.california_now_60_mean_summer = new double[24];
		this.california_now_60_mean_fall = new double[24];
		this.california_now_60_mean_winter = new double[24];
		this.california_deep_60_mean_spring = new double[24];
		this.california_deep_60_mean_summer = new double[24];
		this.california_deep_60_mean_fall = new double[24];
		this.california_deep_60_mean_winter = new double[24];

		// Initialize above arrays.
		for (int i = 0; i < 24; i++) {

			this.california_now_60_mean_spring[i] = 0;
			this.california_now_60_mean_summer[i] = 0;
			this.california_now_60_mean_fall[i] = 0;
			this.california_now_60_mean_winter[i] = 0;
			this.california_deep_60_mean_spring[i] = 0;
			this.california_deep_60_mean_summer[i] = 0;
			this.california_deep_60_mean_fall[i] = 0;
			this.california_deep_60_mean_winter[i] = 0;

		}

		/*
		 * Helper arrays taking track of number of values added to each field in
		 * order to take average in the end.
		 */
		int[] numberOfSpringValues = new int[24];
		int[] numberOfSummerValues = new int[24];
		int[] numberOfFallValues = new int[24];
		int[] numberOfWinterValues = new int[24];

		// Initialize above arrays.
		for (int i = 0; i < 24; i++) {

			numberOfSpringValues[i] = 0;
			numberOfSummerValues[i] = 0;
			numberOfFallValues[i] = 0;
			numberOfWinterValues[i] = 0;

		}

		// for (int i = 0; i < this.dates_60_min.length; i++) {
		//
		// System.out.println(this.dates_60_min[i]);
		// Calendar cal = this.parseTimestamp(this.dates_60_min[i].trim());
		// System.out.println("month: " + cal.get(cal.MONTH));
		// System.out.println("day of month: " + cal.get(cal.DAY_OF_MONTH));
		//
		// }

		for (int i = 0; i < this.california_now_60_mean.length; i++) {

			// System.out.println(this.california_now_60_mean[i]);

			Calendar cal = this.parseTimestamp(this.dates_60_min[i]);
			// System.out.println("month: " + cal.MONTH);
			// System.out.println("day of month: " + cal.DAY_OF_MONTH);

			if ((cal.get(Calendar.MONTH) == Calendar.MARCH && cal
					.get(Calendar.DAY_OF_MONTH) >= 20)
					|| cal.get(Calendar.MONTH) == Calendar.APRIL
					|| cal.get(Calendar.MONTH) == Calendar.MAY
					|| (cal.get(Calendar.MONTH) == Calendar.JUNE && cal
							.get(Calendar.DAY_OF_MONTH) < 21)) {

				/*
				 * Spring value.
				 */

				// System.out.println(this.dates_60_min[i]);

				switch (cal.get(Calendar.HOUR_OF_DAY)) {
				case 0:
					numberOfSpringValues[0] += 1;
					this.california_now_60_mean_spring[0] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[0] += this.california_deep_60_mean[i];
					break;
				case 1:
					numberOfSpringValues[1] += 1;
					this.california_now_60_mean_spring[1] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[1] += this.california_deep_60_mean[i];
					break;
				case 2:
					numberOfSpringValues[2] += 1;
					this.california_now_60_mean_spring[2] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[2] += this.california_deep_60_mean[i];
					break;
				case 3:
					numberOfSpringValues[3] += 1;
					this.california_now_60_mean_spring[3] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[3] += this.california_deep_60_mean[i];
					break;
				case 4:
					numberOfSpringValues[4] += 1;
					this.california_now_60_mean_spring[4] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[4] += this.california_deep_60_mean[i];
					break;
				case 5:
					numberOfSpringValues[5] += 1;
					this.california_now_60_mean_spring[5] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[5] += this.california_deep_60_mean[i];
					break;
				case 6:
					numberOfSpringValues[6] += 1;
					this.california_now_60_mean_spring[6] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[6] += this.california_deep_60_mean[i];
					break;
				case 7:
					numberOfSpringValues[7] += 1;
					this.california_now_60_mean_spring[7] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[7] += this.california_deep_60_mean[i];
					break;
				case 8:
					numberOfSpringValues[8] += 1;
					this.california_now_60_mean_spring[8] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[8] += this.california_deep_60_mean[i];
					break;
				case 9:
					numberOfSpringValues[9] += 1;
					this.california_now_60_mean_spring[9] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[9] += this.california_deep_60_mean[i];
					break;
				case 10:
					numberOfSpringValues[10] += 1;
					this.california_now_60_mean_spring[10] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[10] += this.california_deep_60_mean[i];
					break;
				case 11:
					numberOfSpringValues[11] += 1;
					this.california_now_60_mean_spring[11] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[11] += this.california_deep_60_mean[i];
					break;
				case 12:
					numberOfSpringValues[12] += 1;
					this.california_now_60_mean_spring[12] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[12] += this.california_deep_60_mean[i];
					break;
				case 13:
					numberOfSpringValues[13] += 1;
					this.california_now_60_mean_spring[13] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[13] += this.california_deep_60_mean[i];
					break;
				case 14:
					numberOfSpringValues[14] += 1;
					this.california_now_60_mean_spring[14] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[14] += this.california_deep_60_mean[i];
					break;
				case 15:
					numberOfSpringValues[15] += 1;
					this.california_now_60_mean_spring[15] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[15] += this.california_deep_60_mean[i];
					break;
				case 16:
					numberOfSpringValues[16] += 1;
					this.california_now_60_mean_spring[16] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[16] += this.california_deep_60_mean[i];
					break;
				case 17:
					numberOfSpringValues[17] += 1;
					this.california_now_60_mean_spring[17] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[17] += this.california_deep_60_mean[i];
					break;
				case 18:
					numberOfSpringValues[18] += 1;
					this.california_now_60_mean_spring[18] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[18] += this.california_deep_60_mean[i];
					break;
				case 19:
					numberOfSpringValues[19] += 1;
					this.california_now_60_mean_spring[19] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[19] += this.california_deep_60_mean[i];
					break;
				case 20:
					numberOfSpringValues[20] += 1;
					this.california_now_60_mean_spring[20] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[20] += this.california_deep_60_mean[i];
					break;
				case 21:
					numberOfSpringValues[21] += 1;
					this.california_now_60_mean_spring[21] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[21] += this.california_deep_60_mean[i];
					break;
				case 22:
					numberOfSpringValues[22] += 1;
					this.california_now_60_mean_spring[22] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[22] += this.california_deep_60_mean[i];
					break;
				case 23:
					numberOfSpringValues[23] += 1;
					this.california_now_60_mean_spring[23] += this.california_now_60_mean[i];
					this.california_deep_60_mean_spring[23] += this.california_deep_60_mean[i];
					break;
				default:
					System.out
							.println("ERROR in switch statement of method computeSeasonalHourlyMeans.");
					break;
				}

			}

			if ((cal.get(Calendar.MONTH) == Calendar.JUNE && cal
					.get(Calendar.DAY_OF_MONTH) >= 21)
					|| cal.get(Calendar.MONTH) == Calendar.JULY
					|| cal.get(Calendar.MONTH) == Calendar.AUGUST
					|| (cal.get(Calendar.MONTH) == Calendar.SEPTEMBER && cal
							.get(Calendar.DAY_OF_MONTH) < 23)) {

				/*
				 * Summer value.
				 */

				switch (cal.get(Calendar.HOUR_OF_DAY)) {
				case 0:
					numberOfSummerValues[0] += 1;
					this.california_now_60_mean_summer[0] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[0] += this.california_deep_60_mean[i];
					break;
				case 1:
					numberOfSummerValues[1] += 1;
					this.california_now_60_mean_summer[1] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[1] += this.california_deep_60_mean[i];
					break;
				case 2:
					numberOfSummerValues[2] += 1;
					this.california_now_60_mean_summer[2] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[2] += this.california_deep_60_mean[i];
					break;
				case 3:
					numberOfSummerValues[3] += 1;
					this.california_now_60_mean_summer[3] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[3] += this.california_deep_60_mean[i];
					break;
				case 4:
					numberOfSummerValues[4] += 1;
					this.california_now_60_mean_summer[4] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[4] += this.california_deep_60_mean[i];
					break;
				case 5:
					numberOfSummerValues[5] += 1;
					this.california_now_60_mean_summer[5] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[5] += this.california_deep_60_mean[i];
					break;
				case 6:
					numberOfSummerValues[6] += 1;
					this.california_now_60_mean_summer[6] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[6] += this.california_deep_60_mean[i];
					break;
				case 7:
					numberOfSummerValues[7] += 1;
					this.california_now_60_mean_summer[7] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[7] += this.california_deep_60_mean[i];
					break;
				case 8:
					numberOfSummerValues[8] += 1;
					this.california_now_60_mean_summer[8] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[8] += this.california_deep_60_mean[i];
					break;
				case 9:
					numberOfSummerValues[9] += 1;
					this.california_now_60_mean_summer[9] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[9] += this.california_deep_60_mean[i];
					break;
				case 10:
					numberOfSummerValues[10] += 1;
					this.california_now_60_mean_summer[10] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[10] += this.california_deep_60_mean[i];
					break;
				case 11:
					numberOfSummerValues[11] += 1;
					this.california_now_60_mean_summer[11] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[11] += this.california_deep_60_mean[i];
					break;
				case 12:
					numberOfSummerValues[12] += 1;
					this.california_now_60_mean_summer[12] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[12] += this.california_deep_60_mean[i];
					break;
				case 13:
					numberOfSummerValues[13] += 1;
					this.california_now_60_mean_summer[13] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[13] += this.california_deep_60_mean[i];
					break;
				case 14:
					numberOfSummerValues[14] += 1;
					this.california_now_60_mean_summer[14] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[14] += this.california_deep_60_mean[i];
					break;
				case 15:
					numberOfSummerValues[15] += 1;
					this.california_now_60_mean_summer[15] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[15] += this.california_deep_60_mean[i];
					break;
				case 16:
					numberOfSummerValues[16] += 1;
					this.california_now_60_mean_summer[16] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[16] += this.california_deep_60_mean[i];
					break;
				case 17:
					numberOfSummerValues[17] += 1;
					this.california_now_60_mean_summer[17] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[17] += this.california_deep_60_mean[i];
					break;
				case 18:
					numberOfSummerValues[18] += 1;
					this.california_now_60_mean_summer[18] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[18] += this.california_deep_60_mean[i];
					break;
				case 19:
					numberOfSummerValues[19] += 1;
					this.california_now_60_mean_summer[19] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[19] += this.california_deep_60_mean[i];
					break;
				case 20:
					numberOfSummerValues[20] += 1;
					this.california_now_60_mean_summer[20] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[20] += this.california_deep_60_mean[i];
					break;
				case 21:
					numberOfSummerValues[21] += 1;
					this.california_now_60_mean_summer[21] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[21] += this.california_deep_60_mean[i];
					break;
				case 22:
					numberOfSummerValues[22] += 1;
					this.california_now_60_mean_summer[22] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[22] += this.california_deep_60_mean[i];
					break;
				case 23:
					numberOfSummerValues[23] += 1;
					this.california_now_60_mean_summer[23] += this.california_now_60_mean[i];
					this.california_deep_60_mean_summer[23] += this.california_deep_60_mean[i];
					break;
				default:
					System.out
							.println("ERROR in switch statement of method computeSeasonalHourlyMeans.");
					break;
				}

			}

			if ((cal.get(Calendar.MONTH) == Calendar.SEPTEMBER && cal
					.get(Calendar.DAY_OF_MONTH) >= 23)
					|| cal.get(Calendar.MONTH) == Calendar.OCTOBER
					|| cal.get(Calendar.MONTH) == Calendar.NOVEMBER
					|| (cal.get(Calendar.MONTH) == Calendar.DECEMBER && cal
							.get(Calendar.DAY_OF_MONTH) < 22)) {

				/*
				 * Fall value.
				 */

				switch (cal.get(Calendar.HOUR_OF_DAY)) {
				case 0:
					numberOfFallValues[0] += 1;
					this.california_now_60_mean_fall[0] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[0] += this.california_deep_60_mean[i];
					break;
				case 1:
					numberOfFallValues[1] += 1;
					this.california_now_60_mean_fall[1] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[1] += this.california_deep_60_mean[i];
					break;
				case 2:
					numberOfFallValues[2] += 1;
					this.california_now_60_mean_fall[2] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[2] += this.california_deep_60_mean[i];
					break;
				case 3:
					numberOfFallValues[3] += 1;
					this.california_now_60_mean_fall[3] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[3] += this.california_deep_60_mean[i];
					break;
				case 4:
					numberOfFallValues[4] += 1;
					this.california_now_60_mean_fall[4] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[4] += this.california_deep_60_mean[i];
					break;
				case 5:
					numberOfFallValues[5] += 1;
					this.california_now_60_mean_fall[5] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[5] += this.california_deep_60_mean[i];
					break;
				case 6:
					numberOfFallValues[6] += 1;
					this.california_now_60_mean_fall[6] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[6] += this.california_deep_60_mean[i];
					break;
				case 7:
					numberOfFallValues[7] += 1;
					this.california_now_60_mean_fall[7] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[7] += this.california_deep_60_mean[i];
					break;
				case 8:
					numberOfFallValues[8] += 1;
					this.california_now_60_mean_fall[8] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[8] += this.california_deep_60_mean[i];
					break;
				case 9:
					numberOfFallValues[9] += 1;
					this.california_now_60_mean_fall[9] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[9] += this.california_deep_60_mean[i];
					break;
				case 10:
					numberOfFallValues[10] += 1;
					this.california_now_60_mean_fall[10] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[10] += this.california_deep_60_mean[i];
					break;
				case 11:
					numberOfFallValues[11] += 1;
					this.california_now_60_mean_fall[11] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[11] += this.california_deep_60_mean[i];
					break;
				case 12:
					numberOfFallValues[12] += 1;
					this.california_now_60_mean_fall[12] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[12] += this.california_deep_60_mean[i];
					break;
				case 13:
					numberOfFallValues[13] += 1;
					this.california_now_60_mean_fall[13] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[13] += this.california_deep_60_mean[i];
					break;
				case 14:
					numberOfFallValues[14] += 1;
					this.california_now_60_mean_fall[14] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[14] += this.california_deep_60_mean[i];
					break;
				case 15:
					numberOfFallValues[15] += 1;
					this.california_now_60_mean_fall[15] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[15] += this.california_deep_60_mean[i];
					break;
				case 16:
					numberOfFallValues[16] += 1;
					this.california_now_60_mean_fall[16] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[16] += this.california_deep_60_mean[i];
					break;
				case 17:
					numberOfFallValues[17] += 1;
					this.california_now_60_mean_fall[17] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[17] += this.california_deep_60_mean[i];
					break;
				case 18:
					numberOfFallValues[18] += 1;
					this.california_now_60_mean_fall[18] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[18] += this.california_deep_60_mean[i];
					break;
				case 19:
					numberOfFallValues[19] += 1;
					this.california_now_60_mean_fall[19] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[19] += this.california_deep_60_mean[i];
					break;
				case 20:
					numberOfFallValues[20] += 1;
					this.california_now_60_mean_fall[20] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[20] += this.california_deep_60_mean[i];
					break;
				case 21:
					numberOfFallValues[21] += 1;
					this.california_now_60_mean_fall[21] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[21] += this.california_deep_60_mean[i];
					break;
				case 22:
					numberOfFallValues[22] += 1;
					this.california_now_60_mean_fall[22] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[22] += this.california_deep_60_mean[i];
					break;
				case 23:
					numberOfFallValues[23] += 1;
					this.california_now_60_mean_fall[23] += this.california_now_60_mean[i];
					this.california_deep_60_mean_fall[23] += this.california_deep_60_mean[i];
					break;
				default:
					System.out
							.println("ERROR in switch statement of method computeSeasonalHourlyMeans.");
					break;

				}

			}

			if ((cal.get(Calendar.MONTH) == Calendar.DECEMBER && cal
					.get(Calendar.DAY_OF_MONTH) >= 22)
					|| cal.get(Calendar.MONTH) == Calendar.JANUARY
					|| cal.get(Calendar.MONTH) == Calendar.FEBRUARY
					|| (cal.get(Calendar.MONTH) == Calendar.MARCH && cal
							.get(Calendar.DAY_OF_MONTH) < 20)) {

				/*
				 * Winter value.
				 */

				switch (cal.get(Calendar.HOUR_OF_DAY)) {
				case 0:
					numberOfWinterValues[0] += 1;
					this.california_now_60_mean_winter[0] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[0] += this.california_deep_60_mean[i];
					break;
				case 1:
					numberOfWinterValues[1] += 1;
					this.california_now_60_mean_winter[1] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[1] += this.california_deep_60_mean[i];
					break;
				case 2:
					numberOfWinterValues[2] += 1;
					this.california_now_60_mean_winter[2] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[2] += this.california_deep_60_mean[i];
					break;
				case 3:
					numberOfWinterValues[3] += 1;
					this.california_now_60_mean_winter[3] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[3] += this.california_deep_60_mean[i];
					break;
				case 4:
					numberOfWinterValues[4] += 1;
					this.california_now_60_mean_winter[4] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[4] += this.california_deep_60_mean[i];
					break;
				case 5:
					numberOfWinterValues[5] += 1;
					this.california_now_60_mean_winter[5] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[5] += this.california_deep_60_mean[i];
					break;
				case 6:
					numberOfWinterValues[6] += 1;
					this.california_now_60_mean_winter[6] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[6] += this.california_deep_60_mean[i];
					break;
				case 7:
					numberOfWinterValues[7] += 1;
					this.california_now_60_mean_winter[7] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[7] += this.california_deep_60_mean[i];
					break;
				case 8:
					numberOfWinterValues[8] += 1;
					this.california_now_60_mean_winter[8] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[8] += this.california_deep_60_mean[i];
					break;
				case 9:
					numberOfWinterValues[9] += 1;
					this.california_now_60_mean_winter[9] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[9] += this.california_deep_60_mean[i];
					break;
				case 10:
					numberOfWinterValues[10] += 1;
					this.california_now_60_mean_winter[10] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[10] += this.california_deep_60_mean[i];
					break;
				case 11:
					numberOfWinterValues[11] += 1;
					this.california_now_60_mean_winter[11] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[11] += this.california_deep_60_mean[i];
					break;
				case 12:
					numberOfWinterValues[12] += 1;
					this.california_now_60_mean_winter[12] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[12] += this.california_deep_60_mean[i];
					break;
				case 13:
					numberOfWinterValues[13] += 1;
					this.california_now_60_mean_winter[13] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[13] += this.california_deep_60_mean[i];
					break;
				case 14:
					numberOfWinterValues[14] += 1;
					this.california_now_60_mean_winter[14] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[14] += this.california_deep_60_mean[i];
					break;
				case 15:
					numberOfWinterValues[15] += 1;
					this.california_now_60_mean_winter[15] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[15] += this.california_deep_60_mean[i];
					break;
				case 16:
					numberOfWinterValues[16] += 1;
					this.california_now_60_mean_winter[16] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[16] += this.california_deep_60_mean[i];
					break;
				case 17:
					numberOfWinterValues[17] += 1;
					this.california_now_60_mean_winter[17] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[17] += this.california_deep_60_mean[i];
					break;
				case 18:
					numberOfWinterValues[18] += 1;
					this.california_now_60_mean_winter[18] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[18] += this.california_deep_60_mean[i];
					break;
				case 19:
					numberOfWinterValues[19] += 1;
					this.california_now_60_mean_winter[19] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[19] += this.california_deep_60_mean[i];
					break;
				case 20:
					numberOfWinterValues[20] += 1;
					this.california_now_60_mean_winter[20] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[20] += this.california_deep_60_mean[i];
					break;
				case 21:
					numberOfWinterValues[21] += 1;
					this.california_now_60_mean_winter[21] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[21] += this.california_deep_60_mean[i];
					break;
				case 22:
					numberOfWinterValues[22] += 1;
					this.california_now_60_mean_winter[22] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[22] += this.california_deep_60_mean[i];
					break;
				case 23:
					numberOfWinterValues[23] += 1;
					this.california_now_60_mean_winter[23] += this.california_now_60_mean[i];
					this.california_deep_60_mean_winter[23] += this.california_deep_60_mean[i];
					break;
				default:
					System.out
							.println("ERROR in switch statement of method computeSeasonalHourlyMeans.");
					break;
				}

			}

		}

		// Normalize.
		for (int i = 0; i < 24; i++) {

			this.california_now_60_mean_spring[i] = this.california_now_60_mean_spring[i]
					/ (double) numberOfSpringValues[i];
			this.california_now_60_mean_summer[i] = this.california_now_60_mean_summer[i]
					/ (double) numberOfSummerValues[i];
			this.california_now_60_mean_fall[i] = this.california_now_60_mean_fall[i]
					/ (double) numberOfFallValues[i];
			this.california_now_60_mean_winter[i] = this.california_now_60_mean_winter[i]
					/ (double) numberOfWinterValues[i];

			this.california_deep_60_mean_spring[i] = this.california_deep_60_mean_spring[i]
					/ (double) numberOfSpringValues[i];
			this.california_deep_60_mean_summer[i] = this.california_deep_60_mean_summer[i]
					/ (double) numberOfSummerValues[i];
			this.california_deep_60_mean_fall[i] = this.california_deep_60_mean_fall[i]
					/ (double) numberOfFallValues[i];
			this.california_deep_60_mean_winter[i] = this.california_deep_60_mean_winter[i]
					/ (double) numberOfWinterValues[i];

		}

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		WindDataStorage storage = new WindDataStorage();

		storage.init();

		storage.computeHourlyMeans();

		storage.computeSeasonalHourlyMeans();

		storage.printWindHourlySeasonalMeans();

	}

}
