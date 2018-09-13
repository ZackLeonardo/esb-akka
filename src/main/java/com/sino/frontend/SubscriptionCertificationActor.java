package com.sino.frontend;

import com.sino.frontend.HttpServerRouter.ESBResult;
import com.sino.frontend.HttpServerRouter.Params;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SubscriptionCertificationActor extends AbstractActor {
	
	private final LoggingAdapter log = Logging.getLogger(context().system(), this);
	
	private ActorRef nextactorRef;

	static Props props(ActorRef nextactorRef) {
		return Props.create(SubscriptionCertificationActor.class, () -> new SubscriptionCertificationActor(nextactorRef));
	} 
	
	public SubscriptionCertificationActor(ActorRef nextactorRef) {
		this.nextactorRef = nextactorRef;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Params.class, params -> {
					log.info("get params: " + params.getParams());
					//通过查询token判断是否已经订阅该service服务
					boolean SCResult = true;
					
					if (SCResult) {
						nextactorRef.tell(params.getArgs(), getSender());
					} else {
						getSender().tell(new ESBResult("error", "not subscripe yet"), getSelf());
					}
				})
				.matchAny(params -> {
					log.error("---error---get wrong args: " + params.toString());
					getSender().tell(new ESBResult("error", "wrong params"), getSender());
					})
				.build();
	}

}
