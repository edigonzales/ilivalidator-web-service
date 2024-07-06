package ch.so.agi.ilivalidator.job;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.interlis2.validator.Validator;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.ilivalidator.mail.MailService;

@Service
public class JobService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private String preferredIliRepo;    
    
    private MailService mailService;
    
    public JobService(@Value("${app.preferredIliRepo}") String preferredIliRepo, MailService mailService) {
        this.preferredIliRepo = preferredIliRepo;
        this.mailService = mailService;
    }
    
    @Job(name = "Ilivalidator", retries=0)
    public synchronized boolean validate(JobContext jobContext, Path[] transferFiles, String profile, String email) {
        String jobId = jobContext.getJobId().toString();
        
        List<String> transferFileNames = new ArrayList<>();
        for (Path transferFile : transferFiles) {
            transferFileNames.add(transferFile.toAbsolutePath().toString());
        }

        Path logFilePath = Paths.get(transferFiles[0].getParent().toString(), jobId + ".log");
        String logFileName = logFilePath.toFile().getAbsolutePath();                
        log.debug("<{}> Log file name: {}", jobId, logFileName);

        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_LOGFILE, logFileName);
        settings.setValue(Validator.SETTING_XTFLOG, logFileName + ".xtf");
        settings.setValue(Validator.SETTING_CSVLOG, logFileName + ".csv");

        String settingIlidirs = Validator.SETTING_DEFAULT_ILIDIRS;
        if (preferredIliRepo != null) {
            settingIlidirs = preferredIliRepo + ";" + settingIlidirs;   
        }
        settings.setValue(Validator.SETTING_ILIDIRS, settingIlidirs);
        log.debug("<{}> Setting ilidirs: {}", jobId, settingIlidirs);

        if (!profile.isEmpty()) {
            settings.setValue(Validator.SETTING_META_CONFIGFILE, "ilidata:" + profile);
        }
        
        log.info("Validation start");
        boolean valid = Validator.runValidation(transferFileNames.toArray(new String[0]), settings);
        log.info("Validation end");

        if (!email.isEmpty()) {             
            String fileNames = transferFileNames.stream()
                    .map(f -> {
                        return Paths.get(f).toFile().getName();
                    })
                    .collect(Collectors.joining(", "));
            
            String mailSubject = (valid?"DONE":"FAILED") + " / " + jobId + " / "+ fileNames;
            String mailBody = "Job-ID: %s".formatted(jobId);
 
            try {
                mailService.send(email, mailSubject, mailBody, logFileName);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("<{}> Error while sending email: {}", jobId, e.getMessage());
            }
        }
        
        return valid;
    }

}
