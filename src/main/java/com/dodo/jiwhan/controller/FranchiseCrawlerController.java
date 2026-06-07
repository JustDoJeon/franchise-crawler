package com.dodo.jiwhan.controller;

import com.dodo.jiwhan.service.FranchiseCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class FranchiseCrawlerController {

    @Autowired
    private FranchiseCrawlerService crawlerService;

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @PostMapping("/api/crawl")
    public ResponseEntity<byte[]> crawlFranchiseData() {
        try {
            byte[] excelData = crawlerService.crawlFranchiseData();
            String fileName = "franchise_list_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
