package com.toolbox.modbus.tcplistener;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.PageSize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class HtmlToPdfConverter {

    public static void main(String[] args) {
        File htmlFile = new File("bol.html"); // Replace with your file path

        String htmlContent = "";
        try {
            htmlContent = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return; // Stop execution if the file can't be read
        }

        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(htmlFile);
            renderer.layout();
            try (FileOutputStream fos = new FileOutputStream("output.pdf")) {
                renderer.createPDF(fos);
            }
            try (FileOutputStream fos = new FileOutputStream("output.pdf")) {
                renderer.createPDF(fos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
