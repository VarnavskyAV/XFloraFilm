package com.alaka_ala.florafilm.fragments.changelog.models;

import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;

import java.util.List;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ChangelogViewHolder> {

    private final List<ChangelogItem> changelogItems;

    public ChangelogAdapter(List<ChangelogItem> changelogItems) {
        this.changelogItems = changelogItems;
    }

    @NonNull
    @Override
    public ChangelogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changelog, parent, false);
        return new ChangelogViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ChangelogViewHolder holder, int position) {
        ChangelogItem item = changelogItems.get(position);

        holder.versionTextView.setText("Версия " + item.getVersion());
        holder.dateTextView.setText(item.getDate());

        // Применяем форматирование
        SpannableStringBuilder formattedDesc = item.getFormattedDescription(holder.itemView.getContext());
        holder.descriptionTextView.setText(formattedDesc);
        holder.descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return changelogItems.size();
    }

    static class ChangelogViewHolder extends RecyclerView.ViewHolder {
        TextView versionTextView;
        TextView dateTextView;
        TextView descriptionTextView;

        public ChangelogViewHolder(@NonNull View itemView) {
            super(itemView);
            versionTextView = itemView.findViewById(R.id.versionTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}
