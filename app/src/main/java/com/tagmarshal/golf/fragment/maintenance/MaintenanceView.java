package com.tagmarshal.golf.fragment.maintenance;

interface MaintenanceView {
    void onTimerFinish();
    void onTimerError(Throwable throwable);
}
