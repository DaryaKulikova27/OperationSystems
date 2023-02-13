package ru.dasha.ll;

public class State {
    private final String name;
    private final String[] guideCharacters;
    private final boolean isError;
    private final boolean isPushToStack;
    private final boolean isShift;
    private final boolean isEnd;
    private final int nextStateID;

    public State(
            String name,
            String[] guideCharacters,
            boolean isShift,
            boolean isError,
            int nextStateID,
            boolean isPushToStack,
            boolean isEnd
    ) {
        this.name = name;
        this.guideCharacters = guideCharacters;
        this.isError = isError;
        this.isPushToStack = isPushToStack;
        this.isShift = isShift;
        this.isEnd = isEnd;
        this.nextStateID = nextStateID;
    }

    public String getName() {
        return name;
    }

    public String[] getGuideCharacters() {
        return guideCharacters;
    }

    public boolean isError() {
        return isError;
    }

    public boolean isPushToStack() {
        return isPushToStack;
    }

    public boolean isShift() {
        return isShift;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public int getNextStateID() {
        return nextStateID;
    }
}