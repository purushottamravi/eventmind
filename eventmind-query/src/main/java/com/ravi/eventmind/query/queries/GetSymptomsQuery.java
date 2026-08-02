package com.ravi.eventmind.query.queries;

/**
 * Just a question: "give me the symptoms that match this name, on this page, this size."
 * No logic, the handler figures out the answer.
 */
public record GetSymptomsQuery(String name, int page, int size) {
}
