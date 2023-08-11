package com.halimoglu.selenium.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Service;
import static spark.Service.ignite;
import static spark.Spark.get;

/**
 *
 * @author fhali
 */
public class ApiToXweb {

    spark.Service s;
    String selector_username_text;
    String selector_next_text;
    String selector_password_text;
    String selector_tweet_text;
    String selector_send_text;

    public ApiToXweb() {
        s = ignite().port(4570);

        selector_username_text = System.getenv("TWITTER_SELECTOR_USERNAME");
        selector_next_text = System.getenv("TWITTER_SELECTOR_NEXT");
        selector_password_text = System.getenv("TWITTER_SELECTOR_PASSWORD");
        selector_tweet_text = System.getenv("TWITTER_SELECTOR_CONTENT");
        selector_send_text = System.getenv("TWITTER_SELECTOR_SEND");

        JsonObject jo = new JsonObject();
        jo.addProperty("TWITTER_SELECTOR_USERNAME", selector_username_text);
        jo.addProperty("TWITTER_SELECTOR_NEXT", selector_next_text);
        jo.addProperty("TWITTER_SELECTOR_PASSWORD", selector_password_text);
        jo.addProperty("TWITTER_SELECTOR_CONTENT", selector_tweet_text);
        jo.addProperty("TWITTER_SELECTOR_SEND", selector_send_text);

        s.get("/hello", (req, res) -> "{\"Hello\":\"World\"");
        s.get("/env", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                res.type("application/json");
                return jo.toString();
            }
        });

        s.post("/tweet", new Route() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                res.type("application/json");
                String body = req.body();
                System.out.println("new request: "+body);
                JsonObject jo = JsonParser.parseString(body).getAsJsonObject();
                
                return tweet(
                        jo.get("user").getAsString(),
                        jo.get("pass").getAsString(),
                        jo.get("text").getAsString()
                );
            }
        });
    }

    private String tweet(String username, String password, String content) {
        WebDriver driver = null;
        try {
            By selector_username = By.xpath(selector_username_text);
            By selector_next = By.xpath(selector_next_text);
            By selector_password = By.xpath(selector_password_text);
            By selector_tweet = By.xpath(selector_tweet_text);
            By selector_send = By.xpath(selector_send_text);

            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.setHeadless(true);
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            driver = new ChromeDriver(options);
            WebDriverWait bekle = new WebDriverWait(driver, Duration.ofSeconds(5));

            driver.get("https://twitter.com/i/flow/login");

            bekle.until(ExpectedConditions.presenceOfElementLocated(selector_username));
            driver.findElement(selector_username).sendKeys(username);
            driver.findElement(selector_next).click();
            bekle.until(ExpectedConditions.presenceOfElementLocated(selector_password));
            WebElement e1 = driver.findElement(selector_password);
            e1.sendKeys(password);
            e1.sendKeys(Keys.ENTER);
            bekle.until(ExpectedConditions.urlToBe("https://twitter.com/home"));

            bekle.until(ExpectedConditions.presenceOfElementLocated(selector_tweet));
            driver.findElement(selector_tweet).sendKeys(content);
            driver.findElement(selector_send).click();
            return "{\"status\":\"OK\"}";
        } catch (Exception ex) {
            
            JsonObject jo = new JsonObject();
            jo.addProperty("status", "error");
            jo.addProperty("message", ex.getMessage());
            return jo.toString();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

    }

    public static void main(String[] args) {

        new ApiToXweb();

    }

}
