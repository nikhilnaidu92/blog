package com.aexp.commercial.data.service;

import com.aexp.commercial.data.model.Address;
import com.aexp.commercial.data.model.OwnershipDemographicData;
import com.aexp.commercial.data.model.PrincipalOwners;
import com.aexp.commercial.data.model.db.DemographicDetailEntity;
import com.aexp.commercial.data.model.db.EmailRequestEntity;
import com.aexp.commercial.data.model.enums.AccountType;
import com.aexp.commercial.data.model.enums.EntityType;
import com.aexp.commercial.data.repository.DemographicDetailRepository;
import com.aexp.commercial.data.repository.EmailRequestRepository;
import com.aexp.commercial.data.triggers.DataFeedTrigger;
import com.aexp.commercial.data.util.FeedServiceUtil;
import com.aexp.commercial.data.util.HiPEDUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataFeedService implements ResourceLoaderAware {
  private static final Logger log = LoggerFactory.getLogger(DataFeedTrigger.class);
  private static final String DEMOGRAPHIC_TEMPLATE_PATH = "classpath:templates/Demographic_Data_Feed_Template.xls";
  private static final String EMAIL_TEMPLATE_PATH = "classpath:templates/Email_Data_Feed_Template.xls";
  private final DemographicDetailRepository demographicDetailRepository;
  private final EmailRequestRepository emailRequestRepository;
  private final HiPEDUtil hiPEDUtil;
  private final ObjectMapper mapper;
  private ResourceLoader resourceLoader;

  public DataFeedService(DemographicDetailRepository demographicDetailRepository,
                         EmailRequestRepository emailRequestRepository,
                         HiPEDUtil hiPEDUtil) {
    this.demographicDetailRepository = demographicDetailRepository;
    this.emailRequestRepository = emailRequestRepository;
    this.hiPEDUtil = hiPEDUtil;
    this.mapper = new ObjectMapper();
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void generateDemographicDataFile() {
    String fileName = "DEMOGRAPHIC_DATA.xls";
    try (HSSFWorkbook hssfWorkBook = new HSSFWorkbook(createWorkBookTemplate(DEMOGRAPHIC_TEMPLATE_PATH), true)) {
      List<DemographicDetailEntity> demographicDetails =
        demographicDetailRepository.findDemographicDetailEntitiesBySentToBigDataIndicator(false);
      populateDemographicSheet(hssfWorkBook.getSheetAt(0), demographicDetails);
      saveWorkbookToFile(hssfWorkBook, fileName);
    } catch (Exception ex) {
      log.error("Error generating demographic data file: {}", ex.getMessage(), ex);
    }
  }

  public void generateEmailDataFile() {
    String fileName = "EMAIL_DATA.xls";
    try (HSSFWorkbook hssfWorkBook = new HSSFWorkbook(createWorkBookTemplate(EMAIL_TEMPLATE_PATH), true)) {
      List<EmailRequestEntity> emailRequests = emailRequestRepository.findEmailRequestEntitiesBySentToBigDataIndicator(false);
      populateEmailSheet(hssfWorkBook.getSheetAt(0), emailRequests);
      saveWorkbookToFile(hssfWorkBook, fileName);
    } catch (Exception ex) {
      log.error("Error generating email data file: {}", ex.getMessage(), ex);
    }
  }

  private void populateDemographicSheet(Sheet sheet, List<DemographicDetailEntity> demographicDetails) {
    int rowIndex = 1;
    for (DemographicDetailEntity demographicDetailEntity : demographicDetails) {
      Row row = sheet.createRow(rowIndex++);
      setDemographicRowData(row, demographicDetailEntity);
      demographicDetailEntity.setLastUpdateTimestamp(LocalDateTime.now());
      demographicDetailEntity.setSentToBigDataIndicator(true);
      demographicDetailRepository.save(demographicDetailEntity);
    }
  }

  private void setDemographicRowData(Row row, DemographicDetailEntity demographicDetail) {
    try {
      row.createCell(0).setCellValue(String.valueOf(demographicDetail.getApplicationIdentifier()));
      row.createCell(1).setCellValue(demographicDetail.getReferenceNumber());
      row.createCell(2).setCellValue(demographicDetail.getEntityTypeText());
      if (demographicDetail.getEmailRequestIdentifier() != null) {
        row.createCell(3).setCellValue(String.valueOf(demographicDetail.getEmailRequestIdentifier()));
      }
      row.createCell(4).setCellValue(demographicDetail.getCompanyName());
      OwnershipDemographicData ownershipData =
        mapper.convertValue(demographicDetail.getOwnershipDemographicData(), OwnershipDemographicData.class);
      if (ownershipData != null) {
        row.createCell(5).setCellValue(String.valueOf(demographicDetail.getOwnershipDemographicData()));
        setOwnershipData(row, demographicDetail, ownershipData);
      }
      row.createCell(52).setCellValue(demographicDetail.getLastUpdateTimestamp().toString());
      row.createCell(53).setCellValue(demographicDetail.getCreateTimestamp().toString());
      row.createCell(54).setCellValue(demographicDetail.getCreateSourceName());
      row.createCell(55).setCellValue(demographicDetail.getLastUpdateSourceName());
      row.createCell(56).setCellValue(demographicDetail.getAccountTypeText());
      if (demographicDetail.getAccountTypeText() != null
        && (demographicDetail.getAccountTypeText().equals(AccountType.CLIENT_ORIGIN_ID.name())
        || demographicDetail.getAccountTypeText().equals(AccountType.MCA.name()))
        && demographicDetail.getAccountIdentifier() != null) {
        row.createCell(67).setCellValue(demographicDetail.getAccountIdentifier());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setOwnershipData(Row row, DemographicDetailEntity demographicDetail, OwnershipDemographicData ownershipData) {
    try {
      int poCount = ownershipData.getNumberOfPrincipalOwners();
      row.createCell(6).setCellValue(poCount);
      row.createCell(7).setCellValue(ownershipData.getBusinessOwnershipStatus() != null
        ? FeedServiceUtil.getBusinessOwnershipStatus(ownershipData.getBusinessOwnershipStatus())
        : "notProvided");

      if (demographicDetail.getAccountTypeText() != null && demographicDetail.getAccountTypeText().equals(AccountType.CM15.name())
        && demographicDetail.getAccountIdentifier() != null) {
        row.createCell(8).setCellValue(demographicDetail.getAccountIdentifier());
      }
      if (demographicDetail.getEntityTypeText().equals(EntityType.TIN.name()) && demographicDetail.getEntityIdentifier() != null) {
        row.createCell(9).setCellValue(hiPEDUtil.decryptString(demographicDetail.getEntityIdentifier()));
      } else if (demographicDetail.getEntityTypeText().equals(EntityType.EIN.name()) && demographicDetail.getEntityIdentifier() != null) {
        row.createCell(10).setCellValue(hiPEDUtil.decryptString(demographicDetail.getEntityIdentifier()));
      } else if (demographicDetail.getEntityTypeText().equals(EntityType.PASSPORT.name())
        && demographicDetail.getEntityIdentifier() != null) {
        row.createCell(11).setCellValue(hiPEDUtil.decryptString(demographicDetail.getEntityIdentifier()));
      } else if (demographicDetail.getEntityTypeText().equals(EntityType.SSN.name()) && demographicDetail.getEntityIdentifier() != null) {
        row.createCell(12).setCellValue(hiPEDUtil.decryptString(demographicDetail.getEntityIdentifier()));
      } else if (demographicDetail.getEntityTypeText().equals(EntityType.CRO_ID.name())
        && demographicDetail.getEntityIdentifier() != null) {
        row.createCell(66).setCellValue(hiPEDUtil.decryptString(demographicDetail.getEntityIdentifier()));
      }
      row.createCell(13).setCellValue(ownershipData.getNaicsCode());
      row.createCell(14).setCellValue(ownershipData.getEmployeeCount());
      row.createCell(15).setCellValue(ownershipData.getTimeInBusiness());

      // Populate Principal Owners
      for (int i = 0; i < poCount; i++) {
        if (ownershipData.getPrincipalOwners().size() > i) {
          PrincipalOwners owner = ownershipData.getPrincipalOwners().get(i);
          setPrincipalOwnerData(row, owner, i);
        } else {
          row.createCell(16 + i * 9).setCellValue("notProvided");
        }
      }

      if (ownershipData.getAddress() != null) {
        setAddressData(row, ownershipData.getAddress());
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setPrincipalOwnerData(Row row, PrincipalOwners owner, int index) {
    int baseCell = 16 + index * 9;
    row.createCell(baseCell).setCellValue(FeedServiceUtil.getPrincipalOwnerEthnicity(owner));
    row.createCell(baseCell + 1).setCellValue(FeedServiceUtil.getPrincipalOwnerEthnicityFreeFormText(owner));
    row.createCell(baseCell + 2).setCellValue(FeedServiceUtil.getPrincipalOwnerRace(owner));
    row.createCell(baseCell + 3).setCellValue(FeedServiceUtil.getPOAmericanIndianAlaskaText(owner));
    row.createCell(baseCell + 4).setCellValue(FeedServiceUtil.getPOAsianFreeFormText(owner));
    row.createCell(baseCell + 5).setCellValue(FeedServiceUtil.getPOAfricanAmericanFreeFormText(owner));
    row.createCell(baseCell + 6).setCellValue(FeedServiceUtil.getPOPacificIslanderFreeFormText(owner));
    row.createCell(baseCell + 7).setCellValue(FeedServiceUtil.getPOGenderFlag(owner));
    row.createCell(baseCell + 8).setCellValue(FeedServiceUtil.getPOGenderFreeFormText(owner));
  }

  private void setAddressData(Row row, Address address) {
    row.createCell(57).setCellValue(address.getLine1());
    row.createCell(58).setCellValue(address.getLine2());
    row.createCell(59).setCellValue(address.getLine3());
    row.createCell(60).setCellValue(address.getLine4());
    row.createCell(61).setCellValue(address.getType());
    row.createCell(62).setCellValue(address.getCity());
    row.createCell(63).setCellValue(address.getState());
    row.createCell(64).setCellValue(address.getPostalCode());
    row.createCell(65).setCellValue(address.getAlphaCountryCode());
  }

  private void populateEmailSheet(Sheet sheet, List<EmailRequestEntity> emailRequests) {
    int rowIndex = 1;
    for (EmailRequestEntity emailRequestEntity : emailRequests) {
      Row row = sheet.createRow(rowIndex++);
      setEmailRowData(row, emailRequestEntity);
      emailRequestEntity.setLastUpdateTimestamp(LocalDateTime.now());
      emailRequestEntity.setSentToBigDataIndicator(true);
      emailRequestRepository.save(emailRequestEntity);
    }
  }

  private void setEmailRowData(Row row, EmailRequestEntity emailData) {
    try {
      row.createCell(0).setCellValue(String.valueOf(emailData.getEmailRequestIdentifier()));
      row.createCell(1).setCellValue(emailData.getEmailTrackingIdentifier());
      if (emailData.getApplicationIdentifier() != null) {
        row.createCell(2).setCellValue(String.valueOf(emailData.getApplicationIdentifier()));
      }
      row.createCell(3).setCellValue(emailData.getReferenceNumber());
      row.createCell(4).setCellValue(emailData.getEntityTypeText());
      row.createCell(5).setCellValue(emailData.getEmailAddressText());
      row.createCell(6).setCellValue(emailData.getStatusText());
      row.createCell(7).setCellValue(emailData.getLastUpdateTimestamp().toString());
      row.createCell(8).setCellValue(emailData.getCreateTimestamp().toString());
      row.createCell(9).setCellValue(emailData.getCreateSourceName());
      row.createCell(10).setCellValue(emailData.getLastUpdateSourceName());
      row.createCell(11).setCellValue(emailData.getCompanyName());
      if (emailData.getAccountTypeText() != null && emailData.getAccountTypeText().equals(AccountType.CM15.name())
        && emailData.getAccountIdentifier() != null) {
        row.createCell(16).setCellValue(emailData.getAccountIdentifier());
      } else {
        row.createCell(12).setCellValue(emailData.getAccountIdentifier());
      }
      row.createCell(13).setCellValue(emailData.getCustomerFirstNm());
      row.createCell(14).setCellValue(emailData.getCustomerLastNm());
      row.createCell(15).setCellValue(emailData.getAccountTypeText());
      if (emailData.getEntityTypeText().equals(EntityType.TIN.name()) && emailData.getEntityIdentifier() != null) {
        row.createCell(17).setCellValue(hiPEDUtil.decryptString(emailData.getEntityIdentifier()));
      } else if (emailData.getEntityTypeText().equals(EntityType.EIN.name()) && emailData.getEntityIdentifier() != null) {
        row.createCell(18).setCellValue(hiPEDUtil.decryptString(emailData.getEntityIdentifier()));
      } else if (emailData.getEntityTypeText().equals(EntityType.PASSPORT.name()) && emailData.getEntityIdentifier() != null) {
        row.createCell(19).setCellValue(hiPEDUtil.decryptString(emailData.getEntityIdentifier()));
      } else if (emailData.getEntityTypeText().equals(EntityType.SSN.name()) && emailData.getEntityIdentifier() != null) {
        row.createCell(20).setCellValue(hiPEDUtil.decryptString(emailData.getEntityIdentifier()));
      } else if (emailData.getEntityTypeText().equals(EntityType.CRO_ID.name()) && emailData.getEntityIdentifier() != null) {
        row.createCell(22).setCellValue(hiPEDUtil.decryptString(emailData.getEntityIdentifier()));
      }
      row.createCell(21).setCellValue(emailData.getNonSensitiveDataIndicator());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private POIFSFileSystem createWorkBookTemplate(String templatePath) throws IOException {
    Resource resource = resourceLoader.getResource(templatePath);
    return new POIFSFileSystem(resource.getInputStream());
  }

  private void saveWorkbookToFile(HSSFWorkbook workbook, String fileName) throws IOException {
    try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
      workbook.write(fileOutputStream);
    }
  }
}
