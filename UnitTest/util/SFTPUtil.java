
package com.aexp.commercial.data.util;

import com.aexp.commercial.data.config.BatchPropertyConfig;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;

import static com.aexp.commercial.data.constants.BatchConstants.PHX_TZ;

@Service
public class SFTPUtil {

  private final BatchPropertyConfig batchPropertyConfig;
  private static final Logger logger = LoggerFactory.getLogger(SFTPUtil.class);
  int portNumber = 22;

  public SFTPUtil(BatchPropertyConfig batchPropertyConfig) {
    this.batchPropertyConfig = batchPropertyConfig;
  }

  public void sendFile(String fileName) throws Exception {
    JSch jSch = new JSch();
    Session session = null;
    ChannelSftp channelSftp = null;
    try {
      session = jSch.getSession(batchPropertyConfig.getSftpUsername(), batchPropertyConfig.getSftpUrl(), portNumber);
      session.setPassword(batchPropertyConfig.getSftpPassword());
      Properties props = new Properties();
      props.put("StrictHostKeyChecking", "No");
      session.setConfig(props);
      session.connect();

      channelSftp = (ChannelSftp) session.openChannel("sftp");
      channelSftp.connect();
      channelSftp.lcd(batchPropertyConfig.getSftpRemoteDir());
      channelSftp.cd(batchPropertyConfig.getSftpFolder());
      channelSftp.put(fileName, ".");
      logger.info("{} got uploaded successfully.", fileName);
    } catch (Exception e) {
      logger.error("CDD Batch Exception while sending file to SFTP", e);
      throw new Exception(e);
    } finally {
      if (null != channelSftp)
        channelSftp.disconnect();
      if (null != session)
        session.disconnect();
    }
  }

  public String getFileName(String fileName) {
    return batchPropertyConfig.getSftpRemoteDir() + fileName + LocalDate.now(ZoneId.of(PHX_TZ)) + ".xls";
  }
}
