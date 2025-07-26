package com.tagmarshal.golf.mostmediasdk;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.AdvertisementModel;

import java.io.File;
import java.util.List;

/**
 * Enhanced banner adapter that properly handles rotation across multiple advertisements
 * and their individual banner images using MostMediaSDK utilities
 */
public class EnhancedBannerAdapter extends RecyclerView.Adapter<EnhancedBannerAdapter.SliderViewHolder> {
    private static final String TAG = "EnhancedBannerAdapter";
    
    private List<AdvertisementModel> advertisementList;
    private Context context;

    public EnhancedBannerAdapter(Context context, List<AdvertisementModel> advertisementList) {
        this.context = context;
        this.advertisementList = advertisementList;
        Log.d(TAG, "🎬 EnhancedBannerAdapter created with " + 
              (advertisementList != null ? advertisementList.size() : 0) + " advertisements");
    }

    public List<AdvertisementModel> getAdvertisementList() {
        return advertisementList;
    }

    public void setAdvertisementList(List<AdvertisementModel> advertisementList) {
        this.advertisementList = advertisementList;
        Log.d(TAG, "📝 Advertisement list updated with " + 
              (advertisementList != null ? advertisementList.size() : 0) + " advertisements");
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.advertisement_banner, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        // Use MostMediaSDK to get the correct banner for this position
        MostMediaSDK.BannerInfo bannerInfo = MostMediaSDK.getBannerByGlobalIndex(advertisementList, position);
        
        if (bannerInfo == null) {
            Log.w(TAG, "⚠️ No banner info found for position " + position);
            return;
        }
        
        Log.d(TAG, "🖼️ Binding banner at position " + position + ": " + bannerInfo.imageUrl);
        
        File appSpecificDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File imageFile = new File(appSpecificDownloadsDir, bannerInfo.imageUrl);

        if (imageFile.exists()) {
            Glide.with(holder.imageView.getContext())
                    .load(imageFile)
                    .into(holder.imageView);
            Log.d(TAG, "✅ Image loaded successfully for position " + position);
        } else {
            Log.w(TAG, "⚠️ Image file not found: " + imageFile.getAbsolutePath());
        }
    }

    @Override
    public int getItemCount() {
        // Use MostMediaSDK to get the total number of individual banners
        int totalBanners = MostMediaSDK.getTotalBannerCount(advertisementList);
        Log.d(TAG, "📊 Total banner count: " + totalBanners);
        return totalBanners;
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.bannerImage);
        }
    }
} 