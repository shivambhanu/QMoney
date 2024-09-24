
package com.crio.warmup.stock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.cglib.core.Local;
import java.time.LocalDate;
import java.util.Map;

// "2024-04-18": {
//   "1. open": "182.3500",
//   "2. high": "183.4600",
//   "3. low": "180.1700",
//   "4. close": "181.4700",
//   "5. volume": "2886733"
// }

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphavantageDailyResponse {

  @JsonProperty(value = "Time Series (Daily)")
  private Map<LocalDate, AlphavantageCandle> candles;

  public Map<LocalDate, AlphavantageCandle> getCandles() {
    return candles;
  }

  public void setCandles(Map<LocalDate, AlphavantageCandle> candles) {
    this.candles = candles;
  }
}
