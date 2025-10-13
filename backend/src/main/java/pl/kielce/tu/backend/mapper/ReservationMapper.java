package pl.kielce.tu.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.model.entity.Reservation;

@Component
@RequiredArgsConstructor
public class ReservationMapper {

    public ReservationDto toDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return ReservationDto.builder()
                .id(reservation.getId())
                .rentalStart(reservation.getRentalStart())
                .rentalEnd(reservation.getRentalEnd())
                .createdAt(reservation.getCreatedAt())
                .dvdId(reservation.getDvd() != null ? reservation.getDvd().getId() : null)
                .dvdTitle(reservation.getDvd() != null ? reservation.getDvd().getTitle() : null)
                .count(reservation.getCount())
                .status(reservation.getStatus())
                .build();
    }

    public List<ReservationDto> toDtoList(List<Reservation> reservations) {
        if (reservations == null) {
            return null;
        }

        return reservations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
