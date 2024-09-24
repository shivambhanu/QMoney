
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {
  RestTemplate restTemplate;
  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonMappingException, JsonProcessingException {
      String tempUrl = prepareUrl(symbol, from, to);
      // TiingoCandle[] allCandles = restTemplate.getForObject(tempUrl, TiingoCandle[].class);

      String jsonString = restTemplate.getForObject(tempUrl, String.class);
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
        // Convert JSON string to a Person object
      TiingoCandle[] allCandles = mapper.readValue(jsonString, TiingoCandle[].class);
      
      List<Candle> candles = new ArrayList<>();
      for(TiingoCandle currCandle : allCandles)
        candles.add(currCandle);
      
      return candles;
  }


  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest

  public static String prepareUrl(String symbol, LocalDate from, LocalDate to) {
    String targetUrl = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + from + "&endDate=" + to + "&token=aac9905763932f2fdbebefbbe6c063942d293d01";
    return targetUrl;
  }
}
