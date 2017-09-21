package org.numerateweb.math.util;

import java.util.ArrayList;
import java.util.List;

import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;

public class ParseResult<T> {

	public T value = null;
	public List<ParseError> errors = new ArrayList<ParseError>(0);

	public ParseResult(T value) {
		this.value = value;
	}

	public ParseResult(List<ParseError> errors) {
		this.errors = errors;
	}

	public boolean matched() {
		return value != null;
	}

	public String errorMessage() {
		return ErrorUtils.printParseErrors(errors);
	}
	/*
	"\n--- ParseErrors ---\n" + printParseErrors(result)
	+ "\n--- ParseTree ---\n" + printNodeTree(result)
	*/
}
