package net.bloberry.tarifficator.utils;

import net.bloberry.tarificator.metadata.RateType;
import net.bloberry.tarificator.metadata.TimeUnit;
import net.bloberry.tarificator.metadata.TimeUnitDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.bloberry.tarificator.metadata.Constants.*;

/**
 *
 */
public class ParserTools {
    private static final Logger LOGGER = Logger.getLogger( ParserTools.class.getName() );

    /**
     *  Convert Date range from string representation:  'Monday%Tues, 10:15-12:30' , 'Tues' or in Date format  '01/Jan/2000' , '01/01/2000' or in Date range format : '01/Jan/2000 % 02-01-2000, 15:30 -16:30'
     * @param date
     * @return
     */
    public static final TimeUnit convertDateRange(String date){
        TimeUnit tu = new TimeUnit();
        //Check all regular expression patterns here
        if(date == null || date.isEmpty()) throw new DateParserException("Time unit's date can't be empty");
        // check time range present in date string
        String[] datetime_rng =  date.split(DATETIME_SEPAR);
        // Check if datetime separator presents in date string
        if(  datetime_rng.length > 1  ){
            String datetime = datetime_rng[1];
            int[][] hourMinutes = convToHourMinutesRange(datetime);
            tu.setHoursRange(hourMinutes[0]);
            tu.setMinutsRange(hourMinutes[1]);
            date = datetime_rng[0];
        }
        // Do date range separation
        String[] datesRange = date.split(RANGE_SEPAR);
        // is string dates range ?
        for ( int i=0 ; i <  datesRange.length; i++)
        {
            TimeUnitDate tudStart = convertDate(datesRange[i]);
            tu.getDateOfTheWeek()[i] = tudStart.getDateOfTheWeek();
            tu.getEventDate()[i] = tudStart.getEventDate();
        }
        return tu;
    }

    /**
     * Convert Date from string representation:  'Monday' ,'Tues' or in Date format  '01/Jan/2000' , '01/01/2000' or special events Christmas, Thansgiving, Memorialday
     * @param date
     * @return
     */
    private static TimeUnitDate convertDate(String date) {
        TimeUnitDate tud = new TimeUnitDate();
        if( isDayOfWeek(date) ) {
            tud.setDateOfTheWeek(convDayOfWeek(date));
        } else if( isDate(date) && !isTime(date)){
            tud.setEventDate(convToDay(date));
        } else {
            String year;
            Calendar cal = Calendar.getInstance();
            int y = cal.get(Calendar.YEAR);

            if( date.contains("-") )
                year = date.split("-")[1];
            else {
                year = String.valueOf(y);
            }
            if(date.toLowerCase().contains(CHRISTMAS)){
                String  christmas = "25-Dec-".concat(year);
                tud.setEventDate(convToDay(christmas));
            } else if(date.toLowerCase().contains(THANKSGIVING)){
                // 2nd TUESDAY of October
                int m = 10; // October
                LocalDate eventDate = CalendarTools.getNdayOfWeekforTheMonthAndYear(2, DayOfWeek.TUESDAY.getValue(), m-1, y);
                tud.setEventDate(eventDate);
            } else if(date.toLowerCase().contains(MEMORIAL_DAY)){
                // 5th MONDAY is May
                int m = 5;
                LocalDate eventDate = CalendarTools.getNdayOfWeekforTheMonthAndYear(4, DayOfWeek.MONDAY.getValue()-1, m-1, y);
                tud.setEventDate(eventDate);
            }

        }

        return tud;
    }

    /**
     *  Convert String in format 'dd-MMM-yyyy'  ( '25-Dec-2017' )to LocalDate
     * @param s
     * @return
     */
    public static LocalDate convToDay(String s) {
        LocalDate date;
        Date d = null;
        String pattern = "no_pattern";
        try {
            if (s.matches(REGEXPRESS_dd_mm_yyyy)) {
                if (s.contains("/")) {
                    d = new SimpleDateFormat(pattern = "dd/MM/yyyy", Locale.getDefault()).parse(s);
                } else {
                    d = new SimpleDateFormat(pattern = "dd-MM-yyyy", Locale.getDefault()).parse(s);
                }
            } else if (s.matches(REGEXPRESS_mm_dd_yyyy)) {
                if (s.contains("/")) {
                    d = new SimpleDateFormat(pattern = "MM/dd/yyyy", Locale.getDefault()).parse(s);
                } else {
                    d = new SimpleDateFormat(pattern = "MM-dd-yyyy", Locale.getDefault()).parse(s);
                }
            } else if (s.matches(REGEXPRESS_dd_MMM_YYYY)) {
                if (s.contains("/")) {
                    d = new SimpleDateFormat(pattern = "dd/MMM/yyyy", Locale.getDefault()).parse(s);
                } else {
                    d = new SimpleDateFormat(pattern = "dd-MMM-yyyy", Locale.getDefault()).parse(s);
                }
            }
        }catch (ParseException e0) {
                throw new DateParserException(s + " can't parse by following  this template '" + pattern + "'");
        }

        date = CalendarTools.convertToLocalDateViaInstant(d);
        return date;
    }


    /**
     *  Convert Hors, minutes range , see this example: 15:43 % 16:47
     * @param s
     * @return
     */
    public static int[][] convToHourMinutesRange(String s) {
        int[][] hourMinutes = new int[2][2];
        String[] hourMinutesRange = s.split(RANGE_SEPAR);
        if( hourMinutesRange.length < 2 ) throw new DateParserException( " Wrong format: " + s +" hours must follow this range example: 15:30 % 16 ");
        for ( int i=0 ; i <  hourMinutesRange.length; i++)
        {
            int hours   = convToHour(hourMinutesRange[i]);
            int minutes = convToMinutes(hourMinutesRange[i]);
            hourMinutes[0][i] = hours;
            hourMinutes[1][i] = minutes;
        }
        return hourMinutes;
    }

    public static int convToHour(String s) {
        return Integer.parseInt(s.split(HOURMINUTES_SEPAR)[0]);
    }


    public static int convToMinutes(String s) {
        if ( s.split(HOURMINUTES_SEPAR).length > 1 )
            return Integer.parseInt(s.split(HOURMINUTES_SEPAR)[1]);
        else
            return 0;
    }
    /**
     *
     */
    private static class DateParserException extends RuntimeException {
        public DateParserException(String s) {
            LOGGER.log( Level.INFO, s);
        }
    }


    public static boolean isTime(String s){
        return s.matches(REGEXPRESS_24hTIME);
    }


    public static boolean isDate(String s){
        return s.matches(REGEXPRESS_DATE);
    }


    /**
     * @param s
     * @return
     */
    public static boolean isDayOfWeek(String s){
        return s.toLowerCase().matches(REGEXPRESS_DAYS_OF_THE_WEEK);
    }

    /**
     * @param s
     * @return
     */
    public static DayOfWeek convDayOfWeek(String s){
        if( s.toLowerCase().matches( REGEXPRESS_MONDAY) )    return DayOfWeek.MONDAY;
        if( s.toLowerCase().matches( REGEXPRESS_TUESDAY) )   return DayOfWeek.TUESDAY;
        if( s.toLowerCase().matches( REGEXPRESS_WEDNESDAY) ) return DayOfWeek.WEDNESDAY;
        if( s.toLowerCase().matches( REGEXPRESS_THURSDAY) )  return DayOfWeek.THURSDAY;
        if( s.toLowerCase().matches( REGEXPRESS_FRIDAY) )    return DayOfWeek.FRIDAY;
        if( s.toLowerCase().matches( REGEXPRESS_SATURDAY) )  return DayOfWeek.SATURDAY;
        if( s.toLowerCase().matches( REGEXPRESS_SUNDAY) )    return DayOfWeek.SUNDAY;
        throw new DateTimeException("Unable to obtain DayOfWeek from: " + s);
    }

    public static RateType convRateTypes(String s){
      RateType ret = null;
      switch (s.toLowerCase()){
          case PROGRESSIVE: ret = RateType.PROGRESSIVE; break;
          case OTHERTIME:   ret = RateType.OTHERTIME; break;
          case FLAT:        ret = RateType.FLAT; break;
          case LINEAR:      ret = RateType.LINEAR; break;
          case UNIT:        ret = RateType.UNIT; break;
          case BLOCKED:     ret = RateType.BLOCKED ; break;
          default: throw new RuntimeException("There is no proper rate type for your data:  " + s);
      }
      return ret;
    }



    /**
     * @param s
     * @return
     */
    public static int parseTimeIntervalToMinutes(String s){
        int ind = 0;
        int minutes = 0;
        if( (ind = s.indexOf('h')) > 0 ) {
            int number = Integer.parseInt(s.substring(0,ind));
            minutes = number*60;
        } else if( (ind = s.indexOf('m')) > 0 ) {
            minutes = Integer.parseInt(s.substring(0,ind));
        } else throw new DateTimeException("Parking time interval must be present in hours and minutes format: '4h', '5hours' or '35m', '54min'");
     return minutes;
    }

    public static float parseMonetaryUnit(String s) {
        int ind = 0;
        float dollars = 0;
        if( (ind = s.indexOf('c')) > 0 ) {
            int number = Integer.parseInt(s.substring(0,ind));
            dollars = (float) (number/100.0);
        } else if( (ind = s.indexOf('$')) > 0 ) {
            dollars = Float.parseFloat(s.substring(0,ind));
        } else throw new RuntimeException("Parking price must be present in dollars or cents format: '4.50$', '5$' or '35c', '54c'");
        return dollars;
    }


}
