package com.aexp.commercial.data.util;

import com.aexp.commercial.data.model.*;

import java.util.ArrayList;
import java.util.List;

public class PrincipalOwnerUtil {

    public static String getPrincipalOwnerEthnicity(PrincipalOwners principalOwners) {
        Ethnicity ethnicity = principalOwners.getEthnicity();
        if (ethnicity == null) return "notProvided";

        if (ethnicity.getHasDeclined()) return "hasDeclined";
        if (ethnicity.getIsNotHispanicOrLatino()) return "isNotHispanicOrLatino";

        if (ethnicity.getHispanicOrLatino() != null && ethnicity.getHispanicOrLatino().getIsOptionSelected()) {
            return buildHispanicOrLatinoDetails(ethnicity.getHispanicOrLatino());
        }

        return "notProvided";
    }

    private static String buildHispanicOrLatinoDetails(HispanicOrLatino hispanicOrLatino) {
        List<String> details = new ArrayList<>();
        details.add("isHispanicOrLatino");

        if (hispanicOrLatino.getIsCuban()) details.add("Cuban");
        if (hispanicOrLatino.getIsMexican()) details.add("Mexican");
        if (hispanicOrLatino.getIsPuertoRican()) details.add("PuertoRican");
        if (hispanicOrLatino.getOther()) details.add("otherHol");

        return String.join("|", details);
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
        if (principalOwners.getRace() == null) return "notProvided";

        Race race = principalOwners.getRace();
        if (race.getHasDeclined()) return "hasDeclined";

        List<String> races = new ArrayList<>();
        if (race.getIsWhite()) races.add("isWhite");
        if (race.getAmericanIndianOrAlaskaNative() != null && race.getAmericanIndianOrAlaskaNative().getIsOptionSelected()) {
            races.add(getAmericanIndianAlaskaRace(race.getAmericanIndianOrAlaskaNative()));
        }
        if (race.getAsian() != null && race.getAsian().getIsOptionSelected()) {
            races.add(getAsianRace(race.getAsian()));
        }
        if (race.getBlackOrAfricanAmerican() != null && race.getBlackOrAfricanAmerican().getIsOptionSelected()) {
            races.add(getBlackAfricanAmericanRace(race.getBlackOrAfricanAmerican()));
        }
        if (race.getNativeHawaiianOrOtherPacificIslander() != null && race.getNativeHawaiianOrOtherPacificIslander().getIsOptionSelected()) {
            races.add(getNativeHawaiianPacificIslanderRace(race.getNativeHawaiianOrOtherPacificIslander()));
        }

        return races.isEmpty() ? "notProvided" : String.join("|", races);
    }

    private static String getAsianRace(Asian asian) {
        List<String> asianDetails = new ArrayList<>();
        asianDetails.add("isAsian");

        if (asian.getIsAsianIndian()) asianDetails.add("isAsianIndian");
        if (asian.getIsChinese()) asianDetails.add("isChinese");
        if (asian.getIsFilipino()) asianDetails.add("isFilipino");
        if (asian.getIsJapanese()) asianDetails.add("isJapanese");
        if (asian.getIsKorean()) asianDetails.add("isKorean");
        if (asian.getIsVietnamese()) asianDetails.add("isVietnamese");
        if (asian.getOther()) asianDetails.add("otherAsn");

        return String.join("|", asianDetails);
    }

    private static String getBlackAfricanAmericanRace(BlackOrAfricanAmerican black) {
        List<String> blackDetails = new ArrayList<>();
        blackDetails.add("isBlackOrAfricanAmerican");

        if (black.getIsAfricanAmerican()) blackDetails.add("isAfricanAmerican");
        if (black.getIsEthiopian()) blackDetails.add("isEthiopian");
        if (black.getIsHaitian()) blackDetails.add("isHaitian");
        if (black.getIsJamaican()) blackDetails.add("isJamaican");
        if (black.getIsNigerian()) blackDetails.add("isNigerian");
        if (black.getIsSomali()) blackDetails.add("isSomali");
        if (black.getOther()) blackDetails.add("otherBaa");

        return String.join("|", blackDetails);
    }

    private static String getAmericanIndianAlaskaRace(AmericanIndianOrAlaskaNative native) {
        return native.getNameOfTribe() != null ? "inFreeFormAian" : null;
    }

    private static String getNativeHawaiianPacificIslanderRace(NativeHawaiianOrOtherPacificIslander pacificIslander) {
        List<String> details = new ArrayList<>();
        details.add("isNativeHawaiianOrOtherPacificIslander");

        if (pacificIslander.getIsGuamanianOrChamorro()) details.add("isGuamanianOrChamorro");
        if (pacificIslander.getIsNativeHawaiian()) details.add("isNativeHawaiian");
        if (pacificIslander.getIsSamoan()) details.add("isSamoan");
        if (pacificIslander.getOther()) details.add("otherNhpi");

        return String.join("|", details);
    }

    public static String getPOGenderFlag(PrincipalOwners principalOwners) {
        if (principalOwners.getGender() == null) return "notProvided";

        if (principalOwners.getGender().getHasDeclined()) return "hasDeclined";
        if (principalOwners.getGender().getSex() != null) return "inFreeFormGend";

        return "notProvided";
    }

    public static String getPOGenderFreeFormText(PrincipalOwners principalOwners) {
        if (principalOwners.getGender() != null) {
            return principalOwners.getGender().getSex();
        }
        return null;
    }
}
