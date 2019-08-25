package net.bloberry.tarificator.metadata;

public interface  Constants {
    String RANGE_SEPAR = "%";
    String DATETIME_SEPAR = ",";
    String HOURMINUTES_SEPAR = ":";
    String REGEXPRESS_yyyy_mm_dd  = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
    String REGEXPRESS_mm_dd_yyyy  = "((0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])-[12]\\d{3})";
    String REGEXPRESS_dd_mm_yyyy  = "((0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3})";
    String REGEXPRESS_dd_MMM_YYYY  = "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]|(?:Jan|Mar|May|Jul|Aug|Oct|Dec)))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2]|(?:Jan|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)(?:0?2|(?:Feb))\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9]|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep))|(?:1[0-2]|(?:Oct|Nov|Dec)))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$";
    String REGEXPRESS_DAYS_OF_THE_WEEK = "((mon|tues|wed(nes)?|thur(s)?|fri|sat(ur)?|sun)(day)?)";
    String REGEXPRESS_MONDAY = "((mon)(day)?)";
    String REGEXPRESS_TUESDAY = "((tues)(day)?)";
    String REGEXPRESS_WEDNESDAY = "((wed)(nesday)?)";
    String REGEXPRESS_THURSDAY = "((thurs)(day)?)";
    String REGEXPRESS_FRIDAY = "((fri)(day)?)";
    String REGEXPRESS_SATURDAY = "((sat)(urday)?)";
    String REGEXPRESS_SUNDAY = "((sun)(day)?)";
    String REGEXPRESS_24hTIME = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$";
    String REGEXPRESS_12hTIME = "^(0?[1-9]|1[0-2]):[0-5][0-9]$";
    // Date format  '01/Jan/2000' , '01/01/2000'
    String REGEXPRESS_DATE = "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]|(?:Jan|Mar|May|Jul|Aug|Oct|Dec)))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2]|(?:Jan|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)(?:0?2|(?:Feb))\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9]|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep))|(?:1[0-2]|(?:Oct|Nov|Dec)))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$";

    String PROGRESSIVE ="progressive";
    String OTHERTIME ="othertime";
    String FLAT ="flat";
    String LINEAR ="linear";
    String UNIT ="unit";
    String BLOCKED ="blocked";

    // special events definitions
    String CHRISTMAS = "christmas";
    String THANKSGIVING = "thanksgiving";
    String MEMORIAL_DAY = "memorialday";

    String DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm";

}
