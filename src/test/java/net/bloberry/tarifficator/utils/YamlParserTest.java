package net.bloberry.tarifficator.utils;

import net.bloberry.tarificator.metadata.Rate;
import net.bloberry.tarificator.metadata.Tariff;
import net.bloberry.tarificator.metadata.TimeUnit;
import net.bloberry.tarificator.metadata.Zone;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlParserTest {
    private static final Logger LOGGER = Logger.getLogger( YamlParserTest.class.getName() );
    @Test
    public void getSimpleHashMapFromYaml(){
       Yaml yaml = new Yaml();
       InputStream inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("zone.yaml");
        Map<String, Object> obj = yaml.load(inputStream);
        LOGGER.log( Level.FINE,obj.toString());
    }

    @Test
    public void parseZoneYaml() {
        Yaml yaml = new Yaml(new Constructor(Zone.class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("zone.yaml");

        Zone zone = yaml.load(inputStream);
        LOGGER.log( Level.INFO,"zone Id:" + zone.getZoneId() + ", zone name:" + zone.getName() );
    }

    @Test
    public void parseRate() {
        Yaml yaml = new Yaml(new Constructor(Rate.class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("rate.yaml");

        Rate rate = yaml.load(inputStream);
        LOGGER.log( Level.INFO,"Rate: ");
        LOGGER.log( Level.INFO,rate.toString());
    }

    @Test
    public void parseTimeUnit() {
        Yaml yaml = new Yaml(new Constructor(TimeUnit.class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("time-unit.yaml");

        TimeUnit timeUnit = yaml.load(inputStream);
        LOGGER.log( Level.INFO,"Time-unit: ");
        LOGGER.log( Level.INFO, timeUnit.toString());
    }

    @Test
    public void parseTariff() {
        Yaml yaml = new Yaml(new Constructor(Tariff.class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("tariff.yaml");

        Tariff tariff = yaml.load(inputStream);
        LOGGER.log( Level.INFO,"Tariff: ");
        LOGGER.log( Level.INFO,tariff.toString());
    }

    @Test
    public void testTariffs(){
        Yaml yaml = new Yaml(new Constructor(Tariff.class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("tariff.yaml");

        Tariff tariff = yaml.load(inputStream);
        LOGGER.log( Level.INFO,"Tariff: ");
        LOGGER.log( Level.INFO,tariff.toString());

    }

    @Test
    public void populatePriceForTariffs() throws ParseException {
        Yaml yaml = new Yaml(new Constructor(Tariff[].class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("tariffs.yaml");
        Tariff[] tariffs = yaml.load(inputStream);
        // Total billing price for parking  will be 80
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:25:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"26-Dec-2017 15:35:00");
        float money = CalculatorTools.calculateTariff(tariffs[0], startDateTime,endDateTime);
        LOGGER.log( Level.INFO,"Tariff: " + tariffs[0] + '\n');
        LOGGER.log( Level.INFO," money : " + money);
    }

    @Test
    public void populatePriceForTariffsByZoneIds() throws ParseException {
        Yaml yaml = new Yaml(new Constructor(Tariff[].class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("tariffs.yaml");
        Tariff[] tariffs = yaml.load(inputStream);
        // Total parking time will be 80
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"09-Oct-2018 5:00:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"10-Oct-2018 15:35:00");
        String[] zoneIds = new String[]{"12345"};
        Tariff tariff = null;
        for (Tariff t: tariffs) {
            for ( String zoneId :zoneIds)
                if( zoneId.equals(t.getZoneId())) {
                    tariff = t;
                    float money = CalculatorTools.calculateTariff(tariff, startDateTime,endDateTime);
                    LOGGER.log( Level.INFO,"Tariff: " + tariff + '\n');
                    LOGGER.log( Level.INFO," money : " + money);
                }
        }
        if( tariff == null ) new RuntimeException( "No tariff found for zoneIds: "   + zoneIds);

    }

}
