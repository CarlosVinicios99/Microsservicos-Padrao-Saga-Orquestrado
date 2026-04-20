package br.com.microservices.orchestrated.orchestratorservice.core.service;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.History;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import br.com.microservices.orchestrated.orchestratorservice.core.producer.SagaOrchestratorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;

@Service
public class OrchestratorService {
	
	private Logger logger = Logger.getLogger(OrchestratorService.class.getName());
	
	@Autowired
	private SagaOrchestratorProducer producer;
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired
	private SagaExecutionController sagaExecutionController;
	
	
	public void startSaga(Event event) {
		event.setSource(EEventSource.ORCHESTRATOR);
		event.setStatus(ESagaStatus.SUCCESS);
		ETopics topic = getTopic(event);
		this.logger.log(Level.INFO, "SAGA STARTED!");
		addHistory(event, "Saga started!");
		producer.sendEvent(this.jsonUtil.toJson(event), topic.getTopic());
	}
	
	public void continueSaga(Event event) {
		ETopics topic = getTopic(event);
		this.logger.log(Level.INFO, "SAGA CONTINUING FOR EVENT " + event.getId());
		producer.sendEvent(this.jsonUtil.toJson(event), topic.getTopic());
	}
	
	public void finishSagaSuccess(Event event) {
		event.setSource(EEventSource.ORCHESTRATOR);
		event.setStatus(ESagaStatus.SUCCESS);
		this.logger.log(Level.INFO, "SAGA FINISHED SUCCESSFULLY FOR EVENT " + event.getId());
		addHistory(event, "Saga finished successfully");
		notifyFinishedSaga(event);
	}
	
	public void finishSagaFail(Event event) {
		event.setSource(EEventSource.ORCHESTRATOR);
		event.setStatus(ESagaStatus.FAIL);
		this.logger.log(Level.INFO, "SAGA FINISHED WITH ERRORS FOR EVENT " + event.getId());
		addHistory(event, "Saga finished with errors!");
		notifyFinishedSaga(event);
	}
	
	private ETopics getTopic(Event event) {
		return this.sagaExecutionController.getNextTopic(event);
	}
	
	private void addHistory(Event event, String message) {
		History history = new History();
		
		history.setSource(event.getSource());
		history.setStatus(event.getStatus());
		history.setMessage(message);
		history.setCreatedAt(LocalDateTime.now());
		
		event.addToHistory(history);
	}
	
	private void notifyFinishedSaga(Event event) {
		producer.sendEvent(this.jsonUtil.toJson(event), ETopics.NOTIFY_ENDING.getTopic());
	}
}
