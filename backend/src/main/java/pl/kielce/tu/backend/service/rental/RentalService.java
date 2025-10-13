package pl.kielce.tu.backend.service.rental;

import java.sql.Date;
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
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.RentalFilterMapper;
import pl.kielce.tu.backend.mapper.RentalMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.dto.RentalDto;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.dvd.DvdAvailabilityService;
import pl.kielce.tu.backend.service.rental.transaction.TransactionGeneratorService;

@Service
@RequiredArgsConstructor
public class RentalService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final RentalMapper rentalMapper;
    private final CookieService cookieService;
    private final ClaimsExtractor claimsExtractor;
    private final RentalFilterMapper filterMapper;
    private final RentalRepository rentalRepository;
    private final DvdAvailabilityService dvdAvailabilityService;
    private final TransactionGeneratorService transactionGenerator;

    public ResponseEntity<List<RentalDto>> handleGetUserRentals(HttpServletRequest request, String filter) {
        try {
            Long userId = extractUserIdFromRequest(request);
            RentalStatus status = filterMapper.mapFilterToStatus(filter);
            List<Rental> rentals = getRentalsForUser(userId, status);
            return ResponseEntity.status(HttpStatus.OK).body(rentalMapper.toDtoList(rentals));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<Void> handleReturnDvd(String id) {
        try {
            Long rentalId = filterMapper.parseRentalId(id);
            Rental rental = findRentalById(rentalId);
            validateRentalForReturn(rental);
            updateRentalStatus(rental, RentalStatus.RETURN_REQUESTED);
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
    public ResponseEntity<Void> handleAcceptReturn(String id) {
        try {
            Long rentalId = filterMapper.parseRentalId(id);
            Rental rental = findRentalById(rentalId);
            validateReturnRequest(rental);
            completeRental(rental);
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
    public ResponseEntity<Void> handleDeclineReturn(String id) {
        try {
            Long rentalId = filterMapper.parseRentalId(id);
            Rental rental = findRentalById(rentalId);
            validateReturnRequest(rental);
            updateRentalStatus(rental, RentalStatus.ACTIVE);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<List<RentalDto>> handleGetAllReturnRequests() {
        try {
            List<Rental> returnRequests = getAllReturnRequests();
            return ResponseEntity.status(HttpStatus.OK).body(rentalMapper.toDtoList(returnRequests));
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

    private List<Rental> getRentalsForUser(Long userId, RentalStatus status) {
        Pageable pageable = PageRequest.of(0, 50);
        return rentalRepository.findByUserIdWithOptionalStatus(userId, status, pageable).getContent();
    }

    private Rental findRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));
    }

    private void validateRentalForReturn(Rental rental) {
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalArgumentException("Rental is not active");
        }
    }

    private void validateReturnRequest(Rental rental) {
        if (rental.getStatus() != RentalStatus.RETURN_REQUESTED) {
            throw new IllegalArgumentException("Return not requested for this rental");
        }
    }

    private void updateRentalStatus(Rental rental, RentalStatus status) {
        rental.setStatus(status);
        rentalRepository.save(rental);
    }

    private void completeRental(Rental rental) {
        rental.setStatus(RentalStatus.INACTIVE);
        rental.setReturnDate(new Date(System.currentTimeMillis()));
        rental.setTransaction(transactionGenerator.generateTransaction(rental));
        dvdAvailabilityService.increaseAvailability(rental.getDvd(), rental.getCount());
        rentalRepository.save(rental);
    }

    private List<Rental> getAllReturnRequests() {
        return rentalRepository.findByStatus(RentalStatus.RETURN_REQUESTED);
    }

}
