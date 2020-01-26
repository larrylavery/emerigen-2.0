package com.emerigen.infrastructure.utils;

import java.util.ArrayList;
import java.util.List;

public class CircularList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;
	int wrappedIndex;

	public CircularList() {
		super();
	}

	@Override
	public T get(int index) {
		if (index < 0)
			throw new IllegalArgumentException("Index must be zero or greater");

		if (index < size())
			return super.get(index);

		// Index needs to wrap
		wrappedIndex = index % size();
		return super.get(wrappedIndex);
	}

	@Override
	public T remove(int index) {
		if (index < 0)
			throw new IllegalArgumentException("Index must be zero or greater");

		if (index < size())
			return super.remove(index);

		// Index needs to wrap
		wrappedIndex = index % size();
		return super.remove(wrappedIndex);
	}

	@Override
	public T set(int index, T element) {
		if (index < 0)
			throw new IllegalArgumentException("Index must be zero or greater");

		if (index < size()) {
			return super.set(index, element);

		} else if (size() > 0) {

			// Index needs to wrap
			wrappedIndex = index % size();
			return super.set(wrappedIndex, element);
		} else {

			// Index within bounds call super
			return super.set(index, element);
		}
	}

	public boolean addAll(int index, List<T> elements) {
		if (index < 0)
			throw new IllegalArgumentException("Index must be zero or greater");
		if (elements == null)
			throw new IllegalArgumentException("elements to add must not be null");

		if (index < size())
			return super.addAll(index, elements);

		// Index needs to wrap
		if (size() > 0) {
			wrappedIndex = index % size();
			return super.addAll(wrappedIndex, elements);
		} else {
			return super.addAll(index, elements);
		}

	}

}
