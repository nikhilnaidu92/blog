package com.aexp.commercial.data.service;

import com.aexp.commercial.data.model.Address;
import com.aexp.commercial.data.model.OwnershipDemographicData;
import com.aexp.commercial.data.model.PrincipalOwners;
import com.aexp.commercial.data.model.db.DemographicDetailEntity;
import com.aexp.commercial.data.model.db.EmailRequestEntity;
import com.aexp.commercial.data.repository.DemographicDetailRepository;
import com.aexp.commercial.data.repository.EmailRequestRepository;
import com.aexp.commercial.data.triggers.DataFeedTrigger;
import com.aexp.commercial.data.util.BusinessOwnershipUtil;
import com.aexp.commercial.data.util.PrincipalOwnerUtil;
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
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class DataFeedService implements ResourceLoaderAware {

  private static final Logger log = LoggerFactory.getLogger(DataFeedTrigger.class);
  private static final String DEMOGRAPHIC_TEMPLATE_PATH = "classpath:templates/Demographic_Data_Feed_Template.xls";
  private static final String EMAIL_TEMPLATE_PATH = "classpath:templates/Email_Data_Feed_Template.xls";
  private ResourceLoader resourceLoader;
  private final DemographicDetailRepository demographicDetailRepository;
  private final EmailRequestRepository emailRequestRepository;
  private final HiPEDEncryptionService hiPEDEncryptionService;
  private final ObjectMapper mapper;

  public DataFeedService(DemographicDetailRepository demographicDetailRepository,
                         EmailRequestRepository emailRequestRepository,
                         HiPEDEncryptionService hiPEDEncryptionService) {
    this.demographicDetailRepository = demographicDetailRepository;
    this.emailRequestRepository = emailRequestRepository;
    this.hiPEDEncryptionService = hiPEDEncryptionService;
    this.mapper = new ObjectMapper();
  }

  @Override
  public void setResourceLoader(ResourceLoader resource) {
    this.resourceLoader = resource;
  }

  public String generateDemographicDataFile() {
    String fileName = "DEMOGRAPHIC_DATA.xls";
    UUID applicationId = null;
    try {
      List<DemographicDetailEntity> demographicDetailEntities =
        demographicDetailRepository.findDemographicDetailEntitiesBySentToBigDataIndicator(false);
      int startRow = 2;
      POIFSFileSystem poifsFileSystem = createDemographicWorkBookTemplate();
      HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem, true);
      Sheet sheetAt = hssfWorkbook.getSheetAt(0);
      Row startingRow = sheetAt.createRow(startRow);

      for (DemographicDetailEntity demographicDetailEntity : demographicDetailEntities) {
        applicationId = demographicDetailEntity.getApplicationIdentifier();
        startingRow.createCell(0).setCellValue(String.valueOf(demographicDetailEntity.getApplicationIdentifier()));
        startingRow.createCell(1).setCellValue(demographicDetailEntity.getReferenceNumber());
        startingRow.createCell(2).setCellValue(hiPEDEncryptionService.decryptString(demographicDetailEntity.getEntityIdentifier()));
        startingRow.createCell(3).setCellValue(demographicDetailEntity.getEntityTypeText());
        startingRow.createCell(4).setCellValue(String.valueOf(demographicDetailEntity.getEmailRequestIdentifier()));
        startingRow.createCell(5).setCellValue(demographicDetailEntity.getCompanyName());

        OwnershipDemographicData ownershipDemographicData;
        if (demographicDetailEntity.getOwnershipDemographicData() != null) {
          ownershipDemographicData =
            mapper.convertValue(demographicDetailEntity.getOwnershipDemographicData(), OwnershipDemographicData.class);
          /* Setting business ownership status value to the new column */
          if (ownershipDemographicData.getBusinessOwnershipStatus() != null) {
            startingRow.createCell(6).setCellValue(
              BusinessOwnershipUtil.getBusinessOwnershipStatus(ownershipDemographicData.getBusinessOwnershipStatus()));
          } else {
            startingRow.createCell(6).setCellValue("notProvided");
          }
          int poCount = ownershipDemographicData.getNumberOfPrincipalOwners();
          startingRow.createCell(7).setCellValue(poCount);

          /* Setting Principal owner ethnicity, race and gender */
          if (ownershipDemographicData.getPrincipalOwners() != null && !ownershipDemographicData.getPrincipalOwners().isEmpty()) {
            if (poCount >= 1 && ownershipDemographicData.getPrincipalOwners().get(0) != null) {
              PrincipalOwners principalOwners = ownershipDemographicData.getPrincipalOwners().get(0);
              if (ownershipDemographicData.getPrincipalOwners().get(0).getEthnicity() != null) {
                startingRow.createCell(8).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicity(principalOwners));
                startingRow.createCell(9).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicityFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(0).getRace() != null) {
                startingRow.createCell(10).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerRace(principalOwners));
                startingRow.createCell(11).setCellValue(PrincipalOwnerUtil.getPOAmericanIndianAlaskaText(principalOwners));
                startingRow.createCell(12).setCellValue(PrincipalOwnerUtil.getPOAsianFreeFormText(principalOwners));
                startingRow.createCell(13).setCellValue(PrincipalOwnerUtil.getPOAfricanAmericanFreeFormText(principalOwners));
                startingRow.createCell(14).setCellValue(PrincipalOwnerUtil.getPOPacificIslanderFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(0).getGender() != null) {
                startingRow.createCell(15).setCellValue(PrincipalOwnerUtil.getPOGenderFlag(principalOwners));
                startingRow.createCell(16).setCellValue(PrincipalOwnerUtil.getPOGenderFreeFormText(principalOwners));
              }
            } else {
              startingRow.createCell(8).setCellValue("notProvided");
            }

            if (poCount >= 2 && ownershipDemographicData.getPrincipalOwners().size() >= 2 &&
              ownershipDemographicData.getPrincipalOwners().get(1) != null) {
              PrincipalOwners principalOwners = ownershipDemographicData.getPrincipalOwners().get(1);
              if (ownershipDemographicData.getPrincipalOwners().get(1).getEthnicity() != null) {
                startingRow.createCell(17).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicity(principalOwners));
                startingRow.createCell(18).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicityFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(1).getRace() != null) {
                startingRow.createCell(19).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerRace(principalOwners));
                startingRow.createCell(20).setCellValue(PrincipalOwnerUtil.getPOAmericanIndianAlaskaText(principalOwners));
                startingRow.createCell(21).setCellValue(PrincipalOwnerUtil.getPOAsianFreeFormText(principalOwners));
                startingRow.createCell(22).setCellValue(PrincipalOwnerUtil.getPOAfricanAmericanFreeFormText(principalOwners));
                startingRow.createCell(23).setCellValue(PrincipalOwnerUtil.getPOPacificIslanderFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(1).getGender() != null) {
                startingRow.createCell(24).setCellValue(PrincipalOwnerUtil.getPOGenderFlag(principalOwners));
                startingRow.createCell(25).setCellValue(PrincipalOwnerUtil.getPOGenderFreeFormText(principalOwners));
              }
            } else {
              startingRow.createCell(17).setCellValue("notProvided");
            }

            if (poCount >= 3 && ownershipDemographicData.getPrincipalOwners().size() >= 3 &&
              ownershipDemographicData.getPrincipalOwners().get(2) != null) {
              PrincipalOwners principalOwners = ownershipDemographicData.getPrincipalOwners().get(2);
              if (ownershipDemographicData.getPrincipalOwners().get(2).getEthnicity() != null) {
                startingRow.createCell(26).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicity(principalOwners));
                startingRow.createCell(27).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicityFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(2).getRace() != null) {
                startingRow.createCell(28).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerRace(principalOwners));
                startingRow.createCell(29).setCellValue(PrincipalOwnerUtil.getPOAmericanIndianAlaskaText(principalOwners));
                startingRow.createCell(30).setCellValue(PrincipalOwnerUtil.getPOAsianFreeFormText(principalOwners));
                startingRow.createCell(31).setCellValue(PrincipalOwnerUtil.getPOAfricanAmericanFreeFormText(principalOwners));
                startingRow.createCell(32).setCellValue(PrincipalOwnerUtil.getPOPacificIslanderFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(2).getGender() != null) {
                startingRow.createCell(33).setCellValue(PrincipalOwnerUtil.getPOGenderFlag(principalOwners));
                startingRow.createCell(34).setCellValue(PrincipalOwnerUtil.getPOGenderFreeFormText(principalOwners));
              }
            } else {
              startingRow.createCell(26).setCellValue("notProvided");
            }

            if (poCount >= 4 && ownershipDemographicData.getPrincipalOwners().size() >= 4 &&
              ownershipDemographicData.getPrincipalOwners().get(3) != null) {
              PrincipalOwners principalOwners = ownershipDemographicData.getPrincipalOwners().get(3);
              if (ownershipDemographicData.getPrincipalOwners().get(3).getEthnicity() != null) {
                startingRow.createCell(35).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicity(principalOwners));
                startingRow.createCell(36).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerEthnicityFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(3).getRace() != null) {
                startingRow.createCell(37).setCellValue(PrincipalOwnerUtil.getPrincipalOwnerRace(principalOwners));
                startingRow.createCell(38).setCellValue(PrincipalOwnerUtil.getPOAmericanIndianAlaskaText(principalOwners));
                startingRow.createCell(39).setCellValue(PrincipalOwnerUtil.getPOAsianFreeFormText(principalOwners));
                startingRow.createCell(40).setCellValue(PrincipalOwnerUtil.getPOAfricanAmericanFreeFormText(principalOwners));
                startingRow.createCell(41).setCellValue(PrincipalOwnerUtil.getPOPacificIslanderFreeFormText(principalOwners));
              }
              if (ownershipDemographicData.getPrincipalOwners().get(3).getGender() != null) {
                startingRow.createCell(42).setCellValue(PrincipalOwnerUtil.getPOGenderFlag(principalOwners));
                startingRow.createCell(43).setCellValue(PrincipalOwnerUtil.getPOGenderFreeFormText(principalOwners));
              }
            } else {
              startingRow.createCell(35).setCellValue("notProvided");
            }
          }

          if (ownershipDemographicData.getAddress() != null) {
            Address address = ownershipDemographicData.getAddress();
            startingRow.createCell(50).setCellValue(address.getLine1());
            startingRow.createCell(51).setCellValue(address.getLine2());
            startingRow.createCell(52).setCellValue(address.getLine3());
            startingRow.createCell(53).setCellValue(address.getLine4());
            startingRow.createCell(54).setCellValue(address.getType());
            startingRow.createCell(55).setCellValue(address.getCity());
            startingRow.createCell(56).setCellValue(address.getState());
            startingRow.createCell(57).setCellValue(address.getPostalCode());
            startingRow.createCell(58).setCellValue(address.getAlphaCountryCode());
          }

          startingRow.createCell(59).setCellValue(ownershipDemographicData.getNaicsCode());
          startingRow.createCell(60).setCellValue(ownershipDemographicData.getEmployeeCount());
          startingRow.createCell(61).setCellValue(ownershipDemographicData.getTimeInBusiness());

        }
        startingRow.createCell(44).setCellValue(demographicDetailEntity.getLastUpdateTimestamp().toString());
        startingRow.createCell(45).setCellValue(demographicDetailEntity.getCreateTimestamp().toString());
        startingRow.createCell(46).setCellValue(demographicDetailEntity.getCreateSourceName());
        startingRow.createCell(47).setCellValue(demographicDetailEntity.getLastUpdateSourceName());
        startingRow.createCell(48).setCellValue(demographicDetailEntity.getAccountIdentifier());
        startingRow.createCell(49).setCellValue(demographicDetailEntity.getAccountTypeText());

        startingRow = sheetAt.createRow(++startRow);
      }

      FileOutputStream fileOut = new FileOutputStream(fileName);
      hssfWorkbook.write(fileOut);
      fileOut.close();
    } catch (Exception ex) {
      log.error("Exception occurred for {} due to:" + Arrays.toString(ex.getStackTrace()), applicationId);
    }
    return fileName;
  }

  public String generateEmailDataFile() {
    String fileName = "EMAIL_DATA.xls";
    UUID requestId = null;
    try {
      List<EmailRequestEntity> emailRequestEntities =
        emailRequestRepository.findEmailRequestEntitiesBySentToBigDataIndicator(false);
      int startRow = 1;
      POIFSFileSystem poifsFileSystem = createEmailWorkBookTemplate();
      HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem, true);
      Sheet sheetAt = hssfWorkbook.getSheetAt(0);
      Row startingRow = sheetAt.createRow(startRow);

      for (EmailRequestEntity emailRequestEntity : emailRequestEntities) {
        requestId = emailRequestEntity.getEmailRequestIdentifier();
        startingRow.createCell(0).setCellValue(String.valueOf(emailRequestEntity.getEmailRequestIdentifier()));
        startingRow.createCell(1).setCellValue(emailRequestEntity.getEmailTrackingIdentifier());
        startingRow.createCell(2).setCellValue(String.valueOf(emailRequestEntity.getApplicationIdentifier()));
        startingRow.createCell(3).setCellValue(emailRequestEntity.getReferenceNumber());
        startingRow.createCell(4).setCellValue(hiPEDEncryptionService.decryptString(emailRequestEntity.getEntityIdentifier()));
        startingRow.createCell(5).setCellValue(emailRequestEntity.getEntityTypeText());
        startingRow.createCell(6).setCellValue(emailRequestEntity.getEmailAddressText());
        startingRow.createCell(7).setCellValue(emailRequestEntity.getStatusText());
        startingRow.createCell(8).setCellValue(emailRequestEntity.getLastUpdateTimestamp().toString());
        startingRow.createCell(9).setCellValue(emailRequestEntity.getCreateTimestamp().toString());
        startingRow.createCell(10).setCellValue(emailRequestEntity.getCreateSourceName());
        startingRow.createCell(11).setCellValue(emailRequestEntity.getLastUpdateSourceName());
        startingRow.createCell(12).setCellValue(emailRequestEntity.getCompanyName());
        startingRow.createCell(13).setCellValue(emailRequestEntity.getAccountIdentifier());
        startingRow.createCell(14).setCellValue(emailRequestEntity.getCustomerFirstNm());
        startingRow.createCell(15).setCellValue(emailRequestEntity.getCustomerLastNm());
        startingRow.createCell(16).setCellValue(emailRequestEntity.getAccountTypeText());
        startingRow = sheetAt.createRow(++startRow);
      }
      FileOutputStream fileOut = new FileOutputStream(fileName);
      hssfWorkbook.write(fileOut);
      fileOut.close();
    } catch (Exception ex) {
      log.error("Exception occurred during Email feed file generation for {} due to:" + Arrays.toString(ex.getStackTrace()), requestId);
    }
    return fileName;
  }

  private POIFSFileSystem createDemographicWorkBookTemplate() throws IOException {
    Resource resource = resourceLoader.getResource(DEMOGRAPHIC_TEMPLATE_PATH);
    InputStream inputStream = resource.getInputStream();
    return new POIFSFileSystem(inputStream);
  }

  private POIFSFileSystem createEmailWorkBookTemplate() throws IOException {
    Resource resource = resourceLoader.getResource(EMAIL_TEMPLATE_PATH);
    InputStream inputStream = resource.getInputStream();
    return new POIFSFileSystem(inputStream);
  }

}
