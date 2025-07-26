package androidx.core.app;

import timber.log.Timber;

public abstract class CustomJobIntentService extends JobIntentService {

    @Override
    GenericWorkItem dequeueWork() {
        try {
            return super.dequeueWork();
        } catch (SecurityException exception) {
            Timber.d(exception);
        }
        return null;
    }
}