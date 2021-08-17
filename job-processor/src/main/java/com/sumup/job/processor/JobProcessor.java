package com.sumup.job.processor;

import static com.sumup.job.processor.rest.RestConstants.UNIX_LINE_SEPARATOR;
import static com.sumup.job.processor.rest.RestConstants.USR_BIN_BASH;
import static com.sumup.job.processor.util.ErrorConstants.INVALID_REQUEST_ERROR;
import static io.vertx.core.json.jackson.DatabindCodec.mapper;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sumup.job.processor.data.model.JobProcessorRequest;
import com.sumup.job.processor.data.model.Task;

import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class JobProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessor.class);

	public void process(RoutingContext context) throws JsonProcessingException {
		JobProcessorRequest request = null;
		try {
			request = mapper().convertValue(context.getBodyAsJson(), new TypeReference<JobProcessorRequest>() {
			});
		} catch (IllegalArgumentException e) {
			LOGGER.debug(INVALID_REQUEST_ERROR, e);
			context.response().setStatusCode(HTTP_BAD_REQUEST).setStatusMessage(INVALID_REQUEST_ERROR)
					.end(e.getMessage());
			return;
		}

		if (!isRequestValid(request.getTasks())) {
			context.response().setStatusCode(HTTP_BAD_REQUEST).setStatusMessage(INVALID_REQUEST_ERROR)
					.end("Check for repeaded task tames.");
			return;
		}

		List<Task> sortedTasks = sortTasks(request.getTasks());
		if (sortedTasks.isEmpty()) {
			context.response().setStatusCode(HTTP_BAD_REQUEST).setStatusMessage(INVALID_REQUEST_ERROR)
					.end("Check for cycle or invalid required tasks.");
			return;
		}

		sendResponse(context, sortedTasks, request.isBashScript());
	}

	private void sendResponse(RoutingContext context, List<Task> sortedTasks, boolean isBashScript)
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
			responseBody = DatabindCodec.prettyMapper().writeValueAsString(sortedTasks);
		}

		context.response().setStatusCode(HTTP_OK).end(responseBody);
	}

	public boolean isRequestValid(List<Task> taskList) {
		if (taskList.isEmpty()) {
			return false;
		}

		if (taskList.size() != new HashSet<Task>(taskList).size()) {
			return false;
		}

		return true;
	}

	/**
	 * Uses Kahn’s Topological Sort Algorithm
	 */
	public List<Task> sortTasks(List<Task> taskList) {
		Map<String, Task> taskMap = new HashMap<>();

		for (Task task : taskList) {
			taskMap.put(task.getName(), task);
		}

		// calculate task indegree
		for (Task task : taskList) {
			if (task.getRequiredTasks() != null) {
				for (String requiredTaskName : task.getRequiredTasks()) {
					Task requiredTask = taskMap.get(requiredTaskName);
					// invalid required task name
					if (requiredTask == null) {
						return new ArrayList<Task>();
					}
					requiredTask.increaseIndegree();
				}
			}
		}

		// tasks with zero indegree
		Queue<String> zeroIngegreeQueue = new LinkedList<String>();
		taskMap.forEach((taskName, task) -> {
			if (task.getIndegree() == 0) {
				zeroIngegreeQueue.add(taskName);
			}
		});

		int counter = 0;

		// sort the tasks in reverse order
		List<Task> sortedTaskList = new LinkedList<Task>();
		while (!zeroIngegreeQueue.isEmpty()) {
			Task sortedTask = taskMap.get(zeroIngegreeQueue.poll());

			sortedTaskList.add(sortedTask);
			counter++;

			if (sortedTask.getRequiredTasks() != null) {
				sortedTask.getRequiredTasks().stream().forEach(requiredTask -> {
					if (taskMap.get(requiredTask).decreaseIndegree() == 0) {
						zeroIngegreeQueue.add(requiredTask);
					}
				});
			}
		}

		// check for cycle in the required tasks
		if (counter != taskList.size()) {
			return new LinkedList<Task>();
		}

		Collections.reverse(sortedTaskList);

		return sortedTaskList;
	}
}
