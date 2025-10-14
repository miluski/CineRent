package pl.kielce.tu.backend.service.invoice.strategy;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.entity.Rental;

public interface BillPdfStrategy {

    byte[] generatePdf(Rental rental);

    BillType getSupportedBillType();
}
