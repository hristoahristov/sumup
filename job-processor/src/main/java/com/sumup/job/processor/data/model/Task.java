package com.sumup.job.processor.data.model;

import static com.sumup.job.processor.rest.RestConstants.COMMAND;
import static com.sumup.job.processor.rest.RestConstants.NAME;
import static com.sumup.job.processor.rest.RestConstants.REQUIRES;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

public class Task {

	private String name;

	private String command;

	@JsonInclude(Include.NON_NULL)
	@JsonProperty(access = Access.WRITE_ONLY)
	private List<String> requiredTasks;

	//used only for sorting
	@JsonIgnore
	private int indegree = 0;
	
	@JsonCreator
	public Task(@JsonProperty(value = NAME, required = true) String name,
			@JsonProperty(value = COMMAND, required = true) String command,
			@JsonProperty(value = REQUIRES) List<String> requiredTasks) {
		this.name = name;
		this.command = command;
		this.requiredTasks = requiredTasks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<String> getRequiredTasks() {
		return requiredTasks;
	}

	public void setRequiredTasks(List<String> requiresTasks) {
		this.requiredTasks = requiresTasks;
	}

	public int getIndegree() {
		return indegree;
	}

	public void increaseIndegree() {
		this.indegree += 1;
	}
	
	public int decreaseIndegree() {
		return --indegree;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof Task) {
			Task otherTask = (Task) other;
			if ((getName() != null) && (getName().equals(otherTask.getName()))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getName() != null ? getName().hashCode() : 1;
	}
}
