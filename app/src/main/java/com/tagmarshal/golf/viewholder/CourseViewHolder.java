package com.tagmarshal.golf.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.view.TMTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CourseViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.name)
    TMTextView mName;

    @BindView(R.id.select)
    View mSelect;

    private int pos;

    private OnItemClickListener onItemClickListener;

    public CourseViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.onItemClickListener = onItemClickListener;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(pos);
                }
            }
        });
    }

    public void bindData(String name, int pos) {
        mName.setText(name);
        this.pos = pos;
    }

    public void setSelected(boolean selected) {
        mSelect.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }
}
