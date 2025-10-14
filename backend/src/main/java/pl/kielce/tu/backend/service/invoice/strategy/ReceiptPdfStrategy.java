package pl.kielce.tu.backend.service.invoice.strategy;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class ReceiptPdfStrategy implements BillPdfStrategy {

    private final UserContextLogger userContextLogger;

    @Override
    public byte[] generatePdf(Rental rental) {
        try {
            return createReceiptPdf(rental);
        } catch (Exception e) {
            userContextLogger.logUserOperation("RECEIPT_PDF_GENERATION_ERROR",
                    "Failed to generate receipt PDF for rental: " + rental.getId());
            throw new RuntimeException("Failed to generate receipt PDF", e);
        }
    }

    @Override
    public BillType getSupportedBillType() {
        return BillType.RECEIPT;
    }

    private byte[] createReceiptPdf(Rental rental) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            addReceiptContent(document, rental);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to create PDF document", e);
        }
        return outputStream.toByteArray();
    }

    private void addReceiptContent(Document document, Rental rental) throws DocumentException {
        Transaction transaction = rental.getTransaction();
        addReceiptHeader(document);
        addCompanyInfo(document);
        addReceiptDetails(document, transaction);
        addServiceTable(document, rental, transaction);
        addTotalSection(document, transaction);
        addReceiptFooter(document);
    }

    private void addReceiptHeader(Document document) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Paragraph header = new Paragraph("PARAGON FISKALNY", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        document.add(new Paragraph(" "));
    }

    private void addCompanyInfo(Document document) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Paragraph company = new Paragraph("CineRent Sp. z o.o.", boldFont);
        company.setAlignment(Element.ALIGN_CENTER);
        document.add(company);
        addCenteredParagraph(document, "ul. Choroszczanska 10", normalFont);
        addCenteredParagraph(document, "15-950 Bialystok", normalFont);
        addCenteredParagraph(document, "NIP: 123-456-78-90", normalFont);
        document.add(new Paragraph(" "));
    }

    private void addReceiptDetails(Document document, Transaction transaction) throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        document.add(new Paragraph("Data i godzina: " +
                transaction.getGeneratedAt().format(formatter), normalFont));
        document.add(new Paragraph("Paragon nr: " + transaction.getInvoiceId(), normalFont));
        document.add(new Paragraph(" "));
    }

    private void addServiceTable(Document document, Rental rental, Transaction transaction) throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 4, 1 });
        BigDecimal rentalAmount = calculateRentalAmount(transaction);
        table.addCell(new PdfPCell(new Phrase("Usluga wypozyczenia plyty DVD", normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.format("%.2f PLN", rentalAmount), normalFont)));
        if (transaction.getLateFee().compareTo(BigDecimal.ZERO) > 0) {
            table.addCell(new PdfPCell(new Phrase("Doplata za przetrzymanie", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f PLN", transaction.getLateFee()), normalFont)));
        }
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTotalSection(Document document, Transaction transaction) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Paragraph total = new Paragraph("DO ZAPLATY: " +
                String.format("%.2f PLN", transaction.getTotalAmount()), boldFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
        document.add(new Paragraph(" "));
    }

    private void addReceiptFooter(Document document) throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        addCenteredParagraph(document, "Dziekujemy za zakup!", normalFont);
        addCenteredParagraph(document, "CineRent - Twoje ulubione filmy na DVD", smallFont);
    }

    private void addCenteredParagraph(Document document, String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }

    private BigDecimal calculateRentalAmount(Transaction transaction) {
        return transaction.getTotalAmount().subtract(transaction.getLateFee());
    }

}
