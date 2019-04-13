# bim
A BIM demo, open 3D info in web browser after uploading Autodesk revit file with suffix rvt  


Step1: config Autodesk clientId and clientSecret in the file cn.niukid.bim.Config.java
```
	public final static String ClientId = "your_client_id";
	public final static String ClientSecret = "your_client_secret";
```

after `mvn spring-boot:run`,you can open links as the following in web browser:
```
 http://localhost:8080/html/index.html
 http://localhost:8080/html/index2.html

//上传rvt文件页面
http://localhost:8080/html/upload.html

//获取bucket中的文档信息列表
http://localhost:8080/admin/oss/getBucketsInfo?id=cn.niukud.demo_bucket

http://localhost:8080/admin/oss/bucketOperation?bucket=cn.niukud.demo_bucket&cmd=create
http://localhost:8080/admin/oss/bucketOperation?bucket=cn.niukud.demo_bucket&cmd=detail
http://localhost:8080/admin/oss/bucketOperation?bucket=cn.niukud.demo_bucket&cmd=delete

 curl -X POST -d "bucketKey=demo_bucket"  "http://localhost:8080/admin/oss/createBucket"

 http://localhost:8080/admin/oss/uploadToBucket?filename=pCi4lj1531447339825.rvt
http://localhost:8080/admin/oss/submitTranslate?objectName=dXJuOmFkc2sub2JqZWN0czpvcy5vYmplY3Q6Y24ubml1a3VkLmRlbW9fYnVja2V0L3BDaTRsajE1MzE0NDczMzk4MjUucnZ0

http://localhost:8080/admin/oss/queryState?urn=dXJuOmFkc2sub2JqZWN0czpvcy5vYmplY3Q6Y24ubml1a3VkLmRlbW9fYnVja2V0L3BDaTRsajE1MzE0NDczMzk4MjUucnZ0&acceptEncoding=gzip

```
