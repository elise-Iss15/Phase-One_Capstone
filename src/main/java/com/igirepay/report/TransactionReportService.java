package com.igirepay.report;

import com.igirepay.model.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class TransactionReportService {

    public Path exportCsv(List<Transaction> transactions, Path outputPath) throws IOException {
        StringBuilder csv = new StringBuilder("id,account_id,reference_id,type,amount,timestamp\n");
        for (Transaction tx : transactions) {
            csv.append(tx.getTransactionId()).append(',')
                    .append(tx.getAccountId()).append(',')
                    .append(escape(tx.getReferenceId())).append(',')
                    .append(tx.getTransactionType()).append(',')
                    .append(tx.getAmount()).append(',')
                    .append(tx.getTimestamp()).append('\n');
        }
        Files.writeString(outputPath, csv.toString());
        return outputPath;
    }

    public String formatDailySummary(LocalDate date, Map<String, Double> totals) {
        StringBuilder sb = new StringBuilder();
        sb.append("Daily summary for ").append(date).append('\n');
        if (totals.isEmpty()) {
            sb.append("  No transactions recorded.\n");
        } else {
            totals.forEach((type, total) ->
                    sb.append("  ").append(type).append(": ").append(String.format("%.2f", total)).append('\n'));
        }
        return sb.toString();
    }

    public String formatCustomerStatement(long customerId, List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Statement for customer ").append(customerId).append('\n');
        sb.append("-".repeat(70)).append('\n');
        if (transactions.isEmpty()) {
            sb.append("No transactions.\n");
        } else {
            for (Transaction tx : transactions) {
                sb.append(tx).append('\n');
            }
        }
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
