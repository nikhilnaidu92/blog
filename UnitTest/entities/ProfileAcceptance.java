package com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converts;
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
import org.hibernate.annotations.Type;

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
@Table(name = "profile_acceptance", schema = "bprof")
@Converts({
  @Convert(attributeName = "json", converter = JsonBinaryType.class),
  @Convert(attributeName = "jsonb", converter = JsonBinaryType.class)
})
public class ProfileAcceptance implements Serializable {
  @Id
  @Column(name = "profile_acceptance_identifier")
  private UUID profileAcceptanceId;

  @Type(JsonType.class)
  @Column(name = "acceptance_json_data", columnDefinition = "json")
  private String acceptanceJsonData;

  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Column(name = "last_update_timestamp")
  private LocalDateTime lastUpdateTimestamp = LocalDateTime.now();

  @Column(name = "create_timestamp")
  private LocalDateTime createTimestamp = LocalDateTime.now();

  @Column(name = "last_update_source_name")
  private String lastUpdateBySourceName;

  @Column(name = "create_source_name")
  private String createBySourceName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_company_detail_identifier", nullable = false, insertable = false, updatable = false)
  @ToString.Exclude
  private ProfileCompanyDetail profileCompanyDetail;

}
