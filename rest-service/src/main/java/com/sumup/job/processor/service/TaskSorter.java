package com.sumup.job.processor.service;

import java.util.List;

import com.sumup.job.processor.data.Task;

public interface TaskSorter {
	
	public boolean isTaskListValid(List<Task> taskList);

	public List<Task> sortTasks(List<Task> taskList);
}
