package Test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.io.PrintStream;

/* contains information about the trip such as the data saved and contains 
 * functions like reset trip stats and turn the vpn on */

class Trip {
    private int dataSavedPercentage; /* Percentage Data Savings */

    void setDataSavedPercentage(String dataSavedPercentage) {
        /* getting rid of the percentage sign (from 78% to 78) */
        this.dataSavedPercentage = Integer.parseInt(dataSavedPercentage
                .substring(0, dataSavedPercentage.length() - 1));
    }

    int dataSavedPercentage() {
        return dataSavedPercentage; /* returns the data saved percentage */
    }

    int turnVpnOn(WebDriver driver) {
        if (driver
                .findElement(By
                        .xpath("//div[@id='index']/div/div[2]/div[5]/div/div/div[2]/div[5]/center/div/span[2]"))
                .getText().contains("VPN Off")) { /* if the VPN is off */
            driver.findElement(By
                    .xpath("//div[@id='index']/div/div[2]/div[5]/div/div/div[2]/div[5]/center/div/div/a"))
                    .click(); /* Turn the VPN on */

            return 1;
        }

        return 0;
    }

    /* resets the Trip stats */
    void resetTrip(WebDriver driver) {
        driver.findElement(By.cssSelector("a#trip_button"))
                .click(); /* click the trip button */
        driver.findElement(By.cssSelector("a#reset_trip_stats"))
                .click(); /* click the reset button */
    }
}

/*
 * Class contains the result of the TestSuites. This object is returned by the
 * TestSuites.
 */
class Result {
    private boolean pass; /*
                           * true if function passed; false if function failed
                           */
    private Exception e;
    private boolean exception; /* true if function failed due to exception */
    String errorMessage;

    Result(boolean pass) {
        this.pass = pass;
        this.exception = false;
    }

    Result(boolean pass, Exception e) {
        this.pass = pass;
        this.e = e;
        this.exception = true;
    }

    String resultString() {
        if (pass) {
            return "passed";
        } else if (exception) {
            return "failed with exception: " + e.getMessage();
        } else {
            return "failed: " + errorMessage; /*
                                               * if the program failed but not
                                               * because of an exception
                                               */
        }
    }
}

/* Abstract base class for all test suites */
abstract class TestSuite {
    String name; /* name of the test suite */
    String baseUrl;
    Result runResult;
    PrintStream printLog;

    TestSuite(String baseUrl, PrintStream printStream, String testSuiteName) {
        this.baseUrl = baseUrl;
        printLog = printStream;
        name = testSuiteName;
    }

    /* name of the Test Suite */
    String name() {
        return name;
    }

    abstract Result run(WebDriver driver, WebDriverWait wait, String addition);
}

/* consists of all the BasicTestSuite tests that are performed */
class RegistrationTestSuite extends TestSuite {

    RegistrationTestSuite(String baseUrl, PrintStream printStream,
            String testSuiteName) {
        super(baseUrl, printStream, testSuiteName);
    }

    /* tests the registration page */
    Result run(WebDriver driver, WebDriverWait wait, String addition) {
        printLog.println("Registration Test Suite started!");
        String url = baseUrl + addition; /* Url of the register page */
        /* Opens the registration page */
        driver.get(url);
        WebElement username = driver.findElement(
                By.cssSelector("input#email")); /* username text box */
        WebElement register = driver.findElement(
                By.cssSelector("a#do_register")); /* register button */
        username.clear();
        username.sendKeys("randomtext");
        register.click(); /*
                           * Tries registering with "randomtext"; should fail
                           * with an error message
                           */

        try {
            if (wait.until(ExpectedConditions.alertIsPresent()) != null) {
                driver.switchTo().alert().accept();
            }
        }
        /* If there is no alert for invalid email */
        catch (TimeoutException exceptob) {
            runResult = new Result(false, exceptob);
            return runResult;
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        username.clear();
        username.sendKeys("pankaj@actmbile.com"); /* Valid email */
        register.click();

        runResult = new Result(true);

        return runResult;
    }
}

/* Checks if data savings increase when the VPN is on and cnn.com is opened */
class VpnTestSuite extends TestSuite {

    VpnTestSuite(String baseUrl, PrintStream printStream,
            String testSuiteName) {
        super(baseUrl, printStream, testSuiteName);
    }

    Result run(WebDriver driver, WebDriverWait wait, String addition) {
        printLog.println("VPN Test Suite started!");
        String url = baseUrl + addition;
        driver.get(url); /* Opens the Dashnet dashboard */
        driver.manage().timeouts().implicitlyWait(5,
                TimeUnit.SECONDS); /* implicit wait */
        Trip trip = new Trip();

        /* The page takes some time to load so I wait for 6 seconds */
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        trip.resetTrip(driver); /* Resets the trip stats */

        /* wait again for six seconds */
        try {
            Thread.sleep(6000);
        } catch (InterruptedException f) {
            f.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        /* Ensures that the trip stats have been reset */
        if (!"0%".equals(
                driver.findElement(By.cssSelector("h1.all_time_savings"))
                        .getText())) {

            runResult = new Result(false);
            runResult.errorMessage = driver
                    .findElement(By.cssSelector("h1.all_time_savings"))
                    .getText() + "The trip stats were not reset";

            return runResult;
        }

        /* Turns on the VPN */
        trip.turnVpnOn(driver);

        try {
            Thread.sleep(6000);
        } catch (InterruptedException f) {
            f.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        /* opening cnn.com */
        driver.get("http://edition.cnn.com/");
        driver.get(url);

        /* checks if data savings have increased */
        driver.findElement(By.cssSelector("a#trip_button")).click();
        try {
            Thread.sleep(6000);
        } catch (InterruptedException f) {
            f.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        trip.setDataSavedPercentage(driver
                .findElement(By.cssSelector("h1.all_time_savings")).getText());
        if (trip.dataSavedPercentage() > 0) {
            runResult = new Result(true);
            runResult.errorMessage = "VPN test case passed!";
        } else {
            runResult = new Result(false);
            runResult.errorMessage = "Data Savings did not increase!";
        }

        return runResult;
    }
}

class StaticPageTestSuite extends TestSuite {

    StaticPageTestSuite(String baseUrl, PrintStream printStream,
            String testSuiteName) {
        super(baseUrl, printStream, testSuiteName);
    }

    Result run(WebDriver driver, WebDriverWait wait, String addition) {
        printLog.println("Static Page Test Suite started!");
        String url = baseUrl + addition;
        driver.get(url); /* Opens the Dashnet dashboard */
        driver.manage().timeouts().implicitlyWait(5,
                TimeUnit.SECONDS); /* implicit wait */
        Trip firstOpen = new Trip();

        /* The page takes some time to load so I wait for 6 seconds */
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        firstOpen.resetTrip(driver); /* Resets the trip stats */

        /* wait again for six seconds */
        try {
            Thread.sleep(6000);
        } catch (InterruptedException f) {
            f.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        /* Ensures that the trip stats have been reset */
        if (!"0%".equals(
                driver.findElement(By.cssSelector("h1.all_time_savings"))
                        .getText())) {

            runResult = new Result(false);
            runResult.errorMessage = driver
                    .findElement(By.cssSelector("h1.all_time_savings"))
                    .getText() + "The trip stats were not reset";

            return runResult;
        }

        firstOpen.turnVpnOn(driver); /* turn the VPN on */

        /* wait for 6 seconds */

        try {
            Thread.sleep(6000);
        } catch (InterruptedException f) {
            f.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        /* opening apache.org */
        driver.get("http://apache.org/");
        driver.get(url);

        driver.findElement(By.cssSelector("a#trip_button")).click();
        try {
            Thread.sleep(6000);
        } catch (InterruptedException f) {
            f.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        /* saving the data saved percentage trip stat in firstOpen */

        firstOpen.setDataSavedPercentage(driver
                .findElement(By.cssSelector("h1.all_time_savings")).getText());

        /* opening apache.com a second time */
        Trip secondOpen = new Trip();

        driver.get("http://apache.org/");
        driver.get(url);

        driver.findElement(By.cssSelector("a#trip_button")).click();

        try {
            Thread.sleep(6000);
        } catch (InterruptedException f) {
            f.printStackTrace();
        } catch (Exception anyException) {
            runResult = new Result(false, anyException);
            return runResult;
        }

        /* saving the data saved percentage trip stat in secondOpen */

        secondOpen.setDataSavedPercentage(driver
                .findElement(By.cssSelector("h1.all_time_savings")).getText());

        if (firstOpen.dataSavedPercentage() < secondOpen /* the data saved is */
                .dataSavedPercentage()) { /* more the second time */
            runResult = new Result(true);
        } else {
            runResult = new Result(false);
            runResult.errorMessage = "Data Savings did not increase after opening the second time!";
        }

        return runResult;
    }
}
/*
 * This is the top level Test class which instantiates multiple test suite
 * objects to test the functionality of the Dashnet VPN client. The tests use
 * Selenium WebDriver to drive the Dashnet GUI in a Firefox browser.
 */

public class Test {
    public static void main(String args[]) {
        WebDriver driver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(driver,
                10); /* To create implicit waits */
        String baseUrl = "https://corp.actmobile.com:4443/dashboard/";
        PrintStream printStream = System.out; /*
                                               * can be changed to whatever you
                                               * want
                                               */
        RegistrationTestSuite regTests = new RegistrationTestSuite(baseUrl,
                printStream, "Registration Test Suite ");
        VpnTestSuite vpnTests = new VpnTestSuite(baseUrl, printStream,
                "VPN Test Suite ");
        StaticPageTestSuite staticTests = new StaticPageTestSuite(baseUrl,
                printStream, "Static Page Test Suite ");

        /* Registration tests */
        printStream.println(regTests.name()
                + regTests.run(driver, wait, "#register").resultString());

        /* VPN Tests */
        printStream.println(vpnTests.name()
                + vpnTests.run(driver, wait, "#index").resultString());

        printStream.println(staticTests.name()
                + staticTests.run(driver, wait, "#index").resultString());
    }
}