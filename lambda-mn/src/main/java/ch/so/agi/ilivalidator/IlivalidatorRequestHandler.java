package ch.so.agi.ilivalidator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import ch.ehi.basics.settings.Settings;
import ch.ehi.basics.logging.EhiLogger;
import ch.interlis.iom_j.itf.ItfReader;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.StartBasketEvent;

import org.interlis2.validator.Validator;

@Introspected
public class IlivalidatorRequestHandler extends MicronautRequestHandler<ValidationSettings, ValidationSettings> {
    
    private String FOLDER_PREFIX = "ilivalidator_";
    
    private static S3Client s3 = S3Client.builder().build();
    
    private String s3Bucket = "ch.so.agi.ilivalidator";
        
    @Override
    public ValidationSettings execute(ValidationSettings input) {        
        // Set connect and read timeout to handle failing interlis repositories.
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        // Download data file from S3.
        Path tmpDirectory;
        try {
            tmpDirectory = Files.createTempDirectory(FOLDER_PREFIX);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    
        String key = input.getDataFile();
        String[] keys = key.split("/");
        String subfolder = keys[0];
        String dataFileName = keys[1];
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .build();

        ResponseInputStream is = s3.getObject(getObjectRequest);
        File dataFile = Paths.get(tmpDirectory.toFile().getAbsolutePath(), dataFileName).toFile();
        try {
            Files.copy(is, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        // Validate data file.
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_ILIDIRS, Validator.SETTING_DEFAULT_ILIDIRS);
        String logFileName = dataFile.getAbsolutePath() + ".log"; 
        settings.setValue(Validator.SETTING_LOGFILE, logFileName);
        settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE, Validator.TRUE);
        
        boolean valid = Validator.runValidation(dataFile.getAbsolutePath(), settings);
        
        key = subfolder + "/" +  logFileName.substring(logFileName.lastIndexOf("/")+1);
        input.setLogFile(key);
        input.setValid(valid);
        
        // Upload logfile.
        s3.putObject(PutObjectRequest.builder().bucket(s3Bucket).key(key).build(), Paths.get(logFileName));
        
        return input;
    }
}
