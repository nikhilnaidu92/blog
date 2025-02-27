package com.aexp.commercial.data.util;

import com.aexp.commercial.data.config.BatchPropertyConfig;
import com.aexp.commercial.data.exception.HipedInitException;
import com.aexp.sec.ssaashiped.crypto.ng.CryptoServiceNG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HiPEDUtil {

  private final CryptoServiceNG cryptoServiceNG;
  private static final Logger log = LoggerFactory.getLogger(HiPEDUtil.class);

  public HiPEDUtil(BatchPropertyConfig config) {
    cryptoServiceNG = config.getCryptoObject();
  }

  public String decryptString(String inputBytes) throws Exception {
    String output;
    try {
      output = cryptoServiceNG.decrypt(inputBytes, null);
    } catch (HipedInitException e) {
      throw new HipedInitException("Failed");
    } catch (Exception e) {
      log.error("Error occurred while decrypting using HiPED: ", e);
      throw new Exception("Error during decrypting using HiPED");
    }
    return output;
  }

}
