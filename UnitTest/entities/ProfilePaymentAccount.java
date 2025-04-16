package com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "profile_payment_account", schema = "bprof")
@Converts({
  @Convert(attributeName = "json", converter = JsonBinaryType.class),
  @Convert(attributeName = "jsonb", converter = JsonBinaryType.class)
})
public class ProfilePaymentAccount implements Serializable {
  @Id
  @Column(name = "profile_payment_account_identifier")
  private UUID profilePaymentAccountId;

  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Column(name = "reference_identifier")
  private String referenceId;

  @Column(name = "verification_detail_case_identifier")
  private String verificationDetailCaseId;

  @Column(name = "verification_detail_recommendation_text")
  private String verificationDetailRecommendationText;

  @Column(name = "payment_instrument_identifier")
  private String paymentInstrumentId;

  @Column(name = "payment_instrument_source_name")
  private String paymentInstrumentSourceName;

  @Column(name = "payment_account_name")
  private String paymentAccountName;

  @Column(name = "logic_action_text")
  private String logicActionText;

  @Column(name = "duplicate_indicator")
  private boolean duplicateIndicator;

  @Column(name = "payment_method_code")
  private String paymentMethodCode;

  @Column(name = "payment_routing_number")
  private String paymentRoutingNumber;

  @Column(name = "payment_account_number")
  private String paymentAccountNumber;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "verification_detail_json_data")
  private String verificationDetailJsonData;

  @Column(name = "ip_address_extension_text")
  private String ipAddressExtensionText;

  @Column(name = "transaction_identifier")
  private String transactionId;

  @Column(name = "upload_type_text")
  private String uploadTypeText;

  @Column(name = "uploaded_by_name")
  private String uploadedByName;

  @Column(name = "account_type_text")
  private String accountTypeText;

  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "bank_nick_name")
  private String bankNickName;

  @Column(name = "nm_on_bank_account_text")
  private String nmOnBankAccountText;

  @Column(name = "iban_number")
  private String ibanNumber;

  @Column(name = "sort_code")
  private String sortCode;

  @Column(name = "swift_account_number")
  private String swiftAccountNumber;

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

  @JsonIgnore
  @OneToOne(mappedBy = "profilePaymentAccount", fetch = FetchType.LAZY)
  @ToString.Exclude
  private ProfileDemographics profileDemographics;

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return "Failed to map " + this.getClass().getName() + " to JSON.";
    }
  }
}
