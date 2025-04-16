package com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import jakarta.persistence.OneToOne;
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
@Table(name = "profile_demographics", schema = "bprof")
@Converts({
  @Convert(attributeName = "json", converter = JsonBinaryType.class),
  @Convert(attributeName = "jsonb", converter = JsonBinaryType.class)
})
public class ProfileDemographics implements Serializable {
  @Id
  @Column(name = "profile_demographics_identifier")
  private UUID profileDemographicsId;

  @Column(name = "profile_identifier")
  private UUID profileId;

  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Column(name = "profile_payment_account_identifier")
  private UUID profilePaymentAccountId;

  @Column(name = "address_line_1_text")
  private String addressLine1Text;

  @Column(name = "address_line_2_text")
  private String addressLine2Text;

  @Column(name = "address_line_3_text")
  private String addressLine3Text;

  @Column(name = "region_name")
  private String regionName;

  @Column(name = "region_code")
  private String regionCode;

  @Column(name = "country_name")
  private String countryName;

  @Column(name = "country_code")
  private String countryCode;

  @Column(name = "city_name")
  private String cityName;

  @Column(name = "postal_code")
  private String postalCode;

  @Column(name = "application_source_name")
  private String applicationSourceName;

  @Column(name = "address_type_name")
  private String addressTypeName;

  @Column(name = "telephone_number")
  private String telephoneNumber;

  @Column(name = "supplier_currency_name")
  private String supplierCurrencyName;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "profile_email_address_data")
  private String profileEmailAddressJsonData;

  @Column(name = "profile_full_address_text")
  private String profileFullAddressText;

  @Column(name = "business_name")
  private String businessName;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "adba_name_json_data")
  private String adbaNameJsonData;

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

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_payment_account_identifier", nullable = false, insertable = false, updatable = false)
  @ToString.Exclude
  private ProfilePaymentAccount profilePaymentAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_company_detail_identifier", nullable = false, insertable = false, updatable = false)
  @ToString.Exclude
  private ProfileCompanyDetail profileCompanyDetail;

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return "Failed to map " + this.getClass().getName() + " to JSON.";
    }
  }
}
