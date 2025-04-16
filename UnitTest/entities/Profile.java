package com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converts;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SuperBuilder(toBuilder = true)
@ToString
@Entity
@Table(name = "profile", schema = "bprof")
@Converts({
  @Convert(attributeName = "json", converter = JsonBinaryType.class),
  @Convert(attributeName = "jsonb", converter = JsonBinaryType.class)
})
public class Profile implements Serializable {
  @Id
  @Column(name = "profile_identifier")
  private UUID profileId;

  @Type(JsonType.class)
  @Column(name = "c360_mailing_address_data", columnDefinition = "json")
  private String c360MailingAddressJsonData;

  @Type(JsonType.class)
  @Column(name = "c360_company_contact_data", columnDefinition = "json")
  private String c360CompanyContactJsonData;

  @Column(name = "confidence_source_text")
  private String confidenceSourceTx;

  @Column(name = "confidence_code")
  private String confidenceCode;

  @Column(name = "create_timestamp")
  private LocalDateTime createTimestamp = LocalDateTime.now();

  @Column(name = "last_update_timestamp")
  private LocalDateTime lastUpdateTimestamp = LocalDateTime.now();

  @JsonIgnore
  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<ProfileDemographics> profileDemographics;

  @JsonIgnore
  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<ProfileEvent> profileEventList;

  @JsonIgnore
  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<ProfileCompanyDetail> profileCompanyDetail;

  @JsonIgnore
  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<ProfileCompany> profileCompanyList;

  @JsonIgnore
  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<ProfileContact> profileContactList;

}
