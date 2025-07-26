package com.tagmarshal.golf.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.CourseModel;
import com.tagmarshal.golf.viewholder.CourseViewHolder;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseViewHolder> implements CourseViewHolder.OnItemClickListener {

    private List<CourseModel> courses;

    private int selected = -1;

    public CourseAdapter(List<CourseModel> courses) {
        this.courses = courses;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_course_layout,
                viewGroup, false);
        CourseViewHolder vh = new CourseViewHolder(view, this);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder courseViewHolder, int i) {
        courseViewHolder.bindData(courses.get(i).getCourseName(), i);
        if (i == selected) {
            courseViewHolder.setSelected(true);
        } else {
            courseViewHolder.setSelected(false);
        }
    }

    public String getCourseURL(int pos) {
        return courses.get(pos).getCourseUrl();
    }

    public String getCourseName(int pos) {
        return courses.get(pos).getCourseName();
    }

    public CourseModel getSelectedItem() {
        return selected == -1 ? null : courses.get(selected);
    }

    public void setSelect(int pos) {
        if (pos == selected) {
            selected = -1;
        } else {
            selected = pos;
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    @Override
    public void onItemClick(int pos) {
        setSelect(pos);
    }
}
