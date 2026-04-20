package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import br.com.microservices.orchestrated.orchestratorservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;

@Component
public class SagaExecutionController {
	
	private Logger logger = Logger.getLogger(SagaExecutionController.class.getName());
	
	
	public ETopics getNextTopic(Event event) {
		if(event.getSource().toString().isEmpty() || event.getStatus().toString().isEmpty()) {
			throw new ValidationException("Source and Status must be informed");
		}   
		
		ETopics topic = findTopicBySourceAndStatus(event);
		logCurrentSaga(event, topic);
		return topic;
	}
	
	private ETopics findTopicBySourceAndStatus(Event event) {
		return (ETopics) (Arrays.stream(SagaHandler.SAGA_HANDLER)
			.filter(row -> isEventAndStatusSourceValid(event, row))
			.map(i -> i[SagaHandler.TOPIC_INDEX])
			.findFirst()
			.orElseThrow(() -> new ValidationException("Topic not found")));
	}
	
	private boolean isEventAndStatusSourceValid(Event event, Object[] row) {
		var source = row[SagaHandler.EVENT_SOURCE_INDEX];
		var status = row[SagaHandler.SAGA_STATUS_INDEX];
		
		return event.getSource().equals(source) && event.getStatus().equals(status);
	}
	
	private void logCurrentSaga(Event event, ETopics topic) {
		String sagaId = createSagaId(event);
		String source = event.getSource().toString();
		
		switch(event.getStatus()) {
			case SUCCESS -> this.logger.log(Level.INFO, "### CURRENT SAGA: " + source + " | SUCCESS | NEXT TOPIC " + topic + " | " + sagaId);
			case ROLLBACK_PENDING -> this.logger.log(Level.INFO, "### CURRENT SAGA: " + source + " | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC " + topic + " | " + sagaId);
			case FAIL -> this.logger.log(Level.INFO, "### CURRENT SAGA: " + source + " | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC " + topic + " | " + sagaId);
		}
		
	}
	
	public String createSagaId(Event event) {
		return String.format(
			"ORDER ID: %s | TRANSACTION ID: %s | EVENT ID: %s",
			event.getPayload().getId(),
			event.getTransactionId(),
			event.getId()
		);
	}
}
