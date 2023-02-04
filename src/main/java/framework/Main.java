package framework;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.safari.SafariDriver;

import javax.swing.*;
import java.io.File;
import java.util.List;

public class Main extends Base{
    public static void main(String[] args) {
        /*
        WebDriver safariWebDriver= new SafariDriver();
        safariWebDriver.manage().window().maximize();
        safariWebDriver.get("https://www.google.com");
        */
        setUp();
        JSONArray suites=getSuite();
        for(Object browser: getBrowsers()){
            runTestSuite(suites, browser.toString());
        }
    }

    private static  void runTestSuite(JSONArray suites, String browser){
        for(Object o: suites){
            JSONObject testCaseObject=(JSONObject) o;
            String testCaseDirectory=(String) testCaseObject.get("directory");
            JSONArray ignoreTestCases=(JSONArray) testCaseObject.get("ignoreTestCases");
            Boolean testcaseDirSkip=(Boolean) testCaseObject.get("skip");
            List<File> testCasePaths=getAllTestCaseFilePath(testCaseDirectory);

            if(!testcaseDirSkip){
                for(File path: testCasePaths)
                {
                    testCaseFilePath=path.toString();
                    String testCaseFileName=new File(testCaseFilePath).getName();
                    if(!ignoreTestCases.contains(testCaseFileName)){
                        System.out.println("\n>>>> Executing Test Case File: "+testCaseFileName);
                        runTestScenarios(browser);
                    }
                }
            }
        }
    }

    private static void runTestScenarios(String browser){
        String tsSheetName=(String) getExcelIndexes().get("tsSheetName");
        int tsIdColumnIndex=(int)(long) getExcelIndexes().get("tsIdColumnIndex");
        int tsNameColumnIndex=(int)(long) getExcelIndexes().get("tsNameColumnIndex");
        int tsResultColumnIndex=(int)(long)getExcelIndexes().get("tsResultColumnIndex");
        int tsCommentColumnIndex=(int)(long)getExcelIndexes().get("tsCommentColumnIndex");
        int tsSkipColumnIndex=(int)(long) getExcelIndexes().get("tsSkipColumnIndex");

        XSSFWorkbook workbook=getWorkbook(testCaseFilePath);

        assert workbook!=null;
        XSSFSheet tsSheet=workbook.getSheet(tsSheetName);

        for (int i=1;i<tsSheet.getLastRowNum()+1;i++)
        {
            tc_failed=false;

            Cell tsIdCell=tsSheet.getRow(i).getCell(tsIdColumnIndex);
            Cell tsNameCell=tsSheet.getRow(i).getCell(tsNameColumnIndex);
            Cell tsSkipCell=tsSheet.getRow(i).getCell(tsSkipColumnIndex);

            if(tsIdCell==null || tsIdCell.toString().equalsIgnoreCase("")){
                continue;
            }

            DataFormatter formatter=new DataFormatter();
            int tsId=Integer.parseInt(formatter.formatCellValue(tsIdCell));
            String tsName=formatter.formatCellValue(tsNameCell);
            boolean tsSkip=Boolean.parseBoolean(formatter.formatCellValue(tsSkipCell));

            if(!tsSkip){
                System.out.println("\nScenario #"+tsId+":"+tsName+"\n");
                Keywords.openBrowser(browser);
                runTestCases(tsId,workbook,browser);
                Keywords.closeBrowser();
            }

            if(tc_failed){
                tsSheet.getRow(i).createCell(tsResultColumnIndex).setCellValue("Failed");
                tsSheet.getRow(i).getCell(tsCommentColumnIndex).setCellValue("At least one of the test cases has failed!");
            }
            else {
                tsSheet.getRow(i).createCell(tsResultColumnIndex).setCellValue("Passed");
            }
        }
        saveAndCloseResultFile(workbook, testCaseFilePath, sessionId, browser);
    }

    private static void runTestCases(int tsId,XSSFWorkbook workbook,String browser){
        String tcSheetNamePrefix=(String) getExcelIndexes().get("tcSheetNamePrefix");

        XSSFSheet tcSheet=workbook.getSheet(tcSheetNamePrefix + tsId);

        //Getting indexes for Test Case sheet
        int tcKeywordColumnIndex=(int)(long) getExcelIndexes().get("tcKeywordColumnIndex");
        int tcSelectorTypeColumnIndex=(int)(long) getExcelIndexes().get("tcSelectorTypeColumnIndex");
        int tcSelectorValueColumnIndex=(int)(long) getExcelIndexes().get("tcSelectorValueColumnIndex");
        int tcTestDataColumnIndex=(int)(long) getExcelIndexes().get("tcTestDataColumnIndex");
        int tcResultColumnIndex=(int)(long) getExcelIndexes().get("tcResultColumnIndex");
        int tcCommentColumnIndex=(int)(long) getExcelIndexes().get("tcCommentColumnIndex");
        int tcSkipColumnIndex=(int)(long) getExcelIndexes().get("tcSkipIndex");

        DataFormatter formatter=new DataFormatter();
        for(int j=1;j<tcSheet.getLastRowNum()+1;j++){
            Cell tcKeyword=tcSheet.getRow(j).getCell(tcKeywordColumnIndex);

            //if any test step fails, exit the current scenario
            if(tc_failed){
                System.out.println("\nTest failed! Exiting...\n");
                break;
            }

            if(tcKeyword==null || tcKeyword.toString().equalsIgnoreCase("")){
                continue;
            }

            String keyword=formatter.formatCellValue(tcKeyword).trim();
            String selectorType= formatter.formatCellValue(tcSheet.getRow(j).getCell(tcSelectorTypeColumnIndex)).trim();
            String selectorValue=formatter.formatCellValue(tcSheet.getRow(j).getCell(tcSelectorValueColumnIndex)).trim();
            String testData= formatter.formatCellValue(tcSheet.getRow(j).getCell(tcTestDataColumnIndex)).trim();
            boolean skip=Boolean.parseBoolean(formatter.formatCellValue(tcSheet.getRow(j).getCell(tcSkipColumnIndex)));

            if(!skip){
                runTestSteps(keyword,selectorType,selectorValue,testData,j,tcResultColumnIndex,tcCommentColumnIndex,tcSheet);
            }
        }
    }

    private static void runTestSteps(String keyword, String selectorType,String selectorValue, String testData, int row,int tcResultColumnIndex, int tcCommentColumnIndex, XSSFSheet tcSheet){
        switch (keyword.toLowerCase()){
            case "gotourl":
                Keywords.gotToURL(testData,row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet);
                break;
            case "type":
                Keywords.type(selectorType,selectorValue,testData,row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet);
                break;
            case "click":
                Keywords.click(selectorType,selectorValue,row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet);
                break;
            case "assertUrl":
                Keywords.assertURL(testData,row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet);
                break;
            case "assertText":
                Keywords.asserText(selectorType,selectorValue,testData,row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet);
                break;
            case "checkpresence":
                Keywords.checkPresence(selectorType,selectorValue,row,tcResultColumnIndex,tcCommentColumnIndex,tcSheet);
                break;
        }
    }
}