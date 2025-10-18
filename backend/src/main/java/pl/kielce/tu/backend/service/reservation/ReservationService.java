package pl.kielce.tu.backend.service.reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.ReservationFilterMapper;
import pl.kielce.tu.backend.mapper.ReservationMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.ReservationStatus;
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Reservation;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.repository.ReservationRepository;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.dvd.DvdAvailabilityService;
import pl.kielce.tu.backend.service.rental.factory.RentalFactory;

@Service
@RequiredArgsConstructor
public class ReservationService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final DvdRepository dvdRepository;
    private final RentalFactory rentalFactory;
    private final UserRepository userRepository;
    private final CookieService cookieService;
    private final ClaimsExtractor claimsExtractor;
    private final RentalRepository rentalRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationFilterMapper filterMapper;
    private final DvdAvailabilityService availabilityService;
    private final ReservationRepository reservationRepository;
    private final ReservationValidationService reservationValidationService;

    public ResponseEntity<List<ReservationDto>> handleGetUserReservations(HttpServletRequest request, String filter) {
        try {
            Long userId = extractUserIdFromRequest(request);
            ReservationStatus status = filterMapper.mapFilterToStatus(filter);
            List<Reservation> reservations = getReservationsForUser(userId, status);
            return ResponseEntity.status(HttpStatus.OK).body(reservationMapper.toDtoList(reservations));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<Void> handleCreateReservation(HttpServletRequest request, ReservationDto reservationDto) {
        try {
            Long userId = extractUserIdFromRequest(request);
            validateBasicReservationData(reservationDto);
            Reservation reservation = buildReservation(userId, reservationDto);
            validateReservationAvailability(reservationDto, reservation.getDvd());
            decreaseDvdAvailability(reservation.getDvd(), reservationDto.getCount());
            reservationRepository.save(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<Void> handleAcceptReservation(String id) {
        try {
            Long reservationId = filterMapper.parseReservationId(id);
            Reservation reservation = findReservationById(reservationId);
            validateReservationForAcceptance(reservation);
            createRentalFromReservation(reservation);
            updateReservationStatus(reservation, ReservationStatus.ACCEPTED);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<Void> handleDeclineReservation(String id) {
        try {
            Long reservationId = filterMapper.parseReservationId(id);
            Reservation reservation = findReservationById(reservationId);
            validateReservationForDecline(reservation);
            increaseDvdAvailability(reservation.getDvd(), reservation.getCount());
            updateReservationStatus(reservation, ReservationStatus.REJECTED);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<Void> handleCancelReservation(HttpServletRequest request, String id) {
        try {
            Long userId = extractUserIdFromRequest(request);
            Long reservationId = filterMapper.parseReservationId(id);
            Reservation reservation = findReservationById(reservationId);
            reservationValidationService.validateReservationCancellation(reservation, userId);
            increaseDvdAvailability(reservation.getDvd(), reservation.getCount());
            updateReservationStatus(reservation, ReservationStatus.CANCELLED);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (IllegalArgumentException | ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<List<ReservationDto>> handleGetAllReservations(String filter) {
        try {
            ReservationStatus status = filterMapper.mapFilterToStatus(filter);
            List<Reservation> reservations = getAllReservations(status);
            return ResponseEntity.status(HttpStatus.OK).body(reservationMapper.toDtoList(reservations));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        if (token == null) {
            throw new RuntimeException("Authentication token not found");
        }
        return claimsExtractor.extractUserId(token, jwtSecret);
    }

    private List<Reservation> getReservationsForUser(Long userId, ReservationStatus status) {
        Pageable pageable = PageRequest.of(0, 50);
        return reservationRepository.findByUserIdWithOptionalStatus(userId, status, pageable).getContent();
    }

    private void validateBasicReservationData(ReservationDto reservationDto) {
        validateRequiredFields(reservationDto);
        validateDateRange(reservationDto);
        validateCopyCount(reservationDto);
    }

    private void validateRequiredFields(ReservationDto reservationDto) {
        if (reservationDto.getDvdId() == null) {
            throw new IllegalArgumentException("DVD ID is required");
        }
        if (reservationDto.getRentalStart() == null || reservationDto.getRentalEnd() == null) {
            throw new IllegalArgumentException("Rental dates are required");
        }
    }

    private void validateDateRange(ReservationDto reservationDto) {
        if (reservationDto.getRentalStart().isAfter(reservationDto.getRentalEnd())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    private void validateCopyCount(ReservationDto reservationDto) {
        if (reservationDto.getCount() == null || reservationDto.getCount() <= 0) {
            throw new IllegalArgumentException("Copy count must be greater than zero");
        }
    }

    private void validateReservationAvailability(ReservationDto reservationDto, Dvd dvd) {
        try {
            reservationValidationService.validateReservationRequest(reservationDto, dvd);
        } catch (pl.kielce.tu.backend.exception.ValidationException e) {
            throw new IllegalArgumentException("DVD reservation validation failed: " + e.getMessage());
        }
    }

    private Reservation buildReservation(Long userId, ReservationDto reservationDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Dvd dvd = dvdRepository.findById(reservationDto.getDvdId())
                .orElseThrow(() -> new EntityNotFoundException("DVD not found"));

        return Reservation.builder()
                .user(user)
                .dvd(dvd)
                .rentalStart(reservationDto.getRentalStart())
                .rentalEnd(reservationDto.getRentalEnd())
                .count(reservationDto.getCount())
                .createdAt(LocalDateTime.now())
                .status(ReservationStatus.PENDING)
                .build();
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
    }

    private void validateReservationForAcceptance(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending reservations can be accepted");
        }
    }

    private void validateReservationForDecline(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending reservations can be declined");
        }
    }

    private void updateReservationStatus(Reservation reservation, ReservationStatus status) {
        reservation.setStatus(status);
        reservationRepository.save(reservation);
    }

    private void decreaseDvdAvailability(Dvd dvd, Integer count) {
        availabilityService.decreaseAvailability(dvd, count);
    }

    private void increaseDvdAvailability(Dvd dvd, Integer count) {
        availabilityService.increaseAvailability(dvd, count);
    }

    private void createRentalFromReservation(Reservation reservation) {
        Rental rental = rentalFactory.createFromReservation(reservation);
        rentalRepository.save(rental);
    }

    private List<Reservation> getAllReservations(ReservationStatus status) {
        Pageable pageable = PageRequest.of(0, 100);
        if (status == null) {
            return reservationRepository.findAll(pageable).getContent();
        }
        return reservationRepository.findByStatus(status);
    }

}
