package framework;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Base {
    protected static JSONObject suiteConfigs;
    protected  static int implicitWaitTimeout;
    protected static  int explicitWaitTimeout;
    protected static String testCaseFilePath;
    protected static String sessionId;
    protected static boolean tc_failed=false;

    protected static void setUp(){
        suiteConfigs= getSuiteConfigs();
        sessionId=getCurrentDateTime();
    }

    protected static String getCurrentDateTime() {
        LocalDateTime myDateObj= LocalDateTime.now();
        DateTimeFormatter myFormatObj=DateTimeFormatter.ofPattern("hh.mm.ss.a_MM-dd-yyyy");
        return myDateObj.format(myFormatObj);
    }

    protected static JSONObject getSuiteConfigs() {
        String tsConfigFilePath="src/main/java/configs/testSuiteConfigs.json";
        JSONParser parser= new JSONParser();
        JSONObject configs= new JSONObject();
        try{
            Object obj=parser.parse(new FileReader(tsConfigFilePath));
            configs= (JSONObject) obj;
        } catch (IOException | ParseException e ) {
            e.printStackTrace();
        }
        return  configs;
    }
    protected static JSONArray getBrowsers(){
        return (JSONArray) suiteConfigs.get("browsers");
    }

    protected static JSONArray getSuite()
    {
        return (JSONArray) suiteConfigs.get("suites");
    }

    protected static JSONObject getExcelIndexes(){
        return (JSONObject) suiteConfigs.get("excelIndexes");
    }

    protected  static List<File> getAllTestCaseFilePath(String directoryName) {
        File directory = new File(directoryName);

        List<File> resutList = new ArrayList<File>();

        File[] fList = directory.listFiles();
        resutList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
            } else if (file.isDirectory()) {
                resutList.addAll(getAllTestCaseFilePath(file.getAbsolutePath()));
            }
        }
        return resutList;
    }

    protected static XSSFWorkbook getWorkbook(String testCaseFilePath){
        FileInputStream excelFileStream=null;
        try{
            excelFileStream=new FileInputStream(testCaseFilePath);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        XSSFWorkbook workbook=null;
        try{
            assert excelFileStream!=null;
            workbook=new XSSFWorkbook(excelFileStream);
        }catch (IOException e){
            e.printStackTrace();
        }

        return workbook;
    }

    protected static void saveAndCloseResultFile(XSSFWorkbook workbook, String testCaseFilePath, String sessionId, String browser) {
        String testResultFileName = new File(testCaseFilePath).getName();
        File dirFromSuite = new File((testCaseFilePath.split(testResultFileName)[0]).split(suiteConfigs.get("suitesDir").toString())[1]);
        File testResultDir = new File((testCaseFilePath.split(testResultFileName)[0]) + "../../reports"+ "/" + sessionId + "/" + browser + "/" + dirFromSuite);
        if (!testResultDir.exists()){
            testResultDir.mkdirs();
        }
        String testResultPath = testResultDir +"/"+ "Report_" + testResultFileName;

        FileOutputStream fout= null;
        try {
            fout = new FileOutputStream(testResultPath);
            workbook.write(fout);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
