package com.sumup.job.processor.data.model;

import static com.sumup.job.processor.rest.RestConstants.BASH_SCRIPT;
import static com.sumup.job.processor.rest.RestConstants.TASKS;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobProcessorRequest {

	private List<Task> tasks;

	private boolean bashScript;

	public JobProcessorRequest(@JsonProperty(value = TASKS, required = true) List<Task> tasks,
			@JsonProperty(value = BASH_SCRIPT) boolean bashScript) {
		this.tasks = tasks;
		this.bashScript = bashScript;
	}

	public boolean isBashScript() {
		return bashScript;
	}

	public void setBashScript(boolean bashScript) {
		this.bashScript = bashScript;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
}
