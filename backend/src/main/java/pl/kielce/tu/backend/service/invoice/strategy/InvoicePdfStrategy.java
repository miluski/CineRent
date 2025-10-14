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
public class InvoicePdfStrategy implements BillPdfStrategy {

    private final UserContextLogger userContextLogger;

    @Override
    public byte[] generatePdf(Rental rental) {
        try {
            return createInvoicePdf(rental);
        } catch (Exception e) {
            userContextLogger.logUserOperation("INVOICE_PDF_GENERATION_ERROR",
                    "Failed to generate invoice PDF for rental: " + rental.getId());
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    @Override
    public BillType getSupportedBillType() {
        return BillType.INVOICE;
    }

    private byte[] createInvoicePdf(Rental rental) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            addInvoiceContent(document, rental);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to create PDF document", e);
        }

        return outputStream.toByteArray();
    }

    private void addInvoiceContent(Document document, Rental rental) throws DocumentException {
        Transaction transaction = rental.getTransaction();

        addInvoiceHeader(document);
        addCompanyDetails(document);
        addInvoiceDetails(document, transaction);
        addCustomerDetails(document, rental);
        addServiceTable(document, rental, transaction);
        addTotalSection(document, transaction);
        addInvoiceFooter(document);
    }

    private void addInvoiceHeader(Document document) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph header = new Paragraph("FAKTURA", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        document.add(new Paragraph(" "));
    }

    private void addCompanyDetails(Document document) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        document.add(new Paragraph("Sprzedawca:", boldFont));
        document.add(new Paragraph("CineRent Sp. z o.o.", normalFont));
        document.add(new Paragraph("ul. Choroszczanska 10", normalFont));
        document.add(new Paragraph("15-950 Bialystok", normalFont));
        document.add(new Paragraph("NIP: 123-456-78-90", normalFont));
        document.add(new Paragraph(" "));
    }

    private void addInvoiceDetails(Document document, Transaction transaction) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        document.add(new Paragraph("Numer faktury: " + transaction.getInvoiceId(), boldFont));
        document.add(new Paragraph("Data wystawienia: " +
                transaction.getGeneratedAt().format(formatter), normalFont));
        document.add(new Paragraph(" "));
    }

    private void addCustomerDetails(Document document, Rental rental) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        document.add(new Paragraph("Nabywca:", boldFont));
        document.add(new Paragraph("Klient: " + rental.getUser().getNickname(), normalFont));
        document.add(new Paragraph(" "));
    }

    private void addServiceTable(Document document, Rental rental, Transaction transaction) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 3, 1, 1, 1 });

        addTableHeader(table);
        addServiceRows(table, rental, transaction);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTableHeader(PdfPTable table) {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

        table.addCell(new PdfPCell(new Phrase("Opis", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Ilosc", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Cena jedn.", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Wartosc", boldFont)));
    }

    private void addServiceRows(PdfPTable table, Rental rental, Transaction transaction) {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        BigDecimal rentalAmount = calculateRentalAmount(transaction);

        String dvdTitle = transaction.getDvdTitle();
        table.addCell(new PdfPCell(new Phrase("Usluga wypozyczenia plyty DVD \"" + dvdTitle + "\"", normalFont)));
        table.addCell(new PdfPCell(new Phrase("1", normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.format("%.2f PLN", rentalAmount), normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.format("%.2f PLN", rentalAmount), normalFont)));

        BigDecimal lateFee = transaction.getLateFee();
        table.addCell(new PdfPCell(new Phrase("Doplata za przetrzymanie", normalFont)));
        table.addCell(new PdfPCell(new Phrase("1", normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.format("%.2f PLN", lateFee), normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.format("%.2f PLN", lateFee), normalFont)));
    }

    private BigDecimal calculateRentalAmount(Transaction transaction) {
        return transaction.getTotalAmount().subtract(transaction.getLateFee());
    }

    private void addTotalSection(Document document, Transaction transaction) throws DocumentException {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph total = new Paragraph("RAZEM DO ZAPLATY: " +
                String.format("%.2f PLN", transaction.getTotalAmount()), boldFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
        document.add(new Paragraph(" "));
    }

    private void addInvoiceFooter(Document document) throws DocumentException {
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Paragraph footer = new Paragraph("Dziekujemy za skorzystanie z uslug CineRent!", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

}
