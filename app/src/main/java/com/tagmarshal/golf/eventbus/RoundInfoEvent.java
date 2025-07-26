package com.tagmarshal.golf.eventbus;

import com.tagmarshal.golf.rest.model.RestInRoundModel;

public class RoundInfoEvent {

    private RestInRoundModel roundModel;


    public RoundInfoEvent(RestInRoundModel roundModel) {
        this.roundModel = roundModel;
    }

    public RestInRoundModel getRoundModel() {
        return roundModel;
    }
}

