package com.tecdo.starter.oss;

import com.obs.services.ObsClient;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import com.tecdo.starter.oss.domain.OssFile;
import com.tecdo.starter.oss.domain.PacFile;
import com.tecdo.starter.oss.props.OssProperties;
import com.tecdo.starter.oss.rule.OssRule;
import com.tecdo.starter.tool.util.StringPool;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Zeki on 2023/3/13
 */
@AllArgsConstructor
public class HuaweiObsTemplate implements OssTemplate {

	private final ObsClient obsClient;
	private final OssProperties ossProperties;
	private final OssRule ossRule;

	@Override
	public String fileUrl(String fileName) {
		return getOssHost().concat(StringPool.SLASH).concat(fileName);
	}

	@Override
	public PacFile uploadFile(String fileName, InputStream stream) {
		return uploadFile(ossProperties.getBucketName(), fileName, stream);
	}

	@Override
	public PacFile uploadFile(String bucketName, String fileName, InputStream stream) {
		return put(bucketName, stream, fileName, false);
	}

	@Override
	public void removeFile(String fileName) {
		obsClient.deleteObject(ossProperties.getBucketName(), fileName);
	}

	@Override
	public void removeFile(String bucketName, String fileName) {
		obsClient.deleteObject(bucketName, fileName);
	}

	@Override
	public void removeFiles(List<String> fileNames) {
		fileNames.forEach(this::removeFile);
	}

	/**
	 * 上传文件流
	 *
	 * @param bucketName
	 * @param stream
	 * @param key
	 * @param cover
	 * @return
	 */
	@SneakyThrows
	public PacFile put(String bucketName, InputStream stream, String key, boolean cover) {
		String originalName = key;
		key = getFileName(key);

		// 覆盖上传
		if (cover) {
			obsClient.putObject(bucketName, key, stream);
		} else {
			PutObjectResult response = obsClient.putObject(bucketName, key, stream);
			int retry = 0;
			int retryCount = 5;
			while (StringUtils.isEmpty(response.getEtag()) && retry < retryCount) {
				response = obsClient.putObject(bucketName, key, stream);
				retry++;
			}
		}

		PacFile file = new PacFile();
		file.setOriginalName(originalName);
		file.setName(key);
		file.setUrl(fileUrl(key));
		return file;
	}

	/**
	 * 根据规则生成文件名称规则
	 *
	 * @param originalFilename 原始文件名
	 * @return string
	 */
	private String getFileName(String originalFilename) {
		return ossRule.fileName(originalFilename);
	}

	/**
	 * 获取域名
	 *
	 * @return String
	 */
	public String getOssHost() {
		return ossProperties.getCdnUrl();
	}

	@Override
	public boolean existFile(String fileName) {
		try {
			statFile(ossProperties.getBucketName(), fileName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public OssFile statFile(String bucketName, String fileName) {
		ObjectMetadata stat = obsClient.getObjectMetadata(bucketName, fileName);
		OssFile ossFile = new OssFile();
		ossFile.setName(fileName);
		ossFile.setUrl(fileLink(ossFile.getName()));
		ossFile.setHash(stat.getContentMd5());
		ossFile.setLength(stat.getContentLength());
		ossFile.setUploadTime(stat.getLastModified());
		ossFile.setContentType(stat.getContentType());
		return ossFile;
	}

	@Override
	public String fileLink(String fileName) {
		return getOssHost().concat(StringPool.SLASH).concat(fileName);
	}

    @Override
    public BufferedReader download(String fileName) {
		ObsObject obsObject = obsClient.getObject(ossProperties.getBucketName(), fileName);
		return new BufferedReader(new InputStreamReader(obsObject.getObjectContent()));
	}
}
