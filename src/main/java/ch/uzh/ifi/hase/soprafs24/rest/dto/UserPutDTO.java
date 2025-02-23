package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import java.util.Date;

public class UserPutDTO {
    private UserStatus status;
    private String token;
    private String username;  // Hinzugefügtes Feld für den Benutzernamen
    private Date birthDate;  // Hinzugefügtes Feld für das Geburtsdatum
    public UserStatus getStatus() {
        return status;}
    public void setStatus(UserStatus status){this.status = status;}
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}