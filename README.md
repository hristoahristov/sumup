# Requirements to run the job: 
Java 8 

# To start the job on http://localhost:4000/job-processor/v1/job
1. Go to job-processor/target
2. Execute java -jar job-processor.jar

You will see something like: 
Aug 19, 2021 9:16:42 AM com.sumup.job.processor.rest.JobProcessorRest
INFO: Job processor rest server started successfully on http://localhost:4000
Aug 19, 2021 9:16:42 AM io.vertx.core.impl.launcher.commands.VertxIsolatedDeployer
INFO: Succeeded in deploying verticle 

Additionaly To start the job from IDE use the main method inside JobProcessorRest

#To sort tasks: 
curl -d @test.json http://localhost:4000/job-processor/v1/job

Sample test.json content: 

{"tasks":[{"name":"task1","command":"touch /tmp/file1"},{"name":"task2","command":"echo 'Hello World!' > /tmp/file1","requiredTasks":["task3"]},{"name":"task3","command":"cat /tmp/file1","requiredTasks":["task1"]},{"name":"task4","command":"rm /tmp/file1","requiredTasks":["task2","task3"]}]}

#To return commands as script add to the request json - ("bashScript": true):
 
curl -d @test.json http://localhost:4000/job-processor/v1/job 

Sample test.json content:

{"tasks":[{"name":"task1","command":"touch /tmp/file1"},{"name":"task2","command":"cat /tmp/file1","requiredTasks":["task3"]},{"name":"task3","command":"echo 'Hello World!' > /tmp/file1","requiredTasks":["task1"]},{"name":"task4","command":"rm /tmp/file1","requiredTasks":["task2","task3"]}], "bashScript": true}