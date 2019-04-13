package cn.niukid.bim.controller;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.autodesk.client.ApiException;
import com.autodesk.client.ApiResponse;
import com.autodesk.client.api.DerivativesApi;
import com.autodesk.client.api.ObjectsApi;
import com.autodesk.client.auth.OAuth2TwoLegged;
import com.autodesk.client.model.Job;
import com.autodesk.client.model.JobPayload;
import com.autodesk.client.model.JobPayloadInput;
import com.autodesk.client.model.JobPayloadItem;
import com.autodesk.client.model.JobPayloadOutput;
import com.autodesk.client.model.Manifest;
import com.autodesk.client.model.ObjectDetails;


import cn.niukid.bim.OAuth;

@Component
public class UploadHelper {
	private static final Logger log = LoggerFactory.getLogger(UploadHelper.class);
	

	
	@Async("mySimpleExecutor")
	public void uploadFileToBucketAsync(String bucketKey, String objectName, long contentLength, File file) throws ApiException, Exception  {
		ObjectsApi objectsApi = new ObjectsApi();

			OAuth2TwoLegged forgeOAuth = OAuth.getOAuthInternal();
			if(contentLength>Integer.MAX_VALUE)
			{
				log.warn("uploadFileToBucket: contentLength>Integer.MAX_VALUE"+",contentLength="+contentLength+",Integer.MAX_VALUE="+Integer.MAX_VALUE);
			}
			ApiResponse<ObjectDetails> details = objectsApi.uploadObject(bucketKey, objectName, (int) contentLength, file,
					null, null, forgeOAuth, forgeOAuth.getCredentials());	
			
			log.info("uploadObject Done! details="+details.getData().toString());
	}
	
	
	@Async("mySimpleExecutor")
	public void submitTranslateAsync(String objectName) throws ApiException, Exception
	{
		
          // get oAuth of internal, in order to get the token with higher permissions
          OAuth2TwoLegged forgeOAuth = OAuth.getOAuthInternal();

          DerivativesApi derivativesApi = new DerivativesApi();

     
          JobPayloadInput input = new JobPayloadInput();
          input.setUrn(new String(objectName));
          
          JobPayloadItem formats = new JobPayloadItem();
          formats.setType(JobPayloadItem.TypeEnum.SVF);
          formats.setViews(Arrays.asList(JobPayloadItem.ViewsEnum._3D));
      
          
          JobPayloadOutput output = new JobPayloadOutput();
          output.setFormats(Arrays.asList(formats));
      
          // build the payload to translate the file to svf
          JobPayload job = new JobPayload();
          job.setInput(input);
          job.setOutput(output);

          ApiResponse<Job> res = derivativesApi.translate(job, true, forgeOAuth, forgeOAuth.getCredentials());  
          log.info("response="+res.getData().toString());
          
	}
	
	public ApiResponse<Manifest> getManifest(String urn, String acceptEncoding) throws ApiException, Exception {

		OAuth2TwoLegged forgeOAuth = OAuth.getOAuthInternal();

		DerivativesApi derivativesApi = new DerivativesApi();

		return derivativesApi.getManifest(urn, acceptEncoding, forgeOAuth,forgeOAuth.getCredentials());
	}
	
}
