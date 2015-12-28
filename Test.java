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

class Vpn {

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
}

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
    boolean pass; /*
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

/*
 * Checks if data savings increase when the VPN is on and dynamic website is
 * opened. Checks if data savings when the VPN is on and a static website is
 * opened a second time than when the static website is opened the first time.
 */
class VpnTestSuite extends TestSuite {

    /* websites that are tested */
    class Website {
        String url;
        boolean isStatic; /* true if static, false if dynamic */
    }

    VpnTestSuite(String baseUrl, PrintStream printStream,
            String testSuiteName) {
        super(baseUrl, printStream, testSuiteName);
    }

    Result run(WebDriver driver, WebDriverWait wait, String addition) {
        int i;
        Website websites[] = new Website[2];

        websites[0] = new Website();
        websites[0].url = "http://edition.cnn.com/";
        websites[0].isStatic = false;
        websites[1] = new Website();
        websites[1].url = "http://apache.org/";
        websites[1].isStatic = true;

        printLog.println("VPN Test Suite started!");

        for (i = 0; i < websites.length; ++i) { /*
                                                 * testing all urls in the
                                                 * website class
                                                 */
            String url = baseUrl + addition;
            driver.get(url); /* Opens the Dashnet dashboard */
            driver.manage().timeouts().implicitlyWait(5,
                    TimeUnit.SECONDS); /* implicit wait */
            Trip firstOpen = new Trip();
            Vpn vpn = new Vpn();

            /* The page takes some time to load so I wait for 6 seconds */
            try {
                Thread.sleep(6000);
            } catch (Exception anyException) {
                runResult = new Result(false, anyException);
                return runResult;
            }

            firstOpen.resetTrip(driver); /* Resets the trip stats */

            /* wait again for six seconds */
            try {
                Thread.sleep(6000);
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
            vpn.turnVpnOn(driver);
            try {
                Thread.sleep(6000);

            } catch (Exception anyException) {
                runResult = new Result(false, anyException);
                return runResult;
            }

            /* opening dynamic/static website */
            driver.get(websites[i].url);
            driver.get(url);

            /* checks if data savings have increased */
            driver.findElement(By.cssSelector("a#trip_button")).click();
            try {
                Thread.sleep(6000);
            } catch (Exception anyException) {
                runResult = new Result(false, anyException);
                return runResult;
            }

            firstOpen.setDataSavedPercentage(
                    driver.findElement(By.cssSelector("h1.all_time_savings"))
                            .getText());

            if (websites[i].isStatic == false) { /*
                                                 * check if data savings
                                                 * increased
                                                 */

                if (firstOpen.dataSavedPercentage() > 0) {
                    runResult = new Result(true);
                    runResult.errorMessage = "VPN test case passed!";
                } else {
                    runResult = new Result(false);
                    runResult.errorMessage = "Data Savings did not increase for dynamic website!";
                    return runResult;
                }
            } else { /*
                      * check if data savings are greater the second time a
                      * static website is opened as compared to the first time
                      * it was opened
                      */
                /* opening a second time */
                Trip secondOpen = new Trip();

                driver.get("http://apache.org/");
                driver.get(url);

                driver.findElement(By.cssSelector("a#trip_button")).click();

                try {
                    Thread.sleep(6000);
                } catch (Exception anyException) {
                    runResult = new Result(false, anyException);
                    return runResult;
                }

                /* saving the data saved percentage trip stat in secondOpen */

                secondOpen.setDataSavedPercentage(driver
                        .findElement(By.cssSelector("h1.all_time_savings"))
                        .getText());
               
                if (firstOpen.dataSavedPercentage() < secondOpen /*
                                                                  * the data
                                                                  * saved is
                                                                  */
                        .dataSavedPercentage()) { /* more the second time */
                    runResult = new Result(true);
                } else {
                    runResult = new Result(false);
                    runResult.errorMessage = "Data Savings did not increase after opening the second time!";
                }
            }
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

        /* Registration tests */
        Result functionResult = regTests.run(driver, wait, "#register");       
        printStream.println(regTests.name()
                + functionResult.resultString());
        
        if (functionResult.pass == false) {
            return;
        }

       /* VPN Tests */
        printStream.println(vpnTests.name()
                + vpnTests.run(driver, wait, "#index").resultString());
    }
}