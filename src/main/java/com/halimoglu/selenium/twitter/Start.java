package com.halimoglu.selenium.twitter;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author fhali
 */
public class Start {

    static File pf;
    static boolean browser_init = false;
    static WebDriver driver;
    static WebDriverWait bekle = null;
    static String username = null;
    static String password = null;

    public static void main(String[] args) {
        File root = null;
        try {
            root = new File(Start.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }

        pf = new File(root, "app.properties");
        if (pf.exists() == false) {
            try {
                pf.createNewFile();
                Properties p = new Properties();
                p.setProperty("last_news", "" + System.currentTimeMillis());

                Scanner sc = new Scanner(System.in);
                System.out.print("Enter twitter username: ");
                username = sc.nextLine();
                p.setProperty("username", username);

                System.out.print("password: ");
                password = sc.nextLine();
                p.setProperty("password", password);
                p.store(Files.newOutputStream(pf.toPath()), "init");

            } catch (IOException ex) {
                Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        long last_news_time = read_last_from_properties();

        System.out.println(new Date() + " last_news_time " + last_news_time + " " + new Date(last_news_time));

        String url = "https://feeds.bbci.co.uk/turkce/rss.xml";
        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));

            List<SyndEntry> entries = feed.getEntries();

            entries.sort(new Comparator<SyndEntry>() {
                @Override
                public int compare(SyndEntry o1, SyndEntry o2) {
                    return o1.getPublishedDate().compareTo(o2.getPublishedDate());
                }
            });

            long last = 0;

            for (SyndEntry entry : entries) {
                String title = entry.getTitle();
                String link = entry.getLink();
                link = link.substring(0, link.indexOf("?"));
                long time = entry.getPublishedDate().getTime();

                System.out.println(entry.getPublishedDate() + " " + (time > last_news_time));

                if (time > last_news_time) {
                    System.out.println("tweeting " + title);
                    tweet(title + " " + link);

                    last = time;
                }
            }
            if (last > 0) {
                write_last(last);
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FeedException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static long read_last_from_properties() {
        Properties p = new Properties();
        try {
            p.load(
                    Files.newInputStream(pf.toPath())
            );

            return Long.parseLong(p.getProperty("last_news"));
        } catch (IOException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;

    }

    private static void write_last(long time) {
        Properties p = new Properties();

        try {
            p.load(Files.newInputStream(pf.toPath()));
        } catch (IOException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }

        p.setProperty("last_news", time + "");
        System.out.println("updating prop " + time);
        try {
            p.store(Files.newOutputStream(pf.toPath()), "update");
        } catch (IOException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void tweet(String content) {

        By selector_username = By.cssSelector("input[autocomplete=username]");
        By selector_next = By.xpath("//*[ text() = 'Next' ]");
        By selector_password = By.cssSelector("input[name=password]");
        By selector_tweet = By.cssSelector("div[data-contents=true]");
        By selector_send = By.xpath("//*[ text() = 'Tweetle' ]");

        if (browser_init == false) {

            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.setHeadless(true);
            options.addArguments("--remote-allow-origins=*");
            driver = new ChromeDriver(options);
            bekle = new WebDriverWait(driver, Duration.ofSeconds(5));

            driver.get("https://twitter.com/i/flow/login");

            bekle.until(ExpectedConditions.presenceOfElementLocated(selector_username));
            driver.findElement(selector_username).sendKeys(username);
            driver.findElement(selector_next).click();
            bekle.until(ExpectedConditions.presenceOfElementLocated(selector_password));
            WebElement e1 = driver.findElement(selector_password);
            e1.sendKeys(password);
            e1.sendKeys(Keys.ENTER);
            bekle.until(ExpectedConditions.urlToBe("https://twitter.com/home"));

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    driver.quit();
                }
            });
            browser_init = true;
        } else {
            driver.get("https://twitter.com/home");
        }

        bekle.until(ExpectedConditions.presenceOfElementLocated(selector_tweet));
        driver.findElement(selector_tweet).sendKeys(content);
        driver.findElement(selector_send).click();
    }

}
