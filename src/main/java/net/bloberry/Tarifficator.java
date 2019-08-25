package net.bloberry;

import net.bloberry.tarifficator.utils.CalculatorTools;
import net.bloberry.tarifficator.utils.CalendarTools;
import net.bloberry.tarificator.metadata.Constants;
import net.bloberry.tarificator.metadata.Tariff;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Tarifficator {
    private static final Logger LOGGER = Logger.getLogger( Tarifficator.class.getName() );
    //boolean b= readlogConfigFile();
    public static void main(String[] args) throws ParseException {
        Tarifficator tarifficator = new Tarifficator();
        if ( args.length == 0 ) {
            LOGGER.log(Level.INFO,"No arguments were given.");
            LOGGER.log(Level.INFO,"Use -f : to provide YAML file with tariffs.");
            LOGGER.log(Level.INFO,"Use -start : to define start parking session in pattern " + Constants.DATE_TIME_FORMAT+ ".\n");
            LOGGER.log(Level.INFO,"Use -end : to define end parking session "+ Constants.DATE_TIME_FORMAT+".\n");
            LOGGER.log(Level.INFO,"Use -zone : to provide YAML file with tariffs.");
        } else {
            Tariff[] tariffs = null;
            LocalDateTime start = null;
            LocalDateTime end = null;
            String zone = null;
            String filename = null;
            for( int i=0; i<args.length; i++) {
                switch (args[i]) {
                    case "-f":
                        checkArguments(args,i+1);
                        filename = args[i+1];
                        tariffs = tarifficator.parseYAMLfile(filename);
                        //do stuff with file
                        break;
                    case "-start":
                        checkArguments(args,i+1);
                        start = parseDateTime(args[i+1]);
                        break;
                    case "-end":
                        checkArguments(args,i+1);
                        end = parseDateTime(args[i+1]);
                        break;
                    case "-zone":
                        checkArguments(args,i+1);
                        zone = args[i+1];
                        break;
                    default:
                }
            }

            LOGGER.log(Level.INFO,"Parameters populated - zone: " + zone + ", session start time: " +start+ ", session end time: " +end + ", filename: " + filename );
            Tariff tariff = null;
            for (Tariff t: tariffs) {
 //  use array of zoneIds to applpy multiple tariffs:
                // for ( String zoneId :zoneIds)
                    if( zone.equals(t.getZoneId())) {

                        tariff = t;
                        float money = CalculatorTools.calculateTariff(tariff, start,end);
                        LOGGER.log(Level.FINE,"Tariff: " + tariff + '\n');
                        LOGGER.log(Level.INFO," money : " + money);
                    }
            }
            if( tariff == null ) throw new RuntimeException( "No tariff found for zone: "   + zone);

        }


    }

    private static boolean readlogConfigFile(){
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (Exception e){
            return false;
        }
        return true;
    }

    //Wrong number of arguments
    private static void checkArguments(String[] args, int argNumber) {
        if( argNumber >= args.length) {

            throw new RuntimeException(" Parameter for flag:" + args[argNumber-1] + " must be not empty");
        }
        if( args.length%2 == 1 ) {
            throw new RuntimeException(" Quantity of parameters and flags must be even number:" + args);
        }
    }

    public  static LocalDateTime parseDateTime(String s) throws ParseException {
        return  CalendarTools.parseDate(Constants.DATE_TIME_FORMAT, s);

    }

    private  Tariff[] parseYAMLfile(String filename) {
        Yaml yaml = new Yaml(new Constructor(Tariff[].class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(filename);
        return yaml.load(inputStream);
    }

    public float calculateTariffs(String filename, String zone, String s1, String s2) throws ParseException{

        Tariff[] tariffs = this.parseYAMLfile(filename);
        Tariff  tariff = new Tariff();
        float  money  = 0;
        LocalDateTime start = parseDateTime(s1);
        LocalDateTime end = parseDateTime(s2);
        for (Tariff t: tariffs) {
            //  use array of zoneIds to applpy multiple tariffs:
            // for ( String zoneId :zoneIds)
            if( zone.equals(t.getZoneId())) {
                tariff = t;
                money = CalculatorTools.calculateTariff(tariff, start,end);
                LOGGER.log(Level.FINE,"Tariff: " + tariff + '\n');
                LOGGER.log(Level.INFO," Money to charge : " + money);
            }
        }
        if( tariff == null ) throw new RuntimeException( "No tariff found for zone: "   + zone);
        return money;
    }
}
