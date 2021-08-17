package com.sumup.job.processor.rest;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

import static com.sumup.job.processor.rest.RestConstants.JOB_PROCESSOR_PATH;
import static com.sumup.job.processor.rest.RestConstants.JOB_REST_SERVER_DEFAULT_PORT;
import static com.sumup.job.processor.rest.RestConstants.MAX_REQUEST_MESSAGE_SIZE_BYTES;
import static com.sumup.job.processor.rest.RestConstants.PORT_PROPERTY;

import com.sumup.job.processor.JobProcessor;
import com.sumup.job.processor.util.ErrorConstants;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class JobProcessorRest extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessorRest.class);

	private final int jobRestServerPort;

	private final JobProcessor jobProcessor;

	public JobProcessorRest() {
		jobRestServerPort = Integer.parseInt(System.getProperty(PORT_PROPERTY, JOB_REST_SERVER_DEFAULT_PORT));
		jobProcessor = new JobProcessor();
	}

	// start it from IDE 
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		vertx.deployVerticle(JobProcessorRest.class.getName(), res -> {
			if (res.succeeded()) {
				LOGGER.info("JobProcessor started successfully.");
			} else {
				LOGGER.error("JobProcessor failed to start.", res.cause());
				vertx.close();
			}
		});
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router router = Router.router(vertx);

		router.post(JOB_PROCESSOR_PATH).handler(BodyHandler.create()).blockingHandler(routingContext -> {
			try {
				jobProcessor.process(routingContext);
			} catch (Exception e) {
				LOGGER.error(ErrorConstants.INTERNAL_SERVER_ERROR, e);
				routingContext.response().setStatusCode(HTTP_INTERNAL_ERROR)
						.setStatusMessage(ErrorConstants.INTERNAL_SERVER_ERROR).end();
			}
		});

		vertx.createHttpServer(new HttpServerOptions().setMaxWebSocketMessageSize(MAX_REQUEST_MESSAGE_SIZE_BYTES))
				.requestHandler(router).listen(jobRestServerPort, res -> {
					if (res.succeeded()) {
						LOGGER.info("Job processor rest server started successfully on http://localhost:"
								+ jobRestServerPort);
						startPromise.complete();
					} else {
						startPromise.fail(res.cause());
					}
				});
	}

}
