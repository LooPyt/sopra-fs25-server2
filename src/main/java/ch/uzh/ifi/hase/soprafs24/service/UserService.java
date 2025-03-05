package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.Date;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User newUser) {
        User existingUser = checkIfUserExists(newUser);
        if (existingUser != null) {
            // If the user exists but the passwords don't match
            if (!existingUser.getPassword().equals(newUser.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong password");
            } else {
                // If passwords match, change status to online
                changeStatusToOnline(existingUser);
                newUser.setToken(UUID.randomUUID().toString());
                userRepository.flush();
                return existingUser;
            }
        } else {
            // If user doesn't exist, create a new user
            newUser.setToken(UUID.randomUUID().toString());
            newUser.setCreationDate(new Date());
            newUser.setStatus(UserStatus.ONLINE);
            // Save the new user in the database
            userRepository.save(newUser);
            userRepository.flush();
            log.debug("Created Information for User: {}", newUser);
            return newUser;
        }
    }
    public void saveUser(User ToSave){userRepository.save(ToSave);}

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    public Boolean verifyToken(String tokenToVerify) {
        User userToVerify = userRepository.findByToken(tokenToVerify);
        if (userToVerify == null || userToVerify.getStatus() != UserStatus.ONLINE) {
            String baseErrorMessage = "Token: " + tokenToVerify;
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, baseErrorMessage);
        }
        return true;
    }

    public void changeStatusToOnline(User userWhichGoesOnline) {
        // Find the user by username
        User user = userRepository.findByUsername(userWhichGoesOnline.getUsername());

        // Check if user exists
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for username: " + userWhichGoesOnline.getUsername());
        }

        // If the user exists, change the status to ONLINE
        user.setStatus(UserStatus.ONLINE);
        userRepository.flush(); // Ensure changes are persisted in the database
    }

    public void changeStatusToOffline(String tokenOfOnlineUser) {
        // Find the user by token
        User user = userRepository.findByToken(tokenOfOnlineUser);

        // Check if user exists
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for token: " + tokenOfOnlineUser);
        }

        // If the user exists, change the status to OFFLINE
        user.setStatus(UserStatus.OFFLINE);
        userRepository.flush(); // Ensure changes are persisted in the database
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private User checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        if (userByUsername != null) {//Anpassungen -> Namensprüfung gelöscht, da gemäss Test keine Exception
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
        }

        return userByUsername;
    }
}