package com.sumup.job.processor.rest;

public final class RestConstants {
	
	public static final String NAME = "name";
	
	public static final String COMMAND = "command";
	
	public static final String REQUIRES = "requires";
	
	public static final String TASKS = "tasks";
	
	public static final String BASH_SCRIPT = "bashScript";
	
	public static final String USR_BIN_BASH = "#!/usr/bin/env bash";
	
	public static final String UNIX_LINE_SEPARATOR = "\n";
	
	public static final String JOB_PROCESSOR_BASE_PATH = "/job-processor/v1";
	public static final String JOB_PROCESSOR_PATH = JOB_PROCESSOR_BASE_PATH + "/job";

	public static final String JOB_REST_SERVER_DEFAULT_PORT = "4000";
	public static final String PORT_PROPERTY = "job.processor.port";

	public static final int MAX_REQUEST_MESSAGE_SIZE_BYTES = 2 * 1024 * 1024;
}
