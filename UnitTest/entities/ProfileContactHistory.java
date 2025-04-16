package com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SuperBuilder(toBuilder = true)
@ToString
@Entity
@Table(name = "profile_contact_history", schema = "bprof")
public class ProfileContactHistory implements Serializable {
  @Id
  @Column(name = "profile_contact_identifier")
  private UUID profileContactId;

  @Column(name = "profile_identifier")
  private UUID profileId;

  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Column(name = "contact_description")
  private String contactDescription;

  @Column(name = "role_name")
  private String roleName;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "telephone_number")
  private String telephoneNumber;

  @Column(name = "fax_number")
  private String faxNumber;

  @Column(name = "email_address_text")
  private String emailAddressText;

  @Column(name = "url_text")
  private String urlText;

  @Column(name = "type_text")
  private String typeTx;

  @Column(name = "last_update_timestamp")
  private LocalDateTime lastUpdateTimestamp = LocalDateTime.now();

  @Column(name = "create_timestamp")
  private LocalDateTime createTimestamp = LocalDateTime.now();

  @Column(name = "last_update_source_name")
  private String lastUpdateBySourceName;

  @Column(name = "create_source_name")
  private String createBySourceName;

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return "Failed to map " + this.getClass().getName() + " to JSON.";
    }
  }
}
