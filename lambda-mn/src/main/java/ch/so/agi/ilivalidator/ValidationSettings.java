package ch.so.agi.ilivalidator;

import javax.validation.constraints.NotBlank;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ValidationSettings {

    @NonNull
    @NotBlank
    String datafile;
    
    String logfile;
    
    boolean valid;
    
    public ValidationSettings() {}

    public String getDataFile() {
        return datafile;
    }

    public void setDataFile(String dataFile) {
        this.datafile = dataFile;
    }

    public String getLogFile() {
        return logfile;
    }

    public void setLogFile(String logFile) {
        this.logfile = logFile;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
