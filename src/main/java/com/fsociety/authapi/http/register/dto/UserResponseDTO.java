package com.fsociety.authapi.http.register.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
  @JsonProperty(value = "id_user", index = 0)
  private UUID idUser;

  @JsonProperty(value = "id_person", index = 1)
  private UUID idPerson;

  @JsonProperty(index = 2)
  private String username;

  @JsonProperty(value = "first_name", index = 3)
  private String firstName;

  @JsonProperty(value = "last_name", index = 4)
  private String lastName;

  @JsonProperty(index = 5)
  private String gender;

  @JsonProperty(index = 6)
  private String email;
}
