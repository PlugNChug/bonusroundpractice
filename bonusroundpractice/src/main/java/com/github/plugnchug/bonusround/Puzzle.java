package com.github.plugnchug.bonusround;

public class Puzzle<A, C, S> {

    private final A answer;
    private final C category;
    private final S season;

    public Puzzle(A a, C c, S s) {
        answer = a;
        category = c;
        season = s;
    }

    public A getAnswer() { return answer; }
    public C getCategory() { return category; }
    public S getSeason() { return season; }
}
