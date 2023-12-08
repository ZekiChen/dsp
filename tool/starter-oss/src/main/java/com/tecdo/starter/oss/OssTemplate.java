package com.tecdo.starter.oss;

import com.tecdo.starter.oss.domain.OssFile;
import com.tecdo.starter.oss.domain.PacFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Zeki on 2023/3/13
 */
public interface OssTemplate {

	/**
	 * 获取文件地址
	 *
	 * @param fileName 存储桶对象名称
	 * @return String
	 */
	String fileUrl(String fileName);

	/**
	 * 上传文件
	 *
	 * @param file 上传文件类
	 * @return PacFile
	 */
//	PacFile uploadFile(MultipartFile file);

	/**
	 * 上传文件
	 *
	 * @param file     上传文件类
	 * @param fileName 上传文件名
	 * @return PacFile
	 */
//	PacFile uploadFile(String fileName, MultipartFile file);

	/**
	 * 上传文件
	 *
	 * @param bucketName 存储桶名称
	 * @param fileName   上传文件名
	 * @param file       上传文件类
	 * @return PacFile
	 */
	PacFile uploadFile(String bucketName, String fileName, MultipartFile file);

	/**
	 * 上传文件
	 *
	 * @param fileName 存储桶对象名称
	 * @param stream   文件流
	 * @return PacFile
	 */
	PacFile uploadFile(String fileName, InputStream stream);

	/**
	 * 上传文件
	 *
	 * @param bucketName 存储桶名称
	 * @param fileName   存储桶对象名称
	 * @param stream     文件流
	 * @return PacFile
	 */
	PacFile uploadFile(String bucketName, String fileName, InputStream stream);

	/**
	 * 删除文件
	 *
	 * @param fileName 存储桶对象名称
	 */
	void removeFile(String fileName);

	/**
	 * 删除文件
	 *
	 * @param bucketName 存储桶名称
	 * @param fileName   存储桶对象名称
	 */
	void removeFile(String bucketName, String fileName);

	/**
	 * 批量删除文件
	 *
	 * @param fileNames 存储桶对象名称集合
	 */
	void removeFiles(List<String> fileNames);

	/**
	 * 批量删除文件
	 *
	 * @param bucketName 存储桶名称
	 * @param fileNames  存储桶对象名称集合
	 */
//	void removeFiles(String bucketName, List<String> fileNames);

	/**
	 * 文件是否存在
	 *
	 * @param fileName 存储桶文件名称
	 * @return boolean
	 */
	boolean existFile(String fileName);

	/**
	 * 获取文件信息
	 *
	 * @param bucketName 存储桶名称
	 * @param fileName   存储桶文件名称
	 * @return InputStream
	 */
	OssFile statFile(String bucketName, String fileName);

	/**
	 * 获取文件地址
	 *
	 * @param fileName 存储桶对象名称
	 * @return String
	 */
	String fileLink(String fileName);

	/**
	 * 加载文件
	 *
	 * @param fileName 存储桶对象名称
	 * @return BufferedReader 字符流
	 */
	BufferedReader download(String fileName);
}
