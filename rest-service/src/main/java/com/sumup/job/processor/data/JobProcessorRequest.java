package com.sumup.job.processor.data;

import static com.sumup.job.processor.constants.RestConstants.BASH_SCRIPT;
import static com.sumup.job.processor.constants.RestConstants.TASKS;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobProcessorRequest {

	private List<Task> tasks;

	private Boolean bashScript;

	public JobProcessorRequest(@JsonProperty(value = TASKS, required = true) List<Task> tasks,
			@JsonProperty(value = BASH_SCRIPT) Boolean bashScript) {
		this.tasks = tasks;
		this.bashScript = bashScript;
	}

	public Boolean isBashScript() {
		return bashScript != null ? bashScript : false;
	}

	public void setBashScript(Boolean bashScript) {
		this.bashScript = bashScript;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
}
