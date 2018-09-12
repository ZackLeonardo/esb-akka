package com.sino.frontend;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import scala.concurrent.duration.FiniteDuration;

import static akka.pattern.PatternsCS.ask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.sino.frontend.SubscriptionCertificationActor;

public class HttpServerRouter extends AllDirectives {
	
	private final ActorRef subscriptionCertificationActor;
	
	
	public static void main(String[] args) throws Exception {
		ActorSystem system = ActorSystem.create("routes");
		
		final Http http = Http.get(system);
		final ActorMaterializer materializer = ActorMaterializer.create(system);
		
		HttpServerRouter app = new HttpServerRouter(system);
		
		final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
	    final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
	      ConnectHttp.toHost("localhost", 8080), materializer);

	    System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
	    System.in.read(); // let it run until user presses return

	    binding
	      .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
	      .thenAccept(unbound -> system.terminate()); // and shutdown when done	
	}
	
	private HttpServerRouter(final ActorSystem system) {
		subscriptionCertificationActor = system.actorOf(SubscriptionCertificationActor.props(), "subscriptionCertificationActor");
		
	}
	
	private Route createRoute() {
		return route(
			path("esb", () -> route(
				get(() ->
					parameter("token", token -> 
						parameter("service", service -> 
							parameter("args", args -> 
								parameterOptional(StringUnmarshallers.INTEGER, "type", type -> 
									{
										final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(10, TimeUnit.SECONDS));
										if (!type.isPresent()) {
											try {
												CompletionStage<ESBResult> response = ask(subscriptionCertificationActor, new Params(token, service, args), timeout).thenApply(ESBResult.class::cast);
												
												return completeOKWithFuture(response, Jackson.<ESBResult>marshaller());
											} catch (Exception e) {
												System.out.println(e);
												return complete(StatusCodes.NETWORK_CONNECT_TIMEOUT, "time out");
											}
										}
										else {
//											switch (type.get()) {
//											case 1: 
//											default:
//												
//											}
											return null;
										}
										
										
									}
								)
							)
						)
					) 
				)
					
		)));
	}
	
	public static class Params {
	    public final String token;
	    public final String service;
	    public final String args;

	    public Params(String token, String service, String args) {
	      this.token = token;
	      this.service = service;
	      this.args = args;
	    }
	    
	    public String getParams() {
	    	return "token: " + this.token + "; service: " + this.service + "; args: " + this.args;
	    }
	    
	    public String getToken() {
			return token;
		}

		public String getService() {
			return service;
		}

		public String getArgs() {
			return args;
		}
	 }

	public static class ESBResult {
		public final String status;
		public final  String content;

	    public ESBResult(String status, String content) {
	      this.status = status;
	      this.content = content;
	    }
	 }

	
	
	

}
