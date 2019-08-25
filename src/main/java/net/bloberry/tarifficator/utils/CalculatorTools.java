package net.bloberry.tarifficator.utils;

import net.bloberry.tarificator.metadata.Rate;
import net.bloberry.tarificator.metadata.RateType;
import net.bloberry.tarificator.metadata.Tariff;
import net.bloberry.tarificator.metadata.TimeUnit;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class CalculatorTools {

    private static final Logger LOGGER = Logger.getLogger( CalculatorTools.class.getName() );
    /**
     * @param tariff
     * @param start
     * @param end
     * @return
     */
    public static float calculateTariff(Tariff tariff, LocalDateTime start, LocalDateTime end){
        float total = (float)0;

        long delta = start.until(end, ChronoUnit.MINUTES);
        if(  delta >  tariff.getMaximum_timeValue() && tariff.getMaximum_timeValue() > 0 )  {
            LOGGER.log(Level.INFO, "Parking limit exceeded: " + tariff.getMaximum_time());
            end = start.plusMinutes(tariff.getMaximum_timeValue());
        } else if( delta <= 0  ) {
            LOGGER.log(Level.INFO, "Start time: " + start + " must to be before end time: "  + end );
        }

        // Step 1. Calculate all overlaped time units.
        TimeUnit[]  overlappedTimeUnits  = CalculatorTools.findAndSetOverlappedTimeUnits(tariff);
        total += calculateOverlappedAtTheSameDayTimeUnits(overlappedTimeUnits, start, end);

        // Step 2. check any blockages . If even one block exist. throw 'No parking allowed' exception.
        TimeUnit[]  timeUnits  = tariff.getTimeUnits();
        total += calculateTimeUnits(timeUnits, start, end);
        return centsRoundUp(total);
    }

    private static float centsRoundUp(float a){
        float cents = a - Math.round(a);
        double ret = Math.round(a) + Math.ceil(cents *100)/100.0;
        return (float) ret;

    }

    public static float calculateOverlappedAtTheSameDayTimeUnits(TimeUnit[] timeUnits, LocalDateTime start, LocalDateTime end) {
        float total = (float)0;
        float price = (float)0;
        long  billblMin = 0;
        long  oldBillblTime = 0;
        TimeUnit oldTimeUnit = null;
        LocalDate startDate = start.toLocalDate();
        if(timeUnits == null) return 0;
        Iterable<TimeUnit> iterable = Arrays.asList(timeUnits);
        Iterator<TimeUnit> iterator =  iterable.iterator();
        if(timeUnits != null) {
            TimeUnit timeUnit = iterator.next();
            do {
                if (RateType.BLOCKED == timeUnit.getRate().getType() &&
                        (isInTimeRangeofTU(start, timeUnit) || isInTimeRangeofTU(end, timeUnit) || hasTUin(start, end, timeUnit))) {
                    throw new BlockedParkingException("Parking time:,from: " + start + "to: " + end + "  overlaped with blocking parking period:" + timeUnit);

                } else if (RateType.LINEAR == timeUnit.getRate().getType()) {
                    billblMin = calcBillableMinutes(start, end, timeUnit);
                    Rate rate = timeUnit.getRate();
                    price = (rate.getPriceValue()) / rate.getIntervalValue() * billblMin;
                } else if (RateType.FLAT == timeUnit.getRate().getType()) {
                    // consider FLAT rate is per day FLAT ( for n days it's  LINEAR:  price = n * 1 day rate)
                    int billableDays = calcBillableDaysInRange(start.toLocalDate(), end.toLocalDate(), timeUnit);
                    price = billableDays * timeUnit.getRate().getPriceValue();
                } else if (RateType.UNIT == timeUnit.getRate().getType()) {
                    // consider UNIT rate is 1 price for whole time period it could be price per month or per 3 minutes dosn't matter
                    price = timeUnit.getRate().getPriceValue();
                } else if (RateType.PROGRESSIVE == timeUnit.getRate().getType()) {
                    billblMin = calcBillableMinutes(start, end, timeUnit);
                    price = calcByProgressiveRate(billblMin, timeUnit);
                }
                // consider array of TimeUnits sorted chronologically in ascending order
                // if next element of array ( timeUnit  ) has less priority then old one ( oldTimeUnit) consider
                // this as exceptional situation and decline price calculated on  new (timeUnit). Previous
                // tariff time will be extended on amount of billable time of new (timeUnit) and price will be recalculated
                // for previous (oldTimeUnit)
                // enter to recursion
                RerurnObj retObj = recursion(timeUnit, iterator, billblMin, start, end);
                if( retObj.price >0 ) {
                    total += retObj.price; //the current rate timeUnit rate has bigger priority over next(s) timeUnits
                }
                else {
                    total += price;
                }
                timeUnit = retObj.next;
            } while (timeUnit != null);
        }
        return total;
    }

    private static RerurnObj recursion(TimeUnit timeUnit, Iterator<TimeUnit> iterrator, long billblMin, LocalDateTime start, LocalDateTime end ){
        RerurnObj ret = new RerurnObj();
        float price ;
        if( iterrator.hasNext() &&
                timeUnit.getPriority() > (ret.next = iterrator.next()).getPriority()) {
            // start recursion
            long delta = calcBillableMinutes(start, end, ret.next);
            //exit from recursion
            if( delta > 0 ) {
                price = calculateTimeUnit(delta + billblMin, timeUnit, start, end);
                //enter to recursion
                ret = recursion(timeUnit,iterrator, delta + billblMin, start,end );
                ret.price  += price;
            }

        }

        return ret;
    }

    /**
     * @param billblMin
     * @param timeUnit
     * @param start
     * @param end
     * @return
     */
    private static float calculateTimeUnit(long billblMin, TimeUnit timeUnit, LocalDateTime start, LocalDateTime end) {
        float price = (float) 0.;
        if (RateType.BLOCKED == timeUnit.getRate().getType() &&
                (isInTimeRangeofTU(start, timeUnit) || isInTimeRangeofTU(end, timeUnit) || hasTUin(start, end, timeUnit))) {
            throw new BlockedParkingException("Parking time:,from: " + start + "to: " + end + "  overlaped with blocking parking period:" + timeUnit);

        }else if (RateType.LINEAR == timeUnit.getRate().getType()) {
            Rate rate = timeUnit.getRate();
            price = (rate.getPriceValue())/rate.getIntervalValue()*billblMin;
        }else if (RateType.FLAT == timeUnit.getRate().getType()) {
            // consider FLAT rate is per day FLAT ( for n days it's  LINEAR:  price = n * 1 day rate)
            int billableDays = calcBillableDaysInRange(start.toLocalDate(), end.toLocalDate(), timeUnit);
            price = billableDays*timeUnit.getRate().getPriceValue();
        } else if (RateType.UNIT == timeUnit.getRate().getType()) {
            // consider UNIT rate is 1 price for whole time period it could be price per month or per 3 minutes dosn't matter
            price = timeUnit.getRate().getPriceValue();
        } else if (RateType.PROGRESSIVE == timeUnit.getRate().getType()) {
            price = calcByProgressiveRate(billblMin, timeUnit);
        }

        return price;
    }


    /**
     * @param timeUnits
     * @param start
     * @param end
     * @return
     */
    public static float calculateTimeUnits(TimeUnit[] timeUnits, LocalDateTime start, LocalDateTime end) {
        float total = (float)0;
        float price = (float)0;
        long  billblMin = 0;

        if(timeUnits != null)
            for (TimeUnit timeUnit: timeUnits) {

                if (RateType.BLOCKED == timeUnit.getRate().getType() &&
                        (isInTimeRangeofTU(start, timeUnit) || isInTimeRangeofTU(end, timeUnit) || hasTUin(start, end, timeUnit))) {
                    throw new BlockedParkingException("Parking time:,from: " + start + " to: " + end + "  overlaped with blocking parking period:\n Total: 0");

                }else if (RateType.LINEAR == timeUnit.getRate().getType()) {
                    billblMin = calcBillableMinutes(start, end, timeUnit);
                    Rate rate = timeUnit.getRate();
                    price = (rate.getPriceValue())/rate.getIntervalValue()*billblMin;
                    total += price;
                }else if (RateType.FLAT == timeUnit.getRate().getType()) {
                    // consider FLAT rate is per day FLAT ( for n days it's  LINEAR:  price = n * 1 day rate)
                    int billableDays = calcBillableDaysInRange(start.toLocalDate(), end.toLocalDate(), timeUnit);
                    total += billableDays*timeUnit.getRate().getPriceValue();
                } else if (RateType.UNIT == timeUnit.getRate().getType()) {
                    // consider UNIT rate is 1 price for whole time period it could be price per month or per 3 minutes dosn't matter
                    total += timeUnit.getRate().getPriceValue();
                } else if (RateType.PROGRESSIVE == timeUnit.getRate().getType()) {
                    billblMin = calcBillableMinutes(start, end, timeUnit);
                    total += calcByProgressiveRate(billblMin, timeUnit);
                }
         }
        return total;
    }

    /**
     * @param billblMin
     * @param timeUnit
     * @return
     */
    private static float calcByProgressiveRate(long billblMin, TimeUnit timeUnit){
        Rate rate = timeUnit.getRate();
        float total = (float)0.;
        long tuRange = timeUnit.getTimeUnitRange();
        boolean isMoreThenOneTU = billblMin > tuRange;
        Rate[] subRates = rate.getRate();
        int i = 0;
        do {
            int intervalSumm = 0;
            // if index greater or equal  subRates.length-1 then left index without changes. (Use always last element)
            Rate subRate = subRates[i];
            if( i < subRates.length-1 )
                i+=1;
            else
            if (isMoreThenOneTU) i = 0;

            if (billblMin <= 0) break;
            long interval = subRate.getIntervalValue();
            intervalSumm += interval;
            // if this last interval
            if (billblMin < interval || interval == 0) {
                interval = billblMin;
            }
            if (RateType.LINEAR == subRate.getType())
                //prorate last interval
                total += interval *(subRate.getPriceValue())/subRate.getIntervalValue();

            else if ( RateType.OTHERTIME  == subRate.getType()  ) {
                total += subRate.getPriceValue();
                // Calculate otherTime which is left after all intervals applied
                interval = timeUnit.getTimeUnitRange() - intervalSumm;

            } else //if rate UNIT or FLAT
                total += subRate.getPriceValue();
            billblMin -= interval;
        } while (billblMin > 0);

        return total;
    }
        /**
         * @param start
         * @param end
         * @param tu
         * @return
         */
    public static long calcBillableMinutes(LocalDateTime start, LocalDateTime end, TimeUnit tu) {
        LocalDate[] eventDates = tu.getEventDate();
        DayOfWeek[] daysOfWeek = tu.getDateOfTheWeek();
        int[] hours = tu.getHoursRange();
        int[] minutes = tu.getMinutsRange();
        float ret = (float)0;
        long delta =0;

        if(daysOfWeek[0] != null && daysOfWeek[1] != null ) {
            LocalDate startDate = start.toLocalDate();
            LocalDate endDate = end.toLocalDate();
            LocalDateTime eventDateTime1 = startDate.atTime(hours[0], minutes[0]);
            LocalDateTime eventDateTime2 = endDate.atTime(hours[1], minutes[1]);
            // calculate how many days of week between startDate an endDate
            int billableDays =  calcDaysOfTheWeekInRange( startDate, endDate,daysOfWeek);
            delta = minuteCalculation(start, end, billableDays, eventDateTime1, eventDateTime2, hours, minutes);

        } else if(daysOfWeek[0] != null && daysOfWeek[1] == null ) {
            LocalDate  startDate = start.toLocalDate();
            LocalDate  endDate = end.toLocalDate();
            LocalDateTime eventDateTime1 = startDate.atTime(hours[0], minutes[0]);
            LocalDateTime eventDateTime2 = endDate.atTime(hours[1],minutes[1]);
            // calculate how many days of week between startDate an endDate
            int billableDays =  calcDaysOfTheWeekInRange( startDate, endDate,daysOfWeek);
            if (billableDays  == 1 ) {
                if (daysOfWeek[0] == startDate.getDayOfWeek()) { // firs day  of parking is billable
                    eventDateTime1 = startDate.atTime(hours[0], minutes[0]);
                    eventDateTime2 = startDate.atTime(hours[1],minutes[1]);
                    end = startDate.atTime(end.getHour(), end.getMinute());
                } else if (daysOfWeek[0] == endDate.getDayOfWeek()) { // last day  of parking is billable
                    eventDateTime1 = endDate.atTime(hours[0], minutes[0]);
                    eventDateTime2 = endDate.atTime(hours[1],minutes[1]);
                    start = endDate.atTime(start.getHour(), start.getMinute());
                }
            }
            delta = minuteCalculation(start, end, billableDays, eventDateTime1, eventDateTime2, hours, minutes);

        } else if( eventDates[0] != null  &&  eventDates[1] == null ) {
            LocalDateTime eventDateTime1 = eventDates[0].atTime(hours[0],minutes[0]);
            LocalDateTime eventDateTime2 = eventDates[0].atTime(hours[1],minutes[1]);
            // get last day parking local time
            LocalTime localTime = end.toLocalTime();
            end = localTime.atDate(start.toLocalDate());
            int billableDays = 1;
            delta = minuteCalculation(start, end, billableDays, eventDateTime1, eventDateTime2, hours, minutes);

        } else  if( eventDates[0] != null  &&  eventDates[1] != null ) {
            LocalDateTime eventDateTime1 = eventDates[0].atTime(hours[0],minutes[0]);
            LocalDateTime eventDateTime2 = eventDates[1].atTime(hours[1],minutes[1]);
            int billableDays =  calcBillableDaysInRange(start.toLocalDate(),end.toLocalDate(), eventDates[0],eventDates[1]) + 1;
            delta = minuteCalculation(start, end , billableDays, eventDateTime1, eventDateTime2, hours, minutes);
        }
        return delta;
    }

    private static int calcBillableDaysInRange(LocalDate start, LocalDate end, TimeUnit tu){
        LocalDate[] eventDates = tu.getEventDate();
        DayOfWeek[] daysOfWeek = tu.getDateOfTheWeek();
        int billableDays = 1;
        if( eventDates[0] != null  &&  eventDates[1] != null ) {
            billableDays =  calcBillableDaysInRange(start,end, eventDates[0],eventDates[1]) + 1;
        } else if(daysOfWeek[0] != null && daysOfWeek[1] != null ){
            // calculate how many days of week between startDate an endDate
            billableDays =  calcDaysOfTheWeekInRange( start, end,daysOfWeek);
        }
        return billableDays;
    }

    /**
     * @param start
     * @param end
     * @param parkStart
     * @param parkEnd
     * @return
     */
    private static int calcBillableDaysInRange(LocalDate start, LocalDate end, LocalDate parkStart, LocalDate parkEnd) {

        int billableDays = 1;
        if( start.isBefore(parkStart) && end.isAfter(parkEnd) ) {
            billableDays =  Period.between(parkStart, parkEnd).getDays();
        } else if(  parkEnd.isAfter(end) && parkStart.isBefore( start)) {
            billableDays =  Period.between(start, end).getDays();
        } else if( parkEnd.isAfter(start) && parkStart.isAfter(start) && parkStart.isBefore(end) ) {
            billableDays =  Period.between(parkStart, end).getDays();
        } else if( end.isAfter(parkStart) && start.isAfter(parkStart) && start.isBefore(parkEnd) ) {
            billableDays =  Period.between(start, parkEnd).getDays();
        }
        return billableDays;
    }

    /**
     * @param start
     * @param end
     * @param daysOfWeek
     * @return
     */
    private static int calcDaysOfTheWeekInRange(LocalDate start, LocalDate end, DayOfWeek[] daysOfWeek) {
        Set<DayOfWeek> dayOfWeeks = null;
        if ( daysOfWeek[1] != null) {
            dayOfWeeks = EnumSet.range(daysOfWeek[0], daysOfWeek[1]);
        } else{
            dayOfWeeks = EnumSet.of(daysOfWeek[0]);
        }

        LocalDate date = start;
        int billableDates = 0;
        while( date.isBefore( end ) || date.isEqual(end) ) {
            if( dayOfWeeks.contains( date.getDayOfWeek() ) ) { // If not weekend, collect this LocalDate.
                billableDates +=1 ;
            }
            // Prepare for next loop.
            date = date.plusDays( 1 ); // Increment to next day.
        }
        return billableDates;
    }

    /**
     * @param eventDates
     * @param daysOfWeek
     * @return
     */
    public static boolean isEventDaysHaveTheWeekDaysInRange( LocalDate[] eventDates, DayOfWeek[] daysOfWeek) {
        LocalDate start = eventDates[0];
        if( eventDates[0] == null  ) return false;
        LocalDate end = (eventDates[1] != null? eventDates[1]:start);
        return calcDaysOfTheWeekInRange(start,end, daysOfWeek) > 0;
    }

    /**
     * @param daysOfWeek1
     * @param daysOfWeek2
     * @return
     */
    public static boolean isTheWeekDaysOverlapped( DayOfWeek[] daysOfWeek1, DayOfWeek[] daysOfWeek2) {
        for(DayOfWeek dayOfWeek1 :daysOfWeek1){
            if( dayOfWeek1 == null ) continue;
            for(DayOfWeek dayOfWeek2 :daysOfWeek2){
                if( dayOfWeek2 == null ) continue;
                if( dayOfWeek1.getValue() == dayOfWeek2.getValue()) return true;
            }
        }
        return false;
    }

    /**
     * @param eventDates1
     * @param eventDates2
     * @return
     */
    public static boolean isEventDaysOverlapped( LocalDate[] eventDates1, LocalDate[] eventDates2) {
        if( eventDates1[0] == null || eventDates2[0] == null ) return false;
        LocalDate endDate1 = (eventDates1[1] == null ? eventDates1[0]: eventDates1[1]);
        LocalDate endDate2 = (eventDates2[1] == null ? eventDates2[0]: eventDates2[1]);

        boolean ret = !(eventDates1[0].isBefore(eventDates2[0]) || eventDates1[0].isAfter(endDate2)) ||
                !(eventDates2[0].isBefore(eventDates1[0]) || eventDates2[0].isAfter(endDate1));
        return ret;
    }

    /**
     * @param timeUnit1
     * @param timeUnit2
     * @return
     */
    private static boolean isTimeUnitsOverlapped(TimeUnit timeUnit1, TimeUnit timeUnit2){
        DayOfWeek[] daysOfWeek1 = timeUnit1.getDateOfTheWeek();
        DayOfWeek[] daysOfWeek2 = timeUnit2.getDateOfTheWeek();
        LocalDate[] eventDates1 = timeUnit1.getEventDate();
        LocalDate[] eventDates2 = timeUnit2.getEventDate();
        boolean ret = isTheWeekDaysOverlapped(daysOfWeek1, daysOfWeek2) ||
                isEventDaysHaveTheWeekDaysInRange( eventDates1, daysOfWeek2) ||
                isEventDaysHaveTheWeekDaysInRange( eventDates2, daysOfWeek1) ||
                isEventDaysOverlapped            ( eventDates1, eventDates2);
        return ret;

    }

    /**
     * @param tariff
     * @return
     */
    public static TimeUnit[] findAndSetOverlappedTimeUnits(Tariff tariff) {
        TimeUnit[] timeUnits = tariff.getTimeUnits();
        Set<TimeUnit> overlappedTimeUnits = new TreeSet<>(new CompareByTime());
        List<TimeUnit> listOfTimeUnits = new ArrayList( Arrays.asList(timeUnits) );
        boolean onlyFirst = true;
        for (int i = 0 ; i < listOfTimeUnits.size(); i++) {
            if( RateType.BLOCKED == listOfTimeUnits.get(i).getRate().getType() ) continue;
            for (int j = i+1 ;  j < listOfTimeUnits.size(); j++) {
                // if one of condition applied
                if( RateType.BLOCKED == listOfTimeUnits.get(j).getRate().getType() ) continue;
                if ( isTimeUnitsOverlapped(listOfTimeUnits.get(i), listOfTimeUnits.get(j)) ) {
                    if( onlyFirst ) {
                        overlappedTimeUnits.add(listOfTimeUnits.get(i));
                        onlyFirst = false;
                    }
                    overlappedTimeUnits.add(listOfTimeUnits.get(j));
                }
            }

        }

        if( overlappedTimeUnits.size() > 0  ) {
            // remove overlapped time units from original ArrayList
            for (TimeUnit timeUnit : overlappedTimeUnits)
                listOfTimeUnits.remove(timeUnit);
            tariff.setTimeUnits(listOfTimeUnits.toArray(new TimeUnit[listOfTimeUnits.size()]));
            tariff.setOverlappedTimeUnits(overlappedTimeUnits.toArray(new TimeUnit[overlappedTimeUnits.size()]));

        }
        return tariff.getOverlappedTimeUnits();
    }



    private static long minuteCalculation(LocalDateTime start, LocalDateTime end, int billableDays, LocalDateTime eventDateTime1, LocalDateTime eventDateTime2, int[] hours, int[] minutes) {
        long delta = 0;
        if (isParkingTimeHasTU( start, end, eventDateTime1, eventDateTime2 )) {
            // correct it to the end of  first payable day
            //eventDateTime2 = eventDateTime1.toLocalDate().atTime(hours[1], minutes[1]);
            //delta = billableDays*(CalendarTools.asDate(eventDateTime2).getTime() - CalendarTools.asDate(eventDateTime1).getTime())/MINUTE_MILLIS; // in minutes
            delta = billableDays * populateTimeUnitRange(hours, minutes);

        } else  if( isInTUtimeRange( start, eventDateTime1, eventDateTime2, hours, minutes) && isInTUtimeRange( end, eventDateTime1, eventDateTime2, hours, minutes) ) {
            // calculates minutes for first day only
            // correct it to the end of  first payable day
            if(billableDays > 1) {
                LocalDateTime startFirstDay = start;
                LocalDateTime endFirstDay = start.toLocalDate().atTime(hours[1], minutes[1]);
                //delta = (CalendarTools.asDate(endFirstDay).getTime() - CalendarTools.asDate(startFirstDay).getTime()) / MINUTE_MILLIS; // in minutes
                delta = startFirstDay.until(endFirstDay, ChronoUnit.MINUTES);
                LocalDateTime startLastDay = end.toLocalDate().atTime(hours[0], minutes[0]);
                LocalDateTime endLastDay = end;
                // (CalendarTools.asDate(endLastDay).getTime() - CalendarTools.asDate(startLastDay).getTime()) / MINUTE_MILLIS; // in minutes
                delta +=  startLastDay.until(endLastDay, ChronoUnit.MINUTES);
                billableDays -= 2;
                eventDateTime2 = eventDateTime1.toLocalDate().atTime(hours[1], minutes[1]);
//                (CalendarTools.asDate(eventDateTime2).getTime() - CalendarTools.asDate(eventDateTime1).getTime()) / MINUTE_MILLIS; // in minutes

                delta +=  billableDays*eventDateTime1.until(eventDateTime2, ChronoUnit.MINUTES);
            } else {
                // if only one billable day then then first day of parking is last day of parking
                //(CalendarTools.asDate(end).getTime() - CalendarTools.asDate(start).getTime()) / MINUTE_MILLIS; // in minutes
                // if billableDays == 0 or 1
                delta = billableDays * start.until(end, ChronoUnit.MINUTES);
            }

        } else  if( isInTUtimeRange( end, eventDateTime1, eventDateTime2, hours, minutes)){
            // calculates minutes for last day only
            // correct it to the end of  last payable day
            LocalDateTime startLastDay = end.toLocalDate().atTime(hours[0], minutes[0]);
            LocalDateTime endLastDay = end;
            delta = startLastDay.until(endLastDay, ChronoUnit.MINUTES); // in minutes
            if(billableDays > 1) {
                billableDays -= 1;
                eventDateTime2 = eventDateTime1.toLocalDate().atTime(hours[1], minutes[1]);
                //CalendarTools.asDate(eventDateTime2).getTime() - CalendarTools.asDate(eventDateTime1).getTime()) / MINUTE_MILLIS; // in minutes
                delta +=  billableDays*eventDateTime1.until(eventDateTime2, ChronoUnit.MINUTES);
            } else {
                // if billableDays == 0 or 1
                delta = billableDays * delta;
            }

        } else  if( isInTUtimeRange( start, eventDateTime1, eventDateTime2, hours, minutes)){
                LocalDateTime startFirstDay = start;
                LocalDateTime endFirstDay = start.toLocalDate().atTime(hours[1], minutes[1]);
                //(CalendarTools.asDate(endFirstDay).getTime() - CalendarTools.asDate(startFirstDay).getTime()) / MINUTE_MILLIS; // in minutes
                delta = startFirstDay.until(endFirstDay, ChronoUnit.MINUTES);
                if(billableDays > 1) {
                    billableDays -= 1;
                    eventDateTime2 = eventDateTime1.toLocalDate().atTime(hours[1], minutes[1]);
                    //CalendarTools.asDate(eventDateTime2).getTime() - CalendarTools.asDate(eventDateTime1).getTime()) / MINUTE_MILLIS; // in minutes
                    delta +=  billableDays*eventDateTime1.until(eventDateTime2, ChronoUnit.MINUTES);
                } else {
                    // if billableDays == 0 or 1
                    delta = billableDays * delta;
                }
            // exceptional case when parking start time after end of parking period of first day but still before the
            // start of parking period of last day and end time of parking is after end of last day of parking period
        } else {
            billableDays -= 1;
            delta = billableDays * populateTimeUnitRange(hours, minutes);

        }
        return delta;
    }

    /**
     * Populate the time in minutes for TimeUnit
     */
    private static int populateTimeUnitRange(int[] hoursRange, int[] minutsRange){
        return (hoursRange[1] - hoursRange[0])*60 + (minutsRange[1] - minutsRange[0]);
    }

    public static boolean  hasTUin(LocalDateTime  start, LocalDateTime  end, TimeUnit tu){
        LocalDate[] eventDates = tu.getEventDate();
        DayOfWeek[] dayOfWeek = tu.getDateOfTheWeek();
        int[] hours = tu.getHoursRange();
        int[] minutes = tu.getMinutsRange();
        boolean ret = false;

        if(dayOfWeek[0] != null && dayOfWeek[1] != null ){
            LocalDate  startDate = start.toLocalDate();
            LocalDate  endDate = end.toLocalDate();
            LocalDateTime eventDateTime1 = startDate.atTime(hours[0], minutes[0]);
            LocalDateTime eventDateTime2 = endDate.atTime(hours[1],minutes[1]);
            ret = isParkingTimeHasTU(start,end, eventDateTime1, eventDateTime2);

        } else if(dayOfWeek[0] != null && dayOfWeek[1] == null ) {
            LocalDate  startDate = start.toLocalDate();
            LocalDate  endDate = start.toLocalDate();
            LocalDateTime eventDateTime1 = startDate.atTime(hours[0], minutes[0]);
            LocalDateTime eventDateTime2 = endDate.atTime(hours[1],minutes[1]);
            ret = isParkingTimeHasTU(start,end, eventDateTime1, eventDateTime2);

        } else if( eventDates[0] != null  &&  eventDates[1] == null ) {
            LocalDateTime eventDate1 = eventDates[0].atTime(hours[0],minutes[0]);
            LocalDateTime eventDate2 = eventDates[0].atTime(hours[1],minutes[1]);
            ret = isParkingTimeHasTU( start, end, eventDate1, eventDate2 );

        } else  if( eventDates[0] != null  &&  eventDates[1] != null ) {
            LocalDateTime eventDate1 = eventDates[0].atTime(hours[0],minutes[0]);
            LocalDateTime eventDate2 = eventDates[1].atTime(hours[1],minutes[1]);
            ret = isParkingTimeHasTU( start, end, eventDate1, eventDate2 );
        }
        return ret;

    }



    public static boolean isInTimeRangeofTU(LocalDateTime localTime , TimeUnit tu){
        LocalDate[] eventDates = tu.getEventDate();
        DayOfWeek[] dayOfWeek = tu.getDateOfTheWeek();
        int[] hours = tu.getHoursRange();
        int[] minutes = tu.getMinutsRange();
        boolean ret = false;
        if(dayOfWeek[0] != null && dayOfWeek[1] != null ){
            LocalDate  currentDate = localTime.toLocalDate();
            LocalDateTime eventDate1 = currentDate.atTime(hours[0], minutes[0]);
            LocalDateTime eventDate2 = currentDate.atTime(hours[1],minutes[1]);

            if((localTime.getDayOfWeek().getValue() >= dayOfWeek[0].getValue())
                    && (localTime.getDayOfWeek().getValue() <= dayOfWeek[1].getValue())) {

                // check if there is exact match on boundaries then true dosn't matter what other condition is
                //TODO uncomment this if inclusive rules are applied: ( parking start at the end of blockage )
                //ret = localTime.compareTo(eventDate1) == 0 || localTime.compareTo(eventDate2) == 0  ||
                  ret =  ( localTime.isAfter(eventDate1) && localTime.isBefore(eventDate2));
            }

        } else if(dayOfWeek[0] != null && dayOfWeek[1] == null ){
            LocalDate  currentDate = localTime.toLocalDate();
            LocalDateTime eventDate1 = currentDate.atTime(hours[0], minutes[0]);
            LocalDateTime eventDate2 = currentDate.atTime(hours[1],minutes[1]);

            if( localTime.getDayOfWeek() == dayOfWeek[0] ){
                // check if there is exact match on boundaries then true dosn't matter what other condition is
                //TODO uncomment this if inclusive rules are applied: ( parking start at the end of blockage )
                //ret = localTime.compareTo(eventDate1) == 0 || localTime.compareTo(eventDate2) == 0  ||
                ret =  ( localTime.isAfter(eventDate1) && localTime.isBefore(eventDate2));
            }

        } else if( eventDates[0] != null  &&  eventDates[1] == null ) {
            LocalDateTime eventDate1 = eventDates[0].atTime(hours[0],minutes[0]);
            LocalDateTime eventDate2 = eventDates[0].atTime(hours[1],minutes[1]);
            ret = isInTUtimeRange( localTime, eventDate1, eventDate2, hours, minutes);

        } else  if( eventDates[0] != null  &&  eventDates[1] != null ) {
            LocalDateTime eventDate1 = eventDates[0].atTime(hours[0],minutes[0]);
            LocalDateTime eventDate2 = eventDates[1].atTime(hours[1],minutes[1]);
            ret = isInTUtimeRange( localTime, eventDate1, eventDate2, hours, minutes);
        }
        return ret;
    }

    private static boolean isInTUtimeRange(LocalDateTime localTime, LocalDateTime parkStart, LocalDateTime parkEnd,
                                           int[] hours, int[] minutes){
        if(         localTime.isAfter(  parkStart.minusNanos(1))
                &&  localTime.isBefore( parkEnd.plusNanos(1) )) {
            LocalDateTime endOfTimeRange = localTime.toLocalDate().atTime(hours[1],minutes[1]);
            LocalDateTime beginOfTimeRange = localTime.toLocalDate().atTime(hours[0],minutes[0]);
            //
            return localTime.isBefore(  endOfTimeRange.plusNanos(1) ) &&  localTime.isAfter(  beginOfTimeRange.minusNanos(1) ) ;

        }
        return false;
    }

    /**   Check if [start,end] parking time  boundary  contains parking scheduled times [parkStart,parkEnd]
     * @param start
     * @param end
     * @param parkStart
     * @param parkEnd
     * @return
     */
    private static boolean isParkingTimeHasTU( LocalDateTime start, LocalDateTime end, LocalDateTime parkStart,
                                               LocalDateTime parkEnd){
        return   end.isAfter(  parkEnd.minusNanos(1)) &&  start.isBefore( parkStart.plusNanos(1));
    }


    private static class BlockedParkingException extends RuntimeException {
        public BlockedParkingException(String s) {
            LOGGER.log( Level.INFO, s );
        }
    }

    private static class RerurnObj {
        public float price;
        public  TimeUnit next;
    }
}
