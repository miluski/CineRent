package pl.kielce.tu.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kielce.tu.backend.model.constant.BillType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for generating a specific type of bill document")
public class BillRequestDto {

    @NotNull(message = "Bill type is required")
    @Schema(description = "Type of bill to generate", example = "INVOICE", allowableValues = { "INVOICE", "RECEIPT" })
    private BillType billType;

}
