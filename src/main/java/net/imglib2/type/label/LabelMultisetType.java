package net.imglib2.type.label;

import net.imglib2.img.NativeImg;
import net.imglib2.type.AbstractNativeType;
import net.imglib2.type.NativeTypeFactory;
import net.imglib2.type.label.RefList.RefIterator;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Fraction;

import java.math.BigInteger;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class LabelMultisetType extends AbstractNativeType<LabelMultisetType> implements IntegerType<LabelMultisetType> {

  public interface Entry<E> {

	E getElement();

	int getCount();
  }

  public static final LabelMultisetType type = new LabelMultisetType();

  private final NativeImg<?, VolatileLabelMultisetArray> img;

  private VolatileLabelMultisetArray access;

  private final LabelMultisetEntryList entries;

  private final Set<Entry<Label>> entrySet;

  // this is the constructor if you want it to read from an array
  public LabelMultisetType(final NativeImg<?, VolatileLabelMultisetArray> img) {

	this(img, null);
  }

  // this is the constructor if you want to specify the dataAccess
  public LabelMultisetType(final VolatileLabelMultisetArray access) {

	this(null, access);
  }

  // this is the constructor if you want it to be a variable
  public LabelMultisetType() {

	this(null, new VolatileLabelMultisetArray(1, true, new long[]{Label.INVALID}));
  }

  // this is the constructor if you want it to be a variable
  public LabelMultisetType(final LabelMultisetEntry entry) {

	this();
	access.getValue(i.get(), this.entries);
	this.entries.add(entry);
	this.access.setArgMax(i.get(), entry.getId());
  }

  // this is the constructor if you want it to be a variable
  public LabelMultisetType(final LabelMultisetEntryList entries) {

	this();
	access.getValue(i.get(), this.entries);
	this.entries.addAll(entries);
	updateArgMax();
  }

  private LabelMultisetType(final NativeImg<?, VolatileLabelMultisetArray> img, final VolatileLabelMultisetArray access) {

	this.entries = new LabelMultisetEntryList();
	this.img = img;
	this.access = access;
	this.entrySet = new AbstractSet<Entry<Label>>() {

	  private final RefIterator<Entry<Label>> iterator = new RefIterator<Entry<Label>>() {

		private final RefIterator<LabelMultisetEntry> it = entries.iterator();

		@Override
		public boolean hasNext() {

		  return it.hasNext();
		}

		@Override
		public LabelMultisetEntry next() {

		  return it.next();
		}

		@Override
		public void release() {

		  it.release();
		}

		@Override
		public void reset() {

		  it.reset();
		}
	  };

	  @Override
	  public RefIterator<Entry<Label>> iterator() {

		iterator.reset();
		return iterator;
	  }

	  @Override
	  public int size() {

		return entries.size();
	  }

	  @Override
	  public Stream<Entry<Label>> stream() {

		throw new UnsupportedOperationException("Streams are not compatible with " + getClass().getName() + " because its iterator reuses the same reference.");
	  }

	  @Override
	  public Stream<Entry<Label>> parallelStream() {

		throw new UnsupportedOperationException("Streams are not compatible with " + getClass().getName() + " because its iterator reuses the same reference.");
	  }
	};
  }

  @Override
  public Fraction getEntitiesPerPixel() {

	return new Fraction();
  }

  @Override
  public void updateContainer(final Object c) {

	access = img.update(c);
  }

  @Override
  public LabelMultisetType createVariable() {

	return new LabelMultisetType();
  }

  @Override
  public LabelMultisetType copy() {

	final LabelMultisetType that = new LabelMultisetType(img, access);
	that.i.set(this.i.get());
	return that;
  }

  @Override
  public void set(final LabelMultisetType c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public NativeTypeFactory<LabelMultisetType, ?> getNativeTypeFactory() {

	throw new UnsupportedOperationException();
  }

  @Override
  public LabelMultisetType duplicateTypeOnSameNativeImg() {

	return new LabelMultisetType(img);
  }

  // ==== Multiset< SuperVoxel > =====

  public int size() {

	access.getValue(i.get(), entries);
	return entries.multisetSize();
  }

  public boolean isEmpty() {

	access.getValue(i.get(), entries);
	return entries.isEmpty();
  }

  public boolean contains(final Label l) {

	return contains(l.id());
  }

  public boolean contains(final long id) {

	access.getValue(i.get(), entries);
	return entries.binarySearch(id) >= 0;
  }

  public boolean containsAll(final long[] ids) {

	access.getValue(i.get(), entries);
	for (final long id : ids) {
	  if (entries.binarySearch(id) < 0) {
		return false;
	  }
	}
	return true;
  }

  public boolean containsAll(final Collection<? extends Label> c) {

	access.getValue(i.get(), entries);
	for (final Label l : c) {
	  if (entries.binarySearch(l.id()) < 0) {
		return false;
	  }
	}
	return true;
  }

  public int count(final Label l) {

	return count(l.id());
  }

  public int count(final long id) {

	access.getValue(i.get(), entries);
	final int pos = entries.binarySearch(id);
	if (pos < 0) {
	  return 0;
	}

	return entries.get(pos).getCount();
  }

  public int countWithRef(final long id, LabelMultisetEntry ref) {

	access.getValue(i.get(), entries);
	final int pos = entries.binarySearch(id, ref);
	if (pos < 0) {
	  return 0;
	}

	return entries.get(pos).getCount();
  }

  public Set<Entry<Label>> entrySet() {

	access.getValue(i.get(), entries);
	return entrySet;
  }

  public Set<LabelMultisetEntry> entrySetWithRef(LabelMultisetEntry ref) {

	access.getValue(i.get(), entries);
	return new AbstractSet<LabelMultisetEntry>() {

	  @Override public Iterator<LabelMultisetEntry> iterator() {

		return new Iterator<LabelMultisetEntry>() {

		  int idx = 0;

		  @Override public boolean hasNext() {

			return idx < size();
		  }

		  @Override public LabelMultisetEntry next() {

			return entries.get(idx++, ref);
		  }
		};
	  }

	  @Override public int size() {

		return entries.size();
	  }
	};
  }

  @Override
  public String toString() {

	access.getValue(i.get(), entries);
	return entries.toString();
  }

  // for volatile type
  boolean isValid() {

	return access.isValid();
  }

  @Override
  public boolean valueEquals(final LabelMultisetType other) {

	if (entries.size() != other.entries.size()) {
	  return false;
	}

	final RefIterator<LabelMultisetEntry> ai = entries.iterator();
	final RefIterator<LabelMultisetEntry> bi = other.entries.iterator();

	while (ai.hasNext()) {
	  final LabelMultisetEntry a = ai.next();
	  final LabelMultisetEntry b = bi.next();
	  if (!(a.getId() == b.getId() && a.getCount() == b.getCount())) {
		return false;
	  }
	}
	return true;
  }

  public VolatileLabelMultisetArray getAccess() {

	return this.access;
  }

  @Override
  public void inc() {

	throw new UnsupportedOperationException();
  }

  @Override
  public void dec() {

	throw new UnsupportedOperationException();
  }

  @Override
  public double getMaxValue() {

	throw new UnsupportedOperationException();
  }

  @Override
  public double getMinValue() {

	throw new UnsupportedOperationException();
  }

  @Override
  public double getMinIncrement() {

	throw new UnsupportedOperationException();
  }

  @Override
  public int getBitsPerPixel() {

	throw new UnsupportedOperationException();
  }

  @Override
  public double getRealDouble() {

	return getIntegerLong();
  }

  @Override
  public float getRealFloat() {

	return getIntegerLong();
  }

  @Override
  public double getImaginaryDouble() {

	return 0;
  }

  @Override
  public float getImaginaryFloat() {

	return 0;
  }

  @Override
  public void setReal(final float f) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setReal(final double f) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setImaginary(final float f) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setImaginary(final double f) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setComplexNumber(final float r, final float i) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setComplexNumber(final double r, final double i) {

	throw new UnsupportedOperationException();
  }

  @Override
  public float getPowerFloat() {

	return getRealFloat();
  }

  @Override
  public double getPowerDouble() {

	return getRealDouble();
  }

  @Override
  public float getPhaseFloat() {

	return 0;
  }

  @Override
  public double getPhaseDouble() {

	return 0;
  }

  @Override
  public void complexConjugate() {

	throw new UnsupportedOperationException();
  }

  @Override
  public void add(final LabelMultisetType c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void mul(final LabelMultisetType c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void sub(final LabelMultisetType c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void div(final LabelMultisetType c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setOne() {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setZero() {

	throw new UnsupportedOperationException();
  }

  @Override
  public void mul(final float c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void mul(final double c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(final LabelMultisetType arg0) {

	throw new UnsupportedOperationException();
  }

  @Override
  public int getInteger() {

	return (int)getIntegerLong();
  }

  @Override
  public long getIntegerLong() {

	return argMax();
  }

  @Override
  public BigInteger getBigInteger() {

	final BigInteger mask = new BigInteger("FFFFFFFFFFFFFFFF", 16);
	return BigInteger.valueOf(argMax()).and(mask);
  }

  @Override
  public void setInteger(final int f) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setInteger(final long f) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void setBigInteger(final BigInteger b) {

	throw new UnsupportedOperationException();
  }

  public long argMax() {

	return this.access.argMax(i.get());
  }

  public void updateArgMax() {

	this.access.setArgMax(i.get(), LabelUtils.getArgMax(entrySet()));
  }

  public static LabelMultisetType singleEntryWithSingleOccurrence() {

	return singleEntryWithNumOccurrences(1);
  }

  public static LabelMultisetType singleEntryWithNumOccurrences(final int numOccurrences) {

	return new LabelMultisetType(new LabelMultisetEntry(Label.INVALID, numOccurrences));
  }

  @Override
  public void pow(final LabelMultisetType c) {

	throw new UnsupportedOperationException();
  }

  @Override
  public void pow(final double d) {

	throw new UnsupportedOperationException();
  }
}
