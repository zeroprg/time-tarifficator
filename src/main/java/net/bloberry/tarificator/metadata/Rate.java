package net.bloberry.tarificator.metadata;

import net.bloberry.tarifficator.utils.ParserTools;

/**
 *
 */
public class Rate {
    private Rate[] rate;
    private RateType type;
    private String rateType;
    private String price;
    private String rateInterval;
    private float  priceValue;
    private long   intervalValue;

    public String getRateInterval() { return rateInterval; }

    public Rate[] getRate() { return rate;  }

    public void setRate(Rate[] rate) { this.rate = rate; }

    public RateType getType() {  return type;  }

    public void setType(RateType type) {  this.type = type;  }

    public float getPriceValue() { return priceValue; }

    public void setPriceValue(float priceValue) {  this.priceValue = priceValue; }

    public String getPrice() { return price; }

    public long getIntervalValue() { return intervalValue;   }

    public void setIntervalValue(long intervalValue) {  this.intervalValue = intervalValue; }

    public String getRateType() {return rateType; }

    // Setters which responsible for conversion to calculations units
    public void setRateType(String rateType) {
        this.rateType = rateType;
        this.type = ParserTools.convRateTypes(rateType);
    }

    public void setRateInterval(String rateInterval) {
        this.rateInterval = rateInterval;
        this.intervalValue = ParserTools.parseTimeIntervalToMinutes(rateInterval);
    }
    public void setPrice(String price) {
        this.price = price;
        this.priceValue = ParserTools.parseMonetaryUnit(price);
    }


    @Override
    public String toString(){
         String layer1 =
                 (this.getRateType()!= null && !this.getRateType().isEmpty() ?" type: "+ this.getRateType() + " ,":" ") +
                 (this.getRateInterval()!= null && !this.getRateInterval().isEmpty() ?" rateInterval: "+ this.getRateInterval() + " ,":" " ) +
                 (this.getPrice()!= null && !this.getPrice().isEmpty()?" price: "+ this.getPrice() + " ,":" " );

         StringBuilder sb = new StringBuilder();
         if(this.getRate() != null ) for( Rate subRate :this.getRate()) {

             sb.append( subRate.toString());
         }
         return layer1 + sb.toString();
    }

}
