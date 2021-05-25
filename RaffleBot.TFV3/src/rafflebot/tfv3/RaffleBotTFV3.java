package rafflebot.tfv3;

import java.awt.AWTException;
import java.awt.Robot;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RaffleBotTFV3 {

    public static void main(String[] args) throws AWTException, InterruptedException, IllegalMonitorStateException {
        Robot r = new Robot();
        Scanner scanner = new Scanner(System.in);

        //Getting user login info
		System.out.println("RaffleBot.TF Version 3");
        System.out.print("Enter your steam username: ");
        String user = scanner.nextLine();
        System.out.print("Enter your steam password (Case-Sensitive): ");
        String pass = scanner.nextLine();
        System.out.print("Enter your current Steam Guard code (if you don't have a Steam Guard code, leave this blank): ");
        String steamGuard = scanner.nextLine();

        //Setting up chromedriver
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get("https://scrap.tf/raffles");
        JavascriptExecutor jse = (JavascriptExecutor) driver;

        //Logging in
        driver.findElement(By.cssSelector("#navbar-main > ul.nav.navbar-nav.navbar-right")).click();
        WebElement userField = driver.findElement(By.id("steamAccountName"));
        userField.sendKeys(user);
        WebElement passField = driver.findElement(By.id("steamPassword"));
        passField.sendKeys(pass);
        r.keyPress(KeyEvent.VK_ENTER);
        WebDriverWait wait = new WebDriverWait(driver, 30);
        if (!steamGuard.isEmpty()) { //Only executes if user entered steam guard code
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#twofactorcode_entry")));
            WebElement steamGuardField = driver.findElement(By.cssSelector("#twofactorcode_entry"));
            steamGuardField.sendKeys(steamGuard);
            r.keyPress(KeyEvent.VK_ENTER);
        }
        wait.until(ExpectedConditions.urlToBe("https://scrap.tf/raffles"));

        //Gets the number of active raffles
        String numOfRaffles = driver.findElement(By.className("raffle-list-stat")).getText();
        int numOfRafflesI;
        String numToGet = "";
        boolean gettingNumber = false;
        for (int i = 0; i < numOfRaffles.length(); i++) {
            if (numOfRaffles.charAt(i) == '/') {
                gettingNumber = true;
            } else if (gettingNumber && !Character.isDigit(numOfRaffles.charAt(i))) {
                break;
            } else if (gettingNumber && Character.isDigit(numOfRaffles.charAt(i))) {
                numToGet += numOfRaffles.charAt(i);
            }
        }
        numOfRafflesI = Integer.parseInt(numToGet);

        //Stores all raffle links in a list
        List<WebElement> listOfRaffles = driver.findElements(By.className("panel-raffle"));
		int lastScrollHeight = Integer.parseInt(String.valueOf(((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight")));
		int currentScrollHeight = 0;
		int sameHeightCount = 0;
        while (listOfRaffles.size() != numOfRafflesI || sameHeightCount > 50) {
			currentScrollHeight = Integer.parseInt(String.valueOf(((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight")));
			if (currentScrollHeight > lastScrollHeight){
				lastScrollHeight = currentScrollHeight;
				sameHeightCount = 0;
			}else{
				sameHeightCount++;
			}
            listOfRaffles = driver.findElements(By.className("panel-raffle"));
            jse.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        }
        System.out.println("All raffles retrieved");
//        JOptionPane.showMessageDialog(null, "All raffles retrieved");

		/*The two above steps ensure that the page is fully loaded before using JSoup to retrieve
		the page source and all the links in it
		*/

        //Gets all links on website
        String HTML = driver.getPageSource();
        Document doc = Jsoup.parse(HTML);
        Elements links = doc.select("a[href]");

        //Enters all raffles
        for (Element link : links) {
            String linkToCheck = link.attr("href");
            //Raffle links will always start with /raffles/ and have an upper case letter or digit at pos 10
            if (linkToCheck.startsWith("/raffles/") && (Character.isUpperCase(linkToCheck.charAt(10))
                    || Character.isDigit(linkToCheck.charAt(10)))) {
                String url = "https://scrap.tf" + linkToCheck;
                driver.get(url);
                Thread.sleep(2000);
                try {
                    driver.findElement(By.cssSelector(
                            "#pid-viewraffle > div.container > div > div.well.raffle-well > div.row.raffle-opts-row > div.col-xs-7.enter-raffle-btns > button:nth-child(3)"))
                            .click();
                } catch (NoSuchElementException e) {
                    System.out.println(e);
                }
                Thread.sleep(2000);
                /* the sleeps are necesarry as scrap.tf doesn't allow raffles to be enter less
                than 2 seconds apart */
            }
        }
        System.out.println("All raffles entered");
//        JOptionPane.showMessageDialog(null, "All Raffles Entered");
    }
}
