package com.tecdo.starter.tool.convert;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 类型 转换 服务，添加了 IEnum 转换
 *
 * Created by Zeki on 2022/8/16
 **/
public class PacConversionService extends ApplicationConversionService {

	@Nullable
	private static volatile PacConversionService SHARED_INSTANCE;

	public PacConversionService() {
		this(null);
	}

	public PacConversionService(@Nullable StringValueResolver embeddedValueResolver) {
		super(embeddedValueResolver);
		super.addConverter(new EnumToStringConverter());
		super.addConverter(new StringToEnumConverter());
	}

	/**
	 * Return a shared default application {@code ConversionService} instance, lazily
	 * building it once needed.
	 * <p>
	 * Note: This method actually returns an {@link PacConversionService}
	 * instance. However, the {@code ConversionService} signature has been preserved for
	 * binary compatibility.
	 * @return the shared {@code PacConversionService} instance (never{@code null})
	 */
	public static GenericConversionService getInstance() {
		PacConversionService sharedInstance = PacConversionService.SHARED_INSTANCE;
		if (sharedInstance == null) {
			synchronized (PacConversionService.class) {
				sharedInstance = PacConversionService.SHARED_INSTANCE;
				if (sharedInstance == null) {
					sharedInstance = new PacConversionService();
					PacConversionService.SHARED_INSTANCE = sharedInstance;
				}
			}
		}
		return sharedInstance;
	}

}
