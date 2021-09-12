package com.sumup.job.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class JobProcessorTest {
	private static final String JOB_PROCESSOR_URL = "http://localhost:4000/process";
	
	private static final String VALID_UNSORTED_TASK_LIST = "{\"tasks\":[{\"name\":\"task1\",\"command\":\"touch /tmp/file1\"},{\"name\":\"task2\",\"command\":\"echo 'Hello World!' > /tmp/file1\",\"requiredTasks\":[\"task3\"]},{\"name\":\"task3\",\"command\":\"cat /tmp/file1\",\"requiredTasks\":[\"task1\"]},{\"name\":\"task4\",\"command\":\"rm /tmp/file1\",\"requiredTasks\":[\"task2\",\"task3\"]}]}";
	private static final String EXPECTED_SORTED_TASKS_RESPONSE = "[{\"name\":\"task1\",\"command\":\"touch /tmp/file1\"},{\"name\":\"task3\",\"command\":\"cat /tmp/file1\"},{\"name\":\"task2\",\"command\":\"echo 'Hello World!' > /tmp/file1\"},{\"name\":\"task4\",\"command\":\"rm /tmp/file1\"}]";

	private static final String VALID_UNSORTED_TASK_LIST_BASH_SCRIPT = "{\"tasks\":[{\"name\":\"task1\",\"command\":\"touch /tmp/file1\"},{\"name\":\"task2\",\"command\":\"echo 'Hello World!' > /tmp/file1\",\"requiredTasks\":[\"task3\"]},{\"name\":\"task3\",\"command\":\"cat /tmp/file1\",\"requiredTasks\":[\"task1\"]},{\"name\":\"task4\",\"command\":\"rm /tmp/file1\",\"requiredTasks\":[\"task2\",\"task3\"]}], \"bashScript\": true}";
	private static final String EXPECTED_BASH_SCRIPT_RESPONSE = "#!/usr/bin/env bash\ntouch /tmp/file1\ncat /tmp/file1\necho 'Hello World!' > /tmp/file1\nrm /tmp/file1\n";
	
	private static final String INVALID_TASK_LIST_WITH_CYCLE = "{\"tasks\":[{\"name\":\"task1\",\"command\":\"touch /tmp/file1\"},{\"name\":\"task2\",\"command\":\"echo 'Hello World!' > /tmp/file1\",\"requiredTasks\":[\"task3\"]},{\"name\":\"task3\",\"command\":\"cat /tmp/file1\",\"requiredTasks\":[\"task1\",\"task2\"]},{\"name\":\"task4\",\"command\":\"rm /tmp/file1\",\"requiredTasks\":[\"task2\",\"task3\"]}]}";

	private static final String INVALID_TASK_LIST_DUPLICATE_TASK_NAME = "{\"tasks\":[{\"name\":\"task1\",\"command\":\"touch /tmp/file1\"},{\"name\":\"task1\",\"command\":\"echo 'Hello World!' > /tmp/file1\",\"requiredTasks\":[\"task3\"]},{\"name\":\"task3\",\"command\":\"cat /tmp/file1\",\"requiredTasks\":[\"task1\"]},{\"name\":\"task4\",\"command\":\"rm /tmp/file1\",\"requiredTasks\":[\"task2\",\"task3\"]}]}";;
	
	private static MultiValueMap<String, String> headers;

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeAll
	public static void beforeClass() {
		headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
	}

	@Test
	public void testSortValidTaskList() {
		HttpEntity<String> requestEntity = new HttpEntity<String>(VALID_UNSORTED_TASK_LIST, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(JOB_PROCESSOR_URL, requestEntity, String.class);

		assertThat(response.getStatusCodeValue()).isEqualTo(HttpURLConnection.HTTP_OK);
		assertThat(response.getBody()).isEqualTo(EXPECTED_SORTED_TASKS_RESPONSE);
	}

	@Test
	public void testSortValidTaskListBashScript() {
		HttpEntity<String> requestEntity = new HttpEntity<String>(VALID_UNSORTED_TASK_LIST_BASH_SCRIPT, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(JOB_PROCESSOR_URL, requestEntity, String.class);

		assertThat(response.getStatusCodeValue()).isEqualTo(HttpURLConnection.HTTP_OK);
		assertThat(response.getBody()).isEqualTo(EXPECTED_BASH_SCRIPT_RESPONSE);
	}
	
	@Test
	public void testSortTaskListWithCycleNegative() {
		HttpEntity<String> requestEntity = new HttpEntity<String>(INVALID_TASK_LIST_WITH_CYCLE, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(JOB_PROCESSOR_URL, requestEntity, String.class);

		assertThat(response.getStatusCodeValue()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo("Bad request. Check for cycle or invalid tasks.");
	}
	
	@Test
	public void testSortTaskListDuplicateTaskNameNegative() {
		HttpEntity<String> requestEntity = new HttpEntity<String>(INVALID_TASK_LIST_DUPLICATE_TASK_NAME, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(JOB_PROCESSOR_URL, requestEntity, String.class);

		assertThat(response.getStatusCodeValue()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo("Bad request. Empty task list or duplicate task names.");
	}
}
