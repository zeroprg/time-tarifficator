package net.bloberry.tarifficator.utils;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class CalendarToolsTest {
    private static final Logger LOGGER = Logger.getLogger( CalendarToolsTest.class.getName() );
    @Test
    public void populateCalendarDate() {
        LocalDate localDate = CalendarTools.populateCalendarDate(1, 9, 2018);
        LOGGER.log(Level.INFO,"first  day of October:  %s%n ", localDate);
        assertTrue("2018-10-01" .equals(localDate.toString()));
    }

    @Test
    public void getNdayOfWeekforTheMonthAndYear() {
        // get second Monday of October
        LocalDate localDate = CalendarTools.getNdayOfWeekforTheMonthAndYear(2, DayOfWeek.MONDAY.getValue(), Calendar.OCTOBER, 2018);
        LOGGER.log(Level.INFO," second Monday of October:  %s%n ", localDate);
        assertTrue("2018-10-08" .equals(localDate.toString()));
    }

    @Test
    public void findHolyDay() {
        LocalDate localDate = CalendarTools.findHolyDay(2018);
        LOGGER.log(Level.INFO,"Holy day for 2018 by Gregorian Calendar:  %s%n ", localDate);
        assertTrue("2018-04-01" .equals(localDate.toString()));
        localDate = CalendarTools.findHolyDay(2019);
        LOGGER.log(Level.INFO,"Holy day for 2019 by Gregorian Calendar:  %s%n ", localDate);
        assertTrue("2019-04-21" .equals(localDate.toString()));

    }

}