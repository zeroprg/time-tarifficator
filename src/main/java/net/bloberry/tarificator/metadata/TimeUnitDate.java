package net.bloberry.tarificator.metadata;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 *
 */
public class TimeUnitDate {
    //Range of week days, or one week day in DayOfWeek format 1- Monday 2- Sunday
    private DayOfWeek dateOfTheWeek;
    // Local event date time
    private LocalDate eventDate;


    public DayOfWeek getDateOfTheWeek() {
        return dateOfTheWeek;
    }

    public void setDateOfTheWeek(DayOfWeek dateOfTheWeek) {
        this.dateOfTheWeek = dateOfTheWeek;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }
}
