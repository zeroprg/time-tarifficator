package net.bloberry.tarificator.metadata;

import net.bloberry.tarifficator.utils.ParserTools;

public class Tariff {
    private String tariffId;
    private String zoneId;
    private String description;
    private TimeUnit[] timeUnits;
    private TimeUnit[] overlappedTimeUnits;
    private String maximum_time;

    int maximum_timeValue;

    // Rate priority 0 - lowest priority , 2 highest priority
    int priority;

    public String getMaximum_time() { return maximum_time; }
    public void setMaximum_time(String maximum_time) { this.maximum_time = maximum_time;
        this.maximum_timeValue = ParserTools.parseTimeIntervalToMinutes(maximum_time);
    }
    public int getMaximum_timeValue() {   return maximum_timeValue; }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public TimeUnit[] getTimeUnits() {
        return timeUnits;
    }
    public void setTimeUnits(TimeUnit[] timeUnits) {
        this.timeUnits = timeUnits;
    }
    public String getTariffId() { return tariffId; }
    public void setTariffId(String tariffId) {  this.tariffId = tariffId; }
    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) {   this.zoneId = zoneId; }
    public String getDescription() {  return description; }
    public void setDescription(String description) { this.description = description;}
    public void setOverlappedTimeUnits(TimeUnit[] overlappedTimeUnits){ this.overlappedTimeUnits = overlappedTimeUnits;}


    @Override
    public boolean equals(Object obj) {
        return this.getZoneId().equals(((Tariff)obj).getZoneId());
    }
    @Override
    public int hashCode() { return (this.getZoneId()).hashCode(); }

    public TimeUnit[] getOverlappedTimeUnits() {  return overlappedTimeUnits; }

    public String toString(){
        String layer1 = (this.description != null?this.description:" ") + (this.tariffId)!= null?"tariffId: "+ this.tariffId + " ," :" " +
                (this.zoneId!= null && !this.zoneId.isEmpty()?" type: "+ this.zoneId + " ,":" ") + '\n' +
                "tariff's TimeUnits: " + '\n';
                StringBuilder sb = new StringBuilder();
                if (timeUnits !=null ) for (TimeUnit tu: this.timeUnits) {
                    sb.append(tu.toString());
                }

        return layer1 + sb.toString();
    }
}
