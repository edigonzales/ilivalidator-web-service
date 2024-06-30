package ch.so.agi.ilivalidator.job;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ch.so.agi.ilivalidator.profile.ProfileProperties;
import ch.so.agi.ilivalidator.storage.StorageService;

@RestController
public class JobController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Map<String,String> profiles;

    private StorageService storageService;
    
    private JobScheduler jobScheduler;
    
    private JobService jobService;
    
    public JobController(ProfileProperties profileProperties, StorageService storageService, JobScheduler jobScheduler, JobService jobService) {
        this.profiles = profileProperties.getProfiles();
        this.storageService = storageService;
        this.jobScheduler = jobScheduler;
        this.jobService = jobService;
    }

    @PostMapping(value="/api/jobs", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadFiles(
            @RequestPart(name="jobId", required=true) String jobId,
            @RequestPart(name="files", required=true) MultipartFile[] files, 
            @RequestPart(name="profile", required=false) String profile) {
        
        String qualifiedProfile = profiles.get(profile);
        String profileString = qualifiedProfile==null?"":qualifiedProfile;
        
        log.debug("<{}> Selected profile: {}", jobId, profile);
        log.debug("<{}> Number of uploaded files: {}", jobId, files.length);

        Path[] uploadedFiles;
        try {
            uploadedFiles = storageService.store(files, jobId);
        } catch (IOException e) {
            throw new RuntimeException("Could not store files.");
        }
        
        jobScheduler.enqueue(UUID.fromString(jobId), () -> jobService.validate(JobContext.Null, uploadedFiles, profileString));
        
        return null;
    }
    
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<?> error(Exception e) {
        e.printStackTrace();
        log.error("<{}>", e.getMessage());
        return ResponseEntity
                .internalServerError()
                .body("Please contact service provider.");
    }

}
