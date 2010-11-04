import java.util.Calendar;

import umontreal.iro.lecuyer.functionfit.LeastSquares;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.WeibullDist;

public class WindAndPriceRandomNumberGenerator {

	boolean verbose;

	WindDataStorage storage;

	/*
	 * Arrays for storing the values that serve as input for the estimation of
	 * the stochastic wind model.
	 */
	public double[] california_now_60_norm;
	public double[] california_deep_60_norm;

	public WindAndPriceRandomNumberGenerator() {

		this.verbose = true;

		this.storage = new WindDataStorage();

	}

	public void init() {

		/*
		 * Initialize storage. This loads data from files and does the
		 * preprocessing, e.g. averaging values across time and locations.
		 */
		try {

			this.storage.init();

		} catch (Exception e) {

			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		this.computeWindDataWithoutTrend();

	}

	/*
	 * Method for fitting a Weibull distribution to the measurement points using
	 * SSJ maximum likelihood method.
	 */
	public void fitWeibull(double[] values) {

		double[] params = WeibullDist.getMLE(values, values.length);

		double k = params[0];

		double lambda = 1 / params[1];

		System.out.println("k: " + k);

		System.out.println("lambda: " + lambda);

	}

	public double getMaximum(double[] values) {

		double max = Double.MIN_VALUE;

		for (int i = 0; i < values.length; i++) {

			if (values[i] > max) {

				max = values[i];

			}

		}

		return max;

	}

	/*
	 * Method for printing data and different fits for visual inspection.
	 */
	public void printFit(double[] values) {

		double maxValue = this.getMaximum(values);

		int numberOfPoints = 1000;

		double stepSize = maxValue / numberOfPoints;

		/*
		 * Construct arrays for step-wise CDF.
		 */
		double[] x = new double[numberOfPoints];
		double[] F_x = new double[numberOfPoints];

		for (int i = 0; i < numberOfPoints; i++) {

			x[i] = i * stepSize;

			F_x[i] = 0;

		}

		for (int i = 0; i < values.length; i++) {

			int binNumber = this.getBin(numberOfPoints, stepSize, values[i]);

			for (int j = binNumber; j < numberOfPoints; j++) {

				F_x[j] += (double) 1 / (double) values.length;

			}

		}

		EmpiricalDist ed = new EmpiricalDist(values);

		/*
		 * 1. Weilbull distribution
		 */
		double k_1 = 0.5414;
		double lambda_1 = 1 / 0.7583;

		WeibullDist w1 = new WeibullDist(k_1, lambda_1, 0);

		/*
		 * 2. Weilbull distribution
		 */
		double k_2 = 0.8342;
		double lambda_2 = 1 / 0.8133;

		WeibullDist w2 = new WeibullDist(k_2, lambda_2, 0);

		for (int i = 0; i < (int) numberOfPoints; i++) {

			System.out.println((double) i * stepSize + " " + F_x[i] + " "
					+ w1.cdf((double) i * stepSize) + " "
					+ w2.cdf((double) i * stepSize));

		}

	}

	/*
	 * Method for fitting a Weibull distribution to the measurement points using
	 * regression method.
	 */
	public void fitWeibullRegression(double[] values) {

		/*
		 * Compute binning parameters.
		 */
		int numberOfBins = 100;
		double highestValue = this.getMaximum(values);
		double binSize = highestValue / (double) numberOfBins;

		System.out.println("binSize:" + binSize);
		System.out.println("highestValue:" + highestValue);

		/*
		 * Construct arrays for step-wise CDF.
		 */
		double[] x = new double[numberOfBins];
		double[] F_x = new double[numberOfBins];

		for (int i = 0; i < numberOfBins; i++) {

			x[i] = i * binSize;

			F_x[i] = 0;

		}

		for (int i = 0; i < values.length; i++) {

			int binNumber = this.getBin(numberOfBins, binSize, values[i]);

			for (int j = binNumber; j < numberOfBins; j++) {

				F_x[j] += (double) 1 / (double) values.length;

			}

		}

		/*
		 * Set last value of step-wise CDF to 1 due to account for rounding
		 * mistakes.
		 */
		F_x[numberOfBins - 1] = 1;

		// for (int i = 0; i < F_x.length; i++) {
		//
		// System.out.println(F_x[i]);
		//
		// }

		/*
		 * Transform step-wise CDF into required input arrays for regression.
		 * First and last value are not transformed due to infinity results.
		 */
		double[] z = new double[numberOfBins - 2];
		double[] y = new double[numberOfBins - 2];

		for (int i = 0; i < (numberOfBins - 2); i++) {

			z[i] = Math.log(x[i + 1]);

			y[i] = Math.log(Math.log(1 / (1 - F_x[i + 1])));

		}

		// for (int i = 0; i < y.length; i++) {
		//
		// System.out.println(z[i] + " " + y[i]);
		//
		// }

		LeastSquares ls = new LeastSquares(z, y, 1);

		double[] coefficients = ls.getCoefficients();

		for (int i = 0; i < coefficients.length; i++) {

			System.out.println("coefficients[" + i + "]=" + coefficients[i]);

		}

		/*
		 * beta_0 = -k*log(lambda)
		 */
		double beta_0 = coefficients[0];

		/*
		 * beta_1 = k
		 */
		double beta_1 = coefficients[1];

		double k = beta_1;

		double lambda = Math.exp(-(beta_0 / beta_1));

		System.out.println("k: " + k);

		System.out.println("lambda: " + lambda);

	}

	/*
	 * Method for finding the right bin for a value.
	 */
	public int getBin(int numberOfBins, double binSize, double value) {

		// System.out.println("numberOfBins: " + numberOfBins);
		// System.out.println("binSize: " + binSize);
		// System.out.println("value: " + value);

		int binNumber = -1;

		double currentUpperBound = binSize;

		for (int i = 0; i < numberOfBins; i++) {

			if (value < (currentUpperBound + 0.00000000001)) {

				binNumber = i;
				break;

			} else {

				currentUpperBound += binSize;

			}

		}

		if (binNumber == -1) {

			System.out.println("xxx: " + value);

		}

		return binNumber;

	}

	/*
	 * Method for removing seasonality and trend of original wind data.
	 */
	public void computeWindDataWithoutTrend() {

		/*
		 * Initialize arrays for input data.
		 */
		this.california_now_60_norm = new double[this.storage.dates_60_min.length];
		this.california_deep_60_norm = new double[this.storage.dates_60_min.length];

		for (int i = 0; i < this.storage.dates_60_min.length; i++) {

			// System.out.println(this.california_now_60_mean[i]);

			Calendar cal = this.storage
					.parseTimestamp(this.storage.dates_60_min[i]);
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
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[0];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[0];
					break;
				case 1:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[1];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[1];
					break;
				case 2:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[2];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[2];
					break;
				case 3:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[3];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[3];
					break;
				case 4:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[4];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[4];
					break;
				case 5:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[5];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[5];
					break;
				case 6:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[6];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[6];
					break;
				case 7:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[7];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[7];
					break;

				case 8:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[8];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[8];
					break;

				case 9:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[9];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[9];
					break;

				case 10:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[10];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[10];
					break;

				case 11:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[11];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[11];
					break;

				case 12:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[12];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[12];
					break;

				case 13:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[13];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[13];
					break;

				case 14:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[14];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[14];
					break;

				case 15:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[15];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[15];
					break;

				case 16:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[16];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[16];
					break;

				case 17:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[17];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[17];
					break;

				case 18:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[18];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[18];
					break;

				case 19:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[19];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[19];
					break;

				case 20:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[20];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[20];
					break;

				case 21:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[21];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[21];
					break;

				case 22:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[22];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[22];
					break;

				case 23:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_spring[23];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_spring[23];
					break;

				default:
					System.out.print("ERROR in switch statement.");
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
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[0];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[0];
					break;
				case 1:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[1];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[1];
					break;
				case 2:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[2];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[2];
					break;
				case 3:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[3];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[3];
					break;
				case 4:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[4];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[4];
					break;
				case 5:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[5];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[5];
					break;
				case 6:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[6];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[6];
					break;
				case 7:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[7];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[7];
					break;

				case 8:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[8];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[8];
					break;

				case 9:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[9];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[9];
					break;

				case 10:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[10];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[10];
					break;

				case 11:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[11];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[11];
					break;

				case 12:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[12];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[12];
					break;

				case 13:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[13];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[13];
					break;

				case 14:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[14];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[14];
					break;

				case 15:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[15];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[15];
					break;

				case 16:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[16];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[16];
					break;

				case 17:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[17];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[17];
					break;

				case 18:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[18];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[18];
					break;

				case 19:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[19];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[19];
					break;

				case 20:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[20];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[20];
					break;

				case 21:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[21];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[21];
					break;

				case 22:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[22];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[22];
					break;

				case 23:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_summer[23];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_summer[23];
					break;

				default:
					System.out.print("ERROR in switch statement.");
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
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[0];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[0];
					break;
				case 1:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[1];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[1];
					break;
				case 2:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[2];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[2];
					break;
				case 3:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[3];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[3];
					break;
				case 4:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[4];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[4];
					break;
				case 5:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[5];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[5];
					break;
				case 6:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[6];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[6];
					break;
				case 7:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[7];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[7];
					break;

				case 8:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[8];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[8];
					break;

				case 9:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[9];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[9];
					break;

				case 10:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[10];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[10];
					break;

				case 11:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[11];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[11];
					break;

				case 12:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[12];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[12];
					break;

				case 13:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[13];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[13];
					break;

				case 14:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[14];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[14];
					break;

				case 15:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[15];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[15];
					break;

				case 16:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[16];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[16];
					break;

				case 17:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[17];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[17];
					break;

				case 18:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[18];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[18];
					break;

				case 19:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[19];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[19];
					break;

				case 20:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[20];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[20];
					break;

				case 21:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[21];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[21];
					break;

				case 22:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[22];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[22];
					break;

				case 23:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_fall[23];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_fall[23];
					break;
					
				default:
					System.out.print("ERROR in switch statement.");
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
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[0];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[0];
					break;
				case 1:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[1];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[1];
					break;
				case 2:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[2];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[2];
					break;
				case 3:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[3];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[3];
					break;
				case 4:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[4];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[4];
					break;
				case 5:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[5];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[5];
					break;
				case 6:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[6];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[6];
					break;
				case 7:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[7];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[7];
					break;

				case 8:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[8];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[8];
					break;

				case 9:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[9];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[9];
					break;

				case 10:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[10];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[10];
					break;

				case 11:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[11];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[11];
					break;

				case 12:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[12];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[12];
					break;

				case 13:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[13];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[13];
					break;

				case 14:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[14];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[14];
					break;

				case 15:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[15];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[15];
					break;

				case 16:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[16];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[16];
					break;

				case 17:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[17];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[17];
					break;

				case 18:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[18];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[18];
					break;

				case 19:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[19];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[19];
					break;

				case 20:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[20];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[20];
					break;

				case 21:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[21];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[21];
					break;

				case 22:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[22];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[22];
					break;

				case 23:
					this.california_now_60_norm[i] = this.storage.california_now_60_mean[i]
							/ this.storage.california_now_60_mean_winter[23];
					this.california_deep_60_norm[i] = this.storage.california_deep_60_mean[i]
							/ this.storage.california_deep_60_mean_winter[23];
					break;

				default:
					System.out.print("ERROR in switch statement.");
					break;

				}

			}

		}

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		WindAndPriceRandomNumberGenerator wprng = new WindAndPriceRandomNumberGenerator();

		wprng.init();

		// System.out.println(wprng.storage.dates_60_min[0]);

		// wprng.storage
		// .printWindMeasurementData_60_min(wprng.california_deep_60_norm);

		wprng.fitWeibull(wprng.california_deep_60_norm);

		wprng.fitWeibullRegression(wprng.california_deep_60_norm);

		wprng.printFit(wprng.california_deep_60_norm);

	}

}
