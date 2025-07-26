package com.tagmarshal.golf.eventbus;

public class CartIsMovedAd {
    Integer duration;

    public CartIsMovedAd(int duration) {
       this.duration = duration;
    }

    public Integer getDuration() {
        return duration;
    }
}
