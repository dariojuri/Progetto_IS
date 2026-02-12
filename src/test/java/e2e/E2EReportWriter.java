package e2e;

import java.nio.file.*;
import java.time.Instant;
import java.util.List;

public final class E2EReportWriter {

    public record TestResult(String id, String name, boolean passed, String details, String timestamp) {}

    private E2EReportWriter() {}

    public static void writeHtmlReport(List<TestResult> results, Path out) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='utf-8'><title>E2E Report</title></head><body>");
        sb.append("<h1>System Test (E2E) - Pass/Fail Report</h1>");
        sb.append("<p>Generated: ").append(Instant.now()).append("</p>");
        sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
        sb.append("<tr><th>TC ID</th><th>Name</th><th>Status</th><th>Details</th><th>Timestamp</th></tr>");

        for (TestResult r : results) {
            sb.append("<tr>");
            sb.append("<td>").append(escape(r.id())).append("</td>");
            sb.append("<td>").append(escape(r.name())).append("</td>");
            sb.append("<td>").append(r.passed() ? "PASS" : "FAIL").append("</td>");
            sb.append("<td>").append(escape(r.details())).append("</td>");
            sb.append("<td>").append(escape(r.timestamp())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");

        Files.createDirectories(out.getParent());
        Files.writeString(out, sb.toString());
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
