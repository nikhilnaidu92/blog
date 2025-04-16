package com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity;

import com.aexp.gcs.supplierprofile.profiledatamodel.constants.CompanySource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "profile_company", schema = "bprof")
public class ProfileCompany implements Serializable {
  @Id
  @Column(name = "profile_company_identifier")
  private UUID profileCompanyId;

  @Column(name = "profile_identifier")
  private UUID profileId;

  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Column(name = "type_text")
  private String typeText;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_code")
  private CompanySource sourceCode;

  @Column(name = "value_text")
  private String valueText;

  @Column(name = "unique_flag_indicator")
  private boolean uniqueFlagIndicator;

  @Column(name = "last_update_timestamp")
  private LocalDateTime lastUpdateTimestamp = LocalDateTime.now();

  @Column(name = "create_timestamp")
  private LocalDateTime createTimestamp = LocalDateTime.now();

  @Column(name = "last_update_source_name")
  private String lastUpdateBySourceName;

  @Column(name = "create_source_name")
  private String createBySourceName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_identifier", nullable = false, insertable = false, updatable = false)
  @ToString.Exclude
  private Profile profile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_company_detail_identifier", nullable = false, insertable = false, updatable = false)
  @ToString.Exclude
  private ProfileCompanyDetail profileCompanyDetail;

}
