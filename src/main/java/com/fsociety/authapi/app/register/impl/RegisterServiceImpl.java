package com.fsociety.authapi.app.register.impl;

import com.fsociety.authapi.app.catalog.CatalogService;
import com.fsociety.authapi.app.emailprovider.EmailService;
import com.fsociety.authapi.app.register.RegisterService;
import com.fsociety.authapi.domain.PersonRepository;
import com.fsociety.authapi.domain.User;
import com.fsociety.authapi.domain.UserRepository;
import com.fsociety.authapi.http.register.dto.UserRequestBuilder;
import com.fsociety.authapi.http.register.dto.UserRequestDTO;
import com.fsociety.authapi.http.register.dto.UserResponseBuilder;
import com.fsociety.authapi.http.register.dto.UserResponseDTO;
import com.fsociety.authapi.utils.NotFoundException;
import com.fsociety.authapi.utils.RegistrationException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for user registration.
 *
 * @author Emmanuel H. Ramirez (sxpjb)
 */
@Service
@AllArgsConstructor
public class RegisterServiceImpl implements RegisterService {
  private final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);

  private final EmailService emailService;
  private final CatalogService genderService;
  private final UserRepository userRepository;
  private final PersonRepository personRepository;

  /**
   * Register a new user.
   *
   * @param userRegisterDTO the user registration details
   * @return the response entity with the registered user details
   * @throws RegistrationException if an error occurs during registration
   */
  @Override
  @Transactional
  public UserResponseDTO register(UserRequestDTO userRegisterDTO) throws RegistrationException {
    log.info("Registering user: \n{}", userRegisterDTO.getUsername());

    if (userRepository.findByUsername(userRegisterDTO.getUsername()).isPresent()) {
      throw new RegistrationException("Username already exists. Please choose another username.");
    }

    if (personRepository.findByEmail(userRegisterDTO.getEmail()).isPresent()) {
      throw new RegistrationException("Email already exists. Please choose another email.");
    }

    try {
      User user = buildUserFromRequest(userRegisterDTO);
      UserResponseDTO userResponseDTO = buildResponseFromUser(user);
      sendConfirmationEmail(user);
      return userResponseDTO;
    } catch (Exception e) {
      log.error("Error while registering user.", e);
      throw new RegistrationException(
          "Error while registering user: " + e.getMessage(), e.getCause());
    }
  }

  /**
   * Build a user entity from the request details.
   *
   * @param userRegisterDTO the user registration details
   * @return the user entity
   */
  private User buildUserFromRequest(UserRequestDTO userRegisterDTO) throws NotFoundException {
    UserRequestBuilder userBuilder = new UserRequestBuilder();
    User user =
        userBuilder
            .fromUserRegisterDTO(userRegisterDTO)
            .withGender(userRegisterDTO.getGender(), genderService)
            .withExpirationCode()
            .build();

    user.setPerson(personRepository.save(user.getPerson()));
    return user;
  }

  /**
   * Build a response entity from the user entity.
   *
   * @param user the user entity
   * @return the response entity
   */
  protected UserResponseDTO buildResponseFromUser(User user) throws TransactionalException {
    return new UserResponseBuilder().fromUser(userRepository.save(user)).build();
  }

  private void sendConfirmationEmail(User user) {
    emailService.sendConfirmationEmail(
        user.getUsername(), user.getPerson().getEmail(), user.getConfirmationCode());
  }
}
