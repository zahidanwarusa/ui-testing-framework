package com.umr.utils;

import com.umr.core.config.ConfigLoader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for reading test configuration data from Excel files.
 * Handles one-liner test definitions in TestRunner, TestFlow, and TestData files.
 */
public class ExcelReader {
    private static final ConfigLoader config = ConfigLoader.getInstance();
    private static final String EXCEL_PATH = config.getProperty("excel.path", "./src/main/resources/excel");
    private static final String TEST_RUNNER_FILE = config.getProperty("test.runner.file", "TestRunner.xlsx");
    private static final String TEST_FLOW_FILE = config.getProperty("test.flow.file", "TestFlow.xlsx");
    private static final String TEST_DATA_FILE = config.getProperty("test.data.file", "TestData.xlsx");

    /**
     * Gets active test cases from the TestRunner file.
     *
     * @return A list of maps containing the active test cases
     * @throws IOException If the file cannot be read
     */
    public static List<Map<String, String>> getActiveTestCases() throws IOException {
        LogUtil.info("Getting active test cases from TestRunner");

        String testRunnerPath = EXCEL_PATH + "/" + TEST_RUNNER_FILE;
        List<Map<String, String>> allTests = readExcelFile(testRunnerPath, "TestCases");

        // Filter for active tests (Execute = Y)
        List<Map<String, String>> activeTests = new ArrayList<>();
        for (Map<String, String> test : allTests) {
            String execute = test.getOrDefault("Execute", "N");
            if (execute.equalsIgnoreCase("Y")) {
                activeTests.add(test);
            }
        }

        LogUtil.info("Found " + activeTests.size() + " active test cases");
        return activeTests;
    }

    /**
     * Gets the keyword sequence for a specific test ID from the TestFlow file.
     *
     * @param testId The test ID to get keywords for
     * @return A list of keywords in execution sequence
     * @throws IOException If the file cannot be read
     */
    public static List<String> getKeywordsForTest(String testId) throws IOException {
        LogUtil.info("Getting keywords for test: " + testId);

        String testFlowPath = EXCEL_PATH + "/" + TEST_FLOW_FILE;
        List<Map<String, String>> allFlows = readExcelFile(testFlowPath, "TestFlow");

        // Find the flow for this test ID
        for (Map<String, String> flow : allFlows) {
            if (testId.equals(flow.get("TestID"))) {
                // Extract all keywords from the row
                List<String> keywords = new ArrayList<>();

                // Iterate through all columns starting from second column (index 1)
                for (int i = 1; i <= 20; i++) {
                    String keywordKey = "Keyword" + i;
                    if (flow.containsKey(keywordKey) && !flow.get(keywordKey).isEmpty()) {
                        keywords.add(flow.get(keywordKey));
                    }
                }

                LogUtil.info("Found " + keywords.size() + " keywords for test ID: " + testId);
                return keywords;
            }
        }

        LogUtil.warn("Test flow not found for test ID: " + testId);
        return Collections.emptyList();
    }

    /**
     * Gets test data for a specific test ID from the TestData file.
     *
     * @param testId The test ID to get data for
     * @return A map containing the test data
     * @throws IOException If the file cannot be read
     */
    public static Map<String, String> getTestData(String testId) throws IOException {
        LogUtil.info("Getting test data for test: " + testId);

        String testDataPath = EXCEL_PATH + "/" + TEST_DATA_FILE;
        List<Map<String, String>> allTestData = readExcelFile(testDataPath, "TestData");

        // Find the data for this test ID
        for (Map<String, String> testData : allTestData) {
            if (testId.equals(testData.get("TestID"))) {
                LogUtil.info("Found test data for test ID: " + testId);
                return testData;
            }
        }

        LogUtil.warn("Test data not found for test ID: " + testId);
        return Collections.emptyMap();
    }

    /**
     * Reads an Excel file and returns its data as a list of maps.
     *
     * @param filePath The path to the Excel file
     * @param sheetName The name of the sheet to read
     * @return A list of maps representing rows in the Excel file
     * @throws IOException If the file cannot be read
     */
    private static List<Map<String, String>> readExcelFile(String filePath, String sheetName) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                LogUtil.error("Sheet not found: " + sheetName);
                return data;
            }

            // Get header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                LogUtil.warn("Header row is empty in sheet: " + sheetName);
                return data;
            }

            // Extract header names
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell headerCell = headerRow.getCell(i);
                if (headerCell != null) {
                    headers.add(getCellValueAsString(headerCell));
                } else {
                    headers.add("Column" + i); // Default column name if header is null
                }
            }

            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow != null) {
                    Map<String, String> rowData = new HashMap<>();

                    // Process each cell in the row
                    for (int j = 0; j < headers.size() && j < dataRow.getLastCellNum(); j++) {
                        Cell dataCell = dataRow.getCell(j);
                        String value = (dataCell != null) ? getCellValueAsString(dataCell) : "";
                        rowData.put(headers.get(j), value);
                    }

                    // Only add non-empty rows (at least TestID should be present)
                    if (rowData.containsKey("TestID") && !rowData.get("TestID").isEmpty()) {
                        data.add(rowData);
                    }
                }
            }
        }

        return data;
    }

    /**
     * Gets the cell value as a string, handling different cell types.
     *
     * @param cell The cell to get the value from
     * @return The cell value as a string
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Prevent scientific notation and loss of precision
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.format("%.0f", value);
                    } else {
                        return String.valueOf(value);
                    }
                }

            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());

            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }

            case BLANK:
                return "";

            default:
                return "";
        }
    }
}