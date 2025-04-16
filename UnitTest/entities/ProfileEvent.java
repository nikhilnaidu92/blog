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
@Table(name = "profile_event", schema = "bprof")
@Converts({
  @Convert(attributeName = "json", converter = JsonBinaryType.class),
  @Convert(attributeName = "jsonb", converter = JsonBinaryType.class)
})
public class ProfileEvent implements Serializable {

  @Id
  @Column(name = "profile_event_identifier")
  private UUID profileEventId;

  @Column(name = "profile_identifier")
  private UUID profileId;

  @Column(name = "profile_company_detail_identifier")
  private UUID profileCompanyDetailId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "event_json_data")
  private String eventJsonData;

  @Column(name = "event_type_text")
  private String eventType;

  @Column(name = "event_sub_type_text")
  private String eventSubType;

  @Column(name = "last_update_timestamp")
  private LocalDateTime lastUpdateTimestamp = LocalDateTime.now();

  @Column(name = "create_timestamp")
  private LocalDateTime createTimestamp = LocalDateTime.now();

  @Column(name = "last_update_source_name")
  private String lastUpdateBySourceName;

  @Column(name = "create_source_name")
  private String createBySourceName;

  @Column(name = "event_que_in")
  private boolean eventQueueIndicator;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_identifier", nullable = false, insertable = false, updatable = false)
  @ToString.Exclude
  private Profile profile;

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
