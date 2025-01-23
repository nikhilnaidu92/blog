package com.aexp.commercial.data.util;

import com.aexp.commercial.data.model.AmericanIndianOrAlaskaNative;
import com.aexp.commercial.data.model.Asian;
import com.aexp.commercial.data.model.BlackOrAfricanAmerican;
import com.aexp.commercial.data.model.Ethnicity;
import com.aexp.commercial.data.model.NativeHawaiianOrOtherPacificIslander;
import com.aexp.commercial.data.model.PrincipalOwners;
import com.aexp.commercial.data.model.Race;

public class PrincipalOwnerUtil {

  public static String getPrincipalOwnerEthnicity(PrincipalOwners principalOwners) {
    Ethnicity ethnicity = principalOwners.getEthnicity();
    if (ethnicity.getHasDeclined()) {
      return "hasDeclined";
    } else if (ethnicity.getIsNotHispanicOrLatino()) {
      return "isNotHispanicOrLatino";
    } else if (ethnicity.getHispanicOrLatino().getIsOptionSelected()) {
      if (ethnicity.getHispanicOrLatino().getIsCuban() && ethnicity.getHispanicOrLatino().getIsMexican() &&
        ethnicity.getHispanicOrLatino().getIsPuertoRican() && ethnicity.getHispanicOrLatino().getOther()) {
        return "isHispanicOrLatino|Cuban|Mexican|PuertoRican|otherHol";
      }

      if (ethnicity.getHispanicOrLatino().getIsCuban() && ethnicity.getHispanicOrLatino().getIsMexican() &&
        ethnicity.getHispanicOrLatino().getIsPuertoRican()) {
        return "isHispanicOrLatino|Cuban|Mexican|PuertoRican";
      } else if (ethnicity.getHispanicOrLatino().getIsCuban() && ethnicity.getHispanicOrLatino().getIsMexican() &&
        ethnicity.getHispanicOrLatino().getOther()) {
        return "isHispanicOrLatino|Cuban|Mexican|otherHol";
      } else if (ethnicity.getHispanicOrLatino().getIsCuban() && ethnicity.getHispanicOrLatino().getOther() &&
        ethnicity.getHispanicOrLatino().getIsPuertoRican()) {
        return "isHispanicOrLatino|Cuban|PuertoRican|otherHol";
      } else if (ethnicity.getHispanicOrLatino().getIsMexican() && ethnicity.getHispanicOrLatino().getIsPuertoRican() &&
        ethnicity.getHispanicOrLatino().getOther()) {
        return "isHispanicOrLatino|Mexican|PuertoRican|otherHol";
      }

      if (ethnicity.getHispanicOrLatino().getIsCuban() && ethnicity.getHispanicOrLatino().getIsMexican()) {
        return "isHispanicOrLatino|Cuban|Mexican";
      } else if (ethnicity.getHispanicOrLatino().getIsMexican() && ethnicity.getHispanicOrLatino().getIsPuertoRican()) {
        return "isHispanicOrLatino|Mexican|PuertoRican";
      } else if (ethnicity.getHispanicOrLatino().getIsCuban() && ethnicity.getHispanicOrLatino().getIsPuertoRican()) {
        return "isHispanicOrLatino|Cuban|PuertoRican";
      }

      if (ethnicity.getHispanicOrLatino().getIsCuban()) {
        return "isHispanicOrLatino|Cuban";
      } else if (ethnicity.getHispanicOrLatino().getIsMexican()) {
        return "isHispanicOrLatino|Mexican";
      } else if (ethnicity.getHispanicOrLatino().getIsPuertoRican()) {
        return "isHispanicOrLatino|PuertoRican";
      } else if (ethnicity.getHispanicOrLatino().getOther()) {
        return "isHispanicOrLatino|otherHol";
      }

    } else {
      return "notProvided";
    }
    return null;
  }

  public static String getPrincipalOwnerEthnicityFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getEthnicity() != null && principalOwners.getEthnicity().getHispanicOrLatino() != null &&
      principalOwners.getEthnicity().getHispanicOrLatino().getIsOptionSelected()) {
      return principalOwners.getEthnicity().getHispanicOrLatino().getOtherDetail();
    }
    return null;
  }

  public static String getPrincipalOwnerRace(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null) {
      Race race = principalOwners.getRace();
      if (race.getHasDeclined()) {
        return "hasDeclined";
      }

      /* All 5 combinations */
      if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" + getAsianRace(race.getAsian()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      }

      /* 4 set combinations */
      if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getAsianRace(race.getAsian()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getAsianRace(race.getAsian()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|isWhite";
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getAsianRace(race.getAsian()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      } else if (race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAsianRace(race.getAsian()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      }

      /* 3 set combinations */
      if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected() && race.getBlackOrAfricanAmerican() != null &&
        race.getBlackOrAfricanAmerican().getIsOptionSelected()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" + getAsianRace(race.getAsian()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican());
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected() && race.getNativeHawaiianOrOtherPacificIslander() != null &&
        race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" + getAsianRace(race.getAsian()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected() && race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" + getAsianRace(race.getAsian()) + "|isWhite";
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() && race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "isWhite";
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      } else if (race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
        return getAsianRace(race.getAsian()) + "|" + getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else if (race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAsianRace(race.getAsian()) + "|" + getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|isWhite";
      } else if (race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAsianRace(race.getAsian()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      } else if (race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      }

      /* 2 Set combinations */
      if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getAsian() != null && race.getAsian().getIsOptionSelected()) {
        return
          getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" + getAsianRace(race.getAsian());
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican());
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()) + "|isWhite";
      } else if (race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected()) {
        return getAsianRace(race.getAsian()) + "|" +
          getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican());
      } else if (race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
        return getAsianRace(race.getAsian()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else if (race.getAsian() != null && race.getAsian().getIsOptionSelected() &&
        race.getIsWhite()) {
        return getAsianRace(race.getAsian()) + "|isWhite";
      } else if (race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
        return getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|" +
          getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else if (race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected() &&
        race.getIsWhite() != null) {
        return getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()) + "|isWhite";
      } else if (race.getNativeHawaiianOrOtherPacificIslander() != null &&
        race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected() && race.getIsWhite()) {
        return getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()) + "|isWhite";
      }
      /* Single item */
      if (Boolean.TRUE.equals(race.getIsWhite())) {
        return "isWhite";
      } else if (race.getAsian() != null) {
        return getAsianRace(race.getAsian());
      } else if (race.getAmericanIndianOrAlaskaNative() != null) {
        return getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative());
      } else if (race.getBlackOrAfricanAmerican() != null) {
        return getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican());
      } else if (race.getNativeHawaiianOrOtherPacificIslander() != null) {
        return getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander());
      } else {
        return "notProvided";
      }

    }
    return null;
  }

  public static String getAsianRace(Asian asian) {
    String value = null;
    if (asian.getIsOptionSelected()) {
      value = "isAsian";

      /*6 Set combinations */
      if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() &&
        asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() &&
        asian.getIsKorean() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isJapanese|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|isJapanese|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese() &&
        asian.getOther()) {
        return value + "|isChinese|isFilipino|isJapanese|isKorean|isVietnamese|otherAsn";
      }

      /* 5 set combinations */
      if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isJapanese|isKorean";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() &&
        asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isJapanese|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isJapanese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean() &&
        asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean() &&
        asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean() &&
        asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsKorean() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() &&
        asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isFilipino|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese() &&
        asian.getIsKorean() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsKorean() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese() && asian.getIsKorean() &&
        asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isJapanese|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isChinese|isFilipino|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isChinese|isFilipino|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isChinese|isFilipino|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isChinese|isFilipino|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isChinese|isJapanese|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isFilipino|isJapanese|isKorean|isVietnamese|otherAsn";
      }

      /*4 set combinations*/
      if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isJapanese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isKorean|";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isFilipino|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isFilipino|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isChinese|isJapanese|isKorean";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isJapanese|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isJapanese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isFilipino|isJapanese|isKorean";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isFilipino|isJapanese|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|isJapanese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isFilipino|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isAsianIndian|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isChinese|isFilipino|isJapanese|isKorean";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsVietnamese()) {
        return value + "|isChinese|isFilipino|isJapanese|isVietnamese";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese() && asian.getOther()) {
        return value + "|isChinese|isFilipino|isJapanese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isChinese|isFilipino|isKorean|isVietnamese";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isChinese|isFilipino|isKorean|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isChinese|isFilipino|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isChinese|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isChinese|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsJapanese() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isChinese|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isChinese|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isFilipino|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isFilipino|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isFilipino|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isFilipino|isKorean|isVietnamese|otherAsn";
      } else if (asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isJapanese|isKorean|isVietnamese|otherAsn";
      }

      /* 3 set combinations */
      if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsFilipino()) {
        return value + "|isAsianIndian|isChinese|isFilipino";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsJapanese()) {
        return value + "|isAsianIndian|isChinese|isJapanese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isChinese|isKorean";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isChinese|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsChinese() && asian.getOther()) {
        return value + "|isAsianIndian|isChinese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsJapanese()) {
        return value + "|isAsianIndian|isFilipino|isJapanese";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isFilipino|isKorean";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isFilipino|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino() && asian.getOther()) {
        return value + "|isAsianIndian|isFilipino|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isJapanese|isKorean";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isJapanese|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese() && asian.getOther()) {
        return value + "|isAsianIndian|isJapanese|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isKorean|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isAsianIndian|isKorean|otherAsn";
      } else if (asian.getIsAsianIndian() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isAsianIndian|isVietnamese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsJapanese()) {
        return value + "|isChinese|isFilipino|isJapanese";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsKorean()) {
        return value + "|isChinese|isFilipino|isKorean";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getIsVietnamese()) {
        return value + "|isChinese|isFilipino|isVietnamese";
      } else if (asian.getIsChinese() && asian.getIsFilipino() && asian.getOther()) {
        return value + "|isChinese|isFilipino|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isChinese|isJapanese|isKorean";
      } else if (asian.getIsChinese() && asian.getIsJapanese() && asian.getIsVietnamese()) {
        return value + "|isChinese|isJapanese|isVietnamese";
      } else if (asian.getIsChinese() && asian.getIsJapanese() && asian.getOther()) {
        return value + "|isChinese|isJapanese|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isChinese|isKorean|isVietnamese";
      } else if (asian.getIsChinese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isChinese|isKorean|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isChinese|isVietnamese|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isFilipino|isJapanese|isKorean";
      } else if (asian.getIsFilipino() && asian.getIsJapanese() && asian.getIsVietnamese()) {
        return value + "|isFilipino|isJapanese|isVietnamese";
      } else if (asian.getIsFilipino() && asian.getIsJapanese() && asian.getOther()) {
        return value + "|isFilipino|isJapanese|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isFilipino|isKorean|isVietnamese";
      } else if (asian.getIsFilipino() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isFilipino|isKorean|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isFilipino|isVietnamese|otherAsn";
      } else if (asian.getIsJapanese() && asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isJapanese|isKorean|isVietnamese";
      } else if (asian.getIsJapanese() && asian.getIsKorean() && asian.getOther()) {
        return value + "|isJapanese|isKorean|otherAsn";
      } else if (asian.getIsJapanese() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isJapanese|isVietnamese|otherAsn";
      } else if (asian.getIsKorean() && asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isKorean|isVietnamese|otherAsn";
      }

      /* 2 set combinations */
      if (asian.getIsAsianIndian() && asian.getIsChinese()) {
        return value + "|isAsianIndian|isChinese";
      } else if (asian.getIsAsianIndian() && asian.getIsFilipino()) {
        return value + "|isAsianIndian|isFilipino";
      } else if (asian.getIsAsianIndian() && asian.getIsJapanese()) {
        return value + "|isAsianIndian|isJapanese";
      } else if (asian.getIsAsianIndian() && asian.getIsKorean()) {
        return value + "|isAsianIndian|isKorean";
      } else if (asian.getIsAsianIndian() && asian.getIsVietnamese()) {
        return value + "|isAsianIndian|isVietnamese";
      } else if (asian.getIsAsianIndian() && asian.getOther()) {
        return value + "|isAsianIndian|otherAsn";
      } else if (asian.getIsChinese() && asian.getIsFilipino()) {
        return value + "|isChinese|isFilipino";
      } else if (asian.getIsChinese() && asian.getIsJapanese()) {
        return value + "|isChinese|isJapanese";
      } else if (asian.getIsChinese() && asian.getIsKorean()) {
        return value + "|isChinese|isKorean";
      } else if (asian.getIsChinese() && asian.getIsVietnamese()) {
        return value + "|isChinese|isVietnamese";
      } else if (asian.getIsChinese() && asian.getOther()) {
        return value + "|isChinese|otherAsn";
      } else if (asian.getIsFilipino() && asian.getIsJapanese()) {
        return value + "|isFilipino|isJapanese";
      } else if (asian.getIsFilipino() && asian.getIsKorean()) {
        return value + "|isFilipino|isKorean";
      } else if (asian.getIsFilipino() && asian.getIsVietnamese()) {
        return value + "|isFilipino|isVietnamese";
      } else if (asian.getIsFilipino() && asian.getOther()) {
        return value + "|isFilipino|otherAsn";
      } else if (asian.getIsJapanese() && asian.getIsKorean()) {
        return value + "|isJapanese|isKorean";
      } else if (asian.getIsJapanese() && asian.getIsVietnamese()) {
        return value + "|isJapanese|isVietnamese";
      } else if (asian.getIsJapanese() && asian.getOther()) {
        return value + "|isJapanese|otherAsn";
      } else if (asian.getIsKorean() && asian.getIsVietnamese()) {
        return value + "|isKorean|isVietnamese";
      } else if (asian.getIsKorean() && asian.getOther()) {
        return value + "|isKorean|otherAsn";
      } else if (asian.getIsVietnamese() && asian.getOther()) {
        return value + "|isVietnamese|otherAsn";
      }

      /* Single set */
      if (asian.getIsAsianIndian()) {
        return value + "isAsian|isAsianIndian";
      } else if (asian.getIsKorean()) {
        return value + "isAsian|isKorean";
      } else if (asian.getIsChinese()) {
        return value + "isAsian|isChinese";
      } else if (asian.getOther()) {
        return value + "isAsian|otherAsn";
      } else if (asian.getIsJapanese()) {
        return value + "isAsian|isJapanese";
      } else if (asian.getIsFilipino()) {
        return value + "isAsian|isFilipino";
      } else if (asian.getIsVietnamese()) {
        return value + "isAsian|isVietnamese";
      }
    }
    return value;
  }

  public static String getBlackAfricanAmericanRace(BlackOrAfricanAmerican blackOrAfricanAmerican) {
    String value = "notProvided";
    if (blackOrAfricanAmerican.getIsOptionSelected()) {
      value = "isBlackOrAfricanAmerican";

      /* 6 set combinations */
      if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|isJamaican|isNigerian|isSomali|otherBaa";
      }

      /* 5 set combinations */
      if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isJamaican|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isHaitian|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isJamaican|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|isJamaican|isNigerian|isSomali|otherBaa";
      }

      if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isNigerian|";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isHaitian|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isEthiopian|isHaitian|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isHaitian|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isHaitian|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsSomali() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isHaitian|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|isNigerian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isJamaican|isNigerian|isSomali|otherBaa";
      }

      if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsHaitian()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isAfricanAmerican|isEthiopian|isJamaican";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isEthiopian|isNigerian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isEthiopian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isEthiopian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isAfricanAmerican|isHaitian|isJamaican";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isHaitian|isNigerian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isHaitian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isHaitian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsSomali() &&
        blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isEthiopian|isHaitian|isJamaican";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() &&
        blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isEthiopian|isHaitian|isNigerian";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isHaitian|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isHaitian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isEthiopian|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsNigerian() &&
        blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() &&
        blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isHaitian|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isHaitian|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isHaitian|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isJamaican|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isJamaican|isSomali|otherBaa";
      } else if (blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isNigerian|isSomali|otherBaa";
      }

      if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsEthiopian()) {
        return value + "|isAfricanAmerican|isEthiopian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsHaitian()) {
        return value + "|isAfricanAmerican|isHaitian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isAfricanAmerican|isJamaican";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isAfricanAmerican|isSomali";
      } else if (blackOrAfricanAmerican.getIsAfricanAmerican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isAfricanAmerican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsHaitian()) {
        return value + "|isEthiopian|isHaitian";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isEthiopian|isJamaican";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isEthiopian|isNigerian";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isEthiopian|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isEthiopian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isHaitian|isJamaican";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isHaitian|isNigerian";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isHaitian|isSomali";
      } else if (blackOrAfricanAmerican.getIsHaitian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isHaitian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isJamaican|isNigerian";
      } else if (blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isJamaican|isSomali";
      } else if (blackOrAfricanAmerican.getIsJamaican() && blackOrAfricanAmerican.getOther()) {
        return value + "|isJamaican|otherBaa";
      } else if (blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsNigerian() && blackOrAfricanAmerican.getOther()) {
        return value + "|isNigerian|otherBaa";
      } else if (blackOrAfricanAmerican.getIsSomali() && blackOrAfricanAmerican.getOther()) {
        return value + "|isSomali|otherBaa";
      }

      if (blackOrAfricanAmerican.getIsAfricanAmerican()) {
        return value + "|isAfricanAmerican";
      } else if (blackOrAfricanAmerican.getIsNigerian()) {
        return value + "|isAfricanAmerican|isEthiopian|isHaitian|isJamaican|isNigerian|isSomali";
      } else if (blackOrAfricanAmerican.getIsEthiopian()) {
        return value + "|isEthiopian";
      } else if (blackOrAfricanAmerican.getOther()) {
        return value + "|otherBaa";
      } else if (blackOrAfricanAmerican.getIsJamaican()) {
        return value + "|isJamaican";
      } else if (blackOrAfricanAmerican.getIsHaitian()) {
        return value + "|isHaitian";
      } else if (blackOrAfricanAmerican.getIsSomali()) {
        return value + "|isSomali";
      }
    }
    return value;
  }

  public static String getAmericanIndianAlaskaRace(AmericanIndianOrAlaskaNative americanIndianOrAlaskaNative) {
    if (americanIndianOrAlaskaNative.getIsOptionSelected() && americanIndianOrAlaskaNative.getNameOfTribe() != null) {
      return "inFreeFormAian";
    }
    return null;
  }

  public static String getNativeHawaiianPacificIslanderRace(NativeHawaiianOrOtherPacificIslander nativeHawaiianOrOtherPacificIslander) {
    String value = null;
    if (nativeHawaiianOrOtherPacificIslander.getIsOptionSelected()) {
      value = "isNativeHawaiianOrOtherPacificIslander";
      if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro() && nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian() &&
        nativeHawaiianOrOtherPacificIslander.getIsSamoan() && nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|isGuamanianOrChamorro|isNativeHawaiian|isSamoan|otherNhpi";
      }

      if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro() && nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian() &&
        nativeHawaiianOrOtherPacificIslander.getIsSamoan()) {
        return value + "|isGuamanianOrChamorro|isNativeHawaiian|isSamoan";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro() &&
        nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian() &&
        nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|isGuamanianOrChamorro|isNativeHawaiian|otherNhpi";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro() &&
        nativeHawaiianOrOtherPacificIslander.getIsSamoan() && nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|isGuamanianOrChamorro|isSamoan|otherNhpi";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian() &&
        nativeHawaiianOrOtherPacificIslander.getIsSamoan() && nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|isNativeHawaiian|isSamoan|otherNhpi";
      }

      if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro() && nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian()) {
        return value + "|isGuamanianOrChamorro|isNativeHawaiian";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro() && nativeHawaiianOrOtherPacificIslander.getIsSamoan()) {
        return value + "|isGuamanianOrChamorro|isSamoan";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro() && nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|isGuamanianOrChamorro|otherNhpi";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian() && nativeHawaiianOrOtherPacificIslander.getIsSamoan()) {
        return value + "|isNativeHawaiian|isSamoan";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian() && nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|isNativeHawaiian|otherNhpi";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsSamoan() && nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|isSamoan|otherNhpi";
      }

      if (nativeHawaiianOrOtherPacificIslander.getIsGuamanianOrChamorro()) {
        return value + "|isGuamanianOrChamorro";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsNativeHawaiian()) {
        return value + "|isNativeHawaiian";
      } else if (nativeHawaiianOrOtherPacificIslander.getIsSamoan()) {
        return value + "|isSamoan";
      } else if (nativeHawaiianOrOtherPacificIslander.getOther()) {
        return value + "|otherNhpi";
      }
    }
    return value;
  }

  public static String getPOAmericanIndianAlaskaText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null && principalOwners.getRace().getAmericanIndianOrAlaskaNative() != null &&
      principalOwners.getRace().getAmericanIndianOrAlaskaNative().getNameOfTribe() != null) {
      return principalOwners.getRace().getAmericanIndianOrAlaskaNative().getNameOfTribe();
    }
    return null;
  }

  public static String getPOAsianFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null && principalOwners.getRace().getAsian() != null &&
      principalOwners.getRace().getAsian().getOtherDetail() != null) {
      return principalOwners.getRace().getAsian().getOtherDetail();
    }
    return null;
  }

  public static String getPOAfricanAmericanFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null && principalOwners.getRace().getBlackOrAfricanAmerican() != null &&
      principalOwners.getRace().getBlackOrAfricanAmerican().getOtherDetail() != null) {
      return principalOwners.getRace().getBlackOrAfricanAmerican().getOtherDetail();
    }
    return null;
  }

  public static String getPOPacificIslanderFreeFormText(PrincipalOwners principalOwners) {
    if (principalOwners.getRace() != null && principalOwners.getRace().getNativeHawaiianOrOtherPacificIslander() != null &&
      principalOwners.getRace().getNativeHawaiianOrOtherPacificIslander().getOtherDetail() != null) {
      return principalOwners.getRace().getNativeHawaiianOrOtherPacificIslander().getOtherDetail();
    }
    return null;
  }

  public static String getPOGenderFlag(PrincipalOwners principalOwners) {
    if (principalOwners.getGender() != null) {
      if (principalOwners.getGender().getHasDeclined()) {
        return "hasDeclined";
      } else if (principalOwners.getGender().getSex() != null) {
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

}
