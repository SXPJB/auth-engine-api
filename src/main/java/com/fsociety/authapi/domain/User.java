package com.fsociety.authapi.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.LocalDateTime;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_person", referencedColumnName = "id")
  Person person;

  private String username;
  private String password;
  private String token;
  private Boolean isConfirmed;
  private String confirmationCode;
  private Timestamp confirmationCodeExpires;
  private Boolean active;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  public Map<String, Object> toMap() {
    return Map.of(
        "id_user", id,
        "id_person", person.getId(),
        "username", username,
        "first_name", person.getFirstName(),
        "last_name", person.getLastName());
  }

  @PrePersist
  public void defaultValues() {
    this.active = true;
    this.isConfirmed = false;
    var now = new Timestamp(LocalDateTime.now().toDateTime().getMillis());
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void updateValues() {
    this.updatedAt = new Timestamp(LocalDateTime.now().toDateTime().getMillis());
  }

  public void generateConfirmCode() {
    // Set the confirmation code expiration time to 24 hours
    SecureRandom random = new SecureRandom();
    this.confirmationCode = new BigInteger(130, random).toString(32);
  }

  public void setConfirmCodeExpiresIn2h() {
    this.confirmationCodeExpires =
        new Timestamp(LocalDateTime.now().plusDays(1).toDateTime().getMillis());
  }

  public void regenerateConfirmCode() {
    this.generateConfirmCode();
    this.setConfirmCodeExpiresIn2h();
  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return e.getMessage();
    }
  }
}
