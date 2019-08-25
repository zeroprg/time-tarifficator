package net.bloberry.tarifficator.utils;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 */
public class CalendarTools {
    public static void main(String[] args) {
        LocalDate localDate = LocalDate.now();
        System.out.println("Day of Month: " + localDate.getDayOfMonth());
        System.out.println("Month: " + localDate.getMonth());
        System.out.println("Year: " + localDate.getYear());

        System.out.printf("first day of Month: %s%n",
                localDate.with(TemporalAdjusters.firstDayOfMonth()));
        System.out.printf("first Monday of Month: %s%n", localDate
                .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)));
        System.out.printf("last day of Month: %s%n",
                localDate.with(TemporalAdjusters.lastDayOfMonth()));
        System.out.printf("first day of next Month: %s%n",
                localDate.with(TemporalAdjusters.firstDayOfNextMonth()));
        System.out.printf("first day of next Year: %s%n",
                localDate.with(TemporalAdjusters. firstDayOfNextYear()));
        System.out.printf("first day of Year: %s%n",
                localDate.with(TemporalAdjusters.firstDayOfYear()));

        LocalDate tomorrow = localDate.plusDays(1);
        System.out.println("Day of Month: " + tomorrow.getDayOfMonth());
        System.out.println("Month: " + tomorrow.getMonth());
        System.out.println("Year: " + tomorrow.getYear());

        //LocalDate ldate = getNdayOfTheWeek(int n, int week );
    }

    /**
     * @param dayOfTheMonth
     * @param month
     * @param year
     * @return
     */
    static final  LocalDate populateCalendarDate(int dayOfTheMonth, int month,int year){
        Calendar c = new GregorianCalendar();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.YEAR, year);
        Date d = c.getTime();
        return convertToLocalDateViaInstant(d);
    }

    /**
     * @param n
     * @param dayOfWeek
     * @param month
     * @param year
     * @return
     */
    static final  LocalDate  getNdayOfWeekforTheMonthAndYear(int n, int dayOfWeek, int month, int year ){
        LocalDate firstDayOfMonth = populateCalendarDate(1,month,year);
        LocalDate  ndayOfWeek = firstDayOfMonth.with(TemporalAdjusters.dayOfWeekInMonth(n,DayOfWeek.of(dayOfWeek)));
        return ndayOfWeek;
    }

    /**
     * @param dateToConvert
     * @return
     */
    static final public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    static final public LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    static final public LocalDate getCurrentDate() {
        return convertToLocalDateViaInstant(new Date());
    }

    public static LocalDateTime parseDate(String pattern, String dateInString) throws java.text.ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = sdf.parse(dateInString);
        return convertToLocalDateTimeViaInstant(date);
    }
    /*
     * Compute the day of the year that Easter falls on. Step names E1 E2 etc.,
     * are direct references to Knuth, Vol 1, p 155. @exception
     * IllegalArgumentexception If the year is before 1582 (since the algorithm
     * only works on the Gregorian calendar).
     */
    public static final LocalDate findHolyDay(int year) {
        if (year <= 1582) {
            throw new IllegalArgumentException("Algorithm invalid before April 1583");
        }
        int golden, century, x, z, d, epact, n;

        golden = (year % 19) + 1; /* E1: metonic cycle */
        century = (year / 100) + 1; /* E2: e.g. 1984 was in 20th C */
        x = (3 * century / 4) - 12; /* E3: leap year correction */
        z = ((8 * century + 5) / 25) - 5; /* E3: sync with moon's orbit */
        d = (5 * year / 4) - x - 10;
        epact = (11 * golden + 20 + z - x) % 30; /* E5: epact */
        if ((epact == 25 && golden > 11) || epact == 24)
            epact++;
        n = 44 - epact;
        n += 30 * (n < 21 ? 1 : 0); /* E6: */
        n += 7 - ((d + n) % 7);
        if (n > 31) /* E7: */
            return convertToLocalDateViaInstant(new GregorianCalendar(year, 4 - 1, n - 31).getTime()); /* April */
        else
            return convertToLocalDateViaInstant(new GregorianCalendar(year, 3 - 1, n).getTime()); /* March */
    }

}
