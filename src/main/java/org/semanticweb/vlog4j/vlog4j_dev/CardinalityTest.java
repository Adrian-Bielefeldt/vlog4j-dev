package org.semanticweb.vlog4j.vlog4j_dev;

import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeAtom;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeConstant;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makePredicate;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeRule;
import static org.semanticweb.vlog4j.core.model.implementation.Expressions.makeVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.reasoner.LogLevel;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

public class CardinalityTest {

	public static void main(String[] args) throws ReasonerStateException, EdbIdbSeparationException, IncompatiblePredicateArityException, IOException {
		List<Atom> facts = new ArrayList<>();
		List<Rule> rules = new ArrayList<>();
		
		int size = 130; 
		
		Predicate largePredicateEDB = makePredicate("largePredicateEDB", size);
		Predicate largePredicateIDB = makePredicate("largePredicateIDB", size);
		
		Constant[] constants = new Constant[size];
		Variable[] variables = new Variable[size];
		for (int i = 0; i < size; i++) {
			constants[i] = makeConstant("constant" + i);
			variables[i] = makeVariable("variable" + i);
		}
		
		Atom fact = makeAtom(largePredicateEDB, constants);
		facts.add(fact);
		
		Atom import_head = makeAtom(largePredicateIDB, variables);
		Atom import_body = makeAtom(largePredicateEDB, variables);
		
		Rule import_rule = makeRule(import_head, import_body);
		rules.add(import_rule);
				
		Reasoner reasoner = Reasoner.getInstance();
		
		reasoner.setLogLevel(LogLevel.INFO);

		reasoner.addFacts(facts);
		reasoner.addRules(rules);
		
		reasoner.load();
		reasoner.reason();
		
		System.out.println("Done!");
	}

}
