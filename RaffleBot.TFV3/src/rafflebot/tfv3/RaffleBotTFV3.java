package rafflebot.tfv3;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Scanner;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class RaffleBotTFV3 {

	public static void main(String[] args) throws AWTException, InterruptedException, IllegalMonitorStateException {
		Robot r = new Robot(); // creates new robot object
		boolean buttonFound = false; // buttonFound boolean used for loops
		Scanner scanner = new Scanner(System.in); // creates scanner object
		System.out.print("Enter your steam username: "); // prompts and gets users' steam username
		String user = scanner.nextLine();
		System.out.print("Enter your steam password (Case-Sensitive): "); // prompts and gets users' steam password
		String pass = scanner.nextLine();
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe"); // changes system property
		ChromeOptions chromeOptions = new ChromeOptions(); // creates cromeoptions
		chromeOptions.addArguments("--start-maximized"); // makes the webdriver maximized
		WebDriver driver = new ChromeDriver(chromeOptions); // creates webdriver
		driver.get("https://scrap.tf/raffles"); // opens scrap.tf/raffles
		JavascriptExecutor jse = (JavascriptExecutor) driver; // creates javascripexecutor
		driver.findElement(By.cssSelector("#navbar-main > ul.nav.navbar-nav.navbar-right")).click(); //finds and clicks the login button
		WebElement userField = driver.findElement(By.id("steamAccountName")); // enter users' username and password																// steam guard code to login
		userField.sendKeys(user);
		WebElement passField = driver.findElement(By.id("steamPassword"));
		passField.sendKeys(pass);
		r.keyPress(KeyEvent.VK_ENTER); // presses enter
		// at this point all the user needs to do is enter their steam 2 factor
		// authentication code (if they have one) and press enter
		Thread.sleep(1000); // lets the page load
		String currentURL = driver.getCurrentUrl(); // gets the url of the steam login page
		while (buttonFound == false) {
			if (!driver.getCurrentUrl().equals(currentURL)) { // while loop that waits for the scrap.tf page to load
																// again
				buttonFound = true;
			}
		}
		String numOfRaffles = driver.findElement(By.className("raffle-list-stat")).getText(); // string to get the
																								// number of raffles
		int numOfRafflesI = 0; // int that will hold the integer value for the number of raffles
		String numToGet = ""; // string that will be used to get the number of raffles
		boolean gettingNumber = false; // boolean to check if the number of raffles is being retrieved
		for (int i = 0; i < numOfRaffles.length(); i++) { // runs through the string of the number of raffles
			if (numOfRaffles.charAt(i) == '/') { // if the character is a slash, that means the number of raffles
													// follows it
				gettingNumber = true; // sets gettingNumber to true
			} else if (gettingNumber && !Character.isDigit(numOfRaffles.charAt(i))) { // if the char is not a digit
																						// after the slash, the number
																						// is done being retrievd
				break; // breaks out of the for loop
			} else if (gettingNumber && Character.isDigit(numOfRaffles.charAt(i))) { // if the char is digit, it's part
																						// of the number of raffles
				numToGet += numOfRaffles.charAt(i); // adds to the number of raffles string
			}
		}
		numOfRafflesI = Integer.parseInt(numToGet); // parses the integer to be used for a comparison
		numToGet = ""; // clears the numToGet string
		gettingNumber = false; // sets gettingNumber back to false
		List<WebElement> listOfRaffles = driver.findElements(By.className("panel-raffle")); // list to get all the
																							// raffle web elements
		while (listOfRaffles.size() != numOfRafflesI) { // while loop that continuosly checking if the page is scrolled
														// down enough that every raffle is loaded
			for (int i = 0; i < numOfRaffles.length(); i++) { // for loop must constantly check for the number of active
																// raffles
				if (numOfRaffles.charAt(i) == '/') {
					gettingNumber = true;
				} else if (gettingNumber && !Character.isDigit(numOfRaffles.charAt(i))) {
					break;
				} else if (gettingNumber && Character.isDigit(numOfRaffles.charAt(i))) {
					numToGet += numOfRaffles.charAt(i);
				}
			}
			numOfRafflesI = Integer.parseInt(numToGet);
			numToGet = "";
			gettingNumber = false;
			listOfRaffles = driver.findElements(By.className("panel-raffle")); // constantly updating the list
			jse.executeScript("window.scrollTo(0, document.body.scrollHeight);"); // javascrip executor thats scrolling
																					// down the page
		}
		JOptionPane.showMessageDialog(null, "All raffles retrieved"); // informs the user that all raffles have been
																		// retreived
		String HTML = driver.getPageSource(); // string that gets the page source after every raffle has been loaded

		///////// PAGE SOURCE FOR ALL RAFFLES HAS BEEN RETRIEVED //////////

		Document doc = Jsoup.parse(HTML); // connects to the website
		Elements links = doc.select("a[href]"); // gets every link string on the website
		for (Element link : links) { // for every link strng
			String linkToCheck = link.attr("href"); // get their absoulute links
			if (linkToCheck.length() > 9) { // check if their greater than 9 characters, if they're shorter, they're not
											// what I'm looking for
				if (linkToCheck.substring(0, 9).equals("/raffles/") && (Character.isUpperCase(linkToCheck.charAt(10))
						|| Character.isDigit(linkToCheck.charAt(10)))) {
					// if statement to check if the substring is a raffle link and to check so that
					// it's not the puzzle raffles or create raffle link
					String url = "https://scrap.tf" + linkToCheck; // creates a url string
					driver.get(url); // opens the url
					Thread.sleep(2000); // waits for page to load
					try { // selenium driver finds and clicks the enter raffle button
						driver.findElement(By.cssSelector(
								"#pid-viewraffle > div.container > div > div.well.raffle-well > div.row.raffle-opts-row > div.col-xs-7.enter-raffle-btns > button:nth-child(3)"))
								.click(); // using Selenium click button method
					} catch (NoSuchElementException e) {
						// do nothing and continue
					}
					Thread.sleep(2000); // waits a bit
					// the sleeps are necesarry as scrap.tf doesn't allow raffles to be enter less
					// than 2 seconds apart
				}
			}
		}
		JOptionPane.showMessageDialog(null, "All Raffles Entered");
	}
}
