package com.emerigen.infrastructure.evaporation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.environment.Environment;

public class EvaporationTest {

	@Test
	public void givenObjectWithRelevance_whenCreatedAndRetrievedWithMinRelevanceAfterSleepWithMinimum_thenNullReturned()
			throws InterruptedException {

		// Given

		// When
		Environment.getInstance().setInformationWithRelevance("uniqueInfoKey",
				"relevant info");

		Thread.sleep(2000);

		// Then
		assertThat(Environment.getInstance()
				.getInformationWithMinimumRelevance("uniqueInfoKey", 1.0)).isNull();
	}

	@Test
	public void givenObjectWithRelevance_whenCreatedAndGotAgain_thenRelevanceShouldBeGreatorThanOriginal() {

		// Given
		Environment.getInstance().setInformationWithRelevance("uniqueInfoKey",
				"relevant info");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Then
		assertThat(Environment.getInstance().getInformationWithRelevance("uniqueInfoKey"))
				.isNotNull().hasSameHashCodeAs("relevant info");

		// Increment relevance via couple of retrieves
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");

		assertThat(Environment.getInstance()
				.getInformationWithMinimumRelevance("uniqueInfoKey", .94)).isNotNull();
	}

	@Test
	public void givenObjectWithRelevance_whenCreatedAndEvaporated_thenGetMinimumMethodShouldReturnNull()
			throws InterruptedException {

		// Given
		Environment.getInstance().setInformationWithRelevance("uniqueInfoKey",
				"relevant info");
		assertThat(Environment.getInstance().getInformationWithRelevance("uniqueInfoKey"))
				.isNotNull();

		// Then
		assertThat(Environment.getInstance().getInformationWithRelevance("uniqueInfoKey"))
				.isNotNull();
		Thread.sleep(1000);

		assertThat(Environment.getInstance()
				.getInformationWithMinimumRelevance("uniqueInfoKey", .99)).isNull();

		assertThat(Environment.getInstance()
				.getInformationWithMinimumRelevance("uniqueInfoKey", .97)).isNull();

		assertThat(Environment.getInstance()
				.getInformationWithMinimumRelevance("uniqueInfoKey", .50)).isNotNull();

	}

	@Test
	public void givenObjectWithRelevance_whenCreatedAndSetMultipleTimes_thenGetMinimumWith100MethodShouldNotReturnNull()
			throws InterruptedException {

		// Given
		Environment.getInstance().setInformationWithRelevance("uniqueInfoKey",
				"relevant info");
		assertThat(Environment.getInstance().getInformationWithRelevance("uniqueInfoKey"))
				.isNotNull();

		// Then
		assertThat(Environment.getInstance().getInformationWithRelevance("uniqueInfoKey"))
				.isNotNull();
		Thread.sleep(1000);

		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");
		Environment.getInstance().getInformationWithRelevance("uniqueInfoKey");

		assertThat(Environment.getInstance()
				.getInformationWithMinimumRelevance("uniqueInfoKey", 1.02)).isNotNull();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
