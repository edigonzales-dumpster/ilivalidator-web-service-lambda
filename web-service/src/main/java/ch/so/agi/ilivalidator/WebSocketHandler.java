package ch.so.agi.ilivalidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import ch.interlis.iox.IoxException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.AccessControlPolicy;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketHandler extends AbstractWebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static String FOLDER_PREFIX = "ilivalidator_";

    private static String LOG_ENDPOINT = "log";

    @Value("#{servletContext.contextPath}")
    protected String servletContextPath;

    @Value("${server.port}")
    protected String serverPort;

    @Value("${app.s3Bucket}")
    private String s3Bucket;

//    @Autowired
//    IlivalidatorService ilivalidator;

    HashMap<String, File> sessionFileMap = new HashMap<String, File>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        File file = sessionFileMap.get(session.getId());

        String filename = message.getPayload();

        // ilivalidator must know if it is a ili1 or ili2 transfer file.
        Path copiedFile = Paths.get(file.getParent(), filename);
        Files.copy(file.toPath(), copiedFile, StandardCopyOption.REPLACE_EXISTING);

        session.sendMessage(new TextMessage("Received: " + filename));

//        String logFilename = copiedFile.toFile().getAbsolutePath() + ".log";
//        log.info(logFilename);

        // There is no option for config file support in the GUI at the moment.
        String configFile = "on";

        // Not exposed in the GUI.
        String allObjectsAccessible = "true";

        boolean valid;
        String subfolder;
        String key;
        try {
//            // Run the validation.
            session.sendMessage(new TextMessage("Validating..."));
//            valid = ilivalidator.validate(allObjectsAccessible, configFile, copiedFile.toFile().getAbsolutePath(), logFilename);

            // Upload interlis file to S3.
            Region region = Region.EU_CENTRAL_1;
            S3Client s3 = S3Client.builder().region(region).build();

            subfolder = new File(copiedFile.toFile().getParent()).getName();
            key = subfolder + "/" + copiedFile.toFile().getName();
            
            log.info("Uploading data file object... " + key);
            s3.putObject(PutObjectRequest.builder().bucket(s3Bucket).key(key).build(), copiedFile);
            s3.putObjectAcl( PutObjectAclRequest.builder().bucket(s3Bucket).key(key).acl(ObjectCannedACL.PUBLIC_READ).build());
            log.info("Upload data file complete.");

            // Invoke Lambda function.
            
            
//            // Upload log file to S3.
//            log.info("log file: " + logFilename);
//            Region region = Region.EU_CENTRAL_1;
//            S3Client s3 = S3Client.builder().region(region).build();
//                    
//            String subfolder = new File(new File(logFilename).getParent()).getName();
//            String s3Logfilename = new File(logFilename).getName();
//            key = subfolder + "/" + s3Logfilename;
//            
//            log.info("Uploading object... " + key);
//            s3.putObject(PutObjectRequest.builder().bucket(s3Bucket).key(key).build(), new File(logFilename).toPath());
//            s3.putObjectAcl(PutObjectAclRequest.builder().bucket(s3Bucket).key(key).acl(ObjectCannedACL.PUBLIC_READ).build());
//            log.info("Upload complete");
//            
            s3.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());

            TextMessage errorMessage = new TextMessage("An error occured while validating the data:" + e.getMessage());
            session.sendMessage(errorMessage);

            return;
        }

        // Browser response.
        String resultText = "<span style='background-color:#58D68D;'>...validation done:</span>";
//        if (!valid) {
//            resultText = "<span style='background-color:#EC7063'>...validation failed:</span>";
//        }

        TextMessage resultMessage = new TextMessage(resultText + " <a href='https://s3." + Region.EU_CENTRAL_1.id()
                + ".amazonaws.com/" + s3Bucket + "/" + key + "' target='_blank'>Download log file.</a><br/><br/>   ");
        session.sendMessage(resultMessage);

        sessionFileMap.remove(session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        Path tmpDirectory = Files.createTempDirectory(FOLDER_PREFIX);

        // ilivalidator muss wissen, ob es sich um eine ili1- oder ili2-Datei handelt.
        // Der Namen muss jedoch separat mitgeschickt werden. Gespeichert wird die Datei
        // mit einem
        // generischen Namen und anschliessend umbenannt.
        Path uploadFilePath = Paths.get(tmpDirectory.toString(), "data.file");

        FileChannel fc = new FileOutputStream(uploadFilePath.toFile().getAbsoluteFile(), false).getChannel();
        fc.write(message.getPayload());
        fc.close();

        File file = uploadFilePath.toFile();

        sessionFileMap.put(session.getId(), file);
    }
}