package com.aexp.commercial.data.util;

import com.aexp.commercial.data.model.BusinessOwnershipStatus;

public class BusinessOwnershipUtil {

  public static String getBusinessOwnershipStatus(BusinessOwnershipStatus businessOwnershipStatus) {
    String value = null;
    if (businessOwnershipStatus.getHasDeclined()) {
      value = "hasDeclined";
    } else if (businessOwnershipStatus.getNoneOfTheseApply()) {
      value = "noneOfTheseApply";
    } else {
      if (businessOwnershipStatus.getIsWomanOwned()) {
        value = "isWomanOwned";
      } else if (businessOwnershipStatus.getIsMinorityOwned()) {
        value = "isMinorityOwned";
      } else if (businessOwnershipStatus.getIsLGBTQIPlusOwned()) {
        value = "isLGBTQIPlusOwned";
      }
      if (businessOwnershipStatus.getIsWomanOwned() && businessOwnershipStatus.getIsLGBTQIPlusOwned()) {
        value = "isWomanOwned|isLGBTQIPlusOwned";
      } else if (businessOwnershipStatus.getIsMinorityOwned() && businessOwnershipStatus.getIsLGBTQIPlusOwned()) {
        value = "isMinorityOwned|isLGBTQIPlusOwned";
      } else if (businessOwnershipStatus.getIsWomanOwned() && businessOwnershipStatus.getIsMinorityOwned()) {
        value = "isWomanOwned|isMinorityOwned";
      }
      if (businessOwnershipStatus.getIsWomanOwned() && businessOwnershipStatus.getIsMinorityOwned() &&
        businessOwnershipStatus.getIsLGBTQIPlusOwned()) {
        value = "isWomanOwned|isMinorityOwned|isLGBTQIPlusOwned";
      }
    }
    return value;
  }

}
