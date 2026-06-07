package com.dodo.jiwhan.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FranchiseCrawlerService {

    private static final String BASE_URL =
        "https://franchise.ftc.go.kr/mnu/00013/program/userRqst/list.do"
        + "?column=brd&searchKeyword=&selUpjong=&selIndus=&pageUnit=300&pageIndex=";

    public byte[] crawlFranchiseData() throws IOException {
        List<String[]> data = new ArrayList<>();
        int pageIndex = 1;

        while (true) {
            Document doc = Jsoup.connect(BASE_URL + pageIndex)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30_000)
                .get();

            Element table = doc.selectFirst("table.table");
            if (table == null) break;

            Elements rows = table.select("tr");
            if (rows.size() <= 1) break;

            for (int i = 1; i < rows.size(); i++) {
                Elements cols = rows.get(i).select("td");
                if (cols.isEmpty()) continue;

                String[] rowData = new String[cols.size()];
                for (int j = 0; j < cols.size(); j++) {
                    rowData[j] = cols.get(j).text().trim();
                }
                data.add(rowData);
            }
            pageIndex++;
        }

        return buildExcel(data);
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
