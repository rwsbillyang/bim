package cn.niukid.bim.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.autodesk.client.ApiException;
import com.autodesk.client.ApiResponse;
import com.autodesk.client.api.BucketsApi;
import com.autodesk.client.api.ObjectsApi;
import com.autodesk.client.auth.OAuth2TwoLegged;
import com.autodesk.client.model.BucketObjects;
import com.autodesk.client.model.Buckets;
import com.autodesk.client.model.BucketsItems;
import com.autodesk.client.model.Manifest;
import com.autodesk.client.model.ObjectDetails;
import com.autodesk.client.model.PostBucketsPayload;

import cn.niukid.bim.Config;
import cn.niukid.bim.OAuth;
import cn.niukid.bim.bean.BucketItemBean;
import cn.niukid.bim.utils.FileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// http://localhost:8080/html/index2.html

//上传rvt文件页面
//http://localhost:8080/html/upload.html

//获取bucket中的文档信息列表
//http://localhost:8080/admin/oss/getBucketsInfo?id=cn.niukud.demo_bucket

//http://localhost:8080/admin/oss/bucketOperation?bucket=cn.niukud.demo_bucket&cmd=create
//http://localhost:8080/admin/oss/bucketOperation?bucket=cn.niukud.demo_bucket&cmd=detail
//http://localhost:8080/admin/oss/bucketOperation?bucket=cn.niukud.demo_bucket&cmd=delete

// curl -X POST -d "bucketKey=demo_bucket"  "http://localhost:8080/admin/oss/createBucket"

// http://localhost:8080/admin/oss/uploadToBucket?filename=pCi4lj1531447339825.rvt
//http://localhost:8080/admin/oss/submitTranslate?objectName=dXJuOmFkc2sub2JqZWN0czpvcy5vYmplY3Q6Y24ubml1a3VkLmRlbW9fYnVja2V0L3BDaTRsajE1MzE0NDczMzk4MjUucnZ0

//http://localhost:8080/admin/oss/queryState?urn=dXJuOmFkc2sub2JqZWN0czpvcy5vYmplY3Q6Y24ubml1a3VkLmRlbW9fYnVja2V0L3BDaTRsajE1MzE0NDczMzk4MjUucnZ0&acceptEncoding=gzip

@RestController
@RequestMapping(value = "/admin/oss")
public class OssController {

	private static final Logger log = LoggerFactory.getLogger(OssController.class);
	
	@Autowired
	private UploadHelper uploadHelper;
	
	
/*	
  https://stackoverflow.com/questions/50415591/how-are-buckets-isolated-on-forge-autodesk
全局唯一，建议packageName.bucketName 或 clientId.bucketName
  A unique name you assign to a bucket. It must be globally unique across all applications and
	regions, otherwise the call will fail. Possible values: -_.a-z0-9 (between 3-128 characters in
	length). Note that you cannot change a bucket key.
*/
	public final static String DefaultBucketName = "cn.niukud.demo_bucket";
	
	@GetMapping(value = "/getBucketsInfo")
	public  Flux<BucketItemBean> getBuckets(String id) 
	{
		log.info("id="+id);
		List<BucketItemBean> list = null;
		 try {
	            // get oAuth of internal, in order to get the token with higher permissions
	            OAuth2TwoLegged forgeOAuth = OAuth.getOAuthInternal();
	            
	            if (id==null || "#".equals(id)) {// root
	                BucketsApi bucketsApi = new BucketsApi();

	                ApiResponse<Buckets> buckets = bucketsApi.getBuckets("us", 100, null, forgeOAuth,forgeOAuth.getCredentials());

	                List<BucketsItems> items = buckets.getData().getItems();
	                if(items!=null)
	                {
	                	 	int size = items.size();
	  	               
	 	                list =  new ArrayList<BucketItemBean>(size);
	 	                BucketsItems eachItem = null;
	 	                
	 	                // iterate buckets
	 	                for (int i = 0; i < size; i++) {
	 	                    eachItem = items.get(i);// get bucker info
	 	                    list.add(new BucketItemBean(eachItem.getBucketKey(),eachItem.getBucketKey(),"bucket",true));
	 	                }

	                }
	               

	            } else {

	                // as we have the id (bucketKey), let's return all objects
	                ObjectsApi objectsApi = new ObjectsApi();

	                ApiResponse<BucketObjects> objects = objectsApi.getObjects(id, 100, null, null, forgeOAuth,
	                        forgeOAuth.getCredentials());
	                List<ObjectDetails> objectDetails = objects.getData().getItems();
	                
	                if(objectDetails!=null)
	                {
	                		int size = objectDetails.size();
		                
		                list =  new ArrayList<BucketItemBean>(size);
		                
		                ObjectDetails eachItem = null;
		                String base64Urn = null;
		                
		                // iterate each items of the bucket
		                for (int i = 0; i < size; i++) {
		                    // make a note with the base64 urn of the item
		                    eachItem = objectDetails.get(i);
		                    base64Urn = DatatypeConverter.printBase64Binary(eachItem.getObjectId().getBytes());

		                    list.add(new BucketItemBean(base64Urn,eachItem.getObjectKey(),"object",false));
		                }
	                }
	            }
	        } catch (ApiException autodeskExp) {
	            log.warn("get buckets & objects exception: " + autodeskExp.toString());
	        } catch (Exception exp) {
	        		log.warn("get buckets & objects exception: " + exp.toString());
	        }
		 
		 if(list==null)
			 list =  new ArrayList<BucketItemBean>();
		 return Flux.fromIterable(list);
	}
	
	@GetMapping(value = "/bucketOperation")
	public Mono<Map<String, String>> bucketOperation(@RequestParam(value="bucket",required=false) String bucketKey,String cmd) 
	{
		log.info("bucketKey="+bucketKey+",cmd="+cmd);
		Map<String, String> map = new HashMap<String, String>();
		
		
		if(StringUtils.isEmpty(cmd))
		{
			map.put("ret", "ko");
			map.put("msg", "cmd is empty");
			return Mono.just(map); 
		}
		
		if(bucketKey==null)
		{
			log.info("use default bucketname:"+DefaultBucketName);
			bucketKey=DefaultBucketName;
		}
		
		String msg = null;
        // build the payload of the http request
        BucketsApi bucketsApi = new BucketsApi();
        try {
        		// get oAuth of internal, in order to get the token with higher permissions
            OAuth2TwoLegged forgeOAuth = OAuth.getOAuthInternal(); 
	        ApiResponse response = null;
			switch(cmd)
			{
				case "create":
				{
					response = createBucket(bucketKey,bucketsApi, forgeOAuth);
					break;
				}
				case "delete":
				{
					response = deleteBucket(bucketKey,bucketsApi, forgeOAuth);
					break;
				}
				case "detail":
				{
					response = getBucketDetail(bucketKey,bucketsApi, forgeOAuth);
					break;
				}
				default:
					map.put("ret", "ko");
					map.put("msg", "not support cmd:"+cmd);
					log.warn( "not support cmd:"+cmd);
			}
			if(response!=null)
			{
				if(response.getStatusCode()!=200)
				{
					map.put("ret", "ko");
					msg = "response.getStatusCode:"+response.getStatusCode();
					map.put("msg", msg);
					log.warn(msg );
				}else
				{
					map.put("ret", "ok");
					msg = response.getData().toString();
					map.put("msg", msg);
					log.info(msg );
				}
			}
        }catch (ApiException autodeskExp) {
            msg = "bucketOperation ApiException: " + autodeskExp.toString();
			log.warn(msg);
			map.put("msg", msg);
        } catch (Exception exp) {
        		msg = "bucketOperation exception: " + exp.toString();
        		log.warn(msg);
        		map.put("msg", msg);
        }
		return Mono.just(map);
		
	}
	
	
	private ApiResponse getBucketDetail(String bucketKey, BucketsApi bucketsApi,OAuth2TwoLegged forgeOAuth) throws ApiException, Exception{
		log.info("to getBucketDetail:"+bucketKey);
		//访问一个不存在的bucket，会提示不自己创建的，而导致异常
		return  bucketsApi.getBucketDetails(bucketKey,forgeOAuth,forgeOAuth.getCredentials());
	}
	private ApiResponse createBucket(String bucketKey, BucketsApi bucketsApi,OAuth2TwoLegged forgeOAuth) throws ApiException, Exception {
            log.info("to create bucket:"+bucketKey);
            PostBucketsPayload postBuckets = new PostBucketsPayload();
            postBuckets.setBucketKey(bucketKey);
            // expires in 24h
            postBuckets.setPolicyKey(PostBucketsPayload.PolicyKeyEnum.PERSISTENT);

            return bucketsApi.createBucket(postBuckets, null, forgeOAuth,forgeOAuth.getCredentials());
	}
	private ApiResponse deleteBucket(String bucketKey, BucketsApi bucketsApi,OAuth2TwoLegged forgeOAuth) throws ApiException, Exception{
		log.info("to deleteBucket:"+bucketKey);
		return  bucketsApi.deleteBucket(bucketKey,forgeOAuth,forgeOAuth.getCredentials());
	}

	  //https://segmentfault.com/a/1190000013200710
	  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	  public Mono<Map<String, String>> upload(@RequestPart("file") FilePart filePart)  {
		  	Map<String, String> map = new HashMap<String, String>();
		  	log.info(filePart.filename());
		  	try {
//			  	Path tempFile = Files.createTempFile("test", filePart.filename());
			  
//		        //NOTE 方法一
//		        AsynchronousFileChannel channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE);
//		        DataBufferUtils.write(filePart.content(), channel, 0)
//		                .doOnComplete(() -> {
//		                		log.info("finish upload");
//		                		
//		                		// uploadFileToBucket(DefaultBucketName,filePart.filename(),channel.size()); 
//		                })
//		                .subscribe();
	
		        //NOTE 方法2
		        if(FileUtils.tryCreateDir(Config.UploadDir))
		        {
		        		String filename = FileUtils.getRelativeFileName(null, null, filePart.filename(), true);
		        		File file = new File(Config.UploadDir+filename);
		        		filePart.transferTo(file);
		        		
		        		map.put("ret", "ok");
				    map.put("filename", filename);
		        }else
		        {
		        		map.put("ret", "ko");
					map.put("msg", "fail to create folder:"+Config.UploadDir);
		        }
		   
		  	} catch (Exception e) {
				e.printStackTrace();
				map.put("ret", "ko");
				map.put("msg", "Exception");
			}
	        return Mono.just(map);
	    }
	  
	  
	  @GetMapping(value = "/uploadToBucket")
	  public  Mono<Map<String, String>> uploadToBucket(String filename)  {
		  log.info("filename="+filename);
		  	Map<String, String> map = new HashMap<String, String>();
		  	map.put("ret", "ko");
		  	String msg = null;
		  	if(StringUtils.isEmpty(filename))
		  	{
				map.put("msg", "filename is empty");
		  	}else
		  	{
		  		try {
			  		File file = new File(Config.UploadDir+filename);
			  		if(file.exists())
			  		{
			  			log.info("to upload "+filename);
			  		    uploadHelper.uploadFileToBucketAsync(DefaultBucketName, filename, file.length(), file);	
			  			map.put("ret", "ok");
		  				map.put("msg", "submit done");
			  		}else
			  		{
			  			log.warn("file not exists "+filename);
						map.put("msg", "file not exists");
			  		}
		  		} catch (ApiException autodeskExp) {
		            msg = "uploadToBucket ApiException: " + autodeskExp.toString();
					log.warn(msg);
					map.put("msg", msg);
		        } catch (Exception exp) {
		        		msg = "uploadToBucket exception: " + exp.toString();
		        		log.warn(msg);
		        		map.put("msg", msg);
		        }
		  	}
		  	
		  	return Mono.just(map);
	    }

	  
	  @GetMapping(value = "/submitTranslate")
	  public  Mono<Map<String, String>> submitTranslate(String objectName) {
		  log.info("objectName="+objectName);
			Map<String, String> map = new HashMap<String, String>();
		  	map.put("ret", "ko");
			if(StringUtils.isEmpty(objectName))
		  	{
				map.put("msg", "objectName is empty");
		  	}else
		  	{
		  		String msg = null;
		  		try {
		  			uploadHelper.submitTranslateAsync(objectName);
		  			map.put("ret", "ok");
	  				map.put("msg", "submit done");
		  		}  catch (ApiException autodeskExp) {
		            msg = "submitTranslate ApiException: " + autodeskExp.toString();
					log.warn(msg);
					map.put("msg", msg);
		        } catch (Exception exp) {
		        		msg = "submitTranslate exception: " + exp.toString();
		        		log.warn(msg);
		        		map.put("msg", msg);
		        }
		  	}
			
			return Mono.just(map);
	  }
	  //If specified with `gzip` or `*`, content will be compressed and returned in a GZIP format.
	  @GetMapping(value = "/queryState")
	  public  Mono<Map<String, Object>> queryState(String urn, String acceptEncoding)
	  {
		  log.info("urn="+urn+",acceptEncoding="+acceptEncoding);
		  Map<String, Object> map = new HashMap<String, Object>();
		  	map.put("ret", "ko");
			if(StringUtils.isEmpty(urn))
		  	{
				map.put("msg", "urn is empty");
		  	}else
		  	{
		  		String msg = null;
		  		try {
		  			 ApiResponse<Manifest> response = uploadHelper.getManifest(urn,  acceptEncoding);
		  			if(response.getStatusCode()!=200)
		  			{
						map.put("msg", "statusCode is "+response.getStatusCode());
		  			}else
		  			{
		  				map.put("ret", "ok");
		  				map.put("response", response.getData());
		  			}
		  		} catch (ApiException autodeskExp) {
		            msg = "queryState ApiException: " + autodeskExp.toString();
					log.warn(msg);
					map.put("msg", msg);
		        } catch (Exception exp) {
		        		msg = "queryState exception: " + exp.toString();
		        		log.warn(msg);
		        		map.put("msg", msg);
		        }
		  	}
			
			return Mono.just(map);
	  }
	  
}
