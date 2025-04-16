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
@Table(name = "profile_company_detail_history", schema = "bprof")
public class ProfileCompanyDetailHistory implements Serializable {
  @Id
  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Column(name = "profile_identifier")
  private UUID profileId;

  @Column(name = "version_number")
  private int versionNumber;

  @Column(name = "onboarding_status_description")
  private String onboardingStatusDescription;

  @Column(name = "onboarding_status_code")
  private String onboardingStatusCode;

  @Column(name = "delete_status_indicator")
  private boolean deleteStatusIndicator = false;

  @Column(name = "business_type_text")
  private String businessTypeText;

  @Column(name = "create_source_text")
  private String createSourceText;

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
