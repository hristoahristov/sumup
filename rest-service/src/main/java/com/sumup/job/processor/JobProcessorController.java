package com.sumup.job.processor;

import static com.sumup.job.processor.constants.RestConstants.UNIX_LINE_SEPARATOR;
import static com.sumup.job.processor.constants.RestConstants.USR_BIN_BASH;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumup.job.processor.data.JobProcessorRequest;
import com.sumup.job.processor.data.Task;
import com.sumup.job.processor.service.TaskSorter;

@RestController
public class JobProcessorController {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessorController.class);

	@Autowired
	private TaskSorter taskSorter;
	
	private final ObjectMapper mapper;

	public JobProcessorController() {
		mapper = new ObjectMapper();
	}

	/*
	 * @PostMapping(value = "/process", consumes = {
	 * MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE
	 * }, produces = MediaType.APPLICATION_JSON_VALUE) public List<Task>
	 * process(@RequestParam JobProcessorRequest request) {
	 * 
	 * return request.getTasks(); }
	 */

	@PostMapping(value = "/process", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> process(@RequestParam MultiValueMap<String, String> requestMap) {
		Set<String> requestMapKeys = requestMap.keySet();
		if (requestMapKeys == null || requestMapKeys.isEmpty()) {
			return ResponseEntity.badRequest().body("Bad request. Empty request body.");
		}

		JobProcessorRequest request = null;
		try {
			request = mapper.readValue(requestMapKeys.iterator().next(), JobProcessorRequest.class);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.badRequest().body("Bad request. Invalid request body.");
		}

		if (!taskSorter.isTaskListValid(request.getTasks())) {
			return ResponseEntity.badRequest().body("Bad request. Empty task list or duplicate task names.");
		}

		List<Task> sortedTasks = taskSorter.sortTasks(request.getTasks());
		if (sortedTasks.isEmpty()) {
			return ResponseEntity.badRequest().body("Bad request. Check for cycle or invalid tasks.");
		}

		ResponseEntity<String> response = null;
		try {
			response = generateResponse(sortedTasks, request.isBashScript());
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
			response = ResponseEntity.internalServerError().body("Internal server error.");
		}

		return response;
	}

	private ResponseEntity<String> generateResponse(List<Task> sortedTasks, boolean isBashScript)
			throws JsonProcessingException {
		String responseBody = null;
		if (isBashScript) {
			StringBuilder builder = new StringBuilder();
			builder.append(USR_BIN_BASH).append(UNIX_LINE_SEPARATOR);

			sortedTasks.stream().forEach(task -> {
				builder.append(task.getCommand()).append(UNIX_LINE_SEPARATOR);
			});

			responseBody = builder.toString();
		} else {
			responseBody = mapper.writeValueAsString(sortedTasks);
		}

		return ResponseEntity.ok(responseBody);
	}
}
