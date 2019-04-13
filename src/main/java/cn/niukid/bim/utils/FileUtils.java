package cn.niukid.bim.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
	
	private static final int FILE_SIZE = 4 * 1024;

	public static boolean copyFile(File source, String destination) {
		InputStream in = null;
		OutputStream out = null;
		boolean ret = true;
		try {
			File target = new File(destination);
			if(target.isDirectory()){
				target.mkdirs();
				return false;
			}
			target.getParentFile().mkdirs();
			in = new BufferedInputStream(new FileInputStream(source), FILE_SIZE);
			out = new BufferedOutputStream(new FileOutputStream(target),FILE_SIZE);
			byte[] word = new byte[FILE_SIZE];
			while (in.read(word) > 0) {
				out.write(word);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			ret = false;
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				ret = false;
			}
		}
		return ret;
	}
	
	/** 
	 * 删除单个文件 
	 * @param   sPath    被删除文件的文件名 
	 * @return 单个文件删除成功返回true，否则返回false 
	 */  
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		if (!file.exists())
			return true;
		// 路径为文件且不为空则进行删除
		if (file.isFile()) {
			file.delete();
			flag = true;
		}
		return flag;
	}
	/** 
	 * 删除目录（文件夹）以及目录下的文件 
	 * @param   sPath 被删除目录的文件路径 
	 * @return  目录删除成功返回true，否则返回false 
	 */  
	public static boolean deleteDirectory(String sPath) {  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }else{  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	} 
	/** 
	 *  根据路径删除指定的目录或文件，无论存在与否 
	 *@param sPath  要删除的目录或文件 
	 *@return 删除成功返回 true，否则返回 false。 
	 */  
	public static  boolean DeleteFolder(String sPath) {  
	   // boolean flag = false;  
	    File  file = new File(sPath);  
	    // 判断目录或文件是否存在  
	    if (!file.exists()) {  // 不存在返回 true  
	        return true;  
	    } else {  
	        // 判断是否为文件  
	        if (file.isFile()) {  // 为文件时调用删除文件方法  
	            return deleteFile(sPath);  
	        } else {  // 为目录时调用删除目录方法  
	            return deleteDirectory(sPath);  
	        }  
	    }  
	} 
	

	/**
	* 文件打包的方法
	* @param files 文件信息集合（文件的存放完整路径名称）
	* @param savePath 存储打包文件的路径and filename
	* @return true文件打包成功 false 文件打包失败
	*/
	public static boolean createFilesToZipUsingFilenames(List<String> filenames, String savePath) {
		
		if(filenames!=null && filenames.size()>0) 
		{
			List<File> files = new ArrayList<File>(filenames.size());
			for(String filename:filenames)
				files.add(new File(filename));
			return createFilesToZipUsingFile(files,savePath);
		}else {
			log.warn("list is empty when createFilesToZip");
			return false;
		}
	}
	/**
	* 文件打包的方法
	* @param files 文件信息集合（文件的存放完整路径名称）
	* @param savePath 存储打包文件的路径and filename
	* @return true文件打包成功 false 文件打包失败
	*/
	public static boolean createFilesToZipUsingFile(List<File> files, String savePath) {
		
		boolean result = false;
		// 定义字节流
		byte[] buffer = new byte[FILE_SIZE];
		// 定义zip流
		ZipOutputStream out = null;
		try {
			// 定义打包文件名和存放的路径
			log.info("save zipfile path：" + savePath);
			out = new ZipOutputStream(new FileOutputStream(savePath));
			if (null != files && !files.isEmpty()) {
				//log.info("files number：" + files.size());
				for (int i = 0; i < files.size(); i++) {
					File file = files.get(i);
					// 判断文件是否为空
					if (file.exists()) {
						// 创建输入流
						FileInputStream fis = new FileInputStream(file);
						// 获取文件名
						String name = file.getName();
						// 创建zip对象
						ZipEntry zipEntry = new ZipEntry(name);
						out.putNextEntry(zipEntry);
						int len;
						// 读入需要下载的文件的内容，打包到zip文件
						while ((len = fis.read(buffer)) > 0) {
							out.write(buffer, 0, len);
						}
						out.closeEntry();
						fis.close();
					}
				}
			}
			out.close();
			//log.debug("zip files done");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}
	
	public static boolean tryCreateDir(String dir)
	{
		if (StringUtils.isEmpty(dir))
			return false;
		try {
			File file = new File(dir);
			if (!file.exists()) {
				file.mkdirs(); // 创建文件夹
			}
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		} 
		return false;	
	}
	
	public  static String getRelativeFileName(String prefix,String cat,String orginalFileName,boolean rename)
	{
		StringBuffer sb = new StringBuffer();
		if(prefix!=null)sb.append(prefix).append("/");
		if(cat!=null) sb.append(cat).append("/");
		if(rename)
		{
			String ext = orginalFileName.substring(orginalFileName.lastIndexOf("."));
			sb.append(SixTwoUUID.generateFixedLenRandomStr(6)).append(System.currentTimeMillis()).append(ext);
		}else
		{
			sb.append(orginalFileName);
		}
		return sb.toString();
	}
}
