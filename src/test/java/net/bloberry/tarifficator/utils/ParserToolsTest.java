package net.bloberry.tarifficator.utils;

import net.bloberry.tarificator.metadata.TimeUnit;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserToolsTest {
    private static final Logger LOGGER = Logger.getLogger( ParserToolsTest.class.getName() );
    @Test
    public void isDayOfWeek() {
        assertTrue(ParserTools.isDayOfWeek("Monday"));
        assertTrue(ParserTools.isDayOfWeek("Mon"));
    }

    @Test
    public void convDayOfWeek() {
        LOGGER.log( Level.INFO,"%s", ParserTools.convDayOfWeek("Monday") );
        assertTrue(DayOfWeek.MONDAY ==  ParserTools.convDayOfWeek("Monday"));
        assertTrue(DayOfWeek.MONDAY ==  ParserTools.convDayOfWeek("Mon"));
        assertTrue(DayOfWeek.TUESDAY ==  ParserTools.convDayOfWeek("Tuesday"));
        assertTrue(DayOfWeek.TUESDAY ==  ParserTools.convDayOfWeek("Tues"));
        assertTrue(DayOfWeek.WEDNESDAY ==  ParserTools.convDayOfWeek("Wednesday"));
   }
    @Test(expected = java.time.DateTimeException.class)
    public void convDayOfWeek_negative() {
        LOGGER.log( Level.INFO,"%s", ParserTools.convDayOfWeek("Wes") );
    }

    @Test
    public void convertDateRange() {
        // subcase 1
        String DATE_RANGE = "25-Dec-2017%26-Dec-2017";
        TimeUnit tu = ParserTools.convertDateRange(DATE_RANGE);
        LocalDate[] eventDateRange = tu.getEventDate();
        String s1 = eventDateRange[0].toString().substring(0,10);
        String s2 = eventDateRange[1].toString().substring(0,10);
        LOGGER.log( Level.INFO,"For date ranges:" + DATE_RANGE + " values are startTime:" + s1 + " endTime:" + s2);
        assertTrue( "2017-12-25".equals(s1) && "2017-12-26".equals(s2) );
        // subcase 2
        DATE_RANGE = "Mon%Fri";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        DayOfWeek dw1 =  tu.getDateOfTheWeek()[0];
        DayOfWeek dw2 =  tu.getDateOfTheWeek()[1];
        LOGGER.log( Level.INFO,"For date ranges:" + DATE_RANGE + " values are startTime: " + dw1 + " endTime: " + dw2);
        assertTrue("dw1.getValue() = " + dw1.getValue() + " dw2.getValue() = " + dw2.getValue(), 1 == dw1.getValue() &&  5 ==  dw2.getValue());
        // subcase 3
        DATE_RANGE = "Mon";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        dw1 =  tu.getDateOfTheWeek()[0];
        dw2 =  tu.getDateOfTheWeek()[1];
        LOGGER.log( Level.INFO,"For date ranges:" + DATE_RANGE + " values are startTime: " + dw1 + " endTime: " + dw2);
        assertTrue("dw1.getValue() = " + dw1.getValue() + " dw2 = " + dw2, 1 == dw1.getValue()) ;
        // subcase 3
        DATE_RANGE = "Mon%Sun,15:20%16:45";
        tu = ParserTools.convertDateRange(DATE_RANGE);
        dw1 =  tu.getDateOfTheWeek()[0];
        dw2 =  tu.getDateOfTheWeek()[1];
        int hour1 = tu.getHoursRange()[0];
        int hour2 = tu.getHoursRange()[1];
        int minut1 = tu.getMinutsRange()[0];
        int minut2 = tu.getMinutsRange()[1];

        LOGGER.log( Level.INFO,"For date ranges:" + DATE_RANGE + " values are startTime: " + dw1 + " endTime: " + dw2);
        assertTrue("dw1.getValue() = " + dw1.getValue() + " dw2 = " + dw2.getValue(), 1 == dw1.getValue() && 7 == dw2.getValue()) ;
        LOGGER.log( Level.INFO,"hour1=" + hour1 + " ,hour2=" + hour2 + " ,minut1 =" + minut1 + " ,minut2=" + minut2);
        assertTrue("hour1=" + hour1 + " ,hour2=" + hour2 + " ,minut1 =" + minut1 + " ,minut2=" + minut2,
                hour1==15 && hour2 == 16 && minut1 == 20 && minut2 == 45);
    }

    @Test
    public void convToDay() {
        String s;
        LocalDate result = ParserTools.convToDay("25-Dec-2017");
        s = result.toString().substring(0,10);
        LOGGER.log( Level.INFO,"25-Dec-2017: " +  s);
        assertTrue("2017-12-25".equals(s));
        result = ParserTools.convToDay("25-12-2017");
        s = result.toString().substring(0,10);
        LOGGER.log( Level.INFO,"25-12-2017: " +  s );
        assertTrue("2017-12-25".equals(s));
    }

    @Test
    public void convToHourMinutesRange() {
    }

    @Test
    public void convToHour() {
        int hour  = ParserTools.convToHour("15:16");
        assertTrue(15 == hour);
        hour  = ParserTools.convToHour("00:11");
        assertTrue(0 == hour);

    }

    @Test
    public void convToMinutes() {
        int minutes  = ParserTools.convToMinutes("15:16");
        assertTrue(16 == minutes);
    }

    @Test
    public void isTime() {
        assertTrue( ParserTools.isTime("15:13"));
        assertEquals( false, ParserTools.isTime("15:130"));
        assertTrue(  ParserTools.isTime("00:13"));
    }

    @Test
    public void isDate() {
        assertEquals(false,ParserTools.isDate("25-dec-2017"));
        assertTrue(ParserTools.isDate("25-Dec-2017"));
        assertTrue(ParserTools.isDate("25/Dec/2017"));
        assertTrue(ParserTools.isDate("25/12/2017"));
    }

    @Test
    public void parseTimeIntervalToMinutes(){
        int minutes = ParserTools.parseTimeIntervalToMinutes("2h");
        assertEquals(true, 120 == minutes);
        minutes = ParserTools.parseTimeIntervalToMinutes("30m");
        assertEquals(true, 30 == minutes);
    }

    @Test
    public void parseMonetaryUnit(){
        float dollars = ParserTools.parseMonetaryUnit("2$");
        assertEquals(true, 2 == dollars);
        dollars = ParserTools.parseMonetaryUnit("2.5$");
        assertEquals(true, 2.5 == dollars);
        dollars = ParserTools.parseMonetaryUnit("35c");
        assertEquals(true, (0.35 - dollars) < 0.001);

    }

}