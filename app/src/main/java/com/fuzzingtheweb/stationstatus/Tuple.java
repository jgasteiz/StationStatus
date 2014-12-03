package com.fuzzingtheweb.stationstatus;

public class Tuple<Left, Right> {
    private Left left;
    private Right right;

    public Tuple(Left left, Right right) {
        this.left = left;
        this.right = right;
    }

    public Left getLeft() {
        return left;
    }

    public void setLeft(Left left) {
        this.left = left;
    }

    public Right getRight() {
        return right;
    }

    public void setRight(Right right) {
        this.right = right;
    }
}
