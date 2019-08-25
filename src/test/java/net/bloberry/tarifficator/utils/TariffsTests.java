package net.bloberry.tarifficator.utils;

import net.bloberry.Tarifficator;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertTrue;

public class TariffsTests {
    @Test
    public void earlyBirdTariff() throws ParseException {
        Tarifficator tarifficator =new Tarifficator();
        float result = tarifficator.calculateTariffs("early-bird-tariff.yaml", "2004", "7-Dec-2018 09:30", "7-Dec-2018 11:30");
        assertTrue(Float.compare((float)8.5 , result) == 0);
    }
    @Test
    public void regularTariff() throws ParseException {
        Tarifficator tarifficator =new Tarifficator();
        float result = tarifficator.calculateTariffs("early-bird-tariff.yaml", "2005", "7-Dec-2018 09:30", "7-Dec-2018 11:30");
        assertTrue(Float.compare((float)8.79 , result) == 0);
    }

}
