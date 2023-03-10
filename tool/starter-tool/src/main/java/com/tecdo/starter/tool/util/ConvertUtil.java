package com.tecdo.starter.tool.util;

import com.tecdo.starter.tool.convert.PacConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 基于 spring ConversionService 类型转换
 *
 * Created by Zeki on 2022/8/16
 **/
@SuppressWarnings("unchecked")
public class ConvertUtil {

	/**
	 * Convenience operation for converting a source object to the specified targetType.
	 * {@link TypeDescriptor#forObject(Object)}.
	 * @param source the source object
	 * @param targetType the target type
	 * @param <T> 泛型标记
	 * @return the converted value
	 * @throws IllegalArgumentException if targetType is {@code null},
	 * or sourceType is {@code null} but source is not {@code null}
	 */
	@Nullable
	public static <T> T convert(@Nullable Object source, Class<T> targetType) {
		if (source == null) {
			return null;
		}
		if (ClassUtils.isAssignableValue(targetType, source)) {
			return (T) source;
		}
		GenericConversionService conversionService = PacConversionService.getInstance();
		return conversionService.convert(source, targetType);
	}

	/**
	 * Convenience operation for converting a source object to the specified targetType,
	 * where the target type is a descriptor that provides additional conversion context.
	 * {@link TypeDescriptor#forObject(Object)}.
	 * @param source the source object
	 * @param sourceType the source type
	 * @param targetType the target type
	 * @param <T> 泛型标记
	 * @return the converted value
	 * @throws IllegalArgumentException if targetType is {@code null},
	 * or sourceType is {@code null} but source is not {@code null}
	 */
	@Nullable
	public static <T> T convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		GenericConversionService conversionService = PacConversionService.getInstance();
		return (T) conversionService.convert(source, sourceType, targetType);
	}

	/**
	 * Convenience operation for converting a source object to the specified targetType,
	 * where the target type is a descriptor that provides additional conversion context.
	 * Simply delegates to {@link #convert(Object, TypeDescriptor, TypeDescriptor)} and
	 * encapsulates the construction of the source type descriptor using
	 * {@link TypeDescriptor#forObject(Object)}.
	 * @param source the source object
	 * @param targetType the target type
	 * @param <T> 泛型标记
	 * @return the converted value
	 * @throws IllegalArgumentException if targetType is {@code null},
	 * or sourceType is {@code null} but source is not {@code null}
	 */
	@Nullable
	public static <T> T convert(@Nullable Object source, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		GenericConversionService conversionService = PacConversionService.getInstance();
		return (T) conversionService.convert(source, targetType);
	}

}
