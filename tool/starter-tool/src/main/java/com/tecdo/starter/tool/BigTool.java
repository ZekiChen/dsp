package com.tecdo.starter.tool;

import com.tecdo.starter.tool.util.NumberUtil;
import com.tecdo.starter.tool.util.StringPool;
import com.tecdo.starter.tool.util.StringUtil;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 工具包集合，集成了很多工具
 *
 * Created by Zeki on 2022/8/16
 **/
public class BigTool {

	/**
	 * 断言，必须不能为 null
	 * <blockquote><pre>
	 * public Foo(Bar bar) {
	 *     this.bar = $.requireNotNull(bar);
	 * }
	 * </pre></blockquote>
	 *
	 * @param obj the object reference to check for nullity
	 * @param <T> the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException if {@code obj} is {@code null}
	 */
	public static <T> T requireNotNull(T obj) {
		return Objects.requireNonNull(obj);
	}

	/**
	 * 断言，必须不能为 null
	 * <blockquote><pre>
	 * public Foo(Bar bar, Baz baz) {
	 *     this.bar = $.requireNotNull(bar, "bar must not be null");
	 *     this.baz = $.requireNotNull(baz, "baz must not be null");
	 * }
	 * </pre></blockquote>
	 *
	 * @param obj     the object reference to check for nullity
	 * @param message detail message to be used in the event that a {@code
	 *                NullPointerException} is thrown
	 * @param <T>     the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException if {@code obj} is {@code null}
	 */
	public static <T> T requireNotNull(T obj, String message) {
		return Objects.requireNonNull(obj, message);
	}

	/**
	 * 断言，必须不能为 null
	 * <blockquote><pre>
	 * public Foo(Bar bar, Baz baz) {
	 *     this.bar = $.requireNotNull(bar, () -> "bar must not be null");
	 * }
	 * </pre></blockquote>
	 *
	 * @param obj             the object reference to check for nullity
	 * @param messageSupplier supplier of the detail message to be
	 *                        used in the event that a {@code NullPointerException} is thrown
	 * @param <T>             the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException if {@code obj} is {@code null}
	 */
	public static <T> T requireNotNull(T obj, Supplier<String> messageSupplier) {
		return Objects.requireNonNull(obj, messageSupplier);
	}

	/**
	 * 判断对象是否为null
	 * <p>
	 * This method exists to be used as a
	 * {@link java.util.function.Predicate}, {@code filter($::isNull)}
	 * </p>
	 *
	 * @param obj a reference to be checked against {@code null}
	 * @return {@code true} if the provided reference is {@code null} otherwise
	 * {@code false}
	 * @see java.util.function.Predicate
	 */
	public static boolean isNull(@Nullable Object obj) {
		return Objects.isNull(obj);
	}

	/**
	 * 判断对象是否 not null
	 * <p>
	 * This method exists to be used as a
	 * {@link java.util.function.Predicate}, {@code filter($::notNull)}
	 * </p>
	 *
	 * @param obj a reference to be checked against {@code null}
	 * @return {@code true} if the provided reference is non-{@code null}
	 * otherwise {@code false}
	 * @see java.util.function.Predicate
	 */
	public static boolean notNull(@Nullable Object obj) {
		return Objects.nonNull(obj);
	}

	/**
	 * 首字母变小写
	 *
	 * @param str 字符串
	 * @return {String}
	 */
	public static String firstCharToLower(String str) {
		return StringUtil.firstCharToLower(str);
	}

	/**
	 * 首字母变大写
	 *
	 * @param str 字符串
	 * @return {String}
	 */
	public static String firstCharToUpper(String str) {
		return StringUtil.firstCharToUpper(str);
	}

	/**
	 * 判断是否为空字符串
	 * <pre class="code">
	 * $.isBlank(null)		= true
	 * $.isBlank("")		= true
	 * $.isBlank(" ")		= true
	 * $.isBlank("12345")	= false
	 * $.isBlank(" 12345 ")	= false
	 * </pre>
	 *
	 * @param cs the {@code CharSequence} to check (may be {@code null})
	 * @return {@code true} if the {@code CharSequence} is not {@code null},
	 * its length is greater than 0, and it does not contain whitespace only
	 * @see Character#isWhitespace
	 */
	public static boolean isBlank(@Nullable final CharSequence cs) {
		return StringUtil.isBlank(cs);
	}

	/**
	 * 判断不为空字符串
	 * <pre>
	 * $.isNotBlank(null)	= false
	 * $.isNotBlank("")		= false
	 * $.isNotBlank(" ")	= false
	 * $.isNotBlank("bob")	= true
	 * $.isNotBlank("  bob  ") = true
	 * </pre>
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is
	 * not empty and not null and not whitespace
	 * @see Character#isWhitespace
	 */
	public static boolean isNotBlank(@Nullable final CharSequence cs) {
		return StringUtil.isNotBlank(cs);
	}

	/**
	 * 判断是否有任意一个 空字符串
	 *
	 * @param css CharSequence
	 * @return boolean
	 */
	public static boolean isAnyBlank(final CharSequence... css) {
		return StringUtil.isAnyBlank(css);
	}

	/**
	 * 判断是否全为非空字符串
	 *
	 * @param css CharSequence
	 * @return boolean
	 */
	public static boolean isNoneBlank(final CharSequence... css) {
		return StringUtil.isNoneBlank(css);
	}

	/**
	 * 判断对象是数组
	 *
	 * @param obj the object to check
	 * @return 是否数组
	 */
	public static boolean isArray(@Nullable Object obj) {
		return ObjectUtils.isArray(obj);
	}

	/**
	 * 判断空对象 object、map、list、set、字符串、数组
	 *
	 * @param obj the object to check
	 * @return 数组是否为空
	 */
	public static boolean isEmpty(@Nullable Object obj) {
		return ObjectUtils.isEmpty(obj);
	}

	/**
	 * 对象不为空 object、map、list、set、字符串、数组
	 *
	 * @param obj the object to check
	 * @return 是否不为空
	 */
	public static boolean isNotEmpty(@Nullable Object obj) {
		return !ObjectUtils.isEmpty(obj);
	}

	/**
	 * 判断数组为空
	 *
	 * @param array the array to check
	 * @return 数组是否为空
	 */
	public static boolean isEmpty(@Nullable Object[] array) {
		return ObjectUtils.isEmpty(array);
	}

	/**
	 * 判断数组不为空
	 *
	 * @param array 数组
	 * @return 数组是否不为空
	 */
	public static boolean isNotEmpty(@Nullable Object[] array) {
		return !ObjectUtils.isEmpty(array);
	}

	/**
	 * 对象组中是否存在 Empty Object
	 *
	 * @param os 对象组
	 * @return boolean
	 */
	public static boolean hasEmpty(Object... os) {
		for (Object o : os) {
			if (isEmpty(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 对象组中是否全部为 Empty Object
	 *
	 * @param os 对象组
	 * @return boolean
	 */
	public static boolean isAllEmpty(Object... os) {
		for (Object o : os) {
			if (isNotEmpty(o)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 将字符串中特定模式的字符转换成map中对应的值
	 * <p>
	 * use: format("my name is ${name}, and i like ${like}!", {"name":"L.cm", "like": "Java"})
	 *
	 * @param message 需要转换的字符串
	 * @param params  转换所需的键值对集合
	 * @return 转换后的字符串
	 */
	public static String format(@Nullable String message, @Nullable Map<String, ?> params) {
		return StringUtil.format(message, params);
	}

	/**
	 * 同 log 格式的 format 规则
	 * <p>
	 * use: format("my name is {}, and i like {}!", "L.cm", "Java")
	 *
	 * @param message   需要转换的字符串
	 * @param arguments 需要替换的变量
	 * @return 转换后的字符串
	 */
	public static String format(@Nullable String message, @Nullable Object... arguments) {
		return StringUtil.format(message, arguments);
	}

	/**
	 * 格式化执行时间，单位为 ms 和 s，保留三位小数
	 *
	 * @param nanos 纳秒
	 * @return 格式化后的时间
	 */
	public static String format(long nanos) {
		return StringUtil.format(nanos);
	}

	/**
	 * 比较两个对象是否相等。<br>
	 * 相同的条件有两个，满足其一即可：<br>
	 *
	 * @param obj1 对象1
	 * @param obj2 对象2
	 * @return 是否相等
	 */
	public static boolean equals(Object obj1, Object obj2) {
		return Objects.equals(obj1, obj2);
	}

	/**
	 * 安全的 equals
	 *
	 * @param o1 first Object to compare
	 * @param o2 second Object to compare
	 * @return whether the given objects are equal
	 * @see Object#equals(Object)
	 * @see Arrays#equals
	 */
	public static boolean equalsSafe(@Nullable Object o1, @Nullable Object o2) {
		return ObjectUtils.nullSafeEquals(o1, o2);
	}

	/**
	 * 判断数组中是否包含元素
	 *
	 * @param array   the Array to check
	 * @param element the element to look for
	 * @param <T>     The generic tag
	 * @return {@code true} if found, {@code false} else
	 */
	public static <T> boolean contains(@Nullable T[] array, final T element) {
		if (array == null) {
			return false;
		}
		return Arrays.stream(array).anyMatch(x -> ObjectUtils.nullSafeEquals(x, element));
	}

	/**
	 * 判断迭代器中是否包含元素
	 *
	 * @param iterator the Iterator to check
	 * @param element  the element to look for
	 * @return {@code true} if found, {@code false} otherwise
	 */
	public static boolean contains(@Nullable Iterator<?> iterator, Object element) {
		return CollectionUtils.contains(iterator, element);
	}

	/**
	 * 判断枚举是否包含该元素
	 *
	 * @param enumeration the Enumeration to check
	 * @param element     the element to look for
	 * @return {@code true} if found, {@code false} otherwise
	 */
	public static boolean contains(@Nullable Enumeration<?> enumeration, Object element) {
		return CollectionUtils.contains(enumeration, element);
	}

	/**
	 * 不可变 Set
	 *
	 * @param es  对象
	 * @param <E> 泛型
	 * @return 集合
	 */
	@SafeVarargs
	public static <E> Set<E> ofImmutableSet(E... es) {
		Objects.requireNonNull(es, "args es is null.");
		return Arrays.stream(es).collect(Collectors.toSet());
	}

	/**
	 * 不可变 List
	 *
	 * @param es  对象
	 * @param <E> 泛型
	 * @return 集合
	 */
	@SafeVarargs
	public static <E> List<E> ofImmutableList(E... es) {
		Objects.requireNonNull(es, "args es is null.");
		return Arrays.stream(es).collect(Collectors.toList());
	}

	/**
	 * 强转string,并去掉多余空格
	 *
	 * @param str 字符串
	 * @return {String}
	 */
	public static String toStr(Object str) {
		return toStr(str, "");
	}

	/**
	 * 强转string,并去掉多余空格
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return {String}
	 */
	public static String toStr(Object str, String defaultValue) {
		if (null == str || str.equals(StringPool.NULL)) {
			return defaultValue;
		}
		return String.valueOf(str);
	}

	/**
	 * 强转string(包含空字符串),并去掉多余空格
	 *
	 * @param str          字符串
	 * @param defaultValue 默认值
	 * @return {String}
	 */
	public static String toStrWithEmpty(Object str, String defaultValue) {
		if (null == str || str.equals(StringPool.NULL) || str.equals(StringPool.EMPTY)) {
			return defaultValue;
		}
		return String.valueOf(str);
	}


	/**
	 * 判断一个字符串是否是数字
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {boolean}
	 */
	public static boolean isNumeric(final CharSequence cs) {
		return StringUtil.isNumeric(cs);
	}

	/**
	 * 字符串转 int，为空则返回0
	 *
	 * <pre>
	 *   $.toInt(null) = 0
	 *   $.toInt("")   = 0
	 *   $.toInt("1")  = 1
	 * </pre>
	 *
	 * @param str the string to convert, may be null
	 * @return the int represented by the string, or <code>zero</code> if
	 * conversion fails
	 */
	public static int toInt(final Object str) {
		return NumberUtil.toInt(String.valueOf(str));
	}

	/**
	 * 字符串转 int，为空则返回默认值
	 *
	 * <pre>
	 *   $.toInt(null, 1) = 1
	 *   $.toInt("", 1)   = 1
	 *   $.toInt("1", 0)  = 1
	 * </pre>
	 *
	 * @param str          the string to convert, may be null
	 * @param defaultValue the default value
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static int toInt(@Nullable final Object str, final int defaultValue) {
		return NumberUtil.toInt(String.valueOf(str), defaultValue);
	}

	/**
	 * 字符串转 long，为空则返回0
	 *
	 * <pre>
	 *   $.toLong(null) = 0L
	 *   $.toLong("")   = 0L
	 *   $.toLong("1")  = 1L
	 * </pre>
	 *
	 * @param str the string to convert, may be null
	 * @return the long represented by the string, or <code>0</code> if
	 * conversion fails
	 */
	public static long toLong(final Object str) {
		return NumberUtil.toLong(String.valueOf(str));
	}

	/**
	 * 字符串转 long，为空则返回默认值
	 *
	 * <pre>
	 *   $.toLong(null, 1L) = 1L
	 *   $.toLong("", 1L)   = 1L
	 *   $.toLong("1", 0L)  = 1L
	 * </pre>
	 *
	 * @param str          the string to convert, may be null
	 * @param defaultValue the default value
	 * @return the long represented by the string, or the default if conversion fails
	 */
	public static long toLong(@Nullable final Object str, final long defaultValue) {
		return NumberUtil.toLong(String.valueOf(str), defaultValue);
	}

	/**
	 * <p>Convert a <code>String</code> to an <code>Double</code>, returning a
	 * default value if the conversion fails.</p>
	 *
	 * <p>If the string is <code>null</code>, the default value is returned.</p>
	 *
	 * <pre>
	 *   $.toDouble(null, 1) = 1.0
	 *   $.toDouble("", 1)   = 1.0
	 *   $.toDouble("1", 0)  = 1.0
	 * </pre>
	 *
	 * @param value the string to convert, may be null
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static Double toDouble(Object value) {
		return toDouble(String.valueOf(value), -1.00);
	}

	/**
	 * <p>Convert a <code>String</code> to an <code>Double</code>, returning a
	 * default value if the conversion fails.</p>
	 *
	 * <p>If the string is <code>null</code>, the default value is returned.</p>
	 *
	 * <pre>
	 *   $.toDouble(null, 1) = 1.0
	 *   $.toDouble("", 1)   = 1.0
	 *   $.toDouble("1", 0)  = 1.0
	 * </pre>
	 *
	 * @param value        the string to convert, may be null
	 * @param defaultValue the default value
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static Double toDouble(Object value, Double defaultValue) {
		return NumberUtil.toDouble(String.valueOf(value), defaultValue);
	}

	/**
	 * <p>Convert a <code>String</code> to an <code>Float</code>, returning a
	 * default value if the conversion fails.</p>
	 *
	 * <p>If the string is <code>null</code>, the default value is returned.</p>
	 *
	 * <pre>
	 *   $.toFloat(null, 1) = 1.00f
	 *   $.toFloat("", 1)   = 1.00f
	 *   $.toFloat("1", 0)  = 1.00f
	 * </pre>
	 *
	 * @param value the string to convert, may be null
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static Float toFloat(Object value) {
		return toFloat(String.valueOf(value), -1.0f);
	}

	/**
	 * <p>Convert a <code>String</code> to an <code>Float</code>, returning a
	 * default value if the conversion fails.</p>
	 *
	 * <p>If the string is <code>null</code>, the default value is returned.</p>
	 *
	 * <pre>
	 *   $.toFloat(null, 1) = 1.00f
	 *   $.toFloat("", 1)   = 1.00f
	 *   $.toFloat("1", 0)  = 1.00f
	 * </pre>
	 *
	 * @param value        the string to convert, may be null
	 * @param defaultValue the default value
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static Float toFloat(Object value, Float defaultValue) {
		return NumberUtil.toFloat(String.valueOf(value), defaultValue);
	}

	/**
	 * <p>Convert a <code>String</code> to an <code>Boolean</code>, returning a
	 * default value if the conversion fails.</p>
	 *
	 * <p>If the string is <code>null</code>, the default value is returned.</p>
	 *
	 * <pre>
	 *   $.toBoolean("true", true)  = true
	 *   $.toBoolean("false")   	= false
	 *   $.toBoolean("", false)  	= false
	 * </pre>
	 *
	 * @param value the string to convert, may be null
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static Boolean toBoolean(Object value) {
		return toBoolean(value, null);
	}

	/**
	 * <p>Convert a <code>String</code> to an <code>Boolean</code>, returning a
	 * default value if the conversion fails.</p>
	 *
	 * <p>If the string is <code>null</code>, the default value is returned.</p>
	 *
	 * <pre>
	 *   $.toBoolean("true", true)  = true
	 *   $.toBoolean("false")   	= false
	 *   $.toBoolean("", false)  	= false
	 * </pre>
	 *
	 * @param value        the string to convert, may be null
	 * @param defaultValue the default value
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static Boolean toBoolean(Object value, Boolean defaultValue) {
		if (value != null) {
			String val = String.valueOf(value);
			val = val.toLowerCase().trim();
			return Boolean.parseBoolean(val);
		}
		return defaultValue;
	}

	/**
	 * 转换为Integer数组<br>
	 *
	 * @param str 被转换的值
	 * @return 结果
	 */
	public static Integer[] toIntArray(String str) {
		return toIntArray(",", str);
	}

	/**
	 * 转换为Integer数组<br>
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static Integer[] toIntArray(String split, String str) {
		if (ObjectUtils.isEmpty(str)) {
			return new Integer[]{};
		}
		String[] arr = str.split(split);
		final Integer[] ints = new Integer[arr.length];
		for (int i = 0; i < arr.length; i++) {
			final Integer v = toInt(arr[i], 0);
			ints[i] = v;
		}
		return ints;
	}

	/**
	 * 转换为Integer集合<br>
	 *
	 * @param str 结果被转换的值
	 * @return 结果
	 */
	public static List<Integer> toIntList(String str) {
		return Arrays.asList(toIntArray(str));
	}

	/**
	 * 转换为IntegerSet集合<br>
	 *
	 * @param str 结果被转换的值
	 * @return 结果
	 */
	public static Set<Integer> toIntSet(String str) {
		return new HashSet<>(toIntList(str));
	}

	/**
	 * 转换为Integer集合<br>
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static List<Integer> toIntList(String split, String str) {
		return Arrays.asList(toIntArray(split, str));
	}

	/**
	 * 获取第一位Integer数值
	 *
	 * @param str 被转换的值
	 * @return 结果
	 */
	public static Integer firstInt(String str) {
		return firstInt(",", str);
	}

	/**
	 * 获取第一位Integer数值
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static Integer firstInt(String split, String str) {
		List<Integer> ints = toIntList(split, str);
		if (isEmpty(ints)) {
			return null;
		} else {
			return ints.get(0);
		}
	}

	/**
	 * 转换为Long数组<br>
	 *
	 * @param str 被转换的值
	 * @return 结果
	 */
	public static Long[] toLongArray(String str) {
		return toLongArray(",", str);
	}

	/**
	 * 转换为Long数组<br>
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static Long[] toLongArray(String split, String str) {
		if (ObjectUtils.isEmpty(str)) {
			return new Long[]{};
		}
		String[] arr = str.split(split);
		final Long[] longs = new Long[arr.length];
		for (int i = 0; i < arr.length; i++) {
			final Long v = toLong(arr[i], 0);
			longs[i] = v;
		}
		return longs;
	}

	/**
	 * 转换为Long集合<br>
	 *
	 * @param str 结果被转换的值
	 * @return 结果
	 */
	public static List<Long> toLongList(String str) {
		return Arrays.asList(toLongArray(str));
	}

	/**
	 * 转换为Set集合<br>
	 *
	 * @param str 结果被转换的值
	 * @return 结果
	 */
	public static Set<Long> toLongSet(String str) {
		return new HashSet<>(toLongList(str));
	}

	/**
	 * 转换为Long集合<br>
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static List<Long> toLongList(String split, String str) {
		return Arrays.asList(toLongArray(split, str));
	}

	/**
	 * 转换为Long集合<br>
	 *
	 * @param intList 结果被转换的值
	 * @return 结果
	 */
	public static List<Long> toLongList(List<Integer> intList) {
		if (Objects.isNull(intList)) {
			return Collections.emptyList();
		}
		return intList.stream().mapToLong(Integer::longValue).boxed().collect(Collectors.toList());
	}

	/**
	 * 获取第一位Long数值
	 *
	 * @param str 被转换的值
	 * @return 结果
	 */
	public static Long firstLong(String str) {
		return firstLong(",", str);
	}

	/**
	 * 获取第一位Long数值
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static Long firstLong(String split, String str) {
		List<Long> longs = toLongList(split, str);
		if (isEmpty(longs)) {
			return null;
		} else {
			return longs.get(0);
		}
	}

	/**
	 * 转换为String数组<br>
	 *
	 * @param str 被转换的值
	 * @return 结果
	 */
	public static String[] toStrArray(String str) {
		return toStrArray(",", str);
	}

	/**
	 * 转换为String数组<br>
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static String[] toStrArray(String split, String str) {
		if (isBlank(str)) {
			return new String[]{};
		}
		return str.split(split);
	}

	/**
	 * 转换为String集合<br>
	 *
	 * @param str 结果被转换的值
	 * @return 结果
	 */
	public static List<String> toStrList(String str) {
		return Arrays.asList(toStrArray(str));
	}

	/**
	 * 转换为String集合<br>
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static List<String> toStrList(String split, String str) {
		return Arrays.asList(toStrArray(split, str));
	}

	/**
	 * 获取第一位String数值
	 *
	 * @param str 被转换的值
	 * @return 结果
	 */
	public static String firstStr(String str) {
		return firstStr(",", str);
	}

	/**
	 * 获取第一位String数值
	 *
	 * @param split 分隔符
	 * @param str   被转换的值
	 * @return 结果
	 */
	public static String firstStr(String split, String str) {
		List<String> strs = toStrList(split, str);
		if (isEmpty(strs)) {
			return null;
		} else {
			return strs.get(0);
		}
	}

	/**
	 * 将 long 转短字符串 为 62 进制
	 *
	 * @param num 数字
	 * @return 短字符串
	 */
	public static String to62String(long num) {
		return NumberUtil.to62String(num);
	}

	/**
	 * 将集合拼接成字符串，默认使用`,`拼接
	 *
	 * @param coll the {@code Collection} to convert
	 * @return the delimited {@code String}
	 */
	public static String join(Collection<?> coll) {
		return StringUtil.join(coll);
	}

	/**
	 * 将集合拼接成字符串，默认指定分隔符
	 *
	 * @param coll  the {@code Collection} to convert
	 * @param delim the delimiter to use (typically a ",")
	 * @return the delimited {@code String}
	 */
	public static String join(Collection<?> coll, String delim) {
		return StringUtil.join(coll, delim);
	}

	/**
	 * 将数组拼接成字符串，默认使用`,`拼接
	 *
	 * @param arr the array to display
	 * @return the delimited {@code String}
	 */
	public static String join(Object[] arr) {
		return StringUtil.join(arr);
	}

	/**
	 * 将数组拼接成字符串，默认指定分隔符
	 *
	 * @param arr   the array to display
	 * @param delim the delimiter to use (typically a ",")
	 * @return the delimited {@code String}
	 */
	public static String join(Object[] arr, String delim) {
		return StringUtil.join(arr, delim);
	}

	/**
	 * 切分字符串，不去除切分后每个元素两边的空白符，不去除空白项
	 *
	 * @param str       被切分的字符串
	 * @param separator 分隔符字符
	 * @return 切分后的集合
	 */
	public static List<String> split(CharSequence str, char separator) {
		return StringUtil.split(str, separator, -1);
	}

	/**
	 * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
	 *
	 * @param str       被切分的字符串
	 * @param separator 分隔符字符
	 * @return 切分后的集合
	 */
	public static List<String> splitTrim(CharSequence str, char separator) {
		return StringUtil.splitTrim(str, separator);
	}

	/**
	 * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
	 *
	 * @param str       被切分的字符串
	 * @param separator 分隔符字符
	 * @return 切分后的集合
	 */
	public static List<String> splitTrim(CharSequence str, CharSequence separator) {
		return StringUtil.splitTrim(str, separator);
	}

	/**
	 * 分割 字符串
	 *
	 * @param str       字符串
	 * @param delimiter 分割符
	 * @return 字符串数组
	 */
	public static String[] split(@Nullable String str, @Nullable String delimiter) {
		return StringUtil.delimitedListToStringArray(str, delimiter);
	}

	/**
	 * 分割 字符串 删除常见 空白符
	 *
	 * @param str       字符串
	 * @param delimiter 分割符
	 * @return 字符串数组
	 */
	public static String[] splitTrim(@Nullable String str, @Nullable String delimiter) {
		return StringUtil.delimitedListToStringArray(str, delimiter, " \t\n\n\f");
	}

	/**
	 * 字符串是否符合指定的 表达式
	 *
	 * <p>
	 * pattern styles: "xxx*", "*xxx", "*xxx*" and "xxx*yyy"
	 * </p>
	 *
	 * @param pattern 表达式
	 * @param str     字符串
	 * @return 是否匹配
	 */
	public static boolean simpleMatch(@Nullable String pattern, @Nullable String str) {
		return PatternMatchUtils.simpleMatch(pattern, str);
	}

	/**
	 * 字符串是否符合指定的 表达式
	 *
	 * <p>
	 * pattern styles: "xxx*", "*xxx", "*xxx*" and "xxx*yyy"
	 * </p>
	 *
	 * @param patterns 表达式 数组
	 * @param str      字符串
	 * @return 是否匹配
	 */
	public static boolean simpleMatch(@Nullable String[] patterns, String str) {
		return PatternMatchUtils.simpleMatch(patterns, str);
	}

	/**
	 * 生成uuid
	 *
	 * @return UUID
	 */
	public static String randomUUID() {
		return StringUtil.randomUUID();
	}

}
