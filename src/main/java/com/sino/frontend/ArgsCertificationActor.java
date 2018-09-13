package com.sino.frontend;

import com.sino.frontend.HttpServerRouter.ESBResult;
import com.sino.frontend.HttpServerRouter.Params;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ArgsCertificationActor extends AbstractActor {
	
	private final LoggingAdapter log = Logging.getLogger(context().system(), this);

	static Props props() {
		return Props.create(ArgsCertificationActor.class);
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
		.match(String.class, args -> {
			log.info("get args: " + args);
			getSender().tell(new ESBResult("argsCer", "done"), getSelf());
		})
		.build();
	}

}
