package net.bloberry.tarifficator.utils;


import net.bloberry.tarificator.metadata.TimeUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * This comparator used specifically to compare TimeUnit's time ranges
 * If time ranges overlapped return 0
 * if A before B return 1
 * if A after B return -1
 */
public class CompareByTime implements Comparator<TimeUnit>
{
    // Used for sorting in ascending order of
    // TimeUnit(s)
    public int compare(TimeUnit a, TimeUnit b)
    {

        // compare 2 TimeUnit(s) chronologically after current TimeUnit and they are overlapped
        // has more priority then current TimeUnit
        LocalDate currentDate = LocalDate.now() ;
        LocalDateTime startParkPeriodOfA = currentDate.atTime(a.getHoursRange()[0],a.getMinutsRange()[0]);
        LocalDateTime endParkPeriodOfA = currentDate.atTime(a.getHoursRange()[1],a.getMinutsRange()[1]);
        LocalDateTime startParkPeriodOfB = currentDate.atTime(b.getHoursRange()[0],b.getMinutsRange()[0]);
        LocalDateTime endParkPeriodOfB = currentDate.atTime(b.getHoursRange()[1],b.getMinutsRange()[1]);

        // check if oldTimeUnit overlapped with timeUnit, and if they overlaped return 0
        if (  endParkPeriodOfA.isBefore(startParkPeriodOfB) || endParkPeriodOfA.isEqual(startParkPeriodOfB)   ) return -1;
        else if  ( startParkPeriodOfA.isAfter(endParkPeriodOfB) || startParkPeriodOfA.isEqual(endParkPeriodOfB) ) return 1;

        return 0;
    }

    @Override
    public Comparator<TimeUnit> reversed() {
        return null;
    }

    @Override
    public Comparator<TimeUnit> thenComparing(Comparator<? super TimeUnit> other) {
        return null;
    }

    @Override
    public <U> Comparator<TimeUnit> thenComparing(Function<? super TimeUnit, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        return null;
    }

    @Override
    public <U extends Comparable<? super U>> Comparator<TimeUnit> thenComparing(Function<? super TimeUnit, ? extends U> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<TimeUnit> thenComparingInt(ToIntFunction<? super TimeUnit> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<TimeUnit> thenComparingLong(ToLongFunction<? super TimeUnit> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<TimeUnit> thenComparingDouble(ToDoubleFunction<? super TimeUnit> keyExtractor) {
        return null;
    }
}
