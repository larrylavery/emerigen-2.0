package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;

public class Cycle<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;
	List<T> circularList = new ArrayList<T>();

	public Cycle() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public T get(int index) {
		int wrappedIndex = index % circularList.size();
		return circularList.get(wrappedIndex);
	}

	@Override
	public T remove(int index) {
		int wrappedIndex = index % circularList.size();
		return circularList.remove(wrappedIndex);
	}

	@Override
	public T set(int index, T element) {
		int wrappedIndex = index % circularList.size();
		return circularList.set(wrappedIndex, element);
	}

	public boolean addAll(int index, List<T> elements) {
		int wrappedIndex = index % circularList.size();
		return circularList.addAll(wrappedIndex, elements);
	}

	public void addAll(int index, T element) {
		int wrappedIndex = index % circularList.size();
		circularList.add(wrappedIndex, element);
	}

}
