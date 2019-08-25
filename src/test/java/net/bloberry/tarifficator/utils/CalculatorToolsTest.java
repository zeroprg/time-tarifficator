package net.bloberry.tarifficator.utils;

import net.bloberry.tarificator.metadata.Rate;
import net.bloberry.tarificator.metadata.RateType;
import net.bloberry.tarificator.metadata.Tariff;
import net.bloberry.tarificator.metadata.TimeUnit;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class CalculatorToolsTest {
    private static final Logger LOGGER = Logger.getLogger( CalculatorToolsTest.class.getName() );
    @Test
    public void calculateTarif1() throws ParseException {

        // Total parking time will be 95
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:25:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"26-Dec-2017 15:35:00");

        // Create Tariff
        Tariff tariff = new Tariff();
        // Create one time unit
        String DATE_RANGE = "Mon%Fri,15:20%16:45";
        TimeUnit timeUnit = ParserTools.convertDateRange(DATE_RANGE);
        // Create progressive rate
        Rate rate = new Rate();
        rate.setType(RateType.PROGRESSIVE);

        Rate subRate1 = new Rate();
        subRate1.setIntervalValue(30);  // set first interval as 1/2 hour : 30 minutes
        subRate1.setPriceValue(35);     // set price of interval in 35 units

        Rate subRate2 = new Rate();
        subRate2.setIntervalValue(60);  // set first interval as 1 hour : 60 minutes
        subRate2.setPriceValue(25);     // set price of interval in 25 units

        Rate[] rates = new Rate[] { subRate1, subRate2 };
        rate.setRate(rates);
        timeUnit.setRate(rate);

        // add time unit to Tariff
        TimeUnit[] timeUnits = new TimeUnit[] {timeUnit};
        tariff.setTimeUnits(timeUnits);
        // Calculate tariff
        float money = CalculatorTools.calculateTariff(tariff, startDateTime,endDateTime);
        LOGGER.log(Level.INFO, "Total tariff 'happy , worry free parking' cost " + money);
        // Last 15 minutes of second day parking were not prorated and charged in Full FAT tariff
        // total price will be 95
        assertTrue(95 == money);

        // Create one time unit
        DATE_RANGE = "Sat%Sun";
        TimeUnit dayOffTimeUnit = ParserTools.convertDateRange(DATE_RANGE);
        // Create progressive rate
        rate = new Rate();
        rate.setType(RateType.FLAT);
        rate.setPriceValue(20);
        dayOffTimeUnit.setRate(rate);
        // add time unit to Tariff
        timeUnits = new TimeUnit[] {timeUnit, dayOffTimeUnit};
        tariff.setTimeUnits(timeUnits);
        money = CalculatorTools.calculateTariff(tariff, startDateTime,endDateTime);
        LOGGER.log(Level.INFO,"Total tariff 'happy, worry free parking' cost " + money);
        assertTrue(95 == money);

    }

    @Test
    public void calculateTarif2() throws ParseException {

        // Total parking time will be 95
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:25:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"26-Dec-2017 15:35:00");

        // Create Tariff
        Tariff tariff = new Tariff();
        // Create one time unit
        String DATE_RANGE = "Mon%Fri,9:00%15:00";
        TimeUnit timeUnit = ParserTools.convertDateRange(DATE_RANGE);
        // Create progressive rate
        Rate rate = new Rate();
        rate.setType(RateType.PROGRESSIVE);

        Rate subRate1 = new Rate();
        subRate1.setIntervalValue(30);  // set first interval as 1/2 hour : 30 minutes
        subRate1.setPriceValue(35);     // set price of interval in 35 units

        Rate subRate2 = new Rate();
        subRate2.setIntervalValue(60);  // set first interval as 1 hour : 60 minutes
        subRate2.setPriceValue(25);     // set price of interval in 25 units

        Rate subRate3 = new Rate();
        //subRate3.setIntervalValue(60);  OTHERTIME not required interval
        subRate3.setPriceValue(25);     // set price of interval in 25 units
        subRate3.setType(RateType.OTHERTIME);

        Rate[] rates = new Rate[] { subRate1, subRate2, subRate3 };
        rate.setRate(rates);
        timeUnit.setRate(rate);

        // add time unit to Tariff
        TimeUnit[] timeUnits = new TimeUnit[] {timeUnit};
        tariff.setTimeUnits(timeUnits);
        // Calculate tariff
        float money = CalculatorTools.calculateTariff(tariff, startDateTime,endDateTime);
        LOGGER.log(Level.INFO,"Total tariff 'happy , worry free parking' cost " + money);
        // Last 15 minutes of second day parking were not prorated and charged in Full FAT tariff
        // total price will be 85
        assertTrue(85 == money); //7 hours parking

        // Create one time unit
        DATE_RANGE = "Sat%Sun";
        TimeUnit dayOffTimeUnit = ParserTools.convertDateRange(DATE_RANGE);
        // Create progressive rate
        rate = new Rate();
        rate.setType(RateType.FLAT);
        rate.setPriceValue(20);
        dayOffTimeUnit.setRate(rate);
        // add time unit to Tariff
        timeUnits = new TimeUnit[] {timeUnit, dayOffTimeUnit};
        tariff.setTimeUnits(timeUnits);
        money = CalculatorTools.calculateTariff(tariff, startDateTime, endDateTime);
        LOGGER.log(Level.INFO,"Total tariff 'happy, worry free parking' cost " + money);
        assertTrue(85 == money);

    }



    /**
     *  Test situations when the real parking time over loop time range of the park. meter
     * @throws ParseException
     */
    @Test
    public void calcBillableMinWhenParkTimeHasTU() throws ParseException {

        String DATE_RANGE = "Mon%Fri,15:20%16:45";
        TimeUnit tu = ParserTools.convertDateRange(DATE_RANGE);
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:15:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"26-Dec-2017 16:46:00");

        long minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(170 == minutes);
    }

    /**
     * Test situations when the real parking time in  range of the park. meter
     * @throws ParseException
     */
    @Test
    public void calcBillableMinInTimeRangeOfTU() throws ParseException {
        // Situation 1
        String DATE_RANGE = "Mon%Fri,15:20%16:45";
        TimeUnit tu = ParserTools.convertDateRange(DATE_RANGE);
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:25:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"26-Dec-2017 15:35:00");

        long minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(95 == minutes);

        // Situation 2
        DATE_RANGE = "Mon%Fri,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:25:00");
        endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"27-Dec-2017 15:35:00");

        minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(180 == minutes);

    }

    /**
     * Test situations when the  both real parking time parking and range of the park. meter are overlaped
     * @throws ParseException
     */
    @Test
    public void calcBillableMinInOverlapedTimeRangeOfTU() throws ParseException {
        // Situation 1
        String DATE_RANGE = "Mon%Fri,15:20%16:45";
        TimeUnit tu = ParserTools.convertDateRange(DATE_RANGE);
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:25:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"26-Dec-2017 16:55:00");
        long minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(165 == minutes);

        // Situation 2
        DATE_RANGE = "Mon%Fri,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:25:00");
        endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"27-Dec-2017 16:55:00");
        minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(250 == minutes);


        // Situation 3
        DATE_RANGE = "Mon%Fri,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        startDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:15:00");
        endDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"26-Dec-2017 16:40:00");
        minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(165 == minutes);

        DATE_RANGE = "Mon,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(80 == minutes);

        DATE_RANGE = "25-Dec-2017,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(80 == minutes);

        DATE_RANGE = "25-Dec-2017%28-Dec-2017,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(165 == minutes);

        DATE_RANGE = "20-Dec-2017%28-Dec-2017,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        minutes =  CalculatorTools.calcBillableMinutes(startDateTime, endDateTime, tu);
        LOGGER.log(Level.INFO,"Total billable parking minutes: " + minutes);
        assertTrue(165 == minutes);


    }




    @Test
    public void isInTimeRangeOfTU() throws java.text.ParseException {
        // case 1
        String DATE_RANGE = "25-Dec-2017%26-Dec-2017";
        TimeUnit tu = ParserTools.convertDateRange(DATE_RANGE);
        LocalDateTime localDateTime = CalendarTools.parseDate("dd-MMM-yyyy hh:mm:ss" ,"25-Dec-2017 12:35:00");
        assertTrue( CalculatorTools.isInTimeRangeofTU( localDateTime, tu ) );

        // case 2
        DATE_RANGE = "25-Dec-2017%26-Dec-2017,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        localDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 12:35:00");
        assertNotEquals ( true, CalculatorTools.isInTimeRangeofTU( localDateTime, tu ));

        // case 2
        DATE_RANGE = "25-Dec-2017,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        localDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:35:00");
        assertEquals ( true, CalculatorTools.isInTimeRangeofTU( localDateTime, tu ));

        // case 3
        DATE_RANGE = "Mon%Fri,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        localDateTime = CalendarTools.parseDate("dd-MMM-yyyy HH:mm:ss" ,"25-Dec-2017 15:35:00");
        assertEquals ( true, CalculatorTools.isInTimeRangeofTU( localDateTime, tu ));


    }


    @Test
    public void hasTUin() throws ParseException {
        // case 1
        String DATE_RANGE = "25-Dec-2017%26-Dec-2017";
        TimeUnit tu = ParserTools.convertDateRange(DATE_RANGE);
        LocalDateTime startDateTime = CalendarTools.parseDate("dd-MMM-yyyy hh:mm:ss" ,"24-Dec-2017 12:35:00");
        LocalDateTime endDateTime = CalendarTools.parseDate("dd-MMM-yyyy hh:mm:ss" ,"27-Dec-2017 12:35:00");
        assertTrue( CalculatorTools.hasTUin( startDateTime, endDateTime, tu ) );

    }


}