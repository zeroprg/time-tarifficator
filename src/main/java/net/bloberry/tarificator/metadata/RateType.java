package net.bloberry.tarificator.metadata;


import java.util.logging.Level;
import java.util.logging.Logger;

public enum RateType {

    /**
     * The singleton instance for the rate-type Progressive.
     * This has the numeric value of {@code 1}.
     */
    PROGRESSIVE,
    /**
     * The singleton instance for the  rate-type Linear.
     * This has the numeric value of {@code 2}.
     */
    LINEAR,
    /**
     * The singleton instance for the rate-type Flat used to calculate constant rate
     * which used till the end of interval.
     * This has the numeric value of {@code 3}.
     */
    FLAT,
    /**
     * The singleton instance for the rate-type Unit.
     * It's used to mark time unit price as permanent one time pay fee for n time of time units.
     * Usually used in begining of pay interval as prepaid fee.
     * This has the numeric value of {@code 3}.
     */
    UNIT,
    /**
     * The singleton instance for the rate-type Blocked.
     * This has the numeric value of {@code 3}.
     */
    BLOCKED,
    /**
     * Private cache of all the constants.
     */
    OTHERTIME;

    private static final RateType[] ENUMS = RateType.values();

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code RateType} from an {@code int} value.
     * <p>
     * {@code RateType} is an enum representing the 3 rates: PROGRESSIVE, LINEAR, FLAT  .
     * This factory allows the enum to be obtained from the {@code int} value.
     * The {@code int} value follows the ISO-8601 standard, from 1 (PROGRESSIVE) to 5 (BLOCKED).
     *
     * @param rateType  the rate-type to represent, from 1 (PROGRESSIVE) to 5 (BLOCKED)
     * @return the rate-type singleton, not null
     * @throws RateTypeException if the rate-type is invalid
     */
    public static RateType of(int rateType) {
        if (rateType < 1 || rateType > 6) {
            throw new RateTypeException("Invalid value for RateType: " + rateType);
        }
        return ENUMS[rateType - 1];
    }
    private static final Logger LOGGER = Logger.getLogger( RateType.class.getName() );

    private static class RateTypeException extends RuntimeException  {
        public RateTypeException(String s) {
            LOGGER.log( Level.INFO, "Wrong rate, rates only in dollars (5$) or cents (500c), your rate is:  ", s );
        }
    }
}
