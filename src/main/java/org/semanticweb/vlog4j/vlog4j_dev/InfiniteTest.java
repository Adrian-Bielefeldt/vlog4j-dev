package org.semanticweb.vlog4j.vlog4j_dev;

import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeAtom;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeConstant;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makePredicate;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeRule;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.reasoner.LogLevel;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

public class InfiniteTest {

	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		Future<?> future = executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					infiniteTest();
				} catch (ReasonerStateException | EdbIdbSeparationException | IncompatiblePredicateArityException | IOException e) {
					System.out.println("Unexpected error.");
				}
			}
		});
		
		executor.shutdown();
		
		try {
			future.get(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.out.println("Task was interrupted.");
		} catch (ExecutionException e) {
			System.out.println("Caught exception: " + e);
		} catch (TimeoutException e) {
			System.out.println("Timeout");
			System.exit(1);
		}

		try {
			if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			System.out.println("Task was interrupted.");;
		}
	}
	
	static void infiniteTest() throws ReasonerStateException, IOException, EdbIdbSeparationException, IncompatiblePredicateArityException {
		List<Atom> facts = new ArrayList<>();
		List<Rule> rules = new ArrayList<>();
		
		Predicate start = makePredicate("start", 2);
		Predicate further = makePredicate("further", 2);
		
		facts.add(makeAtom(start, makeConstant("A"), makeConstant("B")));
		
		Variable x = makeVariable("x");
		Variable y = makeVariable("y");

		Atom further_xy = makeAtom(further, x, y);
		Atom start_xy = makeAtom(start, x, y);
		
		Rule import_rule = makeRule(further_xy, start_xy);
		rules.add(import_rule);
		
		Variable z = makeVariable("z");
		
		Atom further_yz = makeAtom(further, y, z);
		Rule infinite_rule = makeRule(further_yz, further_xy);
		rules.add(infinite_rule);
		
		Reasoner reasoner = Reasoner.getInstance();
		
		reasoner.setLogLevel(LogLevel.INFO);
		reasoner.setReasoningTimeout(1);
		
		reasoner.addFacts(facts);
		reasoner.addRules(rules);
		
		reasoner.load();
		reasoner.reason();
	}
}
