package com.emerigen.infrastructure.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class AgentTest {

	@Test
	public final void givenMultipleAgentsCreated_whenAgentReceivesSpreadMessageLikeOnePast_thenBroadcastingStops() {
		// Verify content not changed

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);

		MessageToSpread msg = new MessageToSpread("yyy", 1, 2, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		MessageToSpread msg2 = new MessageToSpread("xxx", 1, 2, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		MessageToSpread msg3 = new MessageToSpread("yyy", 1, 2, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		agents.get(0).spreadMessage(msg);
		agents.get(0).spreadMessage(msg2);
		agents.get(0).spreadMessage(msg3);

		then(agents.get(0).getContent()).as("Content should change to concat agent.content + message.content 2 times")
				.isEqualTo("yyyxxx");

	}

	@Test
	public final void givenMultipleAgentsCreated_whenAgentReceivesSpreadMessageAndHopsNotExceeded_thenBroadcastContinuesWithHopsIncremented() {

		// Given
		Agent a1 = Environment.getInstance().createAgentAtLocation(new Location(4, 4));
		Agent a2 = Environment.getInstance().createAgentAtLocation(new Location(3, 5));
		Agent a3 = Environment.getInstance().createAgentAtLocation(new Location(3, 4));
		Agent a4 = Environment.getInstance().createAgentAtLocation(new Location(3, 3));
		Agent a5 = Environment.getInstance().createAgentAtLocation(new Location(5, 3));
		Agent a6 = Environment.getInstance().createAgentAtLocation(new Location(5, 4));
		Agent a7 = Environment.getInstance().createAgentAtLocation(new Location(5, 5));

		MessageToSpread msg = new MessageToSpread("xxx", 1,
				3, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		a1.spreadMessage(msg);

		// Wait for all agents to propagate the spreadMessage
		try {
			Thread.sleep(500 + Long.parseLong(
					EmerigenProperties.getInstance().getValue("environment.message.spreading.catchup.timer")));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// When neighbors requested

		// Then the content of all messages is changed to "xxx"
		assertThat((String) a1.getContent()).isEqualTo("xxx");
		assertThat((String) a2.getContent()).isEqualTo("xxx");
		assertThat((String) a3.getContent()).isEqualTo("xxx");
		assertThat((String) a4.getContent()).isEqualTo("xxx");
		assertThat((String) a5.getContent()).isEqualTo("xxx");
		assertThat((String) a6.getContent()).isEqualTo("xxx");
		assertThat((String) a7.getContent()).isEqualTo("xxx");
		//softly.assertAll();
	}

	@Test
	public final void givenMultipleAgentsCreated_whenMessageBroadcastedToSpecificAgents_thenOnlyThoseAgentsReceiveMessage() {

		SoftAssertions softly = new SoftAssertions();
		// test content all agents to verify it did or did not change
		// test content of other agents
		// Given
		Agent a1 = Environment.getInstance().createAgentAtLocation(new Location(4, 4));
		Agent a2 = Environment.getInstance().createAgentAtLocation(new Location(3, 5));
		Agent a3 = Environment.getInstance().createAgentAtLocation(new Location(3, 4));
		Agent a4 = Environment.getInstance().createAgentAtLocation(new Location(3, 3));
		Agent a5 = Environment.getInstance().createAgentAtLocation(new Location(5, 3));
		Agent a6 = Environment.getInstance().createAgentAtLocation(new Location(8, 4));
		Agent a7 = Environment.getInstance().createAgentAtLocation(new Location(9, 5));

		MessageToSpread msg = new MessageToSpread("xxx", 1, 
				3, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});
		//TODO				(hashedContent, keyedContent) -> {
//		return (((HashMap<String, Object>) hashedContent)
//				.put(((KeyedContent) keyedContent).getKey(), keyedContent));

		// When spreadMessage invoked
		a1.spreadMessage(msg);

		// Wait for all agents to propagate the spreadMessage
		try {
			Thread.sleep(Long.parseLong(
					EmerigenProperties.getInstance().getValue("environment.message.spreading.catchup.timer")));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Then the content of all messages is changed to "xxx"
		softly.assertThat((String) a1.getContent()).isEqualTo("xxx");
		softly.assertThat((String) a2.getContent()).isEqualTo("xxx");
		softly.assertThat((String) a3.getContent()).isEqualTo("xxx");
		softly.assertThat((String) a4.getContent()).isEqualTo("xxx");
		softly.assertThat((String) a5.getContent()).isEqualTo("xxx");
		softly.assertThat((String) a6.getContent()).isEqualTo("");
		softly.assertThat((String) a7.getContent()).isEqualTo("");
		softly.assertAll();
	}

	@Test
	public final void givenMultipleAgentsCreated_whenMessageBroadcasted_thenAgentsReceiveMessage() {
		// test content of other agents
		// Given
		Agent a1 = Environment.getInstance().createAgentAtLocation(new Location(4, 4));
		Agent a2 = Environment.getInstance().createAgentAtLocation(new Location(3, 5));
		Agent a3 = Environment.getInstance().createAgentAtLocation(new Location(3, 4));
		Agent a4 = Environment.getInstance().createAgentAtLocation(new Location(3, 3));
		Agent a5 = Environment.getInstance().createAgentAtLocation(new Location(5, 3));
		// Agent a6 = Environment.getInstance().createAgentAtLocation( new Location(5,
		// 4));
		// Agent a7 = Environment.getInstance().createAgentAtLocation( new Location(5,
		// 5));


		MessageToSpread msg = new MessageToSpread("xxx", 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		a1.spreadMessage(msg);

		// Wait for all agents to propagate the spreadMessage
		try {
			Thread.sleep(Long.parseLong(
					EmerigenProperties.getInstance().getValue("environment.message.spreading.catchup.timer")));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Then the content of all messages is changed to "xxx"
		then((String) a1.getContent()).isEqualTo("xxx");
		then((String) a2.getContent()).isEqualTo("xxx");
		then((String) a3.getContent()).isEqualTo("xxx");
		then((String) a4.getContent()).isEqualTo("xxx");
		then((String) a5.getContent()).isEqualTo("xxx");
		// then((String)a6.getContent()).isEqualTo("xxx");
		// then((String)a7.getContent()).isEqualTo("xxx");
	}

	@Test
	public final void givenNullMessage_whenSpreadMessageInvoked_thenIllegalArgumentException() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);


		// When broadcast invoked
		final Throwable throwable = catchThrowable(() -> agents.get(0).spreadMessage(null));

		then(throwable).as("A null message should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenEmptyRecipients_whenBroadcastMessageInvoked_thenIllegalArgumentException() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);

		MessageToSpread msg = new MessageToSpread(" xxx", 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When broadcast invoked
		final Throwable throwable = catchThrowable(() -> agents.get(0).broadcastMessage(msg, new ArrayList()));

		then(throwable).as("A empty recipients should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullRecipients_whenBroadcastMessageInvoked_thenIllegalArgumentException() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);

		MessageToSpread msg = new MessageToSpread(" xxx", 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When broadcast invoked
		final Throwable throwable = catchThrowable(() -> agents.get(0).broadcastMessage(msg, null));

		then(throwable).as("A null recipients should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullMessage_whenBroadcastMessageInvoked_thenIllegalArgumentException() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);

		// When broadcast invoked
		final Throwable throwable = catchThrowable(() -> agents.get(0).broadcastMessage(null, agents));

		then(throwable).as("A null message should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		// public void broadcastMessage(MessageToSpread message, List<Agent> recipients) {
	}

	@Test
	public final void givenAgentCreated_whenAgentReceivesSpreadMessage_thenDataUpdatedUsingSuppliedContentUpdateFunction() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);

		MessageToSpread msg = new MessageToSpread("xxx", 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		agents.get(0).spreadMessage(msg);

		then(agents.get(0).getContent()).as("Content should change to concat agent.content + message.content")
				.isEqualTo("xxx");

	}

	@Test
	public final void givenAgentCreated_whenAgentReceivesSpreadMessageLikeOnePast_thenBroadcastingContinues() {
		Environment env = Environment.getInstance();
		// Given agent and spreadMessage
		List<Agent> agents = env.createAgents(1);
		String messageContent = "xxx";

		MessageToSpread msg = new MessageToSpread(messageContent, 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		agents.get(0).spreadMessage(msg);
		agents.get(0).spreadMessage(msg);

		then(agents.get(0).getContent()).as("Content should change to concat agent.content + message.content one time")
				.isEqualTo("xxx");

	}

	@Test
	public final void givenAgentCreated_whenAgentReceivesSpreadMessageUnlikeOnePast_thenBroadcastingContinues() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);

		MessageToSpread msg = new MessageToSpread("yyy", 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		MessageToSpread msg2 = new MessageToSpread("xxx", 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		MessageToSpread msg3 = new MessageToSpread("yyy", 1, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		agents.get(0).spreadMessage(msg);
		agents.get(0).spreadMessage(msg2);
		agents.get(0).spreadMessage(msg3);

		then(agents.get(0).getContent()).as("Content should change to concat agent.content + message.content 2 times")
				.isEqualTo("yyyxxx");

	}

	@Test
	public final void givenMultipleAgentsCreated_whenAgentReceivesSpreadMessageAndHopsNotExceeded_thenBroadcastContinues() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);
		String messageContent = "xxx";

		MessageToSpread msg = new MessageToSpread(messageContent, 5, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		agents.get(0).spreadMessage(msg);

		then(agents.get(0).getContent()).as("Content should change to concat agent.content + message.content one time")
				.isEqualTo("xxx");
	}

	@Test
	public final void givenAgentCreated_whenAgentReceivesSpreadMessageAndHopsExceeded_thenBroadcastingStops() {

		// Given agent and spreadMessage
		List<Agent> agents = Environment.getInstance().createAgents(1);
		String messageContent = "xxx";

		MessageToSpread msg = new MessageToSpread(messageContent, 6, 5, (obj1, obj2) -> {
			return (String) obj1 + (String) obj2;
		});

		// When spreadMessage invoked
		agents.get(0).spreadMessage(msg);

		then(agents.get(0).getContent()).as("Content should NOT change if hops exceeded").isEqualTo("");

	}
	

	@Before
	public void setUp() throws Exception {
		Environment.getInstance().initializeData();
	}


}
