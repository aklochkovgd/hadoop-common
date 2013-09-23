package org.apache.hadoop.yarn.api.records;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.util.Shell.OSType;
import org.apache.hadoop.yarn.util.Records;

@Public
@Unstable
public abstract class ExternalCommand {
  
	public static ExternalCommand newInstance(String command, OSType osType) {
  	ExternalCommand cmd = Records.newRecord(ExternalCommand.class);
  	cmd.setCommand(command);
  	cmd.setOSType(osType);
  	return cmd;
	}

	@Public
  @Unstable
  public abstract Shell.OSType getOSType();
  
  @Public
  @Unstable
  public abstract void setOSType(Shell.OSType osType);
  
  @Public
  @Unstable
  public abstract String getCommand();
  
  @Public
  @Unstable
  public abstract void setCommand(String command);
  
}
