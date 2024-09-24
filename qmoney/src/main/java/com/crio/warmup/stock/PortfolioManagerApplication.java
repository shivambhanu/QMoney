
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
//<======================================================================================================================>
//                                                          MODULE - 1
//<======================================================================================================================>

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File inputFile = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    List<String> resList = new ArrayList<>();

    PortfolioTrade[] pTrades = om.readValue(inputFile, PortfolioTrade[].class);
    for(PortfolioTrade currPortfolioTrade : pTrades){
      resList.add(currPortfolioTrade.getSymbol());
    }
    return resList;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


//<======================================================================================================================>
//                                                          MODULE - 2
//<======================================================================================================================>
  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "trades.json";
    String toStringOfObjectMapper = "ObjectMapper";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "bla-bla-bla-bla";

    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper, functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }
  

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

    String strEndDate = args[1];
    LocalDate endDate = LocalDate.parse(strEndDate);

    //get the POJO list of PortfolioTrade objects
    List<PortfolioTrade> tradeList = readTradesFromJson(args[0]);
    
    //Now create the url and find the closing price for each of the trade and create a TotalReturnsDto from that and store it in a list of that type(TotalReturnsDto)
    List<TotalReturnsDto> dtoList = new ArrayList<>();

    for(PortfolioTrade currTradeObj : tradeList){
      String tempUrl = prepareUrl(currTradeObj, endDate, "aac9905763932f2fdbebefbbe6c063942d293d01");

      RestTemplate restTemplate = new RestTemplate();
      TiingoCandle[] tiingCand = restTemplate.getForObject(tempUrl, TiingoCandle[].class);

      //create TotalReturnsDto object and add it to the corresponding type list.
      TotalReturnsDto tempDto = new TotalReturnsDto(currTradeObj.getSymbol(), tiingCand[tiingCand.length-1].getClose());
      dtoList.add(tempDto);
    }

    Collections.sort(dtoList);

    List<String> resList = new ArrayList<>();
    for(TotalReturnsDto currDto : dtoList){
      resList.add(currDto.getSymbol());
    }
    
    return resList;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    List<PortfolioTrade> tradeList = new ArrayList<>();
    ObjectMapper om = getObjectMapper();

    File inputFile = resolveFileFromResources(filename);  //**my biggest mistake was to skip this line of code and using filename instead of inputFile.
    PortfolioTrade[] pTrades = om.readValue(inputFile, PortfolioTrade[].class);

    for(PortfolioTrade currPortfolioTrade : pTrades)
      tradeList.add(currPortfolioTrade);
    
    return tradeList;
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    // https://api.tiingo.com/tiingo/daily/aapl/prices?startDate=2024-04-01&endDate=2024-04-06&token=aac9905763932f2fdbebefbbe6c063942d293d01
    String targetUrl = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
    return targetUrl;
  }



//<======================================================================================================================>
//                                                          MODULE - 3
//<======================================================================================================================>
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Candle candObj = candles.get(0);
    return candObj.getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    int lastCandleIdx = candles.size()-1;
    return candles.get(lastCandleIdx).getClose();
  }

  public static String getToken(){
    return "aac9905763932f2fdbebefbbe6c063942d293d01";
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String tempUrl = prepareUrl(trade, endDate, "aac9905763932f2fdbebefbbe6c063942d293d01");
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] allCandles = restTemplate.getForObject(tempUrl, TiingoCandle[].class);
    
    List<Candle> candles = new ArrayList<>();
    for(TiingoCandle currCandle : allCandles)
      candles.add(currCandle);
    
    return candles;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    Double totalReturn = (sellPrice - buyPrice) / buyPrice;

    //calculate total years = (total days between startDate and endDate divide by 365)
    long daysBetween = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);  //endDate is not counted here
    double totalYears = (double) (daysBetween / 365.24);
    // System.out.println(totalYears);
    Double annualizedReturn = Math.pow(1 + totalReturn, (1.0 / totalYears)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
  }


  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) throws IOException, URISyntaxException {
    //get endDate
    String strEndDate = args[1];
    LocalDate endDate = LocalDate.parse(strEndDate);

    //create trade object pojo
    List<PortfolioTrade> tradeList = readTradesFromJson(args[0]);
    //declare and define the result list
    List<AnnualizedReturn> resList = new ArrayList<>();

    for(PortfolioTrade trade : tradeList){
      List<Candle> candles = fetchCandles(trade, endDate, getToken());
      Double buyPrice = getOpeningPriceOnStartDate(candles);
      Double sellPrice = getClosingPriceOnEndDate(candles);

      AnnualizedReturn tempAnnualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
      resList.add(tempAnnualizedReturn);
    }

    Collections.sort(resList);

    return resList;
  }



  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args) throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       List<PortfolioTrade> portfolioTrades = readTradesFromJson(file);
       RestTemplate restTemplate = new RestTemplate();
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
       return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

