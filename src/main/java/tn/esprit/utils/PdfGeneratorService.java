package tn.esprit.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfGeneratorService {

    private static final String RECEIPT_DIR = "receipts";

    /**
     * Generates a PDF receipt for a completed P2P trade.
     * 
     * @param contractId   The ID of the executed contract.
     * @param actionType   Buy or Sell based on the contract type.
     * @param assetSymbol  The symbol of the asset traded (e.g. BTC).
     * @param quantity     The amount of asset traded.
     * @param pricePerUnit The agreed price per unit.
     * @param totalValue   The total fiat value.
     * @param creatorName  The name of the user who published the contract.
     * @param accepterName The name of the user who accepted the contract.
     * @param qrDataRaw    The raw string data used to generate the validation QR
     *                     code.
     * @return Absolute file path to the generated PDF receipt, or null if failed.
     */
    public static String generateReceipt(
            long contractId, String actionType, String assetSymbol,
            int quantity, double pricePerUnit, double totalValue,
            String creatorName, String accepterName, String qrDataRaw) {

        Document document = new Document();
        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get(RECEIPT_DIR));

            String fileName = "Contract_Receipt_" + contractId + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = Paths.get(RECEIPT_DIR, fileName).toAbsolutePath().toString();

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

            // Title
            Paragraph title = new Paragraph("NEXORA TRADING", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("OFFICIAL P2P TRADE RECEIPT", subFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Paragraph dateParagraph = new Paragraph("Executed On: " + timestamp, normalFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            dateParagraph.setSpacingAfter(20);
            document.add(dateParagraph);

            // Table for Contract Details
            PdfPTable table = new PdfPTable(2); // 2 columns
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableRow(table, "Contract ID:", String.valueOf(contractId), boldFont, normalFont);
            addTableRow(table, "Action Type:", actionType, boldFont, normalFont);
            addTableRow(table, "Asset:", assetSymbol, boldFont, normalFont);
            addTableRow(table, "Quantity Traded:", String.valueOf(quantity), boldFont, normalFont);
            addTableRow(table, "Price Per Unit:", String.format("$%.2f", pricePerUnit), boldFont, normalFont);

            // Highlight total row
            PdfPCell totalLabel = new PdfPCell(new Phrase("Total Value:", boldFont));
            totalLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
            totalLabel.setPadding(8);
            PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("$%.2f", totalValue), boldFont));
            totalValueCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            totalValueCell.setPadding(8);
            table.addCell(totalLabel);
            table.addCell(totalValueCell);

            document.add(table);

            // Parties details
            document.add(new Paragraph(" "));
            Paragraph partiesTitle = new Paragraph("Participating Parties", subFont);
            partiesTitle.setSpacingAfter(10);
            document.add(partiesTitle);

            document.add(new Paragraph("Contract Publisher: " + creatorName, normalFont));
            document.add(new Paragraph("Executing Party: " + accepterName, normalFont));
            document.add(new Paragraph(" "));

            // QR Code Generation (Using our built-in utility but converted for iText)
            try {
                // Generate raw PNG bytes using our ZXing utility
                byte[] qrBytes = QRCodeService.getQRCodeImageBytes(qrDataRaw, 150, 150);
                if (qrBytes != null) {
                    Image qrImage = Image.getInstance(qrBytes);
                    qrImage.setAlignment(Element.ALIGN_CENTER);

                    document.add(new Paragraph("Scan to Verify Contract Authenticity", normalFont));
                    document.add(new Paragraph(" "));
                    document.add(qrImage);
                }
            } catch (Exception e) {
                System.err.println("Could not embed QR code into PDF: " + e.getMessage());
            }

            // Footer
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Thank you for trading on Nexora.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return filePath;

        } catch (Exception e) {
            System.err.println("Error generating PDF receipt: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private static void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        cell1.setPadding(8);
        cell1.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell cell2 = new PdfPCell(new Phrase(value, valueFont));
        cell2.setPadding(8);
        cell2.setBorderColor(BaseColor.LIGHT_GRAY);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}
