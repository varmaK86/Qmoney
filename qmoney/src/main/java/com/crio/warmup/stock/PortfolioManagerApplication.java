package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.management.RuntimeErrorException;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Task:
  //       - Read the json file provided in the argument[0], The file is available in the classpath.
  //       - Go through all of the trades in the given file,
  //       - Prepare the list of all symbols a portfolio has.
  //       - if "trades.json" has trades like
  //         [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  //         Then you should return ["MSFT", "AAPL", "GOOGL"]
  //  Hints:
  //    1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  //       Check if they are of any help to you.
  //    2. Return the list of all symbols in the same order as provided in json.

  private static List<String> getSymbols(String file) throws JsonMappingException, JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    PortfolioTrade[] tradeslist = mapper.readValue(file, PortfolioTrade[].class);
    List<String> symbolList = new ArrayList<>();
    for(PortfolioTrade trade : tradeslist){
      symbolList.add(trade.getSymbol());
    }
    return symbolList;
  }

  //  Note:
  //  1. There can be few unused imports, you will need to fix them to make the build pass.
  //  2. You can use "./gradlew build" to check if your code builds successfully.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper mapper = getObjectMapper();   
    String content = new String(Files.readAllBytes(file.toPath()));
    PortfolioTrade[] tradeslist = mapper.readValue(content, PortfolioTrade[].class);
    List<String> symbolList = new ArrayList<>();
    for(PortfolioTrade trade : tradeslist){
      symbolList.add(trade.getSymbol());
    }
    return symbolList;
     
  }
  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  private static File resolveFileFromResources(String file) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(file).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
//    String token ="11771aa0d612191c70182b364f9b9f67a1272002";
    String token = getToken();
    LocalDate  endDate = LocalDate.parse(args[1]);
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    List<TotalReturnsDto> totalReturnsDtos = new ArrayList<>(); 

    for(PortfolioTrade trade : trades){
      String url = prepareUrl(trade, endDate, token);
      TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class );
      
      if(candles==null || candles.length==0){
        
      }
      if(candles!=null){
        double closingPrice = candles[candles.length-1].getClose();
        totalReturnsDtos.add(new TotalReturnsDto(trade.getSymbol(),closingPrice));
      }
    }
     return totalReturnsDtos.stream()
                            .sorted(Comparator.comparing(TotalReturnsDto::getClosingPrice))
                            .map(TotalReturnsDto::getSymbol)
                            .collect(Collectors.toList());
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper mapper = getObjectMapper();
    return Arrays.asList(mapper.readValue(file, PortfolioTrade[].class));
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    
     return String.format("https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s",trade.getSymbol(),trade.getPurchaseDate(),endDate,token);
  }

  public static String getToken() {
    return "11771aa0d612191c70182b364f9b9f67a1272002";
  }
  // annualizedReturns.sort(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed());
  // return annualizedReturns;

public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
   if(candles==null || candles.isEmpty()){
     throw new IndexOutOfBoundsException("No candles found for start Date");
   }
    return candles.get(0).getOpen();
}

public static Double getClosingPriceOnEndDate(List<Candle> candles) {
  if(candles==null || candles.isEmpty()){
    throw new IndexOutOfBoundsException("No candles found for end Date");
  }
    return candles.get(candles.size()-1).getClose();
}

public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {

  try{
    RestTemplate restTemplate = new RestTemplate();
    String url = prepareUrl(trade, endDate, token);
    TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);
    if(candles == null || candles.length==0) {
      return Collections.emptyList();
    }
    return Arrays.asList(candles);

  }catch(Exception e){
     throw new RuntimeException("Failed to fetch candles for" + trade.getSymbol(),  e);
  }
   
  
}

public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) throws IOException, URISyntaxException {
    LocalDate  endDate = LocalDate.parse(args[1]);
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    String token = getToken();

    List<AnnualizedReturn> returns = new ArrayList<>();

    for(PortfolioTrade trade : trades){
      List<Candle> candles = fetchCandles(trade, endDate, token);
      if(!candles.isEmpty()){
        double buyPrice = getOpeningPriceOnStartDate(candles);
        double sellPrice = getClosingPriceOnEndDate(candles);

        AnnualizedReturn ar = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
        returns.add(ar);
      }
    }
    returns.sort(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed());
    return returns;
}

public static List<String> debugOutputs() {
  
    return Arrays.asList(
      "trades.json file resolved",
      "trades.json read successfully",
      "ObjectMapper initialized with javaTimeModule",
      "Inside mainReadFile method"
    );
    

}

public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
        double buyPrice, double sellPrice) {
    double totalReturn = (sellPrice-buyPrice)/buyPrice;
    double years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365.24;

    if(years<=0){
      return new AnnualizedReturn(trade.getSymbol(), Double.NaN, totalReturn);
    }
    double annualizedReturn = Math.pow( 1+ totalReturn, 1.0/years)-1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
   
}
}

// public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, 
//         double buyPrice, double sellPrice) {
//     return calculateAnnualizedReturn( endDate,trade, buyPrice, sellPrice);
// }

// }

