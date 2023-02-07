

package com.tecdo.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * cache key 封装
 *
 * Created by Zeki on 2023/2/6
 */
@Getter
@ToString
@AllArgsConstructor
public class CacheKey {

	/**
	 * redis key
	 */
	private final String key;
	/**
	 * 超时时间 秒
	 */
	@Nullable
	private Duration expire;

	public CacheKey(String key) {
		this.key = key;
	}

}
