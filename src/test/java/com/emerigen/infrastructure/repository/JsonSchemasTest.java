package com.emerigen.infrastructure.repository;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

/**
 * @author Larry
 *
 */
public class JsonSchemasTest {

	@Test
	public void givenValidJsonCycleWithTwoCycleNodes_whenValidating_thenItShouldValidateSuccessfully() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-valid-two-cycle-nodes.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();

	}

	@Test
	public void givenValidJsonCycle_whenValidating_thenItShouldValidateSuccessfully() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-valid.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();

	}

	@Test
	public void givenInvalidJsonCycleMissingCycleNode_whenValidating_thenValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-invalid-no-cycle-node.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();
	}

	@Test
	public void givenInvalidJsonCycleMissingSensorEvent_whenValidating_thenValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-invalid-no-sensor-event.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();

	}

	@Test
	public void givenValidJsonEntity_whenValidating_thenItShouldValidateSuccessfully() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("entity.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/entity-valid.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();

	}

	@Test
	public void givenJsonEntityWithoutChannels_whenValidating_thenItShouldThrowValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("entity.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/entity-invalid-no-channels.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should not be thrown
		then(throwable).as(
				"An invalidly structured Json entity document without channels should throw a ValidationException")
				.isInstanceOf(ValidationException.class);

	}

	@Test
	public void givenJsonEntityWithoutTimestamp_whenValidating_thenItShouldThrowValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("entity.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/entity-invalid-no-timestamp.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should not be thrown
		then(throwable).as(
				"An invalidly structured Json entity document without a timestamp should throw a ValidationException")
				.isInstanceOf(ValidationException.class);

	}

	@Test
	public void givenJsonEntityWithoutEntityID_whenValidating_thenItShouldThrowValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("entity.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/entity-invalid-no-entity-id.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should not be thrown
		then(throwable).as(
				"A invalidly structured Json entity document without a entityID should throw a ValidationException")
				.isInstanceOf(ValidationException.class);

	}

	@Test
	public void givenJsonEntityWithEmptyChannelArray_whenValidating_thenItShouldThrowValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("entity.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/entity-invalid-empty-channels.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should not be thrown
		then(throwable).as(
				"A invalidly structured Json entity document without empty channels list should throw a ValidationException")
				.isInstanceOf(ValidationException.class);

	}

	@Test
	public void givenJsonEntityWithNoChannelType_whenValidating_thenItShouldThrowValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("entity.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/entity-invalid-no-channel-type.json");

		JSONObject jsonSchema = new JSONObject(new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should not be thrown
		then(throwable).as(
				"A invalidly structured Json entity document without channel type should throw a ValidationException")
				.isInstanceOf(ValidationException.class);

	}
}
