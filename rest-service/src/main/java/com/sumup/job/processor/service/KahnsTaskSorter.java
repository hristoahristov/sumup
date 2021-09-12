package com.sumup.job.processor.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.sumup.job.processor.data.Task;

@Service
public class KahnsTaskSorter implements TaskSorter {

	@Override
	public boolean isTaskListValid(List<Task> taskList) {
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
	@Override
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
