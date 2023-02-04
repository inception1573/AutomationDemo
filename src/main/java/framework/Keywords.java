package framework;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Keywords extends Base{
    static WebDriver driver;

    protected static void openBrowser(String browser){
        System.out.println("Open "+ browser+"browser");
        boolean ignoreCertificateError=(boolean) suiteConfigs.get("ignoreCertificateError");
        String screenSize=(String) suiteConfigs.get("screenSize");

        switch (browser.trim().toLowerCase()){
            case "chrome":
                WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
                ChromeOptions chromeOptions=new ChromeOptions();
                if(ignoreCertificateError){
                    chromeOptions.addArguments("ignore-certificate-errors");
                }
                chromeOptions.addArguments("--window-size="+ screenSize);
                driver=new ChromeDriver(chromeOptions);
                break;
            case "safari":
                WebDriverManager.getInstance(DriverManagerType.SAFARI).setup();
                driver = new SafariDriver();
                break;
            default:
                System.out.println("Browser name \"" + browser + "\" is not recognized!");
        }
        driver.manage().timeouts().implicitlyWait(implicitWaitTimeout, TimeUnit.MICROSECONDS);
    }

    protected  static void closeBrowser(){
        System.out.println("Close the browser");
        if(driver!=null)
        {
            driver.quit();
        }
    }

    protected static void gotToURL(String testData, int row, int tcResultColumnIndex, int tcCommentColumnIndex, XSSFSheet tcSheet){
        try{
            driver.navigate().to(testData);
        }catch (Exception e){
            sendFailedResult(row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet,e.getMessage());
        }
    }

    protected static void type(String selectorType, String selectorValue,String testData,int row, int tcResultColumnIndex,int tcCommentColumnIndex,XSSFSheet tcSheet){
        System.out.println(row + ". Enter \"" + testData + "\" into an element with selector type \"" + selectorType + "\" and selector value \"" + selectorValue+"\"");
        try{
            By locator;
            locator= getLocator(selectorType,selectorValue);
            waitForVisible(locator);
            driver.findElement(locator).sendKeys(testData);

            sendPassedResult(row,tcResultColumnIndex,tcSheet);

            System.out.println("Result: Passed");
        }catch (Exception e)
        {
            sendFailedResult(row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet,e.getMessage());
            System.out.println("Result: Failed");
        }
    }

    protected static void click(String selectorType, String selectorValue,int row,int tcResultColumnIndex, int tcCommentColumnIndex, XSSFSheet tcSheet){
        System.out.println(row + ". Click on an element with selector type \"" + selectorType + "\" and selector value \"" + selectorValue+"\"");
        try{
            By locator;
            locator=getLocator(selectorType,selectorValue);
            waitForVisible(locator);
            waitForClickable(locator);
            driver.findElement(locator).click();

            sendPassedResult(row,tcResultColumnIndex,tcSheet);
            System.out.println("Result: Passed");
        }catch (Exception e){
            sendFailedResult(row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet,e.getMessage());
            System.out.println("Result: Failed");
        }
    }

    protected  static void assertURL(String testData,int row,int tcResultColumnIndex, int tcCommentColumnIndex, XSSFSheet tcSheet){
        System.out.println(row+". Check URL contains \""+testData+"\"");

        try{
            waitForUrlToContainText(testData);
            sendPassedResult(row,tcResultColumnIndex,tcSheet);
            System.out.println("Status: Passed");
        }catch (Exception e){
            sendFailedResult(row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet,e.getMessage());
            System.out.println("Status: Failed");
        }
    }

    protected static void asserText(String selectorType,String selectorValue,String testData, int row, int tcResultColumnIndex, int tcCommentColumnIndex, XSSFSheet tcSheet){
        System.out.println(row + ". Verify \"" + testData + "\" exist on an element with selector type \"" + selectorType + "\" and selector value \"" + selectorValue+"\"");
        try{
            By locator;
            locator=getLocator(selectorType,selectorValue);
            waitForVisible(locator);
            waitForTextToMatch(locator,testData);

            System.out.println("Result: Passed");
            sendPassedResult(row,tcResultColumnIndex,tcSheet);
        }catch (Exception e){
            System.out.println("Result: Failed");
            sendFailedResult(row, tcResultColumnIndex,tcCommentColumnIndex,tcSheet,e.getMessage());
        }
    }

    protected static void checkPresence(String selectorType, String selectorValue,int row,int tcResultColumnIndex,int tcCommentColumnIndex,XSSFSheet tcSheet){
        System.out.println(row + ". Check presence of an element with selector type \"" + selectorType + "\" and selector value \"" + selectorValue+"\"");
        try{
            By locator;
            locator=getLocator(selectorType,selectorType);
            waitForPresence(locator);

            System.out.println("Result: Passed");
            sendPassedResult(row,tcResultColumnIndex,tcSheet);
        }catch (Exception e){
            System.out.println("Result: Failed");
            sendFailedResult(row, tcResultColumnIndex,tcCommentColumnIndex,tcSheet,e.getMessage());
        }
    }

    private static void waitForPresence(By locator){
        WebDriverWait wait=new WebDriverWait(driver,60);
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    private static void waitForTextToMatch(By locator,String testData){
        WebDriverWait wait=new WebDriverWait(driver,explicitWaitTimeout/1000);
        wait.until(ExpectedConditions.textMatches(locator, Pattern.compile(testData.trim())));
    }
    private static void waitForUrlToContainText(String testData){
        WebDriverWait wait=new WebDriverWait(driver,explicitWaitTimeout/1000);
        wait.until(ExpectedConditions.urlContains(testData));
    }
    private static By getLocator(String selectorType,String selectorValue){
        By by;

        switch (selectorType.trim().toLowerCase()){
            case "xpath":
                by= By.xpath(selectorValue);
                break;
            case "cssselector":
                by=By.cssSelector(selectorValue);
                break;
            case "id":
                by=By.id(selectorValue);
                break;
            case "name":
                by=By.name(selectorValue);
                break;
            case "classname":
                by=By.className(selectorValue);
                break;
            case "tagname":
                by=By.tagName(selectorValue);
                break;
            case "linktext":
                by = By.linkText(selectorValue);
                break;
            case "partiallinktext":
                by = By.partialLinkText(selectorValue);
                break;
            default:
                by = null;
                System.out.println("Invalid selector type provided: \"" + selectorType +
                        "\". Selector type must be one of xpath, CssSelector, id, name, ClassName, TagName, LinkText or PartialLinkText");
                break;
        }
        return by;
    }
    private static void sendFailedResult(int row, int tcResultColumnIndex,int tcCommentColumnIndex,XSSFSheet tcSheet, String errorMessage){
        System.out.println("\n >>>>> Below error found <<<<\n\n"+ errorMessage+"\n\n");
        tc_failed=true;

        //closeBrowser();

        tcSheet.getRow(row).createCell(tcResultColumnIndex).setCellValue("Failed");
        tcSheet.getRow(row).createCell(tcCommentColumnIndex).setCellValue(errorMessage);
    }

    private static void waitForVisible(By locator){
        WebDriverWait wait=new WebDriverWait(driver,explicitWaitTimeout/1000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private static void waitForClickable(By locator){
        WebDriverWait wait=new WebDriverWait(driver,explicitWaitTimeout/1000);
        wait.until((ExpectedConditions.elementToBeClickable(locator)));
    }
    private static void sendPassedResult(int row, int tcResultColumnIndex, XSSFSheet tcSheet){
        tcSheet.getRow(row).createCell(tcResultColumnIndex).setCellValue("Passed");
    }
}
