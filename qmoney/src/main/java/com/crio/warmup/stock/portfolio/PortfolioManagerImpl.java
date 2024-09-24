
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  RestTemplate restTemplate;
  StockQuotesService serviceVar;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService chosenService) {
    this.serviceVar = chosenService;
  }



  @Override 
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) {
    //declare and define the result list
    List<AnnualizedReturn> resList = new ArrayList<>();

    for(PortfolioTrade trade : portfolioTrades){
      // List<Candle> candles = fetchCandles(trade, endDate, getToken());
      List<Candle> candles = new ArrayList<>();
      try {
        candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate() ,endDate);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

      Double buyPrice = candles.get(0).getOpen();
      Double sellPrice = candles.get(candles.size()-1).getClose();;

      Double totalReturn = (sellPrice - buyPrice) / buyPrice;
      //calculate total years = (total days between startDate and endDate divide by 365)
      long daysBetween = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);  //endDate is not counted here
      double totalYears = (double) (daysBetween / 365.24);
      // System.out.println(totalYears);
      Double annualizedReturn = Math.pow(1 + totalReturn, (1.0 / totalYears)) - 1;
      AnnualizedReturn tempAnnualizedReturn = new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);

      resList.add(tempAnnualizedReturn);
    }

    Collections.sort(resList);
    return resList;
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    List<Candle> candles = serviceVar.getStockQuote(symbol, from, to);
    // String tempUrl = buildUri(symbol, from, to);
    // TiingoCandle[] allCandles = restTemplate.getForObject(tempUrl, TiingoCandle[].class);
    
    // List<Candle> candles = new ArrayList<>();
    // for(TiingoCandle currCandle : allCandles)
    //   candles.add(currCandle);
    
    return candles;
  }


  // protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
  //   String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?" + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
  //   String apiKey = "aac9905763932f2fdbebefbbe6c063942d293d01";

  //   String uri = uriTemplate.replace("$SYMBOL", symbol)
  //                           .replace("$STARTDATE", startDate.toString())
  //                           .replace("$ENDDATE", endDate.toString())
  //                           .replace("$APIKEY", apiKey);

  //   System.out.println(uri);
  //   return uri;
  // }


  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.



}
