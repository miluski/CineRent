package pl.kielce.tu.backend.service.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserGenreService {

    private final UserRepository userRepository;

    @Transactional
    public void removeGenreFromAllUsers(Long genreId) {
        List<User> usersWithGenre = userRepository.findUsersByPreferredGenreId(genreId);

        for (User user : usersWithGenre) {
            if (user.getPreferredGenres() != null) {
                user.getPreferredGenres().removeIf(genre -> genre.getId().equals(genreId));
                userRepository.save(user);
            }
        }
    }

    public long countUsersWithGenre(Long genreId) {
        return userRepository.findUsersByPreferredGenreId(genreId).size();
    }
    
}
