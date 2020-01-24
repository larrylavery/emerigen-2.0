package com.emerigen.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.knowledge.ChannelType;
import com.emerigen.knowledge.Entity;

public class EntityTest {

	@Test
	public final void givenInvalidEntityWithNullEntityID_whenCreated_thenItShouldThrowIllegalArgumentException() {

		// Given
		List<ChannelType> channels = new ArrayList<ChannelType>();
		ChannelType channelType = new ChannelType("channelTypexx",
				"/Information/dev/logger/src/main/resources/channel-type-1.csv",
				false);
		channels.add(channelType);

		// When
		final Throwable throwable = catchThrowable(() -> new Entity(null, channels));

		// Then
		then(throwable).as("A IllegalArgumentException should be thrown for a null entityID")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidEntityWithEmptyEntityID_whenCreated_thenItShouldThrowIllegalArgumentException() {

		// Given
		List<ChannelType> channels = new ArrayList<ChannelType>();
		ChannelType channelType = new ChannelType("channelTypexx",
				"/Information/dev/logger/src/main/resources/channel-type-1.csv",
				false);
		channels.add(channelType);

		// When
		final Throwable throwable = catchThrowable(() -> new Entity("", channels));

		// Then
		then(throwable).as("A IllegalArgumentException should be thrown for an empty entityID")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidEntityWithEmptyChannels_whenCreated_thenItShouldThrowIllegalArgumentException() {

		// Given
		String entityUuid = UUID.randomUUID().toString();
		List<ChannelType> channels = new ArrayList<ChannelType>();

		// When
		final Throwable throwable = catchThrowable(() -> new Entity(entityUuid, channels));

		// Then
		then(throwable).as("A IllegalArgumentException should be thrown for an empty channels")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidEntityWithNullChannels_whenCreated_thenItShouldThrowIllegalArgumentException() {

		// Given
		String entityUuid = UUID.randomUUID().toString();

		// When
		final Throwable throwable = catchThrowable(() -> new Entity(entityUuid, null));

		// Then
		then(throwable).as("A IllegalArgumentException should be thrown for a null channels")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenValidEntity_whenTranslatedAndLogged_thenItshouldBeTheSameWhenRetrieved() {

		// Given
		String entityUuid = UUID.randomUUID().toString();
		List<ChannelType> channels = new ArrayList<ChannelType>();
		ChannelType channelType = new ChannelType("channelTypexx",
				"/Information/dev/logger/src/main/resources/channel-type-1.csv",
				false);
		channels.add(channelType);

		//When
		Entity entity = new Entity(entityUuid, channels);
		KnowledgeRepository.getInstance().newEntity(entity);

		Entity  retrievedEntity = KnowledgeRepository.getInstance().getEntity(entity.getEntityID());
		assertThat(retrievedEntity).isEqualTo(entity);

	}


	
	@Test
	public final void givenValidEntity_whenCreated_thenItShouldValidateSuccessfully() {

		// Given
		String entityUuid = UUID.randomUUID().toString();
		List<ChannelType> channels = new ArrayList<ChannelType>();
		ChannelType channelType = new ChannelType("channelTypexx",
				"/Information/dev/logger/src/main/resources/channel-type-1.csv",
				false);
		ChannelType channelType2 = new ChannelType("channelTypeyy",
				"/Information/dev/logger/src/main/resources/channel-type-1.csv",
				false);
		channels.add(channelType);
		channels.add(channelType2);

		//When
		Entity entity = new Entity(entityUuid, channels);

		assertThat(entity.getChannels().get(0)).isEqualTo(channelType);
		assertThat(entity.getChannels().get(1)).isEqualTo(channelType2);

	}

	@Test
	public final void givenValidNewEntity_whenCreated_thenItShouldRetrievSuccessfully() {

		// Given
		String entityUuid = UUID.randomUUID().toString();
		List<ChannelType> channels = new ArrayList<ChannelType>();
		ChannelType channelType = new ChannelType("channelTypexx",
				"/Information/dev/logger/src/main/resources/channel-type-1.csv",
				false);
		ChannelType channelType2 = new ChannelType("channelTypeyy",
				"/Information/dev/logger/src/main/resources/channel-type-1.csv",
				false);
		channels.add(channelType);
		channels.add(channelType2);

		//When
		Entity entity = new Entity(entityUuid, channels);
		KnowledgeRepository.getInstance().newEntity(entity);
		String key = entity.getEntityID();
		Entity entityAfter = KnowledgeRepository.getInstance().getEntity(key);

		assertThat(entityAfter.getEntityID()).isEqualTo(entity.getEntityID());
		assertThat(entityAfter.getChannels()).isEqualTo(entity.getChannels());

	}
	


}
