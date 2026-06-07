package com.dodo.jiwhan.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class FranchiseCrawlerService {

    public byte[] crawlFranchiseData() throws IOException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage"); // 컨테이너 환경 필수
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        String baseUrl = "https://franchise.ftc.go.kr/mnu/00013/program/userRqst/list.do"
            + "?column=brd&searchKeyword=&selUpjong=&selIndus=&pageUnit=300&pageIndex=";
        List<String[]> data = new ArrayList<>();
        int pageIndex = 1;

        try {
            while (true) {
                driver.get(baseUrl + pageIndex);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(1L));
                WebElement table = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table")));
                List<WebElement> rows = table.findElements(By.tagName("tr"));

                if (rows.size() <= 1) break;

                for (int i = 1; i < rows.size(); i++) {
                    List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
                    String[] rowData = new String[cols.size()];
                    for (int j = 0; j < cols.size(); j++) {
                        String text = cols.get(j).getText();
                        rowData[j] = (text != null) ? text.trim() : "";
                    }
                    data.add(rowData);
                }
                pageIndex++;
            }

            return buildExcel(data);

        } finally {
            driver.quit();
        }
    }

    private byte[] buildExcel(List<String[]> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Franchise Data");

        String[] headers = {"번호", "상호", "영업표지", "대표자", "등록번호", "최초등록일", "업종"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (String[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                row.createCell(i).setCellValue(rowData[i]);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
