package com.makeyourprofile.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocxExportService {

    private final ObjectMapper objectMapper;

    /**
     * Generates a DOCX file from the provided JSON string representing a profile.
     * @param profileJson The raw JSON string of the profile data.
     * @return Byte array of the generated DOCX file.
     */
    public byte[] generateDocx(String profileJson) throws IOException {
        JsonNode rootNode = objectMapper.readTree(profileJson);
        
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Personal Info Section
            JsonNode personalInfo = rootNode.path("personalInfo");
            createTitle(document, personalInfo.path("name").asText("Name Not Provided"));
            createSubtitle(document, personalInfo.path("title").asText("Title Not Provided"));
            createParagraph(document, 
                    personalInfo.path("email").asText("") + " | " + 
                    personalInfo.path("phone").asText("") + " | " + 
                    personalInfo.path("location").asText(""));
            
            document.createParagraph().createRun().addBreak();
            createParagraph(document, personalInfo.path("bio").asText(""));

            // Skills Section
            JsonNode skills = rootNode.path("skills");
            if (skills.isArray() && !skills.isEmpty()) {
                createSectionHeader(document, "Skills");
                for (JsonNode skillCategory : skills) {
                    String category = skillCategory.path("category").asText("");
                    StringBuilder itemsStr = new StringBuilder();
                    JsonNode items = skillCategory.path("items");
                    if (items.isArray()) {
                        for (int i = 0; i < items.size(); i++) {
                            itemsStr.append(items.get(i).asText());
                            if (i < items.size() - 1) itemsStr.append(", ");
                        }
                    }
                    createParagraph(document, category + ": " + itemsStr.toString());
                }
            }

            // Experience Section
            JsonNode experience = rootNode.path("experience");
            if (experience.isArray() && !experience.isEmpty()) {
                createSectionHeader(document, "Experience");
                for (JsonNode exp : experience) {
                    createBoldParagraph(document, exp.path("role").asText("") + " at " + exp.path("company").asText(""));
                    createParagraph(document, exp.path("startDate").asText("") + " - " + exp.path("endDate").asText(""));
                    
                    JsonNode achievements = exp.path("achievements");
                    if (achievements.isArray()) {
                        for (JsonNode achievement : achievements) {
                            createBulletPoint(document, achievement.asText());
                        }
                    }
                }
            }
            // Education Section
            JsonNode education = rootNode.path("education");
            if (education.isArray() && !education.isEmpty()) {
                createSectionHeader(document, "Education");
                for (JsonNode edu : education) {
                    createBoldParagraph(document, edu.path("degree").asText("") + " in " + edu.path("field").asText(""));
                    createParagraph(document, edu.path("institution").asText("") + ", " + edu.path("location").asText(""));
                    createParagraph(document, edu.path("startDate").asText("") + " - " + edu.path("endDate").asText(""));
                    if (edu.has("gpa") && !edu.path("gpa").asText().isEmpty()) {
                        createParagraph(document, "GPA: " + edu.path("gpa").asText());
                    }
                }
            }

            // Projects Section
            JsonNode projects = rootNode.path("projects");
            if (projects.isArray() && !projects.isEmpty()) {
                createSectionHeader(document, "Projects");
                for (JsonNode proj : projects) {
                    createBoldParagraph(document, proj.path("title").asText(""));
                    createParagraph(document, proj.path("description").asText(""));
                    
                    JsonNode techStack = proj.path("techStack");
                    if (techStack.isArray() && !techStack.isEmpty()) {
                        StringBuilder stackStr = new StringBuilder("Tech Stack: ");
                        for (int i = 0; i < techStack.size(); i++) {
                            stackStr.append(techStack.get(i).asText());
                            if (i < techStack.size() - 1) stackStr.append(", ");
                        }
                        createParagraph(document, stackStr.toString());
                    }
                }
            }

            // Certifications Section
            JsonNode certifications = rootNode.path("certifications");
            if (certifications.isArray() && !certifications.isEmpty()) {
                createSectionHeader(document, "Certifications");
                for (JsonNode cert : certifications) {
                    createBoldParagraph(document, cert.path("title").asText(""));
                    createParagraph(document, cert.path("issuer").asText("") + " - " + cert.path("date").asText(""));
                }
            }
            // Write to output stream
            document.write(out);
            return out.toByteArray();
        }
    }

    private void createTitle(XWPFDocument document, String text) {
        XWPFParagraph p = document.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(24);
        r.setText(text);
    }

    private void createSubtitle(XWPFDocument document, String text) {
        XWPFParagraph p = document.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setFontSize(14);
        r.setColor("666666");
        r.setText(text);
    }

    private void createSectionHeader(XWPFDocument document, String text) {
        document.createParagraph().createRun().addBreak();
        XWPFParagraph p = document.createParagraph();
        p.setBorderBottom(Borders.SINGLE);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(16);
        r.setText(text);
    }

    private void createParagraph(XWPFDocument document, String text) {
        XWPFParagraph p = document.createParagraph();
        XWPFRun r = p.createRun();
        r.setFontSize(11);
        r.setText(text);
    }

    private void createBoldParagraph(XWPFDocument document, String text) {
        XWPFParagraph p = document.createParagraph();
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(12);
        r.setText(text);
    }

    private void createBulletPoint(XWPFDocument document, String text) {
        XWPFParagraph p = document.createParagraph();
        p.setIndentationLeft(360); // indent bullet
        XWPFRun r = p.createRun();
        r.setFontSize(11);
        r.setText("\u2022 " + text); // Unicode bullet point
    }
}
