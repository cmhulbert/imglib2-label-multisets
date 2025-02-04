package net.imglib2.type.label;

/**
 * A storage container to be accessed by {@link MappedAccess}. The storage can
 * grow, see {@link #resize(long)}, which involves reallocating and copying the
 * underlying primitive array.
 *
 * @param <T> the {@link MappedAccess} used to access the container.
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface MappedAccessData<T extends MappedAccess<T>> {

  /**
   * Create a new proxy referring to the region at base offset 0.
   *
   * @return new access (proxy).
   */
  T createAccess();

  /**
   * Update the given {@link MappedAccess} to refer the region starting at
   * {@code baseOffset} in this array.
   */
  void updateAccess(final T access, final long baseOffset);

  /**
   * Get the size in bytes of this container.
   *
   * @return size of this array in bytes.
   */
  long size();

  /**
   * Set the size in bytes of this container.
   */
  void resize(final long size);

  /**
   * A factory for {@link MappedAccessData}.
   *
   * @param <A> the type of {@link MappedAccessData} created by this
   *            factory.
   */
  interface Factory<A extends MappedAccessData<T>, T extends MappedAccess<T>> {

	/**
	 * Create container for {@code size} bytes.
	 */
	A createStorage(final long size);

	/**
	 * Create access (referring to nothing initially).
	 */
	T createAccess();
  }
}
