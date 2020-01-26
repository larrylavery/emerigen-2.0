package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.emerigen.infrastructure.utils.CircularList;

public class CircularListTest {

	CircularList<String> circularList;

	@Test
	public final void givenListWithoutEntries_whenSizeRequested_thenZeroReturned() {

		circularList = new CircularList();
		assertThat(circularList.size()).isEqualTo(0);
	}

	@Test
	public final void givenListWithoutEntries_whenRemoveRequested_thenZeroReturned() {

		circularList = new CircularList();
		String s = "test";
		circularList.remove(s);
		assertThat(circularList.size()).isEqualTo(0);
	}

	@Test
	public final void givenListWithoutEntries_whenEntryAdded_thenSizeIsOne() {

		circularList = new CircularList();
		String s = "test";
		circularList.add(s);
		assertThat(circularList.size()).isEqualTo(1);
	}

	@Test
	public final void givenListWithTwoElements_whenGetWithIndex3Requested_thenFirstIndexObjectRetrieved() {
		circularList = new CircularList<String>();
		String s = "test", s2 = "test2", s3 = "test3";
		circularList.add(s);
		circularList.add(s2);
		circularList.set(1, s3);
		System.out.println("circular list - " + circularList);
		assertThat(circularList.get(1)).isEqualTo(s3);
	}

	@Test
	public final void givenOneEntryList_whenAddAllInvoked_thenListShouldHaveAllEntries() {
		circularList = new CircularList();
		CircularList<String> circularList2 = new CircularList();
		String s = "test", s2 = "test2", s3 = "test3";
		circularList.add(s);
		circularList2.add(s2);
		circularList2.add(s3);
		circularList.addAll(circularList2);
		assertThat(circularList).contains(s).contains(s2).contains(s3);
	}

	@Test
	public final void givenOneEntryList_whenAddAllAtPositionInvoked_thenListShouldHaveAllEntries() {
		circularList = new CircularList<String>();
		CircularList<String> circularList2 = new CircularList();
		String s = "test", s2 = "test2", s3 = "test3";
		circularList.add(s);
		circularList2.add(s2);
		circularList2.add(s3);
		circularList.addAll(0, circularList2);
		assertThat(circularList.get(0)).isEqualTo(s2);
		assertThat(circularList.get(1)).isEqualTo(s3);
		assertThat(circularList.get(2)).isEqualTo(s);
	}

}
