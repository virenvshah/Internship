	package Test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import java.util.concurrent.TimeUnit;

/* contains information about the trip such as the data saved */

class TripStats {
	int dataSavedPercentage;  /* Percentage Data Savings */

	TripStats(String dataSavedPercentage) {
		/* getting rid of the percentage sign (from 78% to 78) */
		this.dataSavedPercentage = Integer.parseInt(dataSavedPercentage.substring(0, dataSavedPercentage.length() - 1));
	}
}

/* Class contains the result of the TestSuites.  This object is returned by the TestSuites. */
class Result {
	String message;
	boolean pass; /* either true or false */
}

/* Abstract base class for all test suites */
abstract class TestSuite {
	String name;
	Result runResult = new Result();

	/* name of the Test Suite */
	String name() {
		return name;
	}

	abstract Result run(WebDriver driver, WebDriverWait wait, String addition);
}

/* consists of all the BasicTestSuite tests that are performed */
class RegistrationTestSuite extends TestSuite {
	String baseUrl;

	RegistrationTestSuite(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/* tests the registration page */
	Result run(WebDriver driver, WebDriverWait wait, String addition) {
		System.out.println("Registration test case started!");
		String url = baseUrl + addition; /* Url of the register page */
		/* Opens the registration page */
		driver.get(url);
		WebElement username = driver.findElement(
				By.cssSelector("input#email")); /* username text box */
		WebElement register = driver.findElement(
				By.cssSelector("a#do_register")); /* register button */
		username.clear();
		username.sendKeys("randomtext");
		register.click(); /* Tries registering with "randomtext" */

		try {
			if (wait.until(ExpectedConditions.alertIsPresent()) != null) {
				driver.switchTo().alert().accept();
			}
		}
		/* If there is no alert for invalid email */
		catch (TimeoutException exceptob) {
			runResult.message = "No alert for invalid email!";
			runResult.pass = false;
			return runResult;
		}

		username.clear();
		username.sendKeys("pankaj@actmbile.com"); /* Valid email */
		register.click();

		runResult.message = "Registration test case passed!";
		runResult.pass = true;
		
		return runResult;
	}
}

/* Checks if data savings increase when the VPN is on and cnn.com is opened */
class VpnTestSuite extends TestSuite {
	String baseUrl;

	VpnTestSuite(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	Result run(WebDriver driver, WebDriverWait wait, String addition) {
		System.out.println("VPN test case started!");
		String url = baseUrl + addition;
		driver.get(url); /* Opens the Dashnet dashboard */
		driver.manage().timeouts().implicitlyWait(5,
				TimeUnit.SECONDS); /* implicit wait */
		
		/* The page takes some time to load so I wait for 6 seconds */
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/* resetting the trip stats */
		driver.findElement(By.cssSelector("a#trip_button")).click();
		driver.findElement(By.cssSelector("a#reset_trip_stats"))
				.click(); /* Resets the trip stats */
		
		/* wait again for six seconds */
		try {
			Thread.sleep(6000);
		} catch (InterruptedException f) {
			f.printStackTrace();
		
		}
		/* Ensures that the trip stats have been reset */
		if (!"0%".equals(
				driver.findElement(By.cssSelector("h1.all_time_savings"))
						.getText())) {
			
			runResult.message = driver
					.findElement(By.cssSelector("h1.all_time_savings"))
					.getText() + "The trip stats were not reset";
			runResult.pass = false;
			return runResult;
		}

		/*
		 * Clicks on the slider. Since the slider is a compound class, I
		 * couldn't use css selector
		 */
		driver.findElement(By
				.xpath("//div[@id='index']/div/div[2]/div[5]/div/div/div[2]/div[5]/center/div/div/a"))
				.click();

		try {
			Thread.sleep(6000);
		} catch (InterruptedException f) {
			f.printStackTrace();
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
		}
		
		TripStats afterBrowsing = new TripStats(driver
				.findElement(By.cssSelector("h1.all_time_savings")).getText());
		if (afterBrowsing.dataSavedPercentage > 0) {
			runResult.message = "VPN test case passed!";
			runResult.pass = true;
		} else {
			runResult.message = "Data Savings did not increase!";
			runResult.pass = false;
		}
		
		return runResult;
	}
}

/*
 * This is the top level Test class which instantiates multiple test suite
 * objects to test the functionality of the Dashnet VPN client.
 */

public class Test {
	public static void main(String args[]) {
		WebDriver driver = new FirefoxDriver();
		WebDriverWait wait = new WebDriverWait(driver, 10);  /* To create implicit waits */
		String baseUrl = "https://corp.actmobile.com:4443/dashboard/";
		RegistrationTestSuite regTests = new RegistrationTestSuite(baseUrl);
		VpnTestSuite vpnTests = new VpnTestSuite(baseUrl);

		/* Registration tests */
		System.out.println(regTests.run(driver, wait, "#register").message);

		/* VPN Tests */
		System.out.println(vpnTests.run(driver, wait, "#index").message);
	}
}