package com.makeyourprofile.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfExportService {

    private final ObjectMapper objectMapper;

    /**
     * Generates a PDF file from the provided JSON string representing a profile.
     * Uses Flying Saucer (ITextRenderer) to convert HTML to PDF.
     * @param profileJson The raw JSON string of the profile data.
     * @return Byte array of the generated PDF file.
     */
    public byte[] generatePdf(String profileJson) throws IOException {
        JsonNode rootNode = objectMapper.readTree(profileJson);
        String htmlContent = generateHtmlFromProfile(rootNode);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            throw new RuntimeException("PDF Generation failed", e);
        }
    }

    private String generateHtmlFromProfile(JsonNode rootNode) {
        JsonNode personalInfo = rootNode.path("personalInfo");

        StringBuilder html = new StringBuilder();
        // Flying Saucer requires valid XHTML - must have XML declaration + XHTML namespace
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            .append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">")
            .append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>")
            .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>")
            .append("<style type=\"text/css\">")
            .append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 40px; }")
            .append("h1 { text-align: center; margin-bottom: 0; }")
            .append("h3 { text-align: center; color: #666; margin-top: 5px; }")
            .append(".contact { text-align: center; font-size: 14px; margin-bottom: 20px; }")
            .append(".section { margin-top: 20px; }")
            .append(".section-title { border-bottom: 1px solid #000; padding-bottom: 5px; font-size: 18px; font-weight: bold; margin-bottom: 10px; }")
            .append(".item-title { font-weight: bold; }")
            .append("</style></head><body>");

        // Header
        html.append("<h1>").append(escapeHtml(personalInfo.path("name").asText(""))).append("</h1>")
            .append("<h3>").append(escapeHtml(personalInfo.path("title").asText(""))).append("</h3>")
            .append("<div class=\"contact\">")
            .append(escapeHtml(personalInfo.path("email").asText(""))).append(" | ")
            .append(escapeHtml(personalInfo.path("phone").asText(""))).append(" | ")
            .append(escapeHtml(personalInfo.path("location").asText("")))
            .append("</div>")
            .append("<p>").append(escapeHtml(personalInfo.path("bio").asText(""))).append("</p>");

        // Skills
        JsonNode skills = rootNode.path("skills");
        if (skills.isArray() && !skills.isEmpty()) {
            html.append("<div class=\"section\"><div class=\"section-title\">Skills</div><ul>");
            for (JsonNode skillCategory : skills) {
                html.append("<li><span class=\"item-title\">")
                    .append(escapeHtml(skillCategory.path("category").asText("")))
                    .append(": </span>");
                JsonNode items = skillCategory.path("items");
                if (items.isArray()) {
                    for (int i = 0; i < items.size(); i++) {
                        html.append(escapeHtml(items.get(i).asText()));
                        if (i < items.size() - 1) html.append(", ");
                    }
                }
                html.append("</li>");
            }
            html.append("</ul></div>");
        }

        // Experience
        JsonNode experience = rootNode.path("experience");
        if (experience.isArray() && !experience.isEmpty()) {
            html.append("<div class=\"section\"><div class=\"section-title\">Experience</div>");
            for (JsonNode exp : experience) {
                html.append("<div><span class=\"item-title\">")
                    .append(escapeHtml(exp.path("role").asText(""))).append(" at ").append(escapeHtml(exp.path("company").asText("")))
                    .append("</span> (").append(escapeHtml(exp.path("startDate").asText(""))).append(" - ").append(escapeHtml(exp.path("endDate").asText(""))).append(")</div>");

                JsonNode achievements = exp.path("achievements");
                if (achievements.isArray() && !achievements.isEmpty()) {
                    html.append("<ul>");
                    for (JsonNode achievement : achievements) {
                        html.append("<li>").append(escapeHtml(achievement.asText())).append("</li>");
                    }
                    html.append("</ul>");
                }
            }
            html.append("</div>");
        }
        // Education
        JsonNode education = rootNode.path("education");
        if (education.isArray() && !education.isEmpty()) {
            html.append("<div class=\"section\"><div class=\"section-title\">Education</div>");
            for (JsonNode edu : education) {
                html.append("<div><span class=\"item-title\">")
                    .append(escapeHtml(edu.path("degree").asText(""))).append(" in ").append(escapeHtml(edu.path("field").asText("")))
                    .append("</span><br/>")
                    .append(escapeHtml(edu.path("institution").asText(""))).append(", ").append(escapeHtml(edu.path("location").asText("")))
                    .append("<br/>(").append(escapeHtml(edu.path("startDate").asText(""))).append(" - ").append(escapeHtml(edu.path("endDate").asText(""))).append(")");
                if (edu.has("gpa") && !edu.path("gpa").asText().isEmpty()) {
                    html.append("<br/>GPA: ").append(escapeHtml(edu.path("gpa").asText()));
                }
                html.append("</div><br/>");
            }
            html.append("</div>");
        }

        // Projects
        JsonNode projects = rootNode.path("projects");
        if (projects.isArray() && !projects.isEmpty()) {
            html.append("<div class=\"section\"><div class=\"section-title\">Projects</div>");
            for (JsonNode proj : projects) {
                html.append("<div><span class=\"item-title\">")
                    .append(escapeHtml(proj.path("title").asText("")))
                    .append("</span><br/>")
                    .append(escapeHtml(proj.path("description").asText("")));
                
                JsonNode techStack = proj.path("techStack");
                if (techStack.isArray() && !techStack.isEmpty()) {
                    html.append("<br/>Tech Stack: ");
                    for (int i = 0; i < techStack.size(); i++) {
                        html.append(escapeHtml(techStack.get(i).asText()));
                        if (i < techStack.size() - 1) html.append(", ");
                    }
                }
                html.append("</div><br/>");
            }
            html.append("</div>");
        }

        // Certifications
        JsonNode certifications = rootNode.path("certifications");
        if (certifications.isArray() && !certifications.isEmpty()) {
            html.append("<div class=\"section\"><div class=\"section-title\">Certifications</div>");
            for (JsonNode cert : certifications) {
                html.append("<div><span class=\"item-title\">")
                    .append(escapeHtml(cert.path("title").asText("")))
                    .append("</span><br/>")
                    .append(escapeHtml(cert.path("issuer").asText(""))).append(" - ").append(escapeHtml(cert.path("date").asText("")))
                    .append("</div><br/>");
            }
            html.append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
