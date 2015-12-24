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
	int dataSaved;
	
	TripStats(String dataSaved) {
		int i;
		char aDataSaved[] = dataSaved.toCharArray();
		
		char temp[] = new char[aDataSaved.length-1];
		for (i = 0; i < aDataSaved.length-1; ++i) {
			temp[i] = aDataSaved[i];
		}
		this.dataSaved = Integer.parseInt(new String(temp));
	}
}

/* consists of all the basic tests that are performed */
class Basic {     
	String baseUrl;
	
	Basic(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	/* tests the registration page */
	boolean registration(WebDriver driver, WebDriverWait wait, String addition) {
		String url = baseUrl + addition;  /* Url of the register page */
		/* Opens the registration page */
		driver.get(url);
		WebElement 	username = driver.findElement(By.cssSelector("input#email")); /* username text box */
		WebElement register = driver.findElement(By.cssSelector("a#do_register")); /* register button */
		username.clear();
		username.sendKeys("randomtext");
		register.click();  /* Tries registering with "randomtext" */

		try {	
			if (wait.until(ExpectedConditions.alertIsPresent()) != null) {
				driver.switchTo().alert().accept();
			}
		}		
		/* If there is no alert for invalid email */
		catch (TimeoutException exceptob){
			System.out.println("No alert for invalid email!");
			return false;
		}
		
		username.clear();
		username.sendKeys("pankaj@actmbile.com"); /* Valid email */
		register.click();
		
		return true;
	}

	/* tests the dashboard page and performs a few basic vpn tests */
	boolean vpn(WebDriver driver, WebDriverWait wait, String addition) {
		String url = baseUrl + addition;
		driver.get(url); /* Opens the dashnet dashboard */
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);  /* implicit wait */
		/* The page takes some time to load so I wait for 6 seconds */
		try {
			Thread.sleep(6000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		driver.findElement(By.cssSelector("a#trip_button")).click();
		driver.findElement(By.cssSelector("a#reset_trip_stats")).click();  /* Resets the trip stats */
		try {
			Thread.sleep(6000);
		}
		catch (InterruptedException f) {
			f.printStackTrace();
		}
		/* Ensures that the trip stats have been reset */
		if (!"0%".equals(driver.findElement(By.cssSelector("h1.all_time_savings")).getText())) {
			System.out.println(driver.findElement(By.cssSelector("h1.all_time_savings")).getText() + "The trip stats were not reset");
			return false;
		}
		
		/* Clicks on the slider.  Since the slider is a compound class, I couldn't use css selector */
		driver.findElement(By.xpath("//div[@id='index']/div/div[2]/div[5]/div/div/div[2]/div[5]/center/div/div/a")).click();
		
		try {
			Thread.sleep(6000);
		}
		catch (InterruptedException f) {
			f.printStackTrace();
		}
		
		driver.get("http://edition.cnn.com/");
		driver.get(url);
		
		/* checks if data savings have increased */
		driver.findElement(By.cssSelector("a#trip_button")).click();
		try {
			Thread.sleep(6000);
		}
		catch (InterruptedException f) {
			f.printStackTrace();
		}	
		TripStats afterBrowsing = new TripStats(driver.findElement(By.cssSelector("h1.all_time_savings")).getText());
		if (afterBrowsing.dataSaved > 0) {
			return true;
		}
		else {
			System.out.println("Data Savings did not increase!");
			return false;
		}
	}
}

/* calls the various tests */
public class Test {
	public static void main(String args[]) {
		WebDriver driver = new FirefoxDriver();
		WebDriverWait wait = new WebDriverWait(driver, 10);
		String baseUrl = "https://corp.actmobile.com:4443/dashboard/";
		Basic ob = new Basic(baseUrl);
		
		/* Registration tests */
		if (ob.registration(driver, wait, "#register") == true) {
			System.out.println("Registration test case passed!");
		}
		else {
			System.out.println("Registration test case failed!");
		}
		
		/* VPN Tests */
		if (ob.vpn(driver, wait, "#index") == true) {
			System.out.println("VPN test case passed!");
		}
		else {
			System.out.println("VPN test case failed!");
		}
	}
}