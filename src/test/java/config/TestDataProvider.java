package config;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;

public class TestDataProvider {

    public static double getProfit(String stock) {
        try {
            FileReader reader = new FileReader("test-data/stock-data.json");
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(reader);
            JSONObject stockData = (JSONObject) jsonObject.get(stock);
            return Double.parseDouble(stockData.get("purchasePrice").toString());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
