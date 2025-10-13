package pl.kielce.tu.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.RentalDto;
import pl.kielce.tu.backend.model.entity.Rental;

@Component
@RequiredArgsConstructor
public class RentalMapper {

    public RentalDto toDto(Rental rental) {
        if (rental == null) {
            return null;
        }

        return RentalDto.builder()
                .id(rental.getId())
                .rentalStart(rental.getRentalStart())
                .rentalEnd(rental.getRentalEnd())
                .returnDate(rental.getReturnDate())
                .createdAt(rental.getCreatedAt())
                .dvdId(rental.getDvd() != null ? rental.getDvd().getId() : null)
                .dvdTitle(rental.getDvd() != null ? rental.getDvd().getTitle() : null)
                .count(rental.getCount())
                .status(rental.getStatus())
                .build();
    }

    public List<RentalDto> toDtoList(List<Rental> rentals) {
        if (rentals == null) {
            return null;
        }

        return rentals.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
