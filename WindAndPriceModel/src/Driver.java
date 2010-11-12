import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;;


public class Driver {

	public static void main(String[] args) throws IOException, ParseException, Exception {
		ArrayList<WindFarm> farms = new ArrayList<WindFarm> ();
		farms.add( new WindFarm(new NRELTrace("wind data/tehachapi.csv"), 30, 6459) );
		farms.add( new WindFarm(new NRELTrace("wind data/clark.csv"), 30, 1500) );
		farms.add( new WindFarm(new NRELTrace("wind data/solano.csv"), 30, 583.45) );
		farms.add( new WindFarm(new NRELTrace("wind data/san gorgonio.csv"), 30, 528) );
		farms.add( new WindFarm(new NRELTrace("wind data/san diego.csv"), 30, 1527) );
		farms.add( new WindFarm(new NRELTrace("wind data/humboldt.csv"), 30, 218.2) );
		farms.add( new WindFarm(new NRELTrace("wind data/imperial.csv"), 30, 547.9) );
		farms.add( new WindFarm(new NRELTrace("wind data/altamont.csv"), 30, 14) );
		farms.add( new WindFarm(new NRELTrace("wind data/monterey.csv"), 30, 0) );
		farms.add( new WindFarm(new NRELTrace("wind data/pacheco.csv"), 30, 0) );
		
		WindModel model = new WindModel(farms);
	}

}
