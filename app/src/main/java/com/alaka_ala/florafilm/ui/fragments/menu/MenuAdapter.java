package com.alaka_ala.florafilm.ui.fragments.menu;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.fragments.menu.models.MenuItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_REGULAR = 0;
    private static final int VIEW_TYPE_INFO = 1;
    private static final int VIEW_TYPE_EXPANDABLE_INFO = 2;

    private final List<MenuItem> menuItems;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MenuItem menuItem);
        void onInfoButtonClick(MenuItem menuItem, int buttonIndex);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public MenuAdapter(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    @Override
    public int getItemViewType(int position) {
        switch (menuItems.get(position).getType()) {
            case INFO: return VIEW_TYPE_INFO;
            case EXPANDABLE_INFO: return VIEW_TYPE_EXPANDABLE_INFO;
            default: return VIEW_TYPE_REGULAR;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_INFO) {
            return new InfoViewHolder(inflater.inflate(R.layout.list_item_info, parent, false));
        } else if (viewType == VIEW_TYPE_EXPANDABLE_INFO) {
            return new ExpandableInfoViewHolder(inflater.inflate(R.layout.list_item_expandable_info, parent, false), this, onItemClickListener);
        }
        return new RegularViewHolder(inflater.inflate(R.layout.list_item_menu, parent, false), onItemClickListener, menuItems);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_REGULAR: ((RegularViewHolder) holder).bind(menuItem); break;
            case VIEW_TYPE_INFO: ((InfoViewHolder) holder).bind(menuItem); break;
            case VIEW_TYPE_EXPANDABLE_INFO: ((ExpandableInfoViewHolder) holder).bind(menuItem); break;
        }
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    private int resolveColorAttr(android.content.Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    // ViewHolder for regular items
    class RegularViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView itemImage;
        TextView itemTitle;
        ColorStateList defaultStrokeColor;

        RegularViewHolder(@NonNull View itemView, OnItemClickListener listener, List<MenuItem> items) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            defaultStrokeColor = cardView.getStrokeColorStateList();

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        void bind(MenuItem menuItem) {
            itemImage.setImageResource(menuItem.getImageResource());
            itemTitle.setText(menuItem.getTitle());

            if (menuItem.getStrokeColorAttr() != 0) {
                cardView.setStrokeColor(resolveColorAttr(itemView.getContext(), menuItem.getStrokeColorAttr()));
            } else {
                cardView.setStrokeColor(defaultStrokeColor);
            }
        }
    }

    // ViewHolder for info items
    static class InfoViewHolder extends RecyclerView.ViewHolder {
        TextView infoText;

        InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            infoText = itemView.findViewById(R.id.info_text);
        }

        void bind(MenuItem menuItem) {
            infoText.setText(menuItem.getTitle());
        }
    }

    // ViewHolder for expandable info items
    class ExpandableInfoViewHolder extends RecyclerView.ViewHolder {
        TextView infoTitle, infoDetails;
        ImageView expandIcon;
        View buttonsContainer;
        MaterialButton button1, button2;
        MenuAdapter adapter;

        ExpandableInfoViewHolder(@NonNull View itemView, MenuAdapter adapter, OnItemClickListener listener) {
            super(itemView);
            this.adapter = adapter;
            infoTitle = itemView.findViewById(R.id.info_title);
            infoDetails = itemView.findViewById(R.id.info_details);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            buttonsContainer = itemView.findViewById(R.id.buttons_container);
            button1 = itemView.findViewById(R.id.button1);
            button2 = itemView.findViewById(R.id.button2);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    MenuItem item = adapter.menuItems.get(pos);
                    item.setExpanded(!item.isExpanded());
                    adapter.notifyItemChanged(pos);
                }
            });

            button1.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onInfoButtonClick(adapter.menuItems.get(pos), 1);
                }
            });

            button2.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onInfoButtonClick(adapter.menuItems.get(pos), 2);
                }
            });
        }

        void bind(MenuItem menuItem) {
            infoTitle.setText(menuItem.getTitle());
            infoDetails.setText(menuItem.getDetails());

            boolean isExpanded = menuItem.isExpanded();
            infoDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            buttonsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            expandIcon.setRotation(isExpanded ? 180f : 0f);

            updateButton(button1, menuItem.getButton1Text());
            updateButton(button2, menuItem.getButton2Text());
        }

        private void updateButton(MaterialButton button, String text) {
            if (!TextUtils.isEmpty(text)) {
                button.setText(text);
                button.setVisibility(View.VISIBLE);
            } else {
                button.setVisibility(View.GONE);
            }
        }
    }
}
