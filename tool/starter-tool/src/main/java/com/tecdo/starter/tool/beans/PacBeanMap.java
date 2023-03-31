package com.tecdo.starter.tool.beans;

import org.springframework.asm.ClassVisitor;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.cglib.core.AbstractClassGenerator;
import org.springframework.cglib.core.ReflectUtils;

import java.security.ProtectionDomain;

/**
 * 重写 cglib BeanMap，支持链式bean
 *
 * Created by Zeki on 2022/8/16
 **/
public abstract class PacBeanMap extends BeanMap {
	protected PacBeanMap() {
	}

	protected PacBeanMap(Object bean) {
		super(bean);
	}

	public static PacBeanMap create(Object bean) {
		PacGenerator gen = new PacGenerator();
		gen.setBean(bean);
		return gen.create();
	}

	/**
	 * newInstance
	 *
	 * @param o Object
	 * @return PacBeanMap
	 */
	@Override
	public abstract PacBeanMap newInstance(Object o);

	public static class PacGenerator extends AbstractClassGenerator {
		private static final Source SOURCE = new Source(PacBeanMap.class.getName());

		private Object bean;
		private Class beanClass;
		private int require;

		public PacGenerator() {
			super(SOURCE);
		}

		/**
		 * Set the bean that the generated map should reflect. The bean may be swapped
		 * out for another bean of the same type using {@link #setBean}.
		 * Calling this method overrides any value previously set using {@link #setBeanClass}.
		 * You must call either this method or {@link #setBeanClass} before {@link #create}.
		 *
		 * @param bean the initial bean
		 */
		public void setBean(Object bean) {
			this.bean = bean;
			if (bean != null) {
				beanClass = bean.getClass();
			}
		}

		/**
		 * Set the class of the bean that the generated map should support.
		 * You must call either this method or {@link #setBeanClass} before {@link #create}.
		 *
		 * @param beanClass the class of the bean
		 */
		public void setBeanClass(Class beanClass) {
			this.beanClass = beanClass;
		}

		/**
		 * Limit the properties reflected by the generated map.
		 *
		 * @param require any combination of {@link #REQUIRE_GETTER} and
		 *                {@link #REQUIRE_SETTER}; default is zero (any property allowed)
		 */
		public void setRequire(int require) {
			this.require = require;
		}

		@Override
		protected ClassLoader getDefaultClassLoader() {
			return beanClass.getClassLoader();
		}

		@Override
		protected ProtectionDomain getProtectionDomain() {
			return ReflectUtils.getProtectionDomain(beanClass);
		}

		/**
		 * Create a new instance of the <code>BeanMap</code>. An existing
		 * generated class will be reused if possible.
		 *
		 * @return {PacBeanMap}
		 */
		public PacBeanMap create() {
			if (beanClass == null) {
				throw new IllegalArgumentException("Class of bean unknown");
			}
			setNamePrefix(beanClass.getName());
			PacBeanMapKey key = new PacBeanMapKey(beanClass, require);
			return (PacBeanMap) super.create(key);
		}

		@Override
		public void generateClass(ClassVisitor v) throws Exception {
			new PacBeanMapEmitter(v, getClassName(), beanClass, require);
		}

		@Override
		protected Object firstInstance(Class type) {
			return ((BeanMap) ReflectUtils.newInstance(type)).newInstance(bean);
		}

		@Override
		protected Object nextInstance(Object instance) {
			return ((BeanMap) instance).newInstance(bean);
		}
	}

}
