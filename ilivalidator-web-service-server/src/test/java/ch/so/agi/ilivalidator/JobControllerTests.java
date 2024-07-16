package ch.so.agi.ilivalidator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.awaitility.Awaitility.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.ilivalidator.job.JobResponse;

public abstract class JobControllerTests {
    static Logger logger = LoggerFactory.getLogger(JobControllerTests.class);

    @LocalServerPort
    protected String port;
    
    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper mapper;
    
    private String API_ENDPOINT_JOBS = "/api/jobs";
    private String OPERATION_LOCATION_HEADER = "Operation-Location";
    //private int RESULT_POLL_DELAY = 5; // seconds
    private int RESULT_POLL_INTERVAL = 5; // seconds
    private int RESULT_WAIT = 5; // minutes

    @Test
    public void validate_File_Interlis2_Ok() throws Exception {
        String serverUrl = "http://localhost:"+port+API_ENDPOINT_JOBS;

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
        parameters.add("jobId", UUID.randomUUID().toString());
        parameters.add("files", new FileSystemResource("src/test/data/VOLLZUG_SO0300002511_1153_20210329115028.xml"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        headers.set("Accept", "text/plain");

        // Datei hochladen und Response-Status-Code auswerten
        ResponseEntity<String> postResponse = restTemplate.postForEntity(
                serverUrl, new HttpEntity<MultiValueMap<String, Object>>(parameters, headers), String.class);

        assertEquals(202, postResponse.getStatusCode().value());
        
        // Warten, bis die Validierung durch ist (=SUCCEEDED)
        String operationLocation = postResponse.getHeaders().toSingleValueMap().get(OPERATION_LOCATION_HEADER);

        await()
            .with().pollInterval(RESULT_POLL_INTERVAL, TimeUnit.SECONDS)
            .and()
            .with().atMost(RESULT_WAIT, TimeUnit.MINUTES)
            .until(new MyCallable(operationLocation, restTemplate));

        // Logfile herunterladen und auswerten
        ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);        
        URL logFileUrl = new URL(jobResponse.getBody().logFileLocation());

        String logFileContents = null;
        try (InputStream in = logFileUrl.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            logFileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        assertTrue(logFileContents.contains("Info: ...validation done"));
    }
 
    
    public class MyCallable implements Callable<Boolean> {

        private final String operationLocation;
        private final TestRestTemplate restTemplate;
        private String jobStatus;
           
        public MyCallable(String operationLocation, TestRestTemplate restTemplate, String jobStatus) {
            this.operationLocation = operationLocation;
            this.restTemplate = restTemplate;
            this.jobStatus = jobStatus;
        }

        public MyCallable(String operationLocation, TestRestTemplate restTemplate) {
            this.operationLocation = operationLocation;
            this.restTemplate = restTemplate;
            this.jobStatus = "SUCCEEDED";
        }
        
        @Override
        public Boolean call() throws Exception {
            logger.info("*******************************************************");
            logger.info("polling: {}", operationLocation);
            logger.info("*******************************************************");
            ResponseEntity<JobResponse> jobResponse = restTemplate.getForEntity(operationLocation, JobResponse.class);
            return jobResponse.getBody().jobStatus().equalsIgnoreCase(this.jobStatus) ? true : false;            
        }        
    }
}


