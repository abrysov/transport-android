/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

/**
 * Class similar to the Optional<T> from Guava but can have 3 states: absent,
 * null and value
 */
public abstract class DbOptional<T> implements Serializable {

	/**
	 * 
	 */	
	private static final long serialVersionUID = 0;
	
	/**
	 * 
	 * @return
	 */
	public static <T> DbOptional<T> absent() {

		return DbOptionalAbsent.withType();
	}

	/**
	 * 
	 * @return
	 */
	public static <T> DbOptional<T> nullValue() {

		return DbOptionalNull.withType();
	}

	/**
	 * 
	 * @param reference
	 * @return
	 */
	public static <T> DbOptional<T> of(T reference) {

		return ((null == reference) ? DbOptional.<T> nullValue()
				: new DbOptionalPresent<T>(reference));
	}

	/**
	 * 
	 * @param nullableReference
	 * @return
	 */
	public static <T> DbOptional<T> fromNullable(T nullableReference) {

		return (nullableReference == null) ? DbOptional.<T> nullValue()
				: new DbOptionalPresent<T>(nullableReference);
	}

	/**
	 * 
	 */
	DbOptional() {

	}

	/**
	 * 
	 * @return
	 */
	public abstract boolean isPresent();

	/**
	 * 
	 * @return
	 */
	public abstract boolean isNull();

	/**
	 * 
	 * @return
	 */
	public abstract T get();

	/**
	 * 
	 * @param defaultValue
	 * @return
	 */
	public abstract T or(T defaultValue);

	/**
	 * 
	 * @param secondChoice
	 * @return
	 */
	public abstract DbOptional<T> or(DbOptional<? extends T> secondChoice);

	/**
	 * 
	 * @return
	 */
	public abstract T orNull();

	/**
	 * 
	 */
	@Override
	public abstract boolean equals(Object object);

	/**
	 * 
	 */
	@Override
	public abstract int hashCode();

	/**
	 * 
	 */
	@Override
	public abstract String toString();
	
	/**
	 * 
	 * @author L3063
	 *
	 * @param <T>
	 */
	public static class DbOptionalAbsent<T> extends DbOptional<T> {

		/**
		 * 
		 */
		static final DbOptionalAbsent<Object> INSTANCE = new DbOptionalAbsent<Object>();
		private static final long serialVersionUID = 0;

		/**
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		static <T> DbOptional<T> withType() {
			
			return (DbOptional<T>) INSTANCE;
		}

		/**
		 * 
		 */
		private DbOptionalAbsent() {

		}

		/**
		 * 
		 */
		@Override
		public boolean isPresent() {
			
			return false;
		}

		/**
		 * 
		 */
		@Override
		public boolean isNull() {
			
			return false;
		}

		/**
		 * 
		 */
		@Override
		public T get() {
			
			throw new IllegalStateException("DbOptional.get() cannot be called on an absent value");
		}

		/**
		 * 
		 */
		@Override
		public T or(T defaultValue) {
			
			return checkNotNull(defaultValue, "use DbOptional.orNull() instead of DbOptional.or(null)");
		}

		/**
		 * 
		 */
		@SuppressWarnings("unchecked")
		@Override
		public DbOptional<T> or(DbOptional<? extends T> secondChoice) {
			
			return (DbOptional<T>) checkNotNull(secondChoice);
		}

		/**
		 * 
		 */
		@Override
		public T orNull() {
			
			return null;
		}

		/**
		 * 
		 */
		@Override
		public boolean equals(Object object) {
			
			return object == this;
		}

		/**
		 * 
		 */
		@Override
		public int hashCode() {
			
			return 0x598df91c;
		}

		/**
		 * 
		 */
		@Override
		public String toString() {
			
			return "DbOptional.absent()";
		}

		/**
		 * 
		 * @return
		 */
		private Object readResolve() {
			
			return INSTANCE;
		}
	}
	
	public static class DbOptionalNull<T> extends DbOptional<T>{
		/**
		 * 
		 */
		static final DbOptionalNull<Object> INSTANCE = new DbOptionalNull<Object>();
		private static final long serialVersionUID = 0;

		/**
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		static <T> DbOptional<T> withType() {
			
			return (DbOptional<T>) INSTANCE;
		}

		/**
		 * 
		 */
		private DbOptionalNull() {

		}

		/**
		 * 
		 */
		@Override
		public boolean isPresent() {
			
			return false;
		}

		/**
		 * 
		 */
		@Override
		public boolean isNull() {
			
			return true;
		}

		/**
		 * 
		 */
		@Override
		public T get() {
			
			return null;
		}

		/**
		 * 
		 */
		@Override
		public T or(T defaultValue) {
			
			return checkNotNull(defaultValue, "use DbOptional.orNull() instead of DbOptional.or(null)");
		}

		/**
		 * 
		 */
		@SuppressWarnings("unchecked")
		@Override
		public DbOptional<T> or(DbOptional<? extends T> secondChoice) {
			
			return (DbOptional<T>) checkNotNull(secondChoice);
		}

		/**
		 * 
		 */
		@Override
		public T orNull() {
			
			return null;
		}

		/**
		 * 
		 */
		@Override
		public boolean equals(Object object) {
			
			return object == this;
		}

		/**
		 * 
		 */
		@Override
		public int hashCode() {
			
			return 0x598df91d;
		}

		/**
		 * 
		 */
		@Override
		public String toString() {
			
			return "DbOptional.nullValue()";
		}

		/**
		 * 
		 * @return
		 */
		private Object readResolve() {
			
			return INSTANCE;
		}
	}
	
	public static class DbOptionalPresent<T> extends DbOptional<T> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 0;

		/**
		 * 
		 */
		private final T reference;

		/**
		 * 
		 * @param reference
		 */
		DbOptionalPresent(T reference) {
			
			this.reference = reference;
		}

		/**
		 * 
		 */
		@Override
		public boolean isPresent() {
			
			return (null != reference);
		}

		/**
		 * 
		 */
		@Override
		public boolean isNull() {
			
			return (null == reference);
		}

		/**
		 * 
		 */
		@Override
		public T get() {
			
			return reference;
		}

		/**
		 * 
		 */
		@Override
		public T or(T defaultValue) {
			
			checkNotNull(defaultValue, "use DbOptional.orNull() instead of DbOptional.or(null)");
			return reference;
		}

		/**
		 * 
		 */
		@Override
		public DbOptional<T> or(DbOptional<? extends T> secondChoice) {
			
			checkNotNull(secondChoice);
			return this;
		}

		/**
		 * 
		 */
		@Override
		public T orNull() {
			
			return reference;
		}

		/**
		 * 
		 */
		@Override
		public boolean equals(Object object) {
			
			if (object instanceof DbOptionalPresent) {
				
				DbOptionalPresent<?> other = (DbOptionalPresent<?>) object;
				return reference.equals(other.reference);
			}
			return false;
		}

		/**
		 * 
		 */
		@Override
		public int hashCode() {
			
			return 0x598df91c + reference.hashCode();
		}

		/**
		 * 
		 */
		@Override
		public String toString() {
			return "DbOptional.of(" + reference + ")";
		}
	}
}