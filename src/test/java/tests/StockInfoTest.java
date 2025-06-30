package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import utils.ConfigReader;
import utils.LoggerUtil;
import config.TestDataProvider;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.logging.Logger;

public class StockInfoTest {

    private WebDriver driver;
    private Logger logger = LoggerUtil.getLogger();
    private double expectedProfit;
    String stockSymbol;

    @BeforeClass
    public void setUp() {
        String browserName=ConfigReader.getProperty("browser.name");
        if(browserName.equals("chrome")){
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
        } else if (browserName.equals("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
        }
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        //Based on the properties file keys this test will work
        stockSymbol = ConfigReader.getProperty("stock.TATACHEM");
        expectedProfit = TestDataProvider.getProfit(stockSymbol);
        logger.info("Expected Profit Price Loaded for " + stockSymbol + ": " + expectedProfit);
    }

    @Test
    public void verifyStockDisplay() throws InterruptedException {
        ExtentReports extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/index.html");
        extent.attachReporter(spark);
        driver.get("https://www.nseindia.com");
        Thread.sleep(3000);

        WebElement searchBox = driver.findElement(By.xpath("//input[contains(@placeholder,'Search by')]"));
        searchBox.sendKeys(stockSymbol);
        Thread.sleep(3000);

        WebElement suggestion = driver.findElement(By.xpath("//span[text()='"+stockSymbol+"']"));
        suggestion.click();
        Thread.sleep(5000);
        // Take screenshot
        try{
        Screenshot screenshot = new AShot()
                .shootingStrategy(ShootingStrategies.viewportPasting(100))
                        .takeScreenshot(driver);

        // Save the screenshot to a local file
        ImageIO.write(screenshot.getImage(),"PNG", new File("screenshots/"+stockSymbol+"_port_Screenshot.png"));

        System.out.println("Screenshot taken successfully.");
    } catch (IOException e) {
        System.out.println("Error while saving screenshot: " + e.getMessage());
    }

        WebElement high_val52 = driver.findElement(By.id("week52highVal"));
        String priceText = high_val52.getText().replace(",", "").trim();
        Assert.assertNotNull(priceText,"52 Week High Price is Null");
        Assert.assertFalse(priceText.isEmpty(),"52 Week High Price is empty");
        double week52HighPrice = Double.parseDouble(priceText);

        logger.info("52 Week High Price: " + week52HighPrice);
        WebElement low_val52 = driver.findElement(By.id("week52lowVal"));
        String priceText_low = low_val52.getText().replace(",", "").trim();
        Assert.assertNotNull(priceText,"52 Week Low Price is Null");
        Assert.assertFalse(priceText.isEmpty(),"52 Week Low Price is empty");
        double week52LowPrice = Double.parseDouble(priceText_low);

        logger.info("52 Week Low Price: " + week52LowPrice);

        double profitLoss = week52HighPrice - week52LowPrice;
        logger.info("Profit/Loss: " + profitLoss);

        assert !priceText.isEmpty() : "Stock price should be displayed.";
        Assert.assertEquals(profitLoss,expectedProfit,"Profit do not match!");
        ExtentTest test = extent.createTest("Stock Info Test");
        test.pass("Profit Price retrieved successfully: " + profitLoss);
        extent.flush();
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }
    @AfterMethod
    public void captureFailure(ITestResult result) {
        if (ITestResult.FAILURE == result.getStatus()) {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                Files.copy(screenshot.toPath(), Paths.get("screenshots", result.getName() + ".png"), StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
