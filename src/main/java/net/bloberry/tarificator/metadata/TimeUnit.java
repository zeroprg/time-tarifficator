package net.bloberry.tarificator.metadata;

import net.bloberry.tarifficator.utils.CalculatorTools;
import net.bloberry.tarifficator.utils.CompareByTime;
import net.bloberry.tarifficator.utils.ParserTools;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class TimeUnit {
    private static final Logger LOGGER = Logger.getLogger( CalculatorTools.class.getName() );
    //Range of week days, or one week day in DayOfWeek format 1- Monday 2- Sunday
    DayOfWeek[] dateOfTheWeek = new DayOfWeek[2];
    // Time range  1 or 2 elements array contains time range in hours and minutes:
    // set deafault vaues for time range as whole day
    int[] hoursRange  =  new int[] {0 , 23};
    int[] minutsRange =  new int[] {0, 59 };
    int priority;
    int timeUnitRange = 1440; // default total amount of minutes
    // Local event date time
    LocalDate[] eventDate = new LocalDate[2];

    //------------------------------------- serialized part of the bean --------------------
    // String representation of parking description (date and time)
    String parkingTime;
    Rate rate;


    public DayOfWeek[] getDateOfTheWeek() {
        return dateOfTheWeek;
    }

    public void setDateOfTheWeek(DayOfWeek[] dateOfTheWeek) {
        if(this.eventDate[0] != null  )
            throw new TimeUnitBoundaryException("eventDate: " + this.eventDate[0] + this.eventDate[1]!= null?" ," +this.eventDate[1]:""
                    + " already specified for this Time Unit:");
        this.dateOfTheWeek = dateOfTheWeek;
    }

    public int[] getHoursRange() {
        return hoursRange;
    }

    public void setHoursRange(int[] hoursRange) {
        this.hoursRange = hoursRange;
        populateTimeUnitRange();
    }

    public int[] getMinutsRange() {
        return minutsRange;
    }

    public void setMinutsRange(int[] minutsRange) {
        this.minutsRange = minutsRange;
        populateTimeUnitRange();
    }

    public LocalDate[] getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate[] eventDate) {
        if(this.dateOfTheWeek[0] != null  )
            throw new TimeUnitBoundaryException("eventDate: " + this.dateOfTheWeek[0] + this.dateOfTheWeek[1]!= null?" ," +this.dateOfTheWeek[1]:""
                    + " already specified for this Time Unit:");

        this.eventDate = eventDate;
    }

    public Rate getRate() {
        return rate;
    }
    public void setRate(Rate rate) {
        this.rate = rate;
    }
    public int getTimeUnitRange() {      return timeUnitRange;  }
    public void setTimeUnitRange(int timeUnitRange) {
        this.timeUnitRange = timeUnitRange;
    }
    public int getPriority() { return priority;  }
    public void setPriority(int priority) { this.priority = priority;  }


    public String getParkingTime() {  return parkingTime;   }

    public void setParkingTime(String parkingTime) {
        TimeUnit tu =  ParserTools.convertDateRange(parkingTime);
        this.parkingTime   =  parkingTime;
        this.dateOfTheWeek =  tu.getDateOfTheWeek();
        this.hoursRange    = tu.getHoursRange();
        this.minutsRange   = tu.getMinutsRange();
        this.eventDate     = tu.getEventDate();
        populateTimeUnitRange();

    }
    /**
     * Populate the time in minutes for TimeUnit
     */
    private void populateTimeUnitRange(){
        this.timeUnitRange  = (this.hoursRange[1] - this.hoursRange[0])*60 + (this.minutsRange[1] - this.minutsRange[0]);
    }

    private class TimeUnitBoundaryException extends RuntimeException {
        public TimeUnitBoundaryException(String s) {
            LOGGER.log(Level.INFO, s);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parking times: " + this.parkingTime + '\n');
        sb.append((this.dateOfTheWeek[0]) != null ? "dateOfTheWeek[0]: " + this.dateOfTheWeek[0] + " ," : " " );
        sb.append((this.dateOfTheWeek[1]) != null ? "dateOfTheWeek[1]: " + this.dateOfTheWeek[1] + " ," : " " );
        sb.append((this.eventDate[0]!= null?"\neventDate[0]: "+ this.eventDate[0] + " ,":" ") + (this.eventDate[1]!= null?"eventDate[1]: "+this.eventDate[1]+ " ,":" ") );
        sb.append("\nstartTime: "+this.hoursRange[0]+":" + this.minutsRange[0] +" endTime: "+this.hoursRange[1]+":" + this.minutsRange[1] + '\n');
        sb.append("\ntimeUnitRange: " + this.timeUnitRange  + '\n');
        sb.append(" Rate: \n" + (this.rate != null?this.rate.toString():"null"));
        return sb.toString();
    }







    @Override
    public boolean equals(Object o) {
        if( o == null && this != null ) return false;
        TimeUnit obj = ((TimeUnit) o);
        boolean ret ;
        CompareByTime compareByTime = new CompareByTime();
        ret = compareByTime.compare(this, (TimeUnit) o ) == 0;
        return ret;
    }



    @Override
    // This hashcode still not sufficient to find overlaped by day TimeUnits
    // Gives the same hash code only objects have exactly the same boundary range.
    // Need additional actions to find overlapped boundaries
    public int hashCode() {
     // Consider hash code as bucket where all TimeUnit(s) of the same hashcode ( the same days range) sits :
       //int ret = 7;
       int ret = (dateOfTheWeek[0] != null? dateOfTheWeek[0].getValue(): 0) +
                 (dateOfTheWeek[1] != null? dateOfTheWeek[1].getValue(): 0) +
                 (eventDate[0] != null?  eventDate[0].getDayOfWeek().getValue(): 0) +
                 (eventDate[1] != null?  eventDate[1].getDayOfWeek().getValue(): 0);

        return ret;
    }

}
