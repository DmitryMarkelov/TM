package com.tagmarshal.golf.fragment.map;

import android.content.Context;
import android.os.Environment;
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

public class AdvertisementBannerAdapter extends RecyclerView.Adapter<AdvertisementBannerAdapter.SliderViewHolder> {

    private List<AdvertisementModel> advertisementList;
    private Context context;

    public AdvertisementBannerAdapter(Context context, List<AdvertisementModel> advertisementList) {
        this.context = context;
        this.advertisementList = advertisementList;
    }

    public List<AdvertisementModel> getAdvertisementList() {
        return advertisementList;
    }

    public void setAdvertisementList(List<AdvertisementModel> advertisementList) {
        this.advertisementList = advertisementList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.advertisement_banner, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        AdvertisementModel advertisementModel = advertisementList.get(0);
        File appSpecificDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File imageFile = new File(appSpecificDownloadsDir, advertisementModel.getDownloadsUrls().get(position)); // File name from the model

        if (imageFile.exists()) {
            Glide.with(holder.imageView.getContext())
                    .load(imageFile)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        if (advertisementList.isEmpty()) {
            return 0;
        }
        return advertisementList.get(0).getDownloadsUrls().size();
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.bannerImage);
        }
    }
}