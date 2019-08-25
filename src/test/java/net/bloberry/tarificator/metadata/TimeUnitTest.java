package net.bloberry.tarificator.metadata;

import org.junit.Test;

public class TimeUnitTest {

    @Test
    public void setDate() {
        TimeUnit tu = new TimeUnit();
        tu.setParkingTime("Mon%Fri");
    }
}