package com.tagmarshal.golf.mostmediasdk;

import com.tagmarshal.golf.rest.model.AdvertisementModel;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Retrofit service interface for MostMedia ad server
 */
public interface MostMediaService {
    
    /**
     * Get advertisements from the new server
     * @return Observable list of advertisements
     */
    @GET("ads")
    Observable<List<AdvertisementModel>> getAdvertisements();
} 