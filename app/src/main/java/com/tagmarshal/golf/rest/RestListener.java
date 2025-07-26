package com.tagmarshal.golf.rest;

public abstract class RestListener<T> {

    public abstract void onSuccess(T type);

    public abstract void onFailure(int code, String message);
}
