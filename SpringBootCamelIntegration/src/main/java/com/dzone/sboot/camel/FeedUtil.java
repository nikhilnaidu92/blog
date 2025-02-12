package com.aexp.commercial.data.util;

import com.aexp.commercial.data.model.AmericanIndianOrAlaskaNative;
import com.aexp.commercial.data.model.Asian;
import com.aexp.commercial.data.model.BlackOrAfricanAmerican;
import com.aexp.commercial.data.model.BusinessOwnershipStatus;
import com.aexp.commercial.data.model.Ethnicity;
import com.aexp.commercial.data.model.HispanicOrLatino;
import com.aexp.commercial.data.model.NativeHawaiianOrOtherPacificIslander;
import com.aexp.commercial.data.model.PrincipalOwners;
import com.aexp.commercial.data.model.Race;

import java.util.ArrayList;
import java.util.List;

public class FeedServiceUtil {

  public static String getPrincipalOwnerEthnicity(PrincipalOwners principalOwners) {
    Ethnicity ethnicity = principalOwners.getEthnicity();
    if (ethnicity == null) {
      return "notProvided";
    }
    if (ethnicity.getHasDeclined()) {
      return "hasDeclined";
    }
    if (ethnicity.getIsNotHispanicOrLatino()) {
      return "isNotHispanicOrLatino";
    }
    if (ethnicity.getHispanicOrLatino() != null
      && (ethnicity.getHispanicOrLatino().getIsOptionSelected() || ethnicity.getHispanicOrLatino().getOther())) {
      return buildHispanicOrLatinoValue(ethnicity.getHispanicOrLatino());
    }
    return "notProvided";
  }

  private static String buildHispanicOrLatinoValue(HispanicOrLatino hispanicOrLatino) {
    StringBuilder value = new StringBuilder("isHispanicOrLatino");
    if (hispanicOrLatino.getIsCuban()) {
      value.append("|Cuban");
    }
    if (hispanicOrLatino.getIsMexican()) {
      value.append("|Mexican");
    }
    if (hispanicOrLatino.getIsPuertoRican()) {
      value.append("|PuertoRican");
    }
    if (hispanicOrLatino.getOther()) {
      value.append("|otherHol");
    }
    return value.toString();
  }

  public static String getPrincipalOwnerEthnicityFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getEthnicity() != null
      && principalOwners.getEthnicity().getHispanicOrLatino() != null
      && principalOwners.getEthnicity().getHispanicOrLatino().getOtherDetail() != null) {
      return principalOwners.getEthnicity().getHispanicOrLatino().getOtherDetail();
    }
    return null;
  }

  public static String getPrincipalOwnerRace(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() == null) {
      return "notProvided";
    }

    Race race = principalOwners.getRace();
    if (race.getHasDeclined()) {
      return "hasDeclined";
    }

    List<String> races = new ArrayList<>();
    if (race.getIsWhite()) {
      races.add("isWhite");
    }
    if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected()) {
      races.add(getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()));
    }
    if (race.getAsian() != null && (race.getAsian().getIsOptionSelected() || race.getAsian().getOther())) {
      races.add(getAsianRace(race.getAsian()));
    }
    if (race.getBlackOrAfricanAmerican() != null
      && (race.getBlackOrAfricanAmerican().getIsOptionSelected() || race.getBlackOrAfricanAmerican().getOther())) {
      races.add(getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()));
    }
    if (race.getNativeHawaiianOrOtherPacificIslander() != null
      && (race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()
      || race.getNativeHawaiianOrOtherPacificIslander().getOther())) {
      races.add(getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()));
    }
    return races.isEmpty() ? "notProvided" : String.join("|", races);
  }

  public static String getAsianRace(Asian asian) {
    StringBuilder value = new StringBuilder("isAsian");
    List<String> selectedOptions = new ArrayList<>();
    if (asian.getIsAsianIndian()) {
      selectedOptions.add("isAsianIndian");
    }
    if (asian.getIsChinese()) {
      selectedOptions.add("isChinese");
    }
    if (asian.getIsFilipino()) {
      selectedOptions.add("isFilipino");
    }
    if (asian.getIsJapanese()) {
      selectedOptions.add("isJapanese");
    }
    if (asian.getIsKorean()) {
      selectedOptions.add("isKorean");
    }
    if (asian.getIsVietnamese()) {
      selectedOptions.add("isVietnamese");
    }
    if (asian.getOther()) {
      selectedOptions.add("otherAsn");
    }
    // Combine all selected options with a pipe separator
    if (!selectedOptions.isEmpty()) {
      value.append("|").append(String.join("|", selectedOptions));
    }
    return value.toString();
  }

  public static String getBlackAfricanAmericanRace(BlackOrAfricanAmerican blackOrAfricanAmerican) {
    StringBuilder value = new StringBuilder("isBlackOrAfricanAmerican");
    List<String> selectedOptions = new ArrayList<>();
    if (blackOrAfricanAmerican.getIsAfricanAmerican()) {
      selectedOptions.add("isAfricanAmerican");
    }
    if (blackOrAfricanAmerican.getIsEthiopian()) {
      selectedOptions.add("isEthiopian");
    }
    if (blackOrAfricanAmerican.getIsHaitian()) {
      selectedOptions.add("isHaitian");
    }
    if (blackOrAfricanAmerican.getIsJamaican()) {
      selectedOptions.add("isJamaican");
    }
    if (blackOrAfricanAmerican.getIsNigerian()) {
      selectedOptions.add("isNigerian");
    }
    if (blackOrAfricanAmerican.getIsSomali()) {
      selectedOptions.add("isSomali");
    }
    if (blackOrAfricanAmerican.getOther()) {
      selectedOptions.add("otherBaa");
    }
    // Combine all selected options with a pipe separator
    if (!selectedOptions.isEmpty()) {
      value.append("|").append(String.join("|", selectedOptions));
    }
    return value.toString();
  }

  public static String getAmericanIndianAlaskaRace(AmericanIndianOrAlaskaNative americanIndianOrAlaskaNative) {
    return americanIndianOrAlaskaNative.getNameOfTribe() != null ? "inFreeFormAian" : null;
  }

  public static String getNativeHawaiianPacificIslanderRace(NativeHawaiianOrOtherPacificIslander nhpi) {
    StringBuilder value = new StringBuilder("isNativeHawaiianOrOtherPacificIslander");
    List<String> selectedOptions = new ArrayList<>();
    if (nhpi.getIsGuamanianOrChamorro()) {
      selectedOptions.add("isGuamanianOrChamorro");
    }
    if (nhpi.getIsNativeHawaiian()) {
      selectedOptions.add("isNativeHawaiian");
    }
    if (nhpi.getIsSamoan()) {
      selectedOptions.add("isSamoan");
    }
    if (nhpi.getOther()) {
      selectedOptions.add("otherNhpi");
    }
    // Combine all selected options with a pipe separator
    if (!selectedOptions.isEmpty()) {
      value.append("|").append(String.join("|", selectedOptions));
    }
    return value.toString();
  }

  public static String getPOAmericanIndianAlaskaText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null
      && principalOwners.getRace().getAmericanIndianOrAlaskaNative() != null) {
      return principalOwners.getRace().getAmericanIndianOrAlaskaNative().getNameOfTribe();
    }
    return null;
  }

  public static String getPOAsianFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null
      && principalOwners.getRace().getAsian() != null) {
      return principalOwners.getRace().getAsian().getOtherDetail();
    }
    return null;
  }

  public static String getPOAfricanAmericanFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null
      && principalOwners.getRace().getBlackOrAfricanAmerican() != null) {
      return principalOwners.getRace().getBlackOrAfricanAmerican().getOtherDetail();
    }
    return null;
  }

  public static String getPOPacificIslanderFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null
      && principalOwners.getRace().getNativeHawaiianOrOtherPacificIslander() != null) {
      return principalOwners.getRace().getNativeHawaiianOrOtherPacificIslander().getOtherDetail();
    }
    return null;
  }

  public static String getPOGenderFlag(PrincipalOwners principalOwners) {
    if (principalOwners.getGender() != null) {
      if (principalOwners.getGender().getHasDeclined()) {
        return "hasDeclined";
      }
      if (principalOwners.getGender().getSex() != null) {
        return "inFreeFormGend";
      }
    }
    return "notProvided";
  }

  public static String getPOGenderFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getGender() != null) {
      return principalOwners.getGender().getSex();
    }
    return null;
  }

  public static String getBusinessOwnershipStatus(BusinessOwnershipStatus businessOwnershipStatus) {
    if (businessOwnershipStatus == null) {
      return null;
    }
    if (businessOwnershipStatus.getHasDeclined()) {
      return "hasDeclined";
    }
    if (businessOwnershipStatus.getNoneOfTheseApply()) {
      return "noneOfTheseApply";
    }
    boolean isWomanOwned = businessOwnershipStatus.getIsWomanOwned();
    boolean isMinorityOwned = businessOwnershipStatus.getIsMinorityOwned();
    boolean isLGBTQIOwned = businessOwnershipStatus.getIsLGBTQIPlusOwned();
    // Build ownership statuses
    StringBuilder value = new StringBuilder();
    if (isWomanOwned) {
      value.append("isWomanOwned");
    }
    if (isMinorityOwned) {
      if (!value.isEmpty()) value.append("|");
      value.append("isMinorityOwned");
    }
    if (isLGBTQIOwned) {
      if (!value.isEmpty()) value.append("|");
      value.append("isLGBTQIPusOwned");
    }
    return !value.isEmpty() ? value.toString() : null;
  }

}
