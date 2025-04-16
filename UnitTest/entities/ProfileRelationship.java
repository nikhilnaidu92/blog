package com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "profile_relationship", schema = "bprof")
public class ProfileRelationship implements Serializable {
  @Id
  @Column(name = "profile_relationship_identifier")
  private UUID profileRelationshipId;

  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Column(name = "counter_profile_company_details_identifier")
  private UUID counterProfileCompanyDetailsId;

  @Column(name = "last_update_timestamp")
  private LocalDateTime lastUpdateTimestamp = LocalDateTime.now();

  @Column(name = "create_timestamp")
  private LocalDateTime createTimestamp = LocalDateTime.now();

  @Column(name = "last_update_source_name")
  private String lastUpdateBySourceName;

  @Column(name = "create_source_name")
  private String createBySourceName;

  @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, targetEntity = ProfileCompanyDetail.class)
  @JoinColumn(name = "profile_company_detail_identifier", nullable = false, insertable = false, updatable = false)
  @ToString.Exclude
  private ProfileCompanyDetail profileCompanyDetail;

  @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, targetEntity = ProfileCompanyDetail.class)
  @JoinColumn(name = "counter_profile_company_details_identifier", nullable = false, insertable = false,
    updatable = false)
  @ToString.Exclude
  private ProfileCompanyDetail counterProfileCompanyDetail;
}
